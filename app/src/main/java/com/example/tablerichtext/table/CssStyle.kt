package com.example.tablerichtext.table

import android.graphics.Color
import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

class CssStyle(private val resource: String) {

    companion object {
        const val TAG = "CssStyle"
        val rgbPattern by lazy { Pattern.compile("rgb\\(([\\s\\S]*?)\\)") }
        val rgbaPattern by lazy { Pattern.compile("rgba\\(([\\s\\S]*?)\\)") }
    }

    var borderColor: Int? = null

    init {
        val style = Util.parseStyle(resource)
        Log.d(TAG, "style=${style}")
        if (borderColor == null) {
            style["border"]?.apply {
                var m: Matcher = rgbPattern.matcher(this)
                if (m.find()) {
                    val rgb = m.group(1)?.split(",")
                    rgb?.apply {
                        borderColor = Color.rgb(
                            rgb[0].trim().toInt(),
                            rgb[1].trim().toInt(),
                            rgb[2].trim().toInt()
                        )
                    }
                } else {
                    m = rgbaPattern.matcher(this)
                    if (m.find()) {
                        val rgba = m.group(1)?.split(",")
                        rgba?.apply {
                            borderColor = Color.argb(
                                (rgba[3].trim().toFloat() * 255).toInt(),
                                rgba[0].trim().toInt(),
                                rgba[1].trim().toInt(),
                                rgba[2].trim().toInt()
                            )
                        }
                    }
                }
            }
        }
    }
}