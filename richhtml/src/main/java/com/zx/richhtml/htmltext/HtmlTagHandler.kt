package com.zx.richhtml.htmltext

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.Html.TagHandler
import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import android.widget.TextView
import com.zx.richhtml.htmltext.ColorToRgb.Companion.toRgb
import com.zx.richhtml.htmltext.span.NumberSpan
import org.xml.sax.XMLReader
import java.util.Stack

/**
 * Some parts of this code are based on android.text.Html
 * Modified from https://github.com/SufficientlySecure/html-textview
 */
class HtmlTagHandler : TagHandler {
    private var mContext: Context? = null
    private var mTextPaint: TextPaint? = null


    private val lists = Stack<String>()

    private val olNextIndex = Stack<Int>()
    fun setTextView(textView: TextView) {
        mContext = textView.context.applicationContext
        mTextPaint = textView.paint
    }

    fun overrideTags(html: String?): String? {
        var html = html ?: return null

        // Wrap HTML tags to prevent parsing custom tags error
        html = "<html>$html</html>"
        html = html.replace("<ul", "<$UNORDERED_LIST")
        html = html.replace("</ul>", "</$UNORDERED_LIST>")
        html = html.replace("<ol", "<$ORDERED_LIST")
        html = html.replace("</ol>", "</$ORDERED_LIST>")
        html = html.replace("<li", "<$LIST_ITEM")
        html = html.replace("</li>", "</$LIST_ITEM>")
        html = html.replace("<font", "<$FONT")
        html = html.replace("</font>", "</$FONT>")
        html = html.replace("<span", "<$SPAN")
        html = html.replace("</span>", "</$SPAN>")
        html = html.replace("<div", "<$DIV")
        html = html.replace("</div>", "</$DIV>")
        return html
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (opening) {
            // opening tag
            if (tag.equals(UNORDERED_LIST, ignoreCase = true)) {
                lists.push(tag)
            } else if (tag.equals(ORDERED_LIST, ignoreCase = true)) {
                lists.push(tag)
                olNextIndex.push(1)
            } else if (tag.equals(LIST_ITEM, ignoreCase = true)) {
                if (output.isNotEmpty() && output[output.length - 1] != '\n') {
                    output.append("\n")
                }
                if (!lists.isEmpty()) {
                    val parentList = lists.peek()
                    if (parentList.equals(ORDERED_LIST, ignoreCase = true)) {
                        start(output, Ol())
                        olNextIndex.push(olNextIndex.pop() + 1)
                    } else if (parentList.equals(UNORDERED_LIST, ignoreCase = true)) {
                        start(output, Ul())
                    }
                }
            } else if (tag.equals(FONT, ignoreCase = true)) {
                startFont(output, xmlReader)
            } else if (tag.equals(SPAN, ignoreCase = true)) {
                startSpan(output, xmlReader)
            } else if (tag.equals(DIV, ignoreCase = true)) {
                handleDiv(output)
            } else if (tag.equals("code", ignoreCase = true)) {
                start(output, Code())
            } else if (tag.equals("center", ignoreCase = true)) {
                start(output, Center())
            } else if (tag.equals("s", ignoreCase = true) || tag.equals(
                    "strike",
                    ignoreCase = true
                )
            ) {
                start(output, Strike())
            } else if (tag.equals("tr", ignoreCase = true)) {
                start(output, Tr())
            } else if (tag.equals("th", ignoreCase = true)) {
                start(output, Th())
            } else if (tag.equals("td", ignoreCase = true)) {
                start(output, Td())
            }
        } else {
            // closing tag
            if (tag.equals(UNORDERED_LIST, ignoreCase = true)) {
                lists.pop()
            } else if (tag.equals(ORDERED_LIST, ignoreCase = true)) {
                lists.pop()
                olNextIndex.pop()
            } else if (tag.equals(LIST_ITEM, ignoreCase = true)) {
                if (!lists.isEmpty()) {
                    if (lists.peek().equals(UNORDERED_LIST, ignoreCase = true)) {
                        if (output.isNotEmpty() && output[output.length - 1] != '\n') {
                            output.append("\n")
                        }
                        // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                        var bulletMargin = indent
                        if (lists.size > 1) {
                            bulletMargin = indent - bullet.getLeadingMargin(true)
                            if (lists.size > 2) {
                                // This get's more complicated when we add a LeadingMarginSpan into the same line:
                                // we have also counter it's effect to BulletSpan
                                bulletMargin -= (lists.size - 2) * listItemIndent
                            }
                        }
                        val newBullet = BulletSpan(bulletMargin)
                        end(
                            output, Ul::class.java, false,
                            LeadingMarginSpan.Standard(listItemIndent * (lists.size - 1)),
                            newBullet
                        )
                    } else if (lists.peek().equals(ORDERED_LIST, ignoreCase = true)) {
                        if (output.isNotEmpty() && output[output.length - 1] != '\n') {
                            output.append("\n")
                        }
                        var numberMargin = listItemIndent * (lists.size - 1)
                        if (lists.size > 2) {
                            // Same as in ordered lists: counter the effect of nested Spans
                            numberMargin -= (lists.size - 2) * listItemIndent
                        }
                        val numberSpan = NumberSpan(mTextPaint, olNextIndex.lastElement() - 1)
                        end(
                            output, Ol::class.java, false,
                            LeadingMarginSpan.Standard(numberMargin),
                            numberSpan
                        )
                    }
                }
            } else if (tag.equals(FONT, ignoreCase = true)) {
                endFont(output)
            }else if (tag.equals(SPAN, ignoreCase = true)) {
                endFont(output)
            } else if (tag.equals(DIV, ignoreCase = true)) {
                handleDiv(output)
            } else if (tag.equals("code", ignoreCase = true)) {
                end(output, Code::class.java, false, TypefaceSpan("monospace"))
            } else if (tag.equals("center", ignoreCase = true)) {
                end(
                    output,
                    Center::class.java,
                    true,
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)
                )
            } else if (tag.equals("s", ignoreCase = true) || tag.equals(
                    "strike",
                    ignoreCase = true
                )
            ) {
                end(output, Strike::class.java, false, StrikethroughSpan())
            } else if (tag.equals("tr", ignoreCase = true)) {
                end(output, Tr::class.java, false)
            } else if (tag.equals("th", ignoreCase = true)) {
                end(output, Th::class.java, false)
            } else if (tag.equals("td", ignoreCase = true)) {
                end(output, Td::class.java, false)
            }
        }
    }

    private class Ul
    private class Ol
    private class Code
    private class Center
    private class Strike
    private class Tr
    private class Th
    private class Td
    private class Font(color: String?, size: String?) {
        var color: String?=null
        var size: String?

        init {
            var color = color
            if(color!=null){
                color = toRgb(color)
            }
            this.color = color
            this.size = size
        }
    }

    /**
     * Mark the opening tag by using private classes
     */
    private fun start(output: Editable, mark: Any) {
        val len = output.length
        output.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK)
    }

    /**
     * Modified from [Html]
     */
    private fun end(
        output: Editable,
        kind: Class<*>,
        paragraphStyle: Boolean,
        vararg replaces: Any
    ) {
        val obj = getLast(output, kind)
        // start of the tag
        val where = output.getSpanStart(obj)
        // end of the tag
        val len = output.length
        output.removeSpan(obj)
        if (where != len) {
            var thisLen = len
            // paragraph styles like AlignmentSpan need to end with a new line!
            if (paragraphStyle) {
                output.append("\n")
                thisLen++
            }
            for (replace in replaces) {
                output.setSpan(replace, where, thisLen, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }


    private fun startSpan(output: Editable, xmlReader: XMLReader) {
        val len = output.length
        val attributes: Map<String, String> = getAttributes(xmlReader)
        val style = attributes["style"]
        if (!TextUtils.isEmpty(style)) {
            analysisStyle(len, len, output, style)
        }
    }


    /**
     * 解析style属性
     *
     * @param startIndex startIndex
     * @param stopIndex stopIndex
     * @param editable editable
     * @param style style
     */
    private fun analysisStyle(startIndex: Int?, stopIndex: Int?, editable: Editable?, style: String?) {
        val attrArray = style?.split(";")
        val attrMap = HashMap<String, String>()
        if (attrArray != null) {
            for (attr in attrArray) {
                val keyValueArray = attr.split(":")
                if (keyValueArray.size == 2) {
                    // 去除前后空格
                    attrMap[keyValueArray[0].trim()] = keyValueArray[1].trim()
                }
            }
        }

        // 字体颜色
        var color = attrMap["color"]
        if (!TextUtils.isEmpty(color)) {
            color=ColorToRgb.toRgb(color!!)
        }
        var absoluteSize:String?=null
        // 字体大小
        var fontSize = attrMap["font-size"]
        if (!TextUtils.isEmpty(fontSize)) {
            fontSize = fontSize!!.split("px")[0]
            try {
                absoluteSize = ColorToRgb.sp2px(mContext!!, fontSize.toInt()).toString()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        try {
            editable?.setSpan(Font(color, absoluteSize), startIndex!!, stopIndex!!, Spannable.SPAN_MARK_MARK)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun startFont(output: Editable, xmlReader: XMLReader) {
        val len = output.length
        val attributes: Map<String, String> = getAttributes(xmlReader)
        val color = attributes["color"]
        val size = attributes["size"]
        output.setSpan(Font(color, size), len, len, Spannable.SPAN_MARK_MARK)
    }

    private fun endFont(output: Editable) {
        val len = output.length
        val obj = getLast(output, Font::class.java)
        val where = output.getSpanStart(obj)
        output.removeSpan(obj)
        if (where != len) {
            val f = obj as Font?
            val color = parseColor(f!!.color)
            val size = parseSize(f.size)
            if (color != -1) {
                output.setSpan(
                    ForegroundColorSpan(color or -0x1000000),
                    where,
                    len,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (size > 0) {
                output.setSpan(
                    AbsoluteSizeSpan(size, true),
                    where,
                    len,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun handleDiv(output: Editable) {
        val len = output.length
        if (len >= 1 && output[len - 1] == '\n') {
            return
        }
        if (len != 0) {
            output.append("\n")
        }
    }

    private fun getAttributes(xmlReader: XMLReader): HashMap<String, String> {
        val attributes = HashMap<String, String>()
        try {
            val elementField = xmlReader.javaClass.getDeclaredField("theNewElement")
            elementField.isAccessible = true
            val element = elementField[xmlReader]
            val attrsField = element.javaClass.getDeclaredField("theAtts")
            attrsField.isAccessible = true
            val attrs = attrsField[element]
            val dataField = attrs.javaClass.getDeclaredField("data")
            dataField.isAccessible = true
            val data = dataField[attrs] as Array<String>
            val lengthField = attrs.javaClass.getDeclaredField("length")
            lengthField.isAccessible = true
            val len = lengthField[attrs] as Int
            /**
             * MSH: Look for supported attributes and add to hash map.
             * This is as tight as things can get :)
             * The data index is "just" where the keys and values are stored.
             */
            for (i in 0 until len) attributes[data[i * 5 + 1]] = data[i * 5 + 4]
        } catch (ignored: Exception) {
        }
        return attributes
    }

    /**
     * dpValue
     */
    private fun parseSize(size: String?): Int {
        var s: Int = try {
            size?.toInt() ?: 0
        } catch (ignored: NumberFormatException) {
            return 0
        }
        s = s.coerceAtLeast(1)
        s = s.coerceAtMost(7)
        val baseSize = px2dp(mTextPaint!!.textSize)
        return s - 3 + baseSize
    }

    private fun px2dp(pxValue: Float): Int {
        val density = mContext!!.resources.displayMetrics.density
        return (pxValue / density + 0.5f).toInt()
    }

    companion object {
        private const val UNORDERED_LIST = "HTML_TEXT_TAG_UL"
        private const val ORDERED_LIST = "HTML_TEXT_TAG_OL"
        private const val LIST_ITEM = "HTML_TEXT_TAG_LI"
        private const val FONT = "HTML_TEXT_TAG_FONT"
        private const val DIV = "HTML_TEXT_TAG_DIV"
        private const val SPAN = "HTML_TEXT_TAG_SPAN"

        private const val indent = 10
        private const val listItemIndent = indent * 2
        private val bullet = BulletSpan(indent)

        private fun getLast(text: Editable, kind: Class<*>): Any? {
            val objs = text.getSpans(0, text.length, kind)
            return if (objs.isEmpty()) {
                null
            } else {
                for (i in objs.size downTo 1) {
                    if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
                        return objs[i - 1]
                    }
                }
                null
            }
        }

        private fun parseColor(colorString: String?): Int {
            return try {
                Color.parseColor(colorString)
            } catch (ignored: Exception) {
                -1
            }
        }
    }
}