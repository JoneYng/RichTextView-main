package com.example.tablerichtext.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.HtmlCompat
import com.example.tablerichtext.table.HtmlTextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.tablerichtext.R.layout.activity_main)
        this.findViewById<HtmlTextView>(com.example.tablerichtext.R.id.htmlTextView)
            .setHtml(getSample()+"", HtmlCompat.FROM_HTML_MODE_LEGACY, null, null)
    }
    //本地数据
    private fun getSample(): String? {
        try {
            val `is`: InputStream = resources.openRawResource(com.example.tablerichtext.R.raw.sample)
            val reader = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
                sb.append("\n")
            }
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}