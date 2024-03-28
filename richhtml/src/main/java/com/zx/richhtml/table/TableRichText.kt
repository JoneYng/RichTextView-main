package com.zx.richhtml.table


import android.content.Context
import android.text.Html
import android.view.View

class TableRichText(override val context: Context, override val source: String) :
    TypeRichView(context, source) {

    override fun getRenderView(
        flags: Int,
        imageGetter: Html.ImageGetter?,
        tagHandler: Html.TagHandler?,
        textColor: Int
    ): View {
         val mRichTableLayout= RichTableLayout(context)
        mRichTableLayout.renderView(source, flags, imageGetter, tagHandler,textColor)
        return mRichTableLayout
    }

}