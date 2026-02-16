package com.vibedev.bluecollar.ui.service

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.adapter.ServiceAdapter
import com.vibedev.bluecollar.databinding.ActivityServiceBinding

class ServiceFragment : Fragment() {

    private var _binding: ActivityServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupServicesRecyclerView()
    }

    private fun setupServicesRecyclerView() {
        val services = AppData.services
        binding.servicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ServiceAdapter(services)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
