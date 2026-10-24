package com.example.toast

import android.app.Activity
import com.sag.todo.task.reminder.core.R as CoreR
import com.sag.todo.task.reminder.databinding.CustomToastLayoutBinding
import javax.inject.Inject

class ToastController @Inject constructor() {
    fun showToast(context: Activity, message: String?, isForSuccessOrNot: Boolean) {
        val customToastLayoutBinding = CustomToastLayoutBinding.inflate(context.layoutInflater)
        customToastLayoutBinding.apply {
            toastIV.setImageResource(if (isForSuccessOrNot) CoreR.drawable.toast_tick_image else CoreR.drawable.toast_cross_image)
            toastMessageTV.isSelected = true
            toastMessageTV.text = message
        }
        CustomToastCompat.showCustomToast(context, customToastLayoutBinding.root)
    }
}