package com.vibedev.bluecollar.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.content.Context
import android.content.Intent
import android.view.ViewGroup

import com.vibedev.bluecollar.data.Service
import com.vibedev.bluecollar.services.GlideService
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.utils.capitalizeEachWord
import com.vibedev.bluecollar.databinding.ItemServiceBinding
import com.vibedev.bluecollar.ui.service.ServiceDetailActivity

class ServiceAdapter(
    private val services: List<Service>
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedService = services[position]
                    val context = binding.root.context

                    val intent = Intent(context, ServiceDetailActivity::class.java).apply {
                        putExtra("SERVICE_ID", clickedService.serviceID)
                    }
                    context.startActivity(intent)
                }
            }
        }

        fun bind(service: Service, context: Context) {
            binding.title.text = service.title.capitalizeEachWord()
            binding.description.text = service.description.capitalizeFirst()
            binding.price.text = service.price
            GlideService.loadImageWithRetry(context, service.icon,
                com.vibedev.bluecollar.R.drawable.progress_animation,
                com.vibedev.bluecollar.R.drawable.add_photo_icon, binding.serviceImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.bind(service, holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return services.size
    }
}
