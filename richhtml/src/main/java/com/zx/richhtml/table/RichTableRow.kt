package com.zx.richhtml.table


import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.zx.richhtml.R
import java.util.regex.Matcher
import java.util.regex.Pattern

class RichTableRow constructor(
    context: Context,
) : TableRow(context) {

    companion object {
        const val TAG = "RichTableRow"
    }

    private var cssStyle: CssStyle? = null

    fun renderView(
        resource: String,
        tableWidths: List<Int>,
        flags: Int,
        imageGetter: Html.ImageGetter?,
        tagHandler: Html.TagHandler?,
        textColor: Int = R.color.colorGray
    ) {
        removeAllViews()
        val tdPattern: Pattern = Pattern.compile("<td[\\s\\S]*?</td>")
        val tdMatcher: Matcher = tdPattern.matcher(resource)
        var index = 0
        var borderColor: Int? = null
        while (tdMatcher.find()) {
            val tdString = tdMatcher.group().replaceFirst("<td", "<div").replace("</td>", "</div>")
            val style = Util.parseStyle(tdString)
            Log.d("getRenderView", "style=${style["border"]}")
            if (borderColor == null) {
                style["border"]?.apply {
                    var m: Matcher = Pattern.compile("rgb\\(([\\s\\S]*?)\\)").matcher(this)
                    if (m.find()) {
                        val rgb = m.group(1)?.split(",")
                        rgb?.apply {
                            borderColor = Color.rgb(
                                rgb[0].trim().toInt(),
                                rgb[1].trim().toInt(),
                                rgb[2].trim().toInt()
                            )
                        }
                    } else {
                        m = Pattern.compile("rgba\\(([\\s\\S]*?)\\)").matcher(this)
                        if (m.find()) {
                            val rgba = m.group(1)?.split(",")
                            rgba?.apply {
                                borderColor = Color.argb(
                                    (rgba[3].trim().toFloat() * 255).toInt(),
                                    rgba[0].trim().toInt(),
                                    rgba[1].trim().toInt(),
                                    rgba[2].trim().toInt()
                                )
                            }
                        }
                    }
                }
            }
            if (index == 0) {
                addLine(true, color = borderColor)
            }
            addView(
                TextView(context).apply {
                    this.text = HtmlCompat.fromHtml(tdString, flags, imageGetter, tagHandler)
                    this.movementMethod = LinkMovementMethod.getInstance()
                    this.setTextColor(ContextCompat.getColor(
                        context, textColor
                        ))
                },
                LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    (tableWidths.getOrNull(index++) ?: 1).toFloat()
                )
            )
            addLine(true, color = borderColor)
        }
    }
}