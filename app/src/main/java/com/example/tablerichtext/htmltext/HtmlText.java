package com.example.tablerichtext.htmltext;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.example.tablerichtext.formula.BitmapCacheUtil;
import com.example.tablerichtext.formula.LaTeXInfo;
import com.example.tablerichtext.formula.VerticalImageSpan;
import com.example.tablerichtext.htmltext.span.ImageClickSpan;
import com.example.tablerichtext.htmltext.span.LinkClickSpan;

import org.scilab.forge.jlatexmath.core.AjLatexMath;
import org.scilab.forge.jlatexmath.core.Insets;
import org.scilab.forge.jlatexmath.core.TeXConstants;
import org.scilab.forge.jlatexmath.core.TeXFormula;
import org.scilab.forge.jlatexmath.core.TeXIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bolts.Continuation;
import bolts.Task;


public class HtmlText {
    private HtmlImageLoader imageLoader;
    private OnTagClickListener onTagClickListener;
    private After after;
    private String source;
    public static final String LATEXPATTERN = "\\$\\$(.+?)\\$\\$";
    private static final String PHANTOMPATTERN = "\\\\phantom\\{(.+?)\\}";

    public interface After {
        CharSequence after(SpannableStringBuilder ssb);
    }

    private HtmlText(String source) {
        this.source = source;
    }

    /**
     * 设置源文本
     */
    public static HtmlText from(String source) {
        //去除汉字偏移
        source = getPatternText(source);
        return new HtmlText(source);
    }

    /**
     * 设置加载器
     */
    public HtmlText setImageLoader(HtmlImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    /**
     * 设置图片、链接点击监听器
     */
    public HtmlText setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
        return this;
    }

    /**
     * 对处理完成的文本再次处理
     */
    public HtmlText after(After after) {
        this.after = after;
        return this;
    }

    /**
     * 注入TextView
     */
    public void into(final TextView textView) {
        if (TextUtils.isEmpty(source)) {
            textView.setText("");
            return;
        }

        HtmlImageGetter imageGetter = new HtmlImageGetter();
        HtmlTagHandler tagHandler = new HtmlTagHandler();
        List<String> imageUrls = new ArrayList<>();

        imageGetter.setTextView(textView);
        imageGetter.setImageLoader(imageLoader);
        imageGetter.getImageSize(source);

        tagHandler.setTextView(textView);
        source = tagHandler.overrideTags(source);

        Spanned spanned = Html.fromHtml(source, imageGetter, tagHandler);
        final SpannableStringBuilder ssb;
        if (spanned instanceof SpannableStringBuilder) {
            ssb = (SpannableStringBuilder) spanned;
        } else {
            ssb = new SpannableStringBuilder(spanned);
        }

        // Hold image url link
        imageUrls.clear();
        ImageSpan[] imageSpans = ssb.getSpans(0, ssb.length(), ImageSpan.class);
        for (int i = 0; i < imageSpans.length; i++) {
            ImageSpan imageSpan = imageSpans[i];
            String imageUrl = imageSpan.getSource();
            int start = ssb.getSpanStart(imageSpan);
            int end = ssb.getSpanEnd(imageSpan);
            imageUrls.add(imageUrl);

            ImageClickSpan imageClickSpan = new ImageClickSpan(textView.getContext(), imageUrls, i);
            imageClickSpan.setListener(onTagClickListener);
            ClickableSpan[] clickableSpans = ssb.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null) {
                for (ClickableSpan cs : clickableSpans) {
                    ssb.removeSpan(cs);
                }
            }
            ssb.setSpan(imageClickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Hold text url link
        URLSpan[] urlSpans = ssb.getSpans(0, ssb.length(), URLSpan.class);
        if (urlSpans != null) {
            for (URLSpan urlSpan : urlSpans) {
                int start = ssb.getSpanStart(urlSpan);
                int end = ssb.getSpanEnd(urlSpan);
                ssb.removeSpan(urlSpan);
                LinkClickSpan linkClickSpan = new LinkClickSpan(textView.getContext(), urlSpan.getURL());
                linkClickSpan.setListener(onTagClickListener);
                ssb.setSpan(linkClickSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
        //异步加载数学公式
        Task.callInBackground(new Callable<ArrayList<LaTeXInfo>>() {
            @Override
            public ArrayList<LaTeXInfo> call() throws Exception {
                return getLaTexInfoList(String.valueOf(ssb));
            }
        }).continueWith(new Continuation<ArrayList<LaTeXInfo>, Object>() {
            @Override
            public Object then(Task<ArrayList<LaTeXInfo>> task) throws Exception {
                ArrayList<LaTeXInfo> laTeXInfos = task.getResult();
                if (laTeXInfos == null) {
                    return null;
                }
                for (int i = 0; i < laTeXInfos.size(); i++) {
                    LaTeXInfo laTeXInfo = laTeXInfos.get(i);
                    Bitmap image = BitmapCacheUtil.getInstance().getBitmapFromMemCache(laTeXInfo.getGroup() + textView.getPaint().getTextSize() / textView.getPaint().density);
                    if (image == null) {
                        image = getBitmap(textView,laTeXInfo.getTeXFormula());
                        BitmapCacheUtil.getInstance().addBitmapToMemoryCache(laTeXInfo.getGroup() + textView.getPaint().getTextSize() / textView.getPaint().density, image);
                    }
                    ssb.setSpan(new VerticalImageSpan(textView.getContext(), image), laTeXInfo.getStart(), laTeXInfo.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                CharSequence charSequence = ssb;
                if (after != null) {
                    charSequence = after.after(ssb);
                }

                textView.setText(charSequence);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }
    /**
     * 获取公式解析对象集合，该操作费时，需要放在子线程执行
     * @param text
     * @return
     */
    public ArrayList<LaTeXInfo> getLaTexInfoList(String text) {
        //设置正则表达式的各种格式。
        Pattern pattern = Pattern.compile(LATEXPATTERN);
        //查找正则表达式的管理类
        Matcher matcher = pattern.matcher(text);
        ArrayList<LaTeXInfo> mLaTexInfos = new ArrayList<>();
        while (matcher.find()) {//查看是否复合正则表达式
            //去除里面复合正则表达式
            final String group = matcher.group();
            if (group.startsWith("$")) {//是一串 LaTexMath公式
                TeXFormula teXFormula = TeXFormula.getPartialTeXFormula(group);
//                TeXFormula teXFormula = new TeXFormula(group);
                LaTeXInfo laTeXInfo = new LaTeXInfo(teXFormula, matcher.start(), matcher.end(), group);
                mLaTexInfos.add(laTeXInfo);
            }
        }
        return mLaTexInfos;
    }
    /**
     * 根据解析后的对象，生成bitmap 需要放在UI线程，子线程中偶尔会出现图片错乱
     * @param formula
     * @return
     */
    private Bitmap getBitmap(TextView textView,TeXFormula formula) {
        TeXIcon icon = formula.new TeXIconBuilder()
                .setStyle(TeXConstants.STYLE_DISPLAY)
                .setSize(textView.getPaint().getTextSize() / textView.getPaint().density)
                .setWidth(TeXConstants.UNIT_SP, textView.getPaint().getTextSize() / textView.getPaint().density, TeXConstants.ALIGN_LEFT)
                .setIsMaxWidth(true)
                .setInterLineSpacing(TeXConstants.UNIT_SP,
                        AjLatexMath.getLeading(textView.getPaint().getTextSize() / textView.getPaint().density))
                .build();
        icon.setInsets(new Insets(5, 5, 5, 5));
        Bitmap image = Bitmap.createBitmap(icon.getIconWidth(), icon.getIconHeight(),
                Bitmap.Config.ARGB_4444);
        System.out.println(" width=" + icon.getBox().getWidth() + " height=" + icon.getBox().getHeight() +
                " iconwidth=" + icon.getIconWidth() + " iconheight=" + icon.getIconHeight());
        Canvas g2 = new Canvas(image);
        g2.drawColor(Color.TRANSPARENT);
        icon.paintIcon(g2, 0, 0);
        return image;
    }
    /**
     * 过滤掉所有\\phantom{}的内容，该内容用来偏移文字显示位置
     * @param text
     * @return
     */
    public  static String getPatternText(String text) {
        //设置正则表达式的各种格式。
        Pattern pattern = Pattern.compile(PHANTOMPATTERN);
        //查找正则表达式的管理类
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {//查看是否复合正则表达式
            //去除里面复合正则表达式
            final String group = matcher.group();
            if (group.startsWith("\\")) {//是一串 LaTexMath公式
                text = text.replace(group, "");
            }
        }
        return text;
    }
}
