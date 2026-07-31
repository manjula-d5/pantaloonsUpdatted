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
import com.rfid.rfidreader.ui.FastImageLoader

class VariantRecyclerAdapter(
    private var onItemClicked: (RvVariantItem) -> Unit
) : ListAdapter<RvVariantItem, VariantRecyclerAdapter.VariantViewHolder>(DiffCallback) {

    fun setOnItemClicked(listener: (RvVariantItem) -> Unit) {
        onItemClicked = listener
    }

    private var selectedItemId: String? = null

    fun setSelectedItem(itemId: String?) {
        if (selectedItemId == itemId) return
        val previous = selectedItemId
        selectedItemId = itemId

        previous?.let { oldId ->
            val oldIndex = currentList.indexOfFirst { it.id == oldId }
            if (oldIndex >= 0) notifyItemChanged(oldIndex)
        }
        itemId?.let { newId ->
            val newIndex = currentList.indexOfFirst { it.id == newId }
            if (newIndex >= 0) notifyItemChanged(newIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_variant_card, parent, false)
        return VariantViewHolder(view)
    }

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id == selectedItemId)
    }

    override fun onViewRecycled(holder: VariantViewHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    inner class VariantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.variantImage)
        private val colorText: TextView = itemView.findViewById(R.id.variantColor)
        private val sizeText: TextView = itemView.findViewById(R.id.variantSize)
        private val root: LinearLayout = itemView.findViewById(R.id.variantCardRoot)

        fun bind(item: RvVariantItem, isSelected: Boolean) {
            colorText.text = item.color
            sizeText.text = itemView.context.getString(R.string.variant_size_text, item.size)
            root.setBackgroundResource(
                if (isSelected) R.drawable.tryon_card_bg_selected else R.drawable.tryon_card_bg
            )

            if (!item.imageUrl.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(item.imageUrl)
                    .thumbnail(0.15f)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_error)
                    .dontAnimate()
                    .apply(FastImageLoader.requestOptions)
                    .into(imageView)
            } else {
                Glide.with(itemView).clear(imageView)
                imageView.setImageResource(R.drawable.img_placeholder)
            }

            itemView.setOnClickListener { onItemClicked(item) }
        }

        fun unbind() {
            Glide.with(itemView).clear(imageView)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<RvVariantItem>() {
            override fun areItemsTheSame(oldItem: RvVariantItem, newItem: RvVariantItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RvVariantItem, newItem: RvVariantItem): Boolean =
                oldItem == newItem
        }
    }
}

