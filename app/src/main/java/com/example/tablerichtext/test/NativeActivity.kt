package com.example.tablerichtext.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.HtmlCompat
import com.example.tablerichtext.App
import com.example.tablerichtext.App.Companion.getSample
import com.example.tablerichtext.table.HtmlTextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder



class NativeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.tablerichtext.R.layout.activity_main)
        this.findViewById<HtmlTextView>(com.example.tablerichtext.R.id.htmlTextView)
            .setHtml(App.getSample(), HtmlCompat.FROM_HTML_MODE_LEGACY, null, null)
    }

}