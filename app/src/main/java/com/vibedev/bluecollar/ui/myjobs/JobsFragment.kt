package com.vibedev.bluecollar.ui.myjobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.adapter.JobAdapter
import com.vibedev.bluecollar.data.Job
import com.vibedev.bluecollar.databinding.FragmentJobsBinding
import com.vibedev.bluecollar.utils.logDebug
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.viewModels.RequestViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JobsFragment : Fragment() {

    private val requestViewModel: RequestViewModel by viewModels()

    private val currentJobsAdapter by lazy {
        JobAdapter(requestViewModel) {
            fetchData(force = true)
        }
    }

    private val previousJobsAdapter by lazy {
        JobAdapter(requestViewModel) {
            fetchData(force = true)
        }
    }

    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!

    private var isCurrentJobsVisible = true
    private var isPreviousJobsVisible = true

    private var isCurrentJobsLoading = false
    private var isPreviousJobsLoading = false

    private var isActionLoading = false

    private var fetchCurrentJobsJob: CoroutineJob? = null
    private var fetchPreviousJobsJob: CoroutineJob? = null
    private var filterDebounceJob: CoroutineJob? = null

    private enum class Filter { TODAY, THIS_WEEK }
    private var currentFilter = Filter.TODAY

    private enum class JobType { CURRENT, PREVIOUS }

    companion object {
        private const val TAG = "JobsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        updateHeaderArrow(binding.currentJobsHeader, isCurrentJobsVisible)
        updateHeaderArrow(binding.previousJobsHeader, isPreviousJobsVisible)
        binding.previousJobsFilterContainer.isVisible = isPreviousJobsVisible

        applySectionVisibility(JobType.CURRENT)
        applySectionVisibility(JobType.PREVIOUS)

        fetchData(force = false)
    }

    private fun setupRecyclerView() {
        binding.currentJobsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = currentJobsAdapter
            setHasFixedSize(true)
        }
        binding.previousJobsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = previousJobsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.currentJobsHeader.setOnClickListener {
            isCurrentJobsVisible = !isCurrentJobsVisible
            updateHeaderArrow(binding.currentJobsHeader, isCurrentJobsVisible)
            applySectionVisibility(JobType.CURRENT)
        }

        binding.previousJobsHeader.setOnClickListener {
            isPreviousJobsVisible = !isPreviousJobsVisible
            updateHeaderArrow(binding.previousJobsHeader, isPreviousJobsVisible)
            binding.previousJobsFilterContainer.isVisible = isPreviousJobsVisible
            applySectionVisibility(JobType.PREVIOUS)
        }

        binding.refreshCurrentJobs.setOnClickListener {
            logDebug(TAG, "Refresh CURRENT clicked")
            fetchCurrentJobs(force = true)
        }

        binding.refreshPreviousJobs.setOnClickListener {
            logDebug(TAG, "Refresh PREVIOUS clicked")
            fetchPreviousJobs(force = true)
        }

        binding.previousJobsFilterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener

            val newFilter = when (checkedId) {
                R.id.chip_this_week -> {
                    binding.chipClear.isVisible = true
                    Filter.THIS_WEEK
                }
                else -> {
                    binding.chipClear.isVisible = false
                    Filter.TODAY
                }
            }

            if (newFilter == currentFilter) return@setOnCheckedStateChangeListener
            currentFilter = newFilter

            filterDebounceJob?.cancel()
            filterDebounceJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(250)
                logDebug(TAG, "Chip changed -> fetch PREVIOUS. filter=$currentFilter")
                fetchPreviousJobs(force = true)
            }
        }

        binding.chipClear.setOnClickListener {
            if (!binding.chipToday.isChecked) binding.chipToday.isChecked = true
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                requestViewModel.isLoading.collect { isLoading ->
                    isActionLoading = isLoading
                    updateGlobalLoadingState()
                }
            }
        }
    }

    private fun updateHeaderArrow(header: TextView, expanded: Boolean) {
        val drawableId =
            if (expanded) R.drawable.ic_keyboard_arrow_up
            else R.drawable.ic_keyboard_arrow_down

        header.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableId, 0)
    }

    private fun applySectionVisibility(jobType: JobType) {
        val b = _binding ?: return

        val expanded = when (jobType) {
            JobType.CURRENT -> isCurrentJobsVisible
            JobType.PREVIOUS -> isPreviousJobsVisible
        }

        val recycler = when (jobType) {
            JobType.CURRENT -> b.currentJobsRecyclerView
            JobType.PREVIOUS -> b.previousJobsRecyclerView
        }

        val empty = when (jobType) {
            JobType.CURRENT -> b.currentJobsEmpty
            JobType.PREVIOUS -> b.previousJobsEmpty
        }

        val adapter = when (jobType) {
            JobType.CURRENT -> currentJobsAdapter
            JobType.PREVIOUS -> previousJobsAdapter
        }

        if (!expanded) {
            recycler.isVisible = false
            empty.isVisible = false
            return
        }

        val isEmpty = adapter.currentList.isEmpty()
        recycler.isVisible = !isEmpty
        empty.isVisible = isEmpty
    }

    private fun updateGlobalLoadingState() {
        val b = _binding ?: return
        val isLoading = isCurrentJobsLoading || isPreviousJobsLoading || isActionLoading

        b.jobsProgress.isVisible = isLoading

        b.refreshCurrentJobs.isEnabled = !isLoading
        b.refreshPreviousJobs.isEnabled = !isLoading
        b.chipClear.isEnabled = !isLoading
        b.currentJobsHeader.isEnabled = !isLoading
        b.previousJobsHeader.isEnabled = !isLoading

        b.previousJobsFilterGroup.children.forEach {
            it.isEnabled = !isLoading
        }
    }

    private fun setLoadingState(jobType: JobType, isLoading: Boolean) {
        when (jobType) {
            JobType.CURRENT -> isCurrentJobsLoading = isLoading
            JobType.PREVIOUS -> isPreviousJobsLoading = isLoading
        }
        updateGlobalLoadingState()
    }

    private fun fetchData(force: Boolean) {
        fetchCurrentJobs(force = force)
        fetchPreviousJobs(force = force)
    }

    private fun fetchCurrentJobs(force: Boolean) {
        if (isCurrentJobsLoading && !force) return

        fetchCurrentJobsJob?.cancel()
        fetchCurrentJobsJob = fetchJobs(JobType.CURRENT) {
            requestViewModel.getCurrentRequests()
        }
    }

    private fun fetchPreviousJobs(force: Boolean) {
        if (isPreviousJobsLoading && !force) return

        fetchPreviousJobsJob?.cancel()
        fetchPreviousJobsJob = fetchJobs(JobType.PREVIOUS) {
            when (currentFilter) {
                Filter.TODAY -> requestViewModel.getPreviousRequestOfToday()
                Filter.THIS_WEEK -> requestViewModel.getPreviousRequestOfThisWeek()
            }
        }
    }

    private fun fetchJobs(
        jobType: JobType,
        jobsFetcher: suspend () -> List<Job>?
    ): CoroutineJob {
        setLoadingState(jobType, true)

        val adapter = when (jobType) {
            JobType.CURRENT -> currentJobsAdapter
            JobType.PREVIOUS -> previousJobsAdapter
        }

        return viewLifecycleOwner.lifecycleScope.launch {
            try {
                logDebug(TAG, "Fetching $jobType jobs...")
                val jobs = jobsFetcher().orEmpty()
                logDebug(TAG, "Fetched $jobType jobs size=${jobs.size}")

                adapter.submitList(jobs)
                applySectionVisibility(jobType)

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logError(TAG, "Error fetching $jobType jobs", e)

                adapter.submitList(emptyList())
                applySectionVisibility(jobType)

            } finally {
                setLoadingState(jobType, false)
            }
        }
    }

    override fun onDestroyView() {
        fetchCurrentJobsJob?.cancel()
        fetchPreviousJobsJob?.cancel()
        filterDebounceJob?.cancel()

        fetchCurrentJobsJob = null
        fetchPreviousJobsJob = null
        filterDebounceJob = null

        binding.currentJobsRecyclerView.adapter = null
        binding.previousJobsRecyclerView.adapter = null

        _binding = null
        super.onDestroyView()
    }
}
