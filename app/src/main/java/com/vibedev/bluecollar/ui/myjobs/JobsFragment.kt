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
            toggleJobsSectionVisibility(JobType.CURRENT, binding.currentJobsHeader, isCurrentJobsVisible)
        }

        binding.previousJobsHeader.setOnClickListener {
            isPreviousJobsVisible = !isPreviousJobsVisible
            toggleJobsSectionVisibility(JobType.PREVIOUS, binding.previousJobsHeader, isPreviousJobsVisible)
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

    private fun toggleJobsSectionVisibility(jobType: JobType, header: TextView, isVisible: Boolean) {
        val drawableId = if (isVisible) R.drawable.ic_keyboard_arrow_up else R.drawable.ic_keyboard_arrow_down
        header.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableId, 0)

        val recyclerView: RecyclerView
        val emptyView: View
        val adapter: JobAdapter

        when (jobType) {
            JobType.CURRENT -> {
                recyclerView = binding.currentJobsRecyclerView
                emptyView = binding.currentJobsEmpty
                adapter = currentJobsAdapter
            }
            JobType.PREVIOUS -> {
                recyclerView = binding.previousJobsRecyclerView
                emptyView = binding.previousJobsEmpty
                adapter = previousJobsAdapter
                binding.previousJobsFilterContainer.isVisible = isVisible
            }
        }

        if (isVisible) {
            val isListEmpty = adapter.currentList.isEmpty()
            recyclerView.isVisible = !isListEmpty
            emptyView.isVisible = isListEmpty
        } else {
            recyclerView.isVisible = false
            emptyView.isVisible = false
        }
    }

    private fun setLoadingState(jobType: JobType, isLoading: Boolean) {
        when (jobType) {
            JobType.CURRENT -> isCurrentJobsLoading = isLoading
            JobType.PREVIOUS -> isPreviousJobsLoading = isLoading
        }
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

    private fun fetchJobs(jobType: JobType, jobsFetcher: suspend () -> List<Job>?) {
        setLoadingState(jobType, true)

        val emptyView: View
        val recyclerView: RecyclerView
        val adapter: JobAdapter
        val isSectionVisible: Boolean

        when (jobType) {
            JobType.CURRENT -> {
                emptyView = binding.currentJobsEmpty
                recyclerView = binding.currentJobsRecyclerView
                adapter = currentJobsAdapter
                isSectionVisible = isCurrentJobsVisible
            }
            JobType.PREVIOUS -> {
                emptyView = binding.previousJobsEmpty
                recyclerView = binding.previousJobsRecyclerView
                adapter = previousJobsAdapter
                isSectionVisible = isPreviousJobsVisible
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jobs = jobsFetcher()
                adapter.submitList(jobs)
                _binding?.let {
                    if (isSectionVisible) {
                        val jobsIsNullOrEmpty = jobs.isNullOrEmpty()
                        emptyView.isVisible = jobsIsNullOrEmpty
                        recyclerView.isVisible = !jobsIsNullOrEmpty
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error fetching $jobType jobs: ${e.message}")
                adapter.submitList(null)
                _binding?.let {
                    if (isSectionVisible) {
                        emptyView.isVisible = true
                        recyclerView.isVisible = false
                    }
                }
            } finally {
                setLoadingState(jobType, false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
