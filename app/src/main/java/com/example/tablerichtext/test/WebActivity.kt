package com.example.tablerichtext.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.HtmlCompat
import com.example.tablerichtext.App
import com.example.tablerichtext.R
import com.example.tablerichtext.mathweb.MathView
import com.example.tablerichtext.table.HtmlTextView

class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        this.findViewById<MathView>(R.id.math_view)
            .setDisplayText(App.getSample())
    }
}