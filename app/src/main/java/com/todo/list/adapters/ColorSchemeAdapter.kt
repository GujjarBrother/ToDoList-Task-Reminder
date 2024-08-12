package com.todo.list.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.databinding.ColorSchemeRecyclerViewSingleItemLayoutBinding
import com.todo.list.models.ColorSchemeModel
import com.todo.list.utils.CommonFunctions.changeVisibility

class ColorSchemeAdapter(
        private val colorSchemeArrayList: ArrayList<ColorSchemeModel>,
        private val callback: (Int) -> Unit
) : RecyclerView.Adapter<ColorSchemeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ColorSchemeRecyclerViewSingleItemLayoutBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorSchemeModel = colorSchemeArrayList[position]
        with(holder.binding) {
            colorCardView.setCardBackgroundColor(colorSchemeModel.color)

            if (colorSchemeModel.isSelected) {
                activeColorImageView.changeVisibility(1)
            } else {
                activeColorImageView.changeVisibility(0)
            }

            holder.itemView.setOnClickListener { _: View? ->
                callback.invoke(colorSchemeModel.id)
            }
        }
    }

    override fun getItemCount() = colorSchemeArrayList.size

    inner class ViewHolder(val binding: ColorSchemeRecyclerViewSingleItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
