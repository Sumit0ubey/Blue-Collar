package com.vibedev.bluecollar

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Application

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.manager.CloudinaryManager
import com.vibedev.bluecollar.viewModels.ServiceViewModel


class BlueCollarApp : Application() {

    private val serviceViewModel: ServiceViewModel by lazy { ServiceViewModel() }

    override fun onCreate() {
        super.onCreate()
        AppwriteManager.init(this)
        CloudinaryManager.init(this)

        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
            AppData.serviceTypes = serviceViewModel.getServiceTypeNames()
            AppData.services = serviceViewModel.getService()
        }
    }
}
