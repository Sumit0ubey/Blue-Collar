package com.vibedev.bluecollar.ui.home

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibedev.bluecollar.adapter.RequestHistoryAdapter
import com.vibedev.bluecollar.data.JobHistory
import com.vibedev.bluecollar.databinding.ActivityRequestHistoryBinding
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.viewModels.RequestHistoryViewModel
import kotlinx.coroutines.launch

class RequestHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestHistoryBinding
    private val requestHistoryViewModel: RequestHistoryViewModel by viewModels()

    private var initialDateFilter: String? = null
    private var initialSortOrder: String? = null
    private var initialLimit: String? = null
    private val TAG = "RequestHistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDropdowns()
        binding.applyButton.setOnClickListener {
            fetchRequestHistory()
            binding.applyButton.visibility = View.GONE
        }
    }

    private fun setupDropdowns() {
        val dateFilters = arrayOf("Today", "This week", "This Month", "This Year")
        val sortOrders = arrayOf("Newest first", "Oldest first")
        val limits = arrayOf("5", "10", "15")

        val dateAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dateFilters)
        val dateEditText = binding.dateFilterDropdown.editText as? AutoCompleteTextView
        dateEditText?.setAdapter(dateAdapter)
        initialDateFilter = dateFilters[2]
        dateEditText?.setText(initialDateFilter, false)
        dateEditText?.doOnTextChanged { _, _, _, _ -> checkFiltersChanged() }

        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sortOrders)
        val sortEditText = binding.sortOrderDropdown.editText as? AutoCompleteTextView
        sortEditText?.setAdapter(sortAdapter)
        initialSortOrder = sortOrders[0]
        sortEditText?.setText(initialSortOrder, false)
        sortEditText?.doOnTextChanged { _, _, _, _ -> checkFiltersChanged() }

        val limitAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, limits)
        val limitEditText = binding.limitDropdown.editText as? AutoCompleteTextView
        limitEditText?.setAdapter(limitAdapter)
        initialLimit = limits[1]
        limitEditText?.setText(initialLimit, false)
        limitEditText?.doOnTextChanged { _, _, _, _ -> checkFiltersChanged() }
        limitEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkFiltersChanged()
                true
            } else {
                false
            }
        }

        fetchRequestHistory()
    }

    private fun checkFiltersChanged() {
        val dateFilter = binding.dateFilterDropdown.editText?.text.toString()
        val sortOrder = binding.sortOrderDropdown.editText?.text.toString()
        val limit = binding.limitDropdown.editText?.text.toString()

        val filtersChanged = dateFilter != initialDateFilter || sortOrder != initialSortOrder || limit != initialLimit
        binding.applyButton.visibility = if (filtersChanged) View.VISIBLE else View.GONE
    }

    private fun fetchRequestHistory() {
        showLoading(true)
        initialDateFilter = binding.dateFilterDropdown.editText?.text.toString()
        initialSortOrder = binding.sortOrderDropdown.editText?.text.toString()
        initialLimit = binding.limitDropdown.editText?.text.toString()

        lifecycleScope.launch {
            val reverseOrder = initialSortOrder == "Oldest first"
            val history = try {
                when (initialDateFilter) {
                    "Today" -> requestHistoryViewModel.getTodayRequestHistory(initialLimit?.toIntOrNull(), reverseOrder)
                    "This week" -> requestHistoryViewModel.getThisWeekRequestHistory(initialLimit?.toIntOrNull(), reverseOrder)
                    "This Month" -> requestHistoryViewModel.getThisMonthRequestHistory(initialLimit?.toIntOrNull(), reverseOrder)
                    "This Year" -> requestHistoryViewModel.getThisYearRequestHistory(initialLimit?.toIntOrNull(), reverseOrder)
                    else -> requestHistoryViewModel.getRequestHistory(initialLimit?.toIntOrNull(), reverseOrder)
                }
            } catch (e: Exception) {
                logError(TAG, "error while fetching request history: " + e.message.toString())
                emptyList<JobHistory>()
            }

            showLoading(false)
            if (history.isEmpty()) {
                binding.recyclerRequestHistory.visibility = View.GONE
                binding.noHistoryText.visibility = View.VISIBLE
            } else {
                binding.recyclerRequestHistory.visibility = View.VISIBLE
                binding.noHistoryText.visibility = View.GONE
                setupRecyclerView(history)
            }
        }
    }

    private fun setupRecyclerView(history: List<JobHistory>) {
        val historyAdapter = RequestHistoryAdapter(history)
        binding.recyclerRequestHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerRequestHistory.adapter = historyAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.recyclerRequestHistory.visibility = View.GONE
            binding.noHistoryText.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
