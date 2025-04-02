package com.sag.todo.list.task.reminder.toasts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.Toast

object CustomToastCompat {
    @SuppressLint("DiscouragedPrivateApi")
    fun makeText(context: Context, message: String, duration: Int): Toast {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            try {
                val toast = Toast.makeText(context, message, duration)
                val field = toast.javaClass.getDeclaredField("mTN")
                field.isAccessible = true
                val mTN = field.get(toast)
                val showField = mTN.javaClass.getDeclaredField("mHandler")
                showField.isAccessible = true
                showField.set(mTN, SafeHandler(showField.get(mTN) as Handler))
                toast
            } catch (_: Exception) {
                Toast.makeText(context, message, duration)
            }
        } else {
            Toast.makeText(context, message, duration)
        }
    }

    fun makeText(context: Context, customView: View, duration: Int): Toast {
        val toast = makeText(context, "", duration)
        toast.view = customView
        toast.setGravity(Gravity.BOTTOM, 0, 100)
        return toast
    }

    fun showCustomToast(context: Context, customView: View, duration: Int = Toast.LENGTH_LONG) {
        if (context is Activity) {
            context.runOnUiThread {
                makeText(context, customView, duration).show()
            }
        } else {
            makeText(context, customView, duration).show()
        }
    }
}