package com.vibedev.bluecollar.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.services.GlideService
import com.vibedev.bluecollar.databinding.ItemPortfolioImageBinding

class PortfolioAdapter(private var imageUrls: List<String>) :
    RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder>() {

    inner class PortfolioViewHolder(val binding: ItemPortfolioImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val binding = ItemPortfolioImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PortfolioViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        val imageView = holder.binding.ivPortfolioImage
        val context = holder.itemView.context

        GlideService.loadImageWithRetry(
            context,
            imageUrl,
            R.drawable.progress_animation,
            R.drawable.add_photo_icon,
            imageView
        )
    }

    fun updateData(newImageUrls: List<String>) {
        imageUrls = newImageUrls
        notifyDataSetChanged()
    }
}
