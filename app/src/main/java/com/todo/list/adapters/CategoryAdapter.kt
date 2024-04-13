package com.todo.list.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.R
import com.todo.list.adapters.CategoryAdapter.CustomViewHolder
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.databinding.CustomPopupMenuRecyclerviewSingleItemLayoutBinding
import com.todo.list.listeners.CategorySelectionListener
import com.todo.list.listeners.SignUpActivityCategorySelectionListener
import com.todo.list.utils.CommonFunctions.DEFAULT_CATEGORY
import com.todo.list.utils.CommonFunctions.PERSONAL_CATEGORY
import com.todo.list.utils.CommonFunctions.WORK_CATEGORY

class CategoryAdapter : ListAdapter<Int, CustomViewHolder> {
    private lateinit var categorySelectionListener: CategorySelectionListener
    private lateinit var signUpActivityCategorySelectionListener: SignUpActivityCategorySelectionListener
    private val forWhich: String

    constructor(categorySelectionListener: CategorySelectionListener, forWhich: String) : super(DIFF_CALLBACK) {
        this.categorySelectionListener = categorySelectionListener
        this.forWhich = forWhich
    }

    constructor(signUpActivityCategorySelectionListener: SignUpActivityCategorySelectionListener,
                forWhich: String) : super(DIFF_CALLBACK) {
        this.signUpActivityCategorySelectionListener = signUpActivityCategorySelectionListener
        this.forWhich = forWhich
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(CustomPopupMenuRecyclerviewSingleItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            if (forWhich.equals("Category", ignoreCase = true)) {
                when (item) {
                    DEFAULT_CATEGORY -> {
                        categoryNameTextView.text = root.context.getString(R.string.select_category_text)
                        categoryNameTextView.textSize = 16f
                        categoryNameTextView.setTextColor(Color.parseColor("#9E9E9E"))
                    }

                    PERSONAL_CATEGORY -> {
                        categoryNameTextView.text = root.context.getString(R.string.personal_text)
                    }

                    WORK_CATEGORY -> {
                        categoryNameTextView.text = root.context.getString(R.string.work_text)
                    }
                }
            } else if (forWhich.equals("Gender", ignoreCase = true)) {
                when (item) {
                    0 -> {
                        categoryNameTextView.text = root.context.getString(R.string.select_gender_text)
                        categoryNameTextView.textSize = 16f
                        categoryNameTextView.setTextColor(Color.parseColor("#9E9E9E"))
                    }

                    1 -> {
                        categoryNameTextView.text = root.context.getString(R.string.male_text)
                    }

                    2 -> {
                        categoryNameTextView.text = root.context.getString(R.string.fe_male_text)
                    }

                    3 -> {
                        categoryNameTextView.text = root.context.getString(R.string.transgender_text)
                    }
                }
            } else if (forWhich.equals("Security Questions", ignoreCase = true)) {
                when (item) {
                    0 -> {
                        categoryNameTextView.text = root.context.getString(R.string.select_security_question_text)
                        categoryNameTextView.textSize = 16f
                        categoryNameTextView.setTextColor(Color.parseColor("#9E9E9E"))
                    }

                    1 -> {
                        categoryNameTextView.text = root.context
                            .getString(R.string.what_is_your_favourite_book_question)
                    }

                    2 -> {
                        categoryNameTextView.text = root.context
                            .getString(R.string.what_is_your_favourite_teacher_name_question)
                    }

                    3 -> {
                        categoryNameTextView.text = root.context
                            .getString(R.string.what_is_your_school_name_question)
                    }

                    4 -> {
                        categoryNameTextView.text = root.context
                            .getString(R.string.what_is_your_favourite_game_question)
                    }
                }
            }
            if (prefs.dayAndNightModeSwitchValue) {
                if (item != DEFAULT_CATEGORY) {
                    categoryNameTextView.setTextColor(ContextCompat.getColor(
                        root.context, R.color.whiteColor))
                }
            }
        }
    }

    inner class CustomViewHolder(val binding: CustomPopupMenuRecyclerviewSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            with(binding) {
                categoryNameTextView.typeface = typeface

                root.setOnClickListener { _: View? ->
                    if (adapterPosition != -1) {
                        if (forWhich.equals("Category", ignoreCase = true)) {
                            categorySelectionListener.selectCategory(getItem(adapterPosition))
                        } else if (forWhich.equals("Gender", ignoreCase = true)) {
                            signUpActivityCategorySelectionListener.selectCategory(getItem(adapterPosition),
                                "Gender")
                        } else if (forWhich.equals("Security Questions", ignoreCase = true)) {
                            signUpActivityCategorySelectionListener.selectCategory(getItem(adapterPosition),
                                "Security Questions")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Int> = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }
    }
}
