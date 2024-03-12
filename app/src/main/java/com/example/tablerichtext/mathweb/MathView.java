package com.example.tablerichtext.mathweb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;

import com.example.tablerichtext.R;

public class MathView extends WebView {
    private String TAG = "KhanAcademyKatexView";
    private static final float default_text_size = 16;
    private String display_text;
    private int text_color;
    private int text_size;
    private Context mContext;
    private boolean clickable = false;
    private boolean enable_zoom_in_controls = false;


    public MathView(Context context) {
        super(context);
        mContext = context;
        configurationSettingWebView(enable_zoom_in_controls);
        setDefaultTextColor(context);
        setDefaultTextSize();
    }


    public MathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        configurationSettingWebView(enable_zoom_in_controls);
        TypedArray mTypeArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MathView,
                0, 0);
        try {
            setBackgroundColor(mTypeArray.getInteger(R.styleable.MathView_setViewBackgroundColor, ContextCompat.getColor(context, android.R.color.transparent)));
            setTextColor(mTypeArray.getColor(R.styleable.MathView_setTextColor, ContextCompat.getColor(context, android.R.color.black)));
            pixelSizeConversion(mTypeArray.getDimension(R.styleable.MathView_setTextSize, default_text_size));
            setDisplayText(mTypeArray.getString(R.styleable.MathView_setText));
            setClickable(mTypeArray.getBoolean(R.styleable.MathView_setClickable, false));


        } catch (Exception e) {
            Log.d(TAG, "Exception:" + e.toString());
        }


    }

    public void setViewBackgroundColor(int color) {
        setBackgroundColor(color);
        this.invalidate();
    }

    private void pixelSizeConversion(float dimension) {
        if (dimension == default_text_size) {
            setTextSize((int) default_text_size);
        } else {
            int pixel_dimen_equivalent_size = (int) ((double) dimension / 1.6);
            setTextSize(pixel_dimen_equivalent_size);
        }
    }


    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    private void configurationSettingWebView(boolean enable_zoom_in_controls) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.setWebContentsDebuggingEnabled(true);
        }
        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        //设定缩放控件隐藏
        settings.setDisplayZoomControls(enable_zoom_in_controls);
        //设置出现缩放工具
        settings.setBuiltInZoomControls(false);
        //设置可以支持缩放
        settings.setSupportZoom(enable_zoom_in_controls);
        //设置true,才能让Webivew支持<meta>标签的viewport属性
        settings.setUseWideViewPort(true);
        //自适应屏幕
        settings.setLoadWithOverviewMode(true);
        //最小缩放等级
        setInitialScale(25);

        this.setVerticalScrollBarEnabled(enable_zoom_in_controls);
        this.setHorizontalScrollBarEnabled(enable_zoom_in_controls);

        Log.d(TAG, "Zoom in controls:" + enable_zoom_in_controls);
    }


    public void setDisplayText(String formula_text) {
        this.display_text = formula_text;
        loadData();
    }


    private String getOfflineKatexConfig() {
        String offline_config =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        " <head>\n" +
                        "        <meta charset=\"UTF-8\">\n" +
                        "        <title>Auto-render</title>\n" +
                        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                        "        <!-- katex核心包 -->\n" +
                        "        <link href=\"file:///android_asset/katex/katex.min.css\" rel=\"stylesheet\" />\n" +
                        "        <script defer src=\"file:///android_asset/katex/katex.min.js\"></script>\n" +
                        "        <!-- katex辅助包：自动渲染 -->\n" +
                        "        <script defer src=\"file:///android_asset/katex/contrib/auto-render.min.js\"></script>" +
                        "   <style type='text/css'>" +
                        "       table {\n" +
                        "       table-layout: fixed;\n" +
                        "       width: 100%;\n" +
                        "       border-collapse: collapse;;\n" +
                        "       border: 1px solid rgb(82, 79, 79);\n" +
                        "      }\n" +
                        "\n" +
                        "  td {\n" +
                        "       border: 1px solid rgb(127, 127, 128);\n" +
                        "       overflow: hidden;\n" +
                        "       white-space: wrap;\n" +
                        "      }" +
                        "       img  { display: inline; height: auto; max-width: 100%;}" +
                        "       body {" +
                        "       background: #fff;" +
                        "       margin: 0px;" +
                        "       line-height: 30px;" +
                        "       padding: 0px;" +
                        "       word-break:break-all;" +//允许自动换行
                        "       font-size:" + this.text_size + "px !important;" +
                        "       color:" + getHexColor(this.text_color) + ";" +
                        "            }" +
                        "   </style>" +
                        " </head>\n" +
                        " <body>\n" +
                        "    <div>{formula}</div>\n" +
                        "    <script>\n" +
                        "      document.addEventListener(\"DOMContentLoaded\", function() {\n" +
                        "        renderMathInElement(document.body, { // renderMathInElement这个函数就是 auto-render.js提供的\n" +
                        "          delimiters: [\n" +
                        "              // 意思是匹配页面上 以$开头和结尾的元素。 display决定是否占一整行\n" +
                        "              {left: '$$', right: '$$', display: false},\n" +
                        "              {left: '\\\\(', right: '\\\\)', display: false}\n" +
                        "          ],\n" +
                        "          throwOnError : false\n" +
                        "        });\n" +
                        "    });\n" +
                        "    var imgs=document.getElementsByTagName(\"img\");\n" +
                        "      for(var i=0;i<imgs.length;i++){\n" +
                        "        console.log(imgs[i],'imgs[i]')\n" +
                        "        imgs[i].onclick=function(){\n" +
                        "          window.jsCallJavaObj.showBigImg(this.src); \n" +
                        "        }\n" +
                        "      }" +
                        "    var tables = document.getElementsByTagName('table');\n" +
                        "        for(var i=0;i<tables.length;i++)\n" +
                        "    {\n" +
                        "        tables[i].style.width = 100+\"%\";\n" +
                        "    }\n" +
                        "    function viewTopHeight(){\n" +
                        "           const innerTop = document.getElementById('anchor').offsetTop;\n" +
                        "           window.jsCallJavaObj.viewTopHeight(innerTop);\n" +
                        "       }\n" +
                        "    </script>\n" +
                        "</<body>" +
                        "</html>";
        return offline_config.replace("{formula}", this.display_text);
    }

    public void setTextSize(int size) {
        this.text_size = size;
        loadData();

    }

    public void setTextColor(int color) {

        this.text_color = color;
        loadData();
    }

    private String getHexColor(int intColor) {
        //Android and javascript color format differ javascript support Hex color, so the android color which user sets is converted to hexcolor to replicate the same in javascript.
        String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
        Log.d(TAG, "Hex Color:" + hexColor);
        return hexColor;
    }


    private void setDefaultTextColor(Context context) {
        //sets default text color to black
        this.text_color = ContextCompat.getColor(context, android.R.color.black);

    }

    private void setDefaultTextSize() {
        //sets view default text size to 18
        this.text_size = (int) default_text_size;
    }

    private void loadData() {
        if (this.display_text != null) {
            this.loadDataWithBaseURL("null", getOfflineKatexConfig(), "text/html", "UTF-8", "about:blank");
        }
        //java回调js代码，不要忘了@JavascriptInterface这个注解，不然点击事件不起作用
        this.addJavascriptInterface(new JsCallJavaObj() {
            @JavascriptInterface
            @Override
            public void showBigImg(String url) {

            }
            @JavascriptInterface
            @Override
            public void viewTopHeight(String height) {

            }
        }, "jsCallJavaObj");
    }

    public void setClickable(boolean is_clickable) {
        this.setEnabled(true);
        this.clickable = is_clickable;
        this.enable_zoom_in_controls = !is_clickable;
        configurationSettingWebView(this.enable_zoom_in_controls);
        this.invalidate();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.clickable && event.getAction() == MotionEvent.ACTION_DOWN) {
            this.callOnClick();
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * Js調用Java接口
     */
    public interface JsCallJavaObj {
        void showBigImg(String url);
        //控件距离顶部的高度
        void viewTopHeight(String url);
    }

}
