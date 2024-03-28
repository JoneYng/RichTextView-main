package com.zx.richhtml.htmltext

import android.content.Context
import java.util.Locale


/**
 * @description:
 * @author: zhouxiang
 * @created: 2024/03/28 09:56
 * @version: V1.0
 */
class ColorToRgb {
    companion object {
        fun toRgb(colorRgb:String):String{
            var color=colorRgb
            if (color.startsWith("rgb")) {
                color = color.replace("rgb(", "")
                color = color.replace(")", "")
                val rgbs = color.split(", ")
                if (rgbs.size > 2) {
                    color = toHex(rgbs[0].toInt(), rgbs[1].toInt(), rgbs[2].toInt())
                }
            }
            return color
        }

         fun toHex(r : Int, g : Int, b : Int) : String {
            return "#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b)
        }

         fun toBrowserHexValue(number: Int) : String {
            val builder = StringBuilder(number.and(0xff).toString(16))
            while (builder.length < 2) {
                builder.append("0")
            }
            return builder.toString().uppercase(Locale.getDefault())
        }

         fun sp2px(context: Context, pxValue: Int) : Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (pxValue * fontScale + 0.5f).toInt()
        }
   }
}