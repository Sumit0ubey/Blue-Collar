package com.vibedev.bluecollar.ui

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import android.widget.ArrayAdapter
import kotlinx.coroutines.launch
import android.os.Bundle
import android.view.View

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.data.JobRequest
import com.vibedev.bluecollar.adapter.JobRequestAdapter
import com.vibedev.bluecollar.viewModels.RequestViewModel
import com.vibedev.bluecollar.databinding.ActivityJobBinding

class JobActivity: AppCompatActivity() {

    private lateinit var binding: ActivityJobBinding
    private val requestViewModel: RequestViewModel by viewModels()
    private var initialServiceType: String? = null
    private var initialLocation: String? = null
    private var jobs: List<JobRequest> = emptyList()

    companion object {
        private const val TAG = "JobActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDropdowns()
        binding.applyButton.setOnClickListener {
            fetchJobRequest()
        }
        observeLoadingState()
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            requestViewModel.isLoading.collect { isLoading ->
                showLoading(isLoading)
            }
        }
    }

    private fun setupDropdowns(){
        val serviceTypeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, AppData.serviceTypes)
        binding.serviceTypeDropdown.setAdapter(serviceTypeAdapter)
        binding.serviceTypeDropdown.doOnTextChanged { _, _, _, _ -> checkFiltersChanged() }

        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, AppData.SERVICE_LOCATIONS)
        binding.locationDropdown.setAdapter(locationAdapter)
        binding.locationDropdown.doOnTextChanged { _, _, _, _ -> checkFiltersChanged() }
        binding.locationDropdown.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkFiltersChanged()
                true
            } else {
                false
            }
        }
        fetchJobRequest()
    }

    private fun checkFiltersChanged() {
        val service = binding.serviceTypeDropdown.text.toString()
        val location = binding.locationDropdown.text.toString()

        val filtersChanged = service != initialServiceType || location != initialLocation
        binding.applyButton.visibility = if (filtersChanged) View.VISIBLE else View.GONE
    }

    private fun fetchJobRequest() {
        showLoading(true)
        val service = binding.serviceTypeDropdown.text.toString()
        val location = binding.locationDropdown.text.toString()

        lifecycleScope.launch {
            val serviceFilter = service.lowercase().ifEmpty { null }
            val locationFilter = location.lowercase().ifEmpty { null }

            jobs = try {
                requestViewModel.getOpenJobs(locationFilter, serviceFilter)
            } catch (e: Exception) {
                logError(TAG, "error while fetching job request: ${e.message}")
                emptyList()
            }

            initialServiceType = service
            initialLocation = location
            checkFiltersChanged()

            showLoading(false)
            if (jobs.isEmpty()) {
                binding.jobRecyclerView.visibility = View.GONE
                binding.noJobsText.visibility = View.VISIBLE
            } else {
                binding.jobRecyclerView.visibility = View.VISIBLE
                binding.noJobsText.visibility = View.GONE
                setupRecyclerView(jobs)
            }
        }
    }

    private fun setupRecyclerView(jobs: List<JobRequest>){
        val jobAdapter = JobRequestAdapter(jobs, requestViewModel) {
            fetchJobRequest()
        }
        binding.jobRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.jobRecyclerView.adapter = jobAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.jobRecyclerView.visibility = View.GONE
            binding.noJobsText.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
