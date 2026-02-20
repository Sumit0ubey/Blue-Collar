package com.vibedev.bluecollar.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vibedev.bluecollar.data.JobShort
import com.vibedev.bluecollar.databinding.ItemJobBinding
import com.vibedev.bluecollar.ui.myjobs.JobDetailsActivity
import com.vibedev.bluecollar.utils.capitalizeFirst
import java.text.SimpleDateFormat
import java.util.Locale

class JobAdapter : ListAdapter<JobShort, JobAdapter.JobViewHolder>(JobDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

    inner class JobViewHolder(private val binding: ItemJobBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: JobShort) {
            binding.jobTitle.text = "Professional ${job.serviceType.capitalizeFirst()} Needed"
            binding.serviceType.text = job.serviceType.capitalizeFirst()
            binding.jobDescription.text = job.description.capitalizeFirst()
            binding.jobStatus.text = job.status.uppercase()
            binding.jobCost.text = "₹ ${job.cost}"

            if (job.date != null) {
                binding.jobDate.text =
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(job.date)
            } else {
                binding.jobDate.text = "Date not available"
            }

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, JobDetailsActivity::class.java)
                intent.putExtra("jobId", job.id)
                context.startActivity(intent)
            }
        }
    }

    companion object {
        private val JobDiffCallback = object : DiffUtil.ItemCallback<JobShort>() {
            override fun areItemsTheSame(oldItem: JobShort, newItem: JobShort): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: JobShort, newItem: JobShort): Boolean {
                return oldItem == newItem
            }
        }
    }
}
