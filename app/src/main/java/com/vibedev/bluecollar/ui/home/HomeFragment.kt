package com.vibedev.bluecollar.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.adapter.RequestHistoryAdapter
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.JobHistory
import com.vibedev.bluecollar.databinding.ActivityHomeBinding
import com.vibedev.bluecollar.databinding.DialogNewRequestBinding
import com.vibedev.bluecollar.utils.capitalizeEachWord
import com.vibedev.bluecollar.utils.showToast
import com.vibedev.bluecollar.viewModels.RequestHistoryViewModel
import com.vibedev.bluecollar.viewModels.RequestViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    var requestHistory: List<JobHistory> = emptyList()
    private val requestViewModel: RequestViewModel by viewModels()
    private val requestHistoryViewModel: RequestHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.welcomeText.text = "Welcome, ${AppData.userProfile?.name?.capitalizeEachWord()}"

        binding.timeFilterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val selectedChip = group.findViewById<Chip>(selectedChipId)

            when (selectedChip.text.toString()) {
                "Today" -> showToast(requireContext(), "Showing Today")
                "This Week" -> showToast(requireContext(), "Showing This Week")
                "This Month" -> showToast(requireContext(), "Showing This Month")
                "This Year" -> showToast(requireContext(), "Showing This Year")
            }
        }

        binding.newRequestButton.setOnClickListener {
            showNewRequestDialog()
        }
        binding.viewAllButton.setOnClickListener {
            startActivity(Intent(requireContext(), RequestHistoryActivity::class.java))
        }

        setupRequestHistoryRecyclerView()
          
    }

    private fun showNewRequestDialog() {
        val dialogBinding = DialogNewRequestBinding.inflate(LayoutInflater.from(requireContext()))

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                AppData.serviceTypes
            )
        dialogBinding.formServiceType.setAdapter(adapter)

        dialogBinding.formName.text = Editable.Factory.getInstance().newEditable(AppData.userProfile?.name)
        dialogBinding.formCity.text = Editable.Factory.getInstance().newEditable(AppData.userProfile?.city)
        dialogBinding.formAddress.text = Editable.Factory.getInstance().newEditable(AppData.userProfile?.address)
        dialogBinding.formPhone.text = Editable.Factory.getInstance().newEditable(AppData.userProfile?.phone)


        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Service Request")
            .setView(dialogBinding.root)
            .setPositiveButton("Submit") { dialog, _ ->
                val name = dialogBinding.formName.text.toString().trim().lowercase()
                val serviceType = dialogBinding.formServiceType.text.toString().trim().lowercase()
                val serviceDescription = dialogBinding.formDescription.text.toString().trim().lowercase()
                val city = dialogBinding.formCity.text.toString().trim().lowercase()
                val address = dialogBinding.formAddress.text.toString().trim().lowercase()
                val phone = dialogBinding.formPhone.text.toString().trim().lowercase()

                val allFields = listOf(name, serviceType, serviceDescription, city, address, phone)
                if (allFields.all { it.isNotEmpty() }) {
                    postNewRequest(name, serviceType, serviceDescription, city, address, phone)
                    dialog.dismiss()
                } else {
                    showToast(requireContext(), "Please fill all fields")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun setupRequestHistoryRecyclerView() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                requestHistory = requestHistoryViewModel.getRequestHistory(limit = 5)
                
                if (requestHistory.isEmpty()) {
                    binding.historyRecyclerView.visibility = View.GONE
                    binding.noHistoryText.visibility = View.VISIBLE
                } else {
                    binding.historyRecyclerView.visibility = View.VISIBLE
                    binding.noHistoryText.visibility = View.GONE
                    
                    val historyAdapter = RequestHistoryAdapter(requestHistory)
                    binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.historyRecyclerView.adapter = historyAdapter
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun postNewRequest(name: String, serviceType: String, serviceDescription: String, city: String, address: String, phone: String) {
        val loadingDialog = createLoadingDialog()
        loadingDialog.show()
        val loadingText = loadingDialog.findViewById<TextView>(R.id.loading_text)

        viewLifecycleOwner.lifecycleScope.launch {
            loadingText?.text = "Submitting your request..."
            try {
                val userId = AppData.authToken ?: throw IllegalStateException("User not authenticated")
                requestViewModel.createRequest(userId, name, city, address, serviceDescription, serviceType)
                showToast(requireContext(), "Request Submitted!")
            } catch (e: Exception) {
                showToast(requireContext(), "Error: ${e.message}")
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    private fun createLoadingDialog(): AlertDialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()
    }

    private fun showLoading(isLoading: Boolean) {
        val contentViews = listOf(
            binding.welcomeText,
            binding.chipScrollView,
            binding.spendCard,
            binding.requestsHeader,
            binding.newRequestButton,
            binding.historyHeader,
            binding.viewAllButton
        )
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        contentViews.forEach { it.visibility = if (isLoading) View.GONE else View.VISIBLE }
        if (isLoading) {
            binding.earnCard.visibility = View.GONE
        } else {
            binding.earnCard.visibility = if (AppData.userProfile?.isServiceProvider == true) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
