package com.vibedev.bluecollar.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vibedev.bluecollar.adapter.NotificationAdapter
import com.vibedev.bluecollar.databinding.ActivityNotificationsBinding
import com.vibedev.bluecollar.viewModels.NotificationViewModel

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    private val notificationViewModel: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerNotifications.layoutManager = LinearLayoutManager(this)

        adapter = NotificationAdapter(
            mutableListOf(),
            onItemClick = { /* to open/detail */ },
            onItemDelete = {
                if (adapter.itemCount == 0) {
                    binding.textNoNotifications.visibility = View.VISIBLE
                    binding.recyclerNotifications.visibility = View.GONE
                }
            }
        )
        binding.recyclerNotifications.adapter = adapter

        attachSwipeToDelete()

        observeViewModel()
        notificationViewModel.fetchNotifications()
    }

    private fun observeViewModel() {
        notificationViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerNotifications.visibility = View.GONE
                binding.textNoNotifications.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        notificationViewModel.notifications.observe(this) { result ->
            result.onSuccess { notifications ->
                if (notifications.isEmpty()) {
                    binding.recyclerNotifications.visibility = View.GONE
                    binding.textNoNotifications.visibility = View.VISIBLE
                } else {
                    binding.recyclerNotifications.visibility = View.VISIBLE
                    binding.textNoNotifications.visibility = View.GONE
                }
                adapter.updateList(notifications)
            }.onFailure { e ->
                binding.recyclerNotifications.visibility = View.GONE
                binding.textNoNotifications.visibility = View.GONE
                Toast.makeText(this, "Failed to load notifications: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attachSwipeToDelete() {
        val itemTouch = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return
                adapter.removeAt(pos)
            }
        }
        ItemTouchHelper(itemTouch).attachToRecyclerView(binding.recyclerNotifications)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}