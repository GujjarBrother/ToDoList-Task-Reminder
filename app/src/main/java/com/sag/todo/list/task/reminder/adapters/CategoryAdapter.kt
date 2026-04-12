package com.sag.todo.list.task.reminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.utils.Prefs
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.CustomPopupMenuRecyclerviewSingleItemLayoutBinding
import com.sag.todo.list.task.reminder.enums.Gender
import com.sag.todo.list.task.reminder.enums.SecurityQuestions
import com.sag.todo.list.task.reminder.enums.TasksCategories
import com.sag.todo.list.task.reminder.utils.AppConstants.getColorResource

class CategoryAdapter(
    private val forWhichInvoked: String,
    private val prefs: Prefs,
    private val callback: (Int, String?) -> Unit
) : ListAdapter<Int, CategoryAdapter.CustomViewHolder>(DiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            CustomPopupMenuRecyclerviewSingleItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            if (forWhichInvoked.equals(other = "Category", ignoreCase = true)) {
                when (item) {
                    TasksCategories.DEFAULT_CATEGORY.ordinal -> {
                        categoryNameTV.text = root.context.getString(com.example.core.R.string.select_category_text)
                        categoryNameTV.textSize = 16F
                        categoryNameTV.setTextColor(holder.itemView.context.getColorResource(R.color.subColor))
                    }
                    TasksCategories.PERSONAL_CATEGORY.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.personal_text)
                    TasksCategories.WORK_CATEGORY.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.work_text)
                }
            } else if (forWhichInvoked.equals(other = "Gender", ignoreCase = true)) {
                when (item) {
                    Gender.NONE.ordinal -> {
                        categoryNameTV.text = root.context.getString(com.example.core.R.string.select_gender_text)
                        categoryNameTV.textSize = 16f
                        categoryNameTV.setTextColor(holder.itemView.context.getColorResource(R.color.subColor))
                    }
                    Gender.MALE.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.male_text)
                    Gender.FEMALE.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.fe_male_text)
                    Gender.TRANSGENDER.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.transgender_text)
                }
            } else if (forWhichInvoked.equals(other = "Security Questions", ignoreCase = true)) {
                when (item) {
                    SecurityQuestions.SELECT_SECURITY_QUESTION.ordinal -> {
                        categoryNameTV.text = root.context.getString(com.example.core.R.string.select_security_question_text)
                        categoryNameTV.textSize = 16F
                        categoryNameTV.setTextColor(holder.itemView.context.getColorResource(R.color.subColor))
                    }
                    SecurityQuestions.QUESTION_1.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.what_is_your_favourite_book_question)
                    SecurityQuestions.QUESTION_2.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.what_is_your_favourite_teacher_name_question)
                    SecurityQuestions.QUESTION_3.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.what_is_your_school_name_question)
                    SecurityQuestions.QUESTION_4.ordinal -> categoryNameTV.text = root.context.getString(com.example.core.R.string.what_is_your_favourite_game_question)
                }
            }
            if (prefs.isDarkModeEnable) {
                if (item != TasksCategories.DEFAULT_CATEGORY.ordinal)
                    categoryNameTV.setTextColor(root.context.getColorResource(R.color.whiteColor))
            }
        }
    }

    inner class CustomViewHolder(val binding: CustomPopupMenuRecyclerviewSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener { _: View? ->
                    val pos = bindingAdapterPosition
                    if (pos != -1) {
                        if (forWhichInvoked.equals(other = "Category", ignoreCase = true)) {
                            callback.invoke(getItem(pos), null)
                        } else if (forWhichInvoked.equals(other = "Gender", ignoreCase = true)) {
                            callback.invoke(getItem(pos), "Gender")
                        } else if (forWhichInvoked.equals(other = "Security Questions", ignoreCase = true)) {
                            callback.invoke(getItem(pos), "Security Questions")
                        }
                    }
                }
            }
        }
    }

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
    }
}
