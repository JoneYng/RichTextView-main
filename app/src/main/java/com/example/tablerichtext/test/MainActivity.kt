package com.example.tablerichtext.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tablerichtext.R
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<MaterialButton>(R.id.btn_native).setOnClickListener {
            startActivity(Intent(this,NativeActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_webview).setOnClickListener {
            startActivity(Intent(this,WebActivity::class.java))
        }
    }
}