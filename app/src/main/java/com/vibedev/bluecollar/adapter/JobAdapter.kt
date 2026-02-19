package com.vibedev.bluecollar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.Job
import com.vibedev.bluecollar.databinding.ItemJobBinding
import com.vibedev.bluecollar.utils.capitalizeEachWord
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.viewModels.RequestViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class JobAdapter(private val requestViewModel: RequestViewModel) :
    ListAdapter<Job, JobAdapter.JobViewHolder>(JobDiffCallback()) {

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

        fun bind(job: Job) {
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

            val userProfile = AppData.userProfile
            if (userProfile != null) {
                if (userProfile.isServiceProvider) {
                    binding.jobPartyName.text = job.customerName.capitalizeEachWord()
                    binding.jobNumber.text = job.customerPhoneNumber
                } else {
                    binding.jobPartyName.text = job.providerName.capitalizeEachWord()
                    binding.jobNumber.text = job.providerNumber
                }
            }

            binding.jobLocation.text = "${job.city}, ${job.address}"

            if (AppData.authToken == job.customerId && (job.status == AppData.OPEN || job.status == AppData.ACCEPTED)) {
                binding.cancelButton.visibility = View.VISIBLE
            } else {
                binding.cancelButton.visibility = View.GONE
            }

            if (AppData.authToken == job.customerId && job.status == AppData.ACCEPTED){
                binding.markCompleteButton.visibility = View.VISIBLE
            } else {
                binding.markCompleteButton.visibility = View.GONE
            }

            binding.cancelButton.setOnClickListener {
                itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                    requestViewModel.cancelJobRequest(job.id)
                }
            }

            binding.markCompleteButton.setOnClickListener {
                itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                    requestViewModel.markJobAsComplete(job.id)
                }
            }
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}
