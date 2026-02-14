package com.vibedev.bluecollar.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.vibedev.bluecollar.data.JobHistory
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.databinding.ItemRequestHistoryBinding

class RequestHistoryAdapter(private val historyList: List<JobHistory>) :
    RecyclerView.Adapter<RequestHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemRequestHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(historyItem: JobHistory) {
            binding.historyRequestType.text = historyItem.serviceType.capitalizeFirst()
            binding.historyDescription.text = historyItem.summary.capitalizeFirst()
            binding.historyPrice.text = historyItem.cost
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemRequestHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size
}
