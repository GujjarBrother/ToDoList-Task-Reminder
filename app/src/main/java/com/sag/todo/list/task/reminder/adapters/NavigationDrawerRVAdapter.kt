package com.sag.todo.list.task.reminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sag.todo.list.task.reminder.databinding.NavigationDrawerRvSingleItemLayoutBinding
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.listeners.AdaptersListener
import com.sag.todo.list.task.reminder.models.NavigationDrawer
import com.sag.todo.list.task.reminder.core.utils.CommonFunctions.changeVisibility

class NavigationDrawerRVAdapter(
    private val listener: AdaptersListener<NavigationDrawer, Int, Int>? = null,
) : ListAdapter<NavigationDrawer, NavigationDrawerRVAdapter.ViewHolder>(NAVIGATION_DRAWER_DIFFUTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        NavigationDrawerRvSingleItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val navigationDrawer = getItem(position)
        with(holder.binding) {
            navigationDrawer.let {
                optionIV.setImageResource(it.image ?: 0)
                titleTV.text = it.title
                subTitleTV.text = it.subTitle
                if (it.heading?.isNotEmpty() == true) {
                    headingTV.changeVisibility(Visibility.VISIBLE.ordinal)
                    headingTV.text = it.heading
                } else {
                    headingTV.changeVisibility(Visibility.GONE.ordinal)
                }
                optionSwitch.changeVisibility(if (it.isSwitch) Visibility.VISIBLE.ordinal else Visibility.GONE.ordinal)
                optionSwitch.isChecked = it.isSwitchChecked
                optionArrowIV.changeVisibility(if (it.isSwitch) Visibility.GONE.ordinal else Visibility.VISIBLE.ordinal)
            }
        }
    }

    inner class ViewHolder(val binding: NavigationDrawerRvSingleItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            with(binding) {
                optionLayout.setOnClickListener {
                    val position = adapterPosition
                    if (position != 0) listener?.itemClicked(getItem(position), position)
                }

                optionSwitch.setOnCheckedChangeListener { _, isChecked ->
                    val position = adapterPosition
                    if (position == 0)
                        listener?.itemClicked(getItem(position), position, isSwitchChecked = isChecked)
                }
            }
        }
    }

    companion object {
        private val NAVIGATION_DRAWER_DIFFUTIL =
            object : DiffUtil.ItemCallback<NavigationDrawer>() {
                override fun areItemsTheSame(
                    oldItem: NavigationDrawer,
                    newItem: NavigationDrawer
                ): Boolean {
                    return oldItem.title == newItem.title
                }

                override fun areContentsTheSame(
                    oldItem: NavigationDrawer,
                    newItem: NavigationDrawer
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}