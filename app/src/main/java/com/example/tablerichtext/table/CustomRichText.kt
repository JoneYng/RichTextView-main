package com.example.tablerichtext.table

import android.content.Context
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import com.example.tablerichtext.formula.RichTextView

class CustomRichText(override val context: Context, public override val source: String) :
    TypeRichView(context, source) {

    override fun getRenderView(
        flags: Int,
        imageGetter: Html.ImageGetter?,
        tagHandler: Html.TagHandler?,
        textColor: Int
    ): View {
        val mLaTexTextView=
            RichTextView(context)
        mLaTexTextView.setTextColor(ContextCompat.getColor(
            context, textColor
        ))
        mLaTexTextView.setMathText(source)
        return mLaTexTextView
    }
}