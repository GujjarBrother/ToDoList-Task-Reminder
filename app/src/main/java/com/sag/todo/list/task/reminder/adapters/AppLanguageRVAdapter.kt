package com.sag.todo.list.task.reminder.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.databinding.LanguageRvSingleItemLayoutBinding
import com.sag.todo.list.task.reminder.listeners.AdaptersListener
import com.sag.todo.list.task.reminder.models.AppLanguage
import com.sag.todo.list.task.reminder.utils.AppConstants.getColorResource

class AppLanguageRVAdapter(
    private val listener: AdaptersListener<AppLanguage, Int, Int>? = null
) : ListAdapter<AppLanguage, AppLanguageRVAdapter.ViewHolder>(LANGUAGE_DIFFUTIL) {

    var previouslySelectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LanguageRvSingleItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(
        holder: ViewHolder, @SuppressLint("RecyclerView") position: Int
    ) {
        val appLanguage = getItem(position)
        holder.binding.apply {
            appLanguage.let {
                flagIV.setImageResource(it.flag)
                languageNameTV.text = it.languageName
                nativeLanguageNameTV.text = it.languageNameInNative
                if (it.isSelected) {
                    previouslySelectedPosition = position
                    root.setStrokeColor(
                        ColorStateList.valueOf(
                            holder.itemView.context.getColorResource(
                                R.color.defaultColor
                            )
                        )
                    )
                    root.strokeWidth = 4
                } else {
                    root.strokeColor = Color.TRANSPARENT
                    root.strokeWidth = 0
                }
            }
        }
    }

    inner class ViewHolder(val binding: LanguageRvSingleItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) listener?.itemClicked(getItem(position), position, previouslySelectedPosition)
                }
            }
        }
    }

    companion object {
        private val LANGUAGE_DIFFUTIL = object : DiffUtil.ItemCallback<AppLanguage>() {
            override fun areItemsTheSame(oldItem: AppLanguage, newItem: AppLanguage) = oldItem.languageName == newItem.languageName
            override fun areContentsTheSame(oldItem: AppLanguage, newItem: AppLanguage) = oldItem == newItem
        }
    }
}