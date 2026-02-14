package com.vibedev.bluecollar.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View

import com.vibedev.bluecollar.data.Notification
import com.vibedev.bluecollar.utils.ColorExtractor
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.utils.capitalizeEachWord
import com.vibedev.bluecollar.databinding.ItemNotificationBinding

class NotificationAdapter(
    private var items: MutableList<Notification> = mutableListOf(),
    private val onItemClick: (Notification) -> Unit,
    private val onItemDelete: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.VH>() {

    inner class VH(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(n: Notification) {

            val iconDrawable = ContextCompat.getDrawable(itemView.context, n.iconRes)

            binding.icon.setImageDrawable(iconDrawable)

            if (iconDrawable != null) {
                ColorExtractor.applyIconColorToBackground(
                    iconDrawable = iconDrawable,
                    viewToColor = binding.iconBackground
                )
            }

            binding.title.text = n.title.capitalizeEachWord()
            binding.message.text = n.message.capitalizeFirst()
            binding.time.text = n.time
            binding.unreadDot.visibility = if (n.isRead) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                if (!n.isRead) {
                    n.isRead = true
                    notifyItemChanged(pos)
                }
                onItemClick(n)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Notification>) {
        val diffCallback = NotificationDiffCallback(this.items, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.items.clear()
        this.items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeAt(position: Int) {
        val removed = items.removeAt(position)
        notifyItemRemoved(position)
        onItemDelete(removed)
    }
}

private class NotificationDiffCallback(
    private val oldList: List<Notification>,
    private val newList: List<Notification>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
