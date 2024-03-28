package com.zx.richhtml.table


import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import com.zx.richhtml.R
import java.util.regex.Matcher
import java.util.regex.Pattern

class HtmlTextView : LinearLayout {
    companion object {
        const val TAG = "HtmlTextView"
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    init {
        orientation = VERTICAL
    }

    private val richList = mutableListOf<TypeRichView>()

    fun setHtml(
        source: String,
        flags: Int,
        imageGetter: Html.ImageGetter?,
        tagHandler: Html.TagHandler?,
        textColor: Int= R.color.black
    ) {
        richList.clear()
        removeAllViews()
        val p: Pattern = Pattern.compile("<table[\\s\\S]*?</table>")
        val tableMatcher: Matcher = p.matcher(source)
        var start = 0
        while (tableMatcher.find()) {
            if (tableMatcher.start() == start) {
                richList.add(TableRichText(context, source.substring(tableMatcher.start(), tableMatcher.end())))
            } else {
                richList.add(CustomRichText(context, source.substring(start, tableMatcher.start())))
                richList.add(TableRichText(context, source.substring(tableMatcher.start(), tableMatcher.end())))
            }
            start = tableMatcher.end()
        }
        if (start != source.length - 1) {
            richList.add(CustomRichText(context, source.substring(start)))
        }
        richList.forEach {
            Log.d(TAG, "html=${it.source}")
            addView(
                it.getRenderView(flags, imageGetter, tagHandler,textColor),
                LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }
    }

}