package com.sag.todo.list.task.reminder.presentation.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.core.utils.controllers.localization.AppLanguage
import com.sag.todo.list.task.reminder.databinding.LanguageRvSingleItemLayoutBinding
import com.sag.todo.list.task.reminder.domain.listeners.AdaptersListener

class AppLanguageRVAdapter(
    private val listener: AdaptersListener<AppLanguage, Int, Int>? = null,
//    private val appLanguageClickCallback: (AppLanguage, Int, Int) -> Unit
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
        with(holder.binding) {
            appLanguage.let {
                flagIV.setImageResource(it.flag)
                languageNameTV.text = it.languageName
                nativeLanguageNameTV.text = it.languageNameInNative

                if (it.isSelected) {
                    previouslySelectedPosition = position
                    root.setStrokeColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                holder.itemView.context, R.color.defaultColor
                            )
                        )
                    )
                    root.strokeWidth = 3
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
            with(binding) {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener?.itemClicked(getItem(position), position, previouslySelectedPosition)
//                        appLanguageClickCallback.invoke(getItem(position), position, previouslySelectedPosition)
                    }
                }
            }
        }
    }

    companion object {
        private val LANGUAGE_DIFFUTIL = object : DiffUtil.ItemCallback<AppLanguage>() {
            override fun areItemsTheSame(
                oldItem: AppLanguage, newItem: AppLanguage
            ): Boolean {
                return oldItem.languageName == newItem.languageName
            }

            override fun areContentsTheSame(
                oldItem: AppLanguage, newItem: AppLanguage
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}