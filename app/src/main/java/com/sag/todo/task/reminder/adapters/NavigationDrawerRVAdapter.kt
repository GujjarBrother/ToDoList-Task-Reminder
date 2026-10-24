package com.sag.todo.task.reminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sag.todo.task.reminder.databinding.NavigationDrawerRvSingleItemLayoutBinding
import com.sag.todo.task.reminder.enums.Visibility
import com.sag.todo.task.reminder.models.NavigationDrawer
import com.sag.todo.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.task.reminder.core.R as CoreR

class NavigationDrawerRVAdapter(
    private val clickCallback: ((Int, Boolean) -> Unit)? = null
) : ListAdapter<NavigationDrawer, NavigationDrawerRVAdapter.ViewHolder>(NAVIGATION_DRAWER_DIFFUTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        NavigationDrawerRvSingleItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val navigationDrawer = getItem(position)
        holder.binding.apply {
            navigationDrawer.let {
                optionSwitch.setOnCheckedChangeListener(null)
                optionIV.setImageResource(it.image ?: 0)
                titleTV.text = it.title
                subTitleTV.text = it.subTitle
                if (it.title.equals(holder.itemView.context.getString(CoreR.string.light_mode_text), true) || it.title.equals(holder.itemView.context.getString(
                        CoreR.string.dark_mode_text), true)) {
                    optionLayout.background = null
                }
                if (it.heading?.isNotEmpty() == true) {
                    headingTV.changeVisibility(Visibility.VISIBLE)
                    headingTV.text = it.heading
                } else headingTV.changeVisibility(Visibility.GONE)
                optionSwitch.changeVisibility(if (it.isSwitch) Visibility.VISIBLE else Visibility.GONE)
                optionSwitch.isChecked = it.isSwitchChecked
                optionArrowIV.changeVisibility(if (it.isSwitch) Visibility.GONE else Visibility.VISIBLE)

                optionLayout.setOnClickListener {
                    val position = holder.bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION && position != 0) clickCallback?.invoke(position, false)
                }

                optionSwitch.setOnCheckedChangeListener { _, isChecked ->
                    val position = holder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener
                    if (position == 0) clickCallback?.invoke(position, isChecked)
                }
            }
        }
    }

    class ViewHolder(val binding: NavigationDrawerRvSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val NAVIGATION_DRAWER_DIFFUTIL =
            object : DiffUtil.ItemCallback<NavigationDrawer>() {
                override fun areItemsTheSame(oldItem: NavigationDrawer, newItem: NavigationDrawer) = oldItem.title == newItem.title
                override fun areContentsTheSame(oldItem: NavigationDrawer, newItem: NavigationDrawer) = oldItem == newItem
            }
    }
}