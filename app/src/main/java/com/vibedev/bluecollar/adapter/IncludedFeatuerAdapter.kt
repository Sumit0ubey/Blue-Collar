package com.vibedev.bluecollar.adapter

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.vibedev.bluecollar.databinding.ItemIncludedFeatureBinding

class IncludedFeaturesAdapter(private var features: List<String>) :
    RecyclerView.Adapter<IncludedFeaturesAdapter.FeatureViewHolder>() {

    inner class FeatureViewHolder(val binding: ItemIncludedFeatureBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val binding = ItemIncludedFeatureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeatureViewHolder(binding)
    }

    override fun getItemCount(): Int = features.size

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.binding.tvFeatureText.text = features[position]
    }

    fun updateData(newFeatures: List<String>) {
        features = newFeatures
        notifyDataSetChanged()
    }
}
