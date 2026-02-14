package com.vibedev.bluecollar.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.net.Uri

import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.services.GlideService
import com.vibedev.bluecollar.databinding.ItemPortfolioUploadBinding

class PortfolioUploadAdapter(
    private val portfolioUris: MutableList<Uri>,
    private val onAddImage: () -> Unit
) : RecyclerView.Adapter<PortfolioUploadAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPortfolioUploadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPortfolioUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < portfolioUris.size) {
            val uri = portfolioUris[position]
            GlideService.loadImageWithRetry(
                holder.itemView.context,
                uri.toString(),
                R.drawable.progress_animation,
                R.drawable.add_photo_icon,
                holder.binding.ivPortfolioImage
            )
            holder.binding.fabRemoveImage.visibility = View.VISIBLE
            holder.binding.btnAddPortfolioImage.visibility = View.GONE
            holder.binding.fabRemoveImage.setOnClickListener {
                portfolioUris.removeAt(position)
                notifyDataSetChanged()
            }
        } else {
            holder.binding.ivPortfolioImage.setImageURI(null)
            holder.binding.fabRemoveImage.visibility = View.GONE
            holder.binding.btnAddPortfolioImage.visibility = View.VISIBLE
            holder.binding.btnAddPortfolioImage.setOnClickListener {
                onAddImage()
            }
        }
    }

    override fun getItemCount(): Int {
        return portfolioUris.size + 1
    }
}
