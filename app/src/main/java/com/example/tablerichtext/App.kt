package com.example.tablerichtext

import android.app.Application
import android.content.Context
import com.zx.richhtml.RichHtml
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder

/**
 */
class App : Application() {
    companion object{
        private var appContext: Application? = null
        fun getAppContext(): Context {
            return appContext!!
        }
        //本地数据
        fun getSample(): String {
            try {
                val `is`: InputStream = getAppContext().resources.openRawResource(com.example.tablerichtext.R.raw.sample)
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
                return ""
            }
        }
    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appContext = this
    }

    override fun onCreate() {
        super.onCreate()
        RichHtml.init(applicationContext)
    }
}