package com.vibedev.bluecollar.ui.myjobs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.adapter.JobAdapter
import com.vibedev.bluecollar.data.Job
import com.vibedev.bluecollar.databinding.FragmentJobsBinding
import com.vibedev.bluecollar.viewModels.RequestViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class JobsFragment : Fragment() {

    private val requestViewModel: RequestViewModel by viewModels()
    private val currentJobsAdapter = JobAdapter()
    private val previousJobsAdapter = JobAdapter()

    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!

    private var isCurrentJobsVisible = true
    private var isPreviousJobsVisible = true

    private var isCurrentJobsLoading = false
    private var isPreviousJobsLoading = false

    private enum class Filter { TODAY, THIS_WEEK }
    private var currentFilter = Filter.TODAY

    private enum class JobType { CURRENT, PREVIOUS }

    companion object {
        private const val TAG = "JobsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()

        fetchData()
    }

    private fun setupRecyclerView() {
        binding.currentJobsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.currentJobsRecyclerView.adapter = currentJobsAdapter
        binding.previousJobsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.previousJobsRecyclerView.adapter = previousJobsAdapter
    }

    private fun setupClickListeners() {
        binding.currentJobsHeader.setOnClickListener {
            isCurrentJobsVisible = !isCurrentJobsVisible
            toggleJobsSectionVisibility(binding.currentJobsGroup, binding.currentJobsHeader, isCurrentJobsVisible)
        }

        binding.previousJobsHeader.setOnClickListener {
            isPreviousJobsVisible = !isPreviousJobsVisible
            toggleJobsSectionVisibility(binding.previousJobsGroup, binding.previousJobsHeader, isPreviousJobsVisible)
        }

        binding.refreshCurrentJobs.setOnClickListener {
            fetchCurrentJobs()
        }

        binding.refreshPreviousJobs.setOnClickListener {
            fetchPreviousJobs()
        }

        binding.previousJobsFilterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener

            currentFilter = when (checkedId) {
                R.id.chip_this_week -> {
                    binding.chipClear.isVisible = true
                    Filter.THIS_WEEK
                }
                else -> {
                    binding.chipClear.isVisible = false
                    Filter.TODAY
                }
            }
            fetchPreviousJobs()
        }

        binding.chipClear.setOnClickListener {
            binding.chipToday.isChecked = true
        }
    }

    private fun toggleJobsSectionVisibility(group: View, header: TextView, isVisible: Boolean) {
        group.isVisible = isVisible
        val drawable = if (isVisible) R.drawable.ic_keyboard_arrow_up else R.drawable.ic_keyboard_arrow_down
        header.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

    private fun updateOverallProgress() {
        _binding?.jobsProgress?.isVisible = isCurrentJobsLoading || isPreviousJobsLoading
    }

    private fun fetchData() {
        fetchCurrentJobs()
        fetchPreviousJobs()
    }

    private fun fetchCurrentJobs() {
        fetchJobs(JobType.CURRENT) { requestViewModel.getCurrentRequests() }
    }

    private fun fetchPreviousJobs() {
        fetchJobs(JobType.PREVIOUS) {
            when (currentFilter) {
                Filter.TODAY -> requestViewModel.getPreviousRequestOfToday()
                Filter.THIS_WEEK -> requestViewModel.getPreviousRequestOfThisWeek()
            }
        }
    }

    private fun setLoadingFlag(jobType: JobType, isLoading: Boolean) {
        when (jobType) {
            JobType.CURRENT -> isCurrentJobsLoading = isLoading
            JobType.PREVIOUS -> isPreviousJobsLoading = isLoading
        }
    }

    private fun fetchJobs(jobType: JobType, jobsFetcher: suspend () -> List<Job>?) {
        setLoadingFlag(jobType, true)
        updateOverallProgress()

        val group: View
        val emptyView: View
        val recyclerView: RecyclerView
        val adapter: JobAdapter
        val isSectionVisible: Boolean

        when (jobType) {
            JobType.CURRENT -> {
                group = binding.currentJobsGroup
                emptyView = binding.currentJobsEmpty
                recyclerView = binding.currentJobsRecyclerView
                adapter = currentJobsAdapter
                isSectionVisible = isCurrentJobsVisible
            }
            JobType.PREVIOUS -> {
                group = binding.previousJobsGroup
                emptyView = binding.previousJobsEmpty
                recyclerView = binding.previousJobsRecyclerView
                adapter = previousJobsAdapter
                isSectionVisible = isPreviousJobsVisible
            }
        }

        group.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jobs = jobsFetcher()
                _binding?.let {
                    val jobsIsNullOrEmpty = jobs.isNullOrEmpty()
                    emptyView.isVisible = jobsIsNullOrEmpty
                    recyclerView.isVisible = !jobsIsNullOrEmpty
                    if (!jobsIsNullOrEmpty) {
                        adapter.submitList(jobs)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error fetching $jobType jobs: ${e.message}")
                _binding?.let {
                    emptyView.isVisible = true
                    recyclerView.isVisible = false
                }
            } finally {
                _binding?.let {
                    if (isSectionVisible) {
                        group.isVisible = true
                    }
                }
                setLoadingFlag(jobType, false)
                updateOverallProgress()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
