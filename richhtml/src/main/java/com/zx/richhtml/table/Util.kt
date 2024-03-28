package com.zx.richhtml.table


import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.zx.richhtml.R
import java.lang.NumberFormatException
import java.util.regex.Matcher
import java.util.regex.Pattern

object Util {

    private val tableColWidthPattern by lazy {
        Pattern.compile("(?:\\s+|\\A)data-ace-table-col-widths?\\s*:\\s*(\\S*)\\b")
    }

    @JvmStatic
    @ColorInt
    fun getHtmlColor(color: String?): Int {
        return try {
            convertValueToInt(color, -1)
        } catch (nfe: NumberFormatException) {
            -1
        }
    }

    private fun convertValueToInt(charSeq: CharSequence?, defaultValue: Int): Int {
        if (null == charSeq) return defaultValue
        val nm = charSeq.toString()
        // XXX This code is copied from Integer.decode() so we don't
        // have to instantiate an Integer!
        var value: Int
        var sign = 1
        var index = 0
        val len = nm.length
        var base = 10
        if ('-' == nm[0]) {
            sign = -1
            index++
        }
        if ('0' == nm[index]) {
            //  Quick check for a zero by itself
            if (index == len - 1) return 0
            val c = nm[index + 1]
            if ('x' == c || 'X' == c) {
                index += 2
                base = 16
            } else {
                index++
                base = 8
            }
        } else if ('#' == nm[index]) {
            index++
            base = 16
        }
        return nm.substring(index).toInt(base) * sign
    }

    fun getAnyKey(style: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(style)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }
        return null
    }

    fun getClass(resource: String): String? {
        val p: Pattern = Pattern.compile("class=\"([\\s\\S]*?)\"")
        val m: Matcher = p.matcher(resource)
        while (m.find()) {
            return m.group(1)?.trim()
        }
        return null
    }

    fun getStyle(resource: String): String? {
        val p: Pattern = Pattern.compile("style=\"([\\s\\S]*?)\"")
        val m: Matcher = p.matcher(resource)
        while (m.find()) {
            return m.group(1)?.trim()
        }
        return null
    }

    fun getTableColWidth(resource: String): String? {
        val p: Pattern = Pattern.compile("data-ace-table-col-widths=\"([\\s\\S]*?)\"")
        val m: Matcher = p.matcher(resource)
        while (m.find()) {
            return m.group(1)?.trim()
        }
        return null
    }

    fun getColGroup(resource: String): String? {
        val p: Pattern = Pattern.compile("<colgroup>([\\s\\S]*?)</colgroup>")
        val m: Matcher = p.matcher(resource)
        while (m.find()) {
            return m.group(1)?.trim()
        }
        return null
    }

    fun parseStyle(resource: String): Map<String, String> {
        val style = getStyle(resource)
        val map = mutableMapOf<String, String>()
        style?.split(";")?.forEach {
            val keyValue = it.trim().split(":")
            if (keyValue.size == 2) {
                map[keyValue[0].trim()] = keyValue[1].trim()
            } else {
                Log.e("parseStyle", "parseStyle error style=$it")
            }
        }
        return map
    }
}

fun ViewGroup.addLine(vertical: Boolean, color: Int? = null, lineWidth: Int? = null) {
    val v = View(context)
    v.layoutParams = if (vertical) TableRow.LayoutParams(
        lineWidth ?: 1 * context.resources.displayMetrics.density.toInt(),
        TableRow.LayoutParams.MATCH_PARENT
    ) else
        TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            lineWidth ?: 1 * context.resources.displayMetrics.density.toInt()
        )
    v.setBackgroundColor(color ?: ContextCompat.getColor(context, R.color.colorGray))
    addView(v)
}