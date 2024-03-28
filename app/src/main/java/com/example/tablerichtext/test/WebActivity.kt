package com.example.tablerichtext.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tablerichtext.App
import com.example.tablerichtext.R
import com.zx.richhtml.mathweb.MathView

class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        this.findViewById<MathView>(R.id.math_view)
            .setDisplayText(App.getSample())
    }
}