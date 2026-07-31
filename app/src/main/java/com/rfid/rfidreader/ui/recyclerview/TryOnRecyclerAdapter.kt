package com.rfid.rfidreader.ui.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rfid.rfidreader.R

/**
 * RecyclerView Adapter for RFID Try-On items.
 */
class TryOnRecyclerAdapter(
    private val onItemClicked: (RvTryOnItem) -> Unit
) : ListAdapter<RvTryOnItem, TryOnRecyclerAdapter.TryOnViewHolder>(DiffCallback) {

    private var selectedItemId: String? = null

    fun setSelectedItem(itemId: String?) {
        if (selectedItemId == itemId) return
        val previousId = selectedItemId
        selectedItemId = itemId

        previousId?.let { oldId ->
            val oldIndex = currentList.indexOfFirst { it.id == oldId }
            if (oldIndex >= 0) notifyItemChanged(oldIndex)
        }
        itemId?.let { newId ->
            val newIndex = currentList.indexOfFirst { it.id == newId }
            if (newIndex >= 0) notifyItemChanged(newIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TryOnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tryon_card, parent, false)
        return TryOnViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: TryOnViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id == selectedItemId)
    }

    class TryOnViewHolder(
        itemView: View,
        private val onItemClicked: (RvTryOnItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.tryOnImage)
        private val brandText: TextView = itemView.findViewById(R.id.tryOnBrand)
        private val metaText: TextView = itemView.findViewById(R.id.tryOnMeta)
        private val rootCard: LinearLayout = itemView.findViewById(R.id.tryOnCardRoot)

        fun bind(item: RvTryOnItem, isSelected: Boolean) {
            brandText.text = item.brand
            metaText.text = item.category
            rootCard.background = null
            imageView.alpha = if (isSelected) 1.0f else 0.85f

            if (!item.imageUrl.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .centerCrop()
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.img_placeholder)
            }

            itemView.setOnClickListener { onItemClicked(item) }
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<RvTryOnItem>() {
            override fun areItemsTheSame(oldItem: RvTryOnItem, newItem: RvTryOnItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RvTryOnItem, newItem: RvTryOnItem): Boolean =
                oldItem == newItem
        }
    }
}
