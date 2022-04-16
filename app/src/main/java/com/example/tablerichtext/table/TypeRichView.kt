package com.example.tablerichtext.table

import android.content.Context
import android.text.Html
import android.view.View
import com.example.tablerichtext.R

abstract class TypeRichView(protected open val context: Context, public open val source: String) {
    abstract fun getRenderView(
        flags: Int,
        imageGetter: Html.ImageGetter?,
        tagHandler: Html.TagHandler?,
        textColor:Int= R.color.colorGray
    ) : View
}