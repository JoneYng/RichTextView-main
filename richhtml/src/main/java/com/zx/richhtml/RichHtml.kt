package com.zx.richhtml

import android.content.Context
import bolts.Task
import org.scilab.forge.jlatexmath.core.AjLatexMath


/**
 * @description:
 * @author: zhouxiang
 * @created: 2024/03/28 17:02
 * @version: V1.0
 */
object RichHtml {
    fun init(context:Context){
        Task.callInBackground<Any> {
            AjLatexMath.init(context)
            null
        }
    }
}