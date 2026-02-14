package com.vibedev.bluecollar.adapter


import android.view.ViewGroup
import android.view.LayoutInflater
import com.vibedev.bluecollar.data.JobRequest
import androidx.recyclerview.widget.RecyclerView
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.databinding.ItemJobRequestBinding


class JobRequestAdapter(
    private val jobs: List<JobRequest>
) : RecyclerView.Adapter<JobRequestAdapter.JobRequestViewHolder>() {

    inner class JobRequestViewHolder(private val binding: ItemJobRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.acceptButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedJob = jobs[position]
                    acceptJob(clickedJob)
                }
            }
        }

        fun bind(job: JobRequest) {
            binding.jobTitle.text = job.serviceType.capitalizeFirst()
            binding.jobDescription.text = job.description?.capitalizeFirst()
            binding.jobAddress.text = job.address?.capitalizeFirst()
            binding.jobCity.text = job.city?.capitalizeFirst()
            binding.jobCost.text = "₹" + job.cost
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobRequestViewHolder {
        val binding = ItemJobRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobRequestViewHolder, position: Int) {
        val job = jobs[position]
        holder.bind(job)
    }

    override fun getItemCount(): Int {
        return jobs.size
    }

    private fun acceptJob(job: JobRequest) {
        // I will develop this function later
    }
}
