package com.sag.todo.list.task.reminder.utils.toasts

import android.app.Activity
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.CustomToastLayoutBinding
import javax.inject.Inject

class ToastController @Inject constructor() {
    fun showToast(context: Activity, message: String?, isForSuccessOrNot: Boolean) {
        val customToastLayoutBinding = CustomToastLayoutBinding.inflate(context.layoutInflater)

        with(customToastLayoutBinding) {
            toastIV.setImageResource(if (isForSuccessOrNot) R.drawable.toast_tick_image else R.drawable.toast_cross_image)
            toastMessageTV.isSelected = true
            toastMessageTV.text = message
        }

        CustomToastCompat.showCustomToast(context, customToastLayoutBinding.root)
    }
}