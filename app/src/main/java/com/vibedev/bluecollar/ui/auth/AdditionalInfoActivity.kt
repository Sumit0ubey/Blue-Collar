package com.vibedev.bluecollar.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vibedev.bluecollar.MainActivity
import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.databinding.ActivityAdditionalInfoBinding
import com.vibedev.bluecollar.manager.SessionManager
import com.vibedev.bluecollar.services.CloudinaryService
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.utils.showToast
import com.vibedev.bluecollar.viewModels.AuthViewModel
import com.vibedev.bluecollar.viewModels.ProfileViewModel
import kotlinx.coroutines.launch

class AdditionalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdditionalInfoBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val accountViewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var imageUri: Uri? = null
    private var TAG = "AdditionalInfoActivity"


    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        binding.profileImage.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdditionalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val serviceTypes = resources.getStringArray(R.array.service_types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, serviceTypes)
        binding.actvServiceType.setAdapter(adapter)

        binding.profileImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_service_provider) {
                binding.tilServiceType.visibility = View.VISIBLE
            } else {
                binding.tilServiceType.visibility = View.GONE
            }
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                val phoneNumber = binding.etPhone.text.toString()
                val city = binding.etCity.text.toString()
                val address = binding.etAddress.text.toString()
                val serviceType = if (binding.rgUserType.checkedRadioButtonId == R.id.rb_service_provider) {
                    binding.actvServiceType.text.toString()
                } else {
                    ""
                }

                showLoading(true, "Uploading image...")
                CloudinaryService.uploadToCloudinary(imageUri) { imageURL ->
                    lifecycleScope.launch {
                        try {
                            showLoading(true, "Saving profile...")
                            saveUserAdditionalInfo(phoneNumber, city, address, serviceType, imageURL)
                        } catch (e: Exception) {
                            logError(TAG, "Error saving additional user info.", e)
                            showToast(this@AdditionalInfoActivity, "Error saving profile: ${e.message}")
                        } finally {
                            showLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (binding.etPhone.text.isNullOrEmpty()) {
            binding.tilPhone.error = "Please enter your phone number"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        if (binding.etCity.text.isNullOrEmpty()) {
            binding.tilCity.error = "Please enter your City"
            isValid = false
        } else {
            binding.tilCity.error = null
        }

        if (binding.etAddress.text.isNullOrEmpty()) {
            binding.tilAddress.error = "Please enter your address"
            isValid = false
        } else {
            binding.tilAddress.error = null
        }

        if (binding.rgUserType.checkedRadioButtonId == R.id.rb_service_provider) {
            if (binding.actvServiceType.text.isNullOrEmpty()) {
                binding.tilServiceType.error = "Please select a service type"
                isValid = false
            } else {
                binding.tilServiceType.error = null
            }
        }
        return isValid
    }

    private fun showLoading(isLoading: Boolean, message: String? = null) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading && message != null) {
            binding.loadingText.text = message
        }
    }

    private suspend fun saveUserAdditionalInfo(
        phoneNumber: String,
        city: String,
        address: String,
        serviceType: String,
        imageUrl: String
    ) {
        val user = accountViewModel.getCurrentUser()

        if (user != null) {
            val isService = binding.rgUserType.checkedRadioButtonId == R.id.rb_service_provider
            profileViewModel.createProfile(
                userId = user.id,
                name = user.name,
                phone = phoneNumber,
                city = city,
                isServiceProvider = isService,
                serviceType = serviceType,
                address = address,
                image = imageUrl
            )

            sessionManager.setProfileCompleted(true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            logError(TAG, "User not logged in")
            showToast(this, "User not logged in", false)
        }
    }
}
