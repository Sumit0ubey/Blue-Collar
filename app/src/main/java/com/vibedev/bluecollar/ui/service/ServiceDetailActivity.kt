package com.vibedev.bluecollar.ui.service

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.adapter.IncludedFeaturesAdapter
import com.vibedev.bluecollar.adapter.PortfolioAdapter
import com.vibedev.bluecollar.data.DetailService
import com.vibedev.bluecollar.databinding.ActivityServiceDetailBinding
import com.vibedev.bluecollar.services.GlideService
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.viewModels.ServiceViewModel
import kotlinx.coroutines.launch

class ServiceDetailActivity : AppCompatActivity() {

    private val serviceViewModel: ServiceViewModel by viewModels()
    private lateinit var binding: ActivityServiceDetailBinding
    private lateinit var featuresAdapter: IncludedFeaturesAdapter
    private lateinit var portfolioAdapter: PortfolioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        setupRecyclerViews()

        val serviceId = intent.getStringExtra("SERVICE_ID")
        if (serviceId != null) {
            loadServiceDetails(serviceId)
        } else {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        featuresAdapter = IncludedFeaturesAdapter(emptyList())
        binding.recyclerViewIncludedItems.apply {
            layoutManager = LinearLayoutManager(this@ServiceDetailActivity)
            adapter = featuresAdapter
            setHasFixedSize(true)
        }

        portfolioAdapter = PortfolioAdapter(emptyList())
        binding.recyclerViewPortfolio.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = portfolioAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadServiceDetails(serviceId: String) {
        lifecycleScope.launch {
            val service = serviceViewModel.getServiceDetail(serviceId)
            if (service != null) {
                updateUi(service)
            } else {
                finish()
            }
        }
    }

    private fun updateUi(service: DetailService) {
        binding.serviceCategory.text = service.serviceType.capitalizeFirst()
        binding.serviceTitle.text = service.title.capitalizeFirst()
        binding.serviceSummary.text = service.summary.capitalizeFirst()
        binding.fabBookNow.text = "Starts at ${service.price}"

        featuresAdapter.updateData(service.included)
        portfolioAdapter.updateData(service.portfolio)

        GlideService.loadImageWithRetry(
            this@ServiceDetailActivity,
            service.icon,
            R.drawable.progress_animation,
            R.drawable.add_photo_icon,
            binding.serviceImage
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
