package com.example.tablerichtext.table

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.ContextCompat
import com.example.tablerichtext.R
import com.example.tablerichtext.table.Util.getColGroup
import com.example.tablerichtext.table.Util.getTableColWidth
import java.util.regex.Matcher
import java.util.regex.Pattern

class RichTableLayout constructor(
    context: Context,
) : TableLayout(context) {
    companion object {
        const val TAG = "RichTableLayout"
    }

    private fun parseTableColWidths(resource: String): List<Int> {
        val list = arrayListOf<Int>()
        val mTableColWidth = getTableColWidth(resource)
        if (mTableColWidth != null) {
            mTableColWidth.split(";").forEach {
                if (it.isNotEmpty()) {
                    list.add(it.toInt())
                }
            }
        } else {
            val group = getColGroup(resource)
            if (group != null) {
                val p: Pattern = Pattern.compile("width=\"([\\s\\S]*?)\"")
                val m: Matcher = p.matcher(group)
                while (m.find()) {
                    Log.e(TAG, "group=$group, ${m.group(1)}")
                    m.group(1)?.trim()?.toInt()?.let { list.add(it) }
                }
            }
        }
        Log.d(TAG, "list=$list")
        return list
    }

    fun renderView(
        resource: String,
        flags: Int,
        imageGetter: Html.ImageGetter?,
        tagHandler: Html.TagHandler?,
        textColor:Int?
    ) {
        removeAllViews()
        addLine()
        val tableWidths = parseTableColWidths(resource)
        val p: Pattern = Pattern.compile("<tr>[\\s\\S]*?</tr>")
        val m: Matcher = p.matcher(resource)
        while (m.find()) {
            val trString = m.group()
            Log.d("trString", "trString=$trString")
            val tableRow = RichTableRow(context)
            if(textColor!=null){
                tableRow.renderView(trString, tableWidths, flags, imageGetter, tagHandler,textColor)
            }else{
                tableRow.renderView(trString, tableWidths, flags, imageGetter, tagHandler)
            }
            this.addView(
                tableRow,
                TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                )
            )
            addLine()
        }
    }

    private fun addLine() {
        val v = View(context)
        v.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1 * context.resources.displayMetrics.density.toInt())
        v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGray))
        addView(v)
    }
}