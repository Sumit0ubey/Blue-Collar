package com.vibedev.bluecollar.ui.myjobs

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.Job
import com.vibedev.bluecollar.databinding.ActivityJobDetailsBinding
import com.vibedev.bluecollar.utils.capitalizeEachWord
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.utils.getDrawableForService
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.viewModels.RequestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailsBinding
    private val viewModel: RequestViewModel by viewModels()
    private var job: Job? = null
    private var jobId: String? = null
    private val tag = "JobDetailsActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        jobId = intent.getStringExtra("jobId")

        setupClickListeners()

        fetchJobDetails()
    }

    private fun fetchJobDetails() {
        val id = jobId ?: return
        lifecycleScope.launch {
            binding.detailsProgress.visibility = View.VISIBLE
            binding.errorLayout.visibility = View.GONE
            binding.jobDetailsContent.visibility = View.GONE

            try {
                job = viewModel.getJobById(id)
                if (job != null) {
                    updateUi()
                    binding.jobDetailsContent.visibility = View.VISIBLE
                } else {
                    binding.errorLayout.visibility = View.VISIBLE
                }
            } catch (_: Exception) {
                binding.errorLayout.visibility = View.VISIBLE
            } finally {
                binding.detailsProgress.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.retryButton.setOnClickListener {
            fetchJobDetails()
        }

        binding.cancelButtonDetails.setOnClickListener {
            val id = jobId ?: return@setOnClickListener
            handleJobAction { viewModel.cancelJobRequest(id) }
        }

        binding.markCompleteButtonDetails.setOnClickListener {
            val id = jobId ?: return@setOnClickListener
            handleJobAction { viewModel.markJobAsComplete(id) }
        }
    }

    private fun handleJobAction(action: suspend () -> Unit) {
        lifecycleScope.launch {
            binding.actionProgress.visibility = View.VISIBLE
            binding.cancelButtonDetails.isEnabled = false
            binding.markCompleteButtonDetails.isEnabled = false

            try {
                action()
                delay(1000)
                fetchJobDetails()
            } catch (e: Exception) {
                logError(tag, "Error handling job action", e)
            } finally {
                binding.actionProgress.visibility = View.GONE
                binding.cancelButtonDetails.isEnabled = true
                binding.markCompleteButtonDetails.isEnabled = true
            }
        }
    }

    private fun updateUi() {
        val formattedDate = job?.date?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: "Date not available"
        val formattedCost = "₹ ${job?.cost}"

        binding.collapsingToolbar.title = "Professional ${job?.serviceType?.capitalizeFirst()} Needed"
        binding.serviceTypeDetails.text = job?.serviceType?.capitalizeFirst()
        binding.jobDescriptionDetails.text = job?.description?.capitalizeFirst()
        binding.jobDateDetails.text = formattedDate
        binding.jobCostDetails.text = formattedCost
        binding.jobLocationDetails.text = job?.address?.capitalizeFirst()

        val drawableResId = getDrawableForService(job?.serviceType?.lowercase() ?: "")
        AppCompatResources.getDrawable(this, drawableResId)?.let {
            binding.jobHeroImage.setImageDrawable(it)
        }

        if (AppData.userProfile?.isServiceProvider ?: false){
            binding.jobPartyNameDetails.text = job?.customerName?.capitalizeEachWord()
            binding.jobNumberDetails.text = job?.customerPhoneNumber
        } else {
            binding.partyHeader.text = getString(R.string.provider_details)
            binding.jobPartyNameDetails.text = job?.providerName?.capitalizeEachWord()
            binding.jobNumberDetails.text = job?.providerNumber
        }

        if (AppData.authToken == job?.customerId && (job?.status == AppData.OPEN || job?.status == AppData.ACCEPTED)
        ) {
            binding.cancelButtonDetails.visibility = View.VISIBLE
        } else {
            binding.cancelButtonDetails.visibility = View.GONE
        }

        if (AppData.authToken == job?.customerId && job?.status == AppData.ACCEPTED) {
            binding.markCompleteButtonDetails.visibility = View.VISIBLE
        } else {
            binding.markCompleteButtonDetails.visibility = View.GONE
        }
    }
    

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
