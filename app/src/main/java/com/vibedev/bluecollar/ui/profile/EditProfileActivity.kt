package com.vibedev.bluecollar.ui.profile

import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.isVisible
import androidx.activity.viewModels
import android.widget.ArrayAdapter
import kotlinx.coroutines.launch
import android.content.Intent
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.net.Uri

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.utils.showToast
import com.vibedev.bluecollar.services.GlideService
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.utils.capitalizeEachWord
import com.vibedev.bluecollar.services.CloudinaryService
import com.vibedev.bluecollar.viewModels.ProfileViewModel
import com.vibedev.bluecollar.adapter.PortfolioUploadAdapter
import com.vibedev.bluecollar.databinding.ActivityEditProfileBinding
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.services.RealtimeNotificationService
import com.vibedev.bluecollar.utils.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private var isServiceProvider: Boolean = false
    private val portfolioUris = mutableListOf<Uri>()
    private lateinit var portfolioAdapter: PortfolioUploadAdapter
    private val profileViewModel: ProfileViewModel by viewModels()
    private var originalProfileImage: String? = null

    private val pickProfileImage: ActivityResultLauncher<Intent> = registerImagePicker { uri ->
        imageUri = uri
        binding.profileImage.setImageURI(uri)
    }

    private val pickPortfolioImage: ActivityResultLauncher<Intent> = registerImagePicker { uri ->
        portfolioUris.add(uri)
        portfolioAdapter.notifyItemInserted(portfolioUris.size - 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun registerImagePicker(onImagePicked: (Uri) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let(onImagePicked)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profile"
    }

    private fun setupViews() {
        isServiceProvider = AppData.userProfile?.isServiceProvider ?: false
        binding.serviceCard.isVisible = isServiceProvider
        loadProfileData()
        if (isServiceProvider) {
            setupServiceTypeDropdown()
            setupPortfolio()
        }
    }

    private fun loadProfileData() {
        AppData.userProfile?.let { user ->
            binding.etFullName.setText(user.name)
            binding.etAddress.setText(user.address)
            binding.etPhone.setText(user.phone)
            binding.etLocation.setText(user.city)
            originalProfileImage = user.profileImage

            if (user.profileImage?.isNotEmpty() == true) {
                GlideService.loadCircularImage(
                    this,
                    user.profileImage,
                    android.R.drawable.ic_menu_camera,
                    android.R.drawable.ic_menu_camera,
                    binding.profileImage
                )
            }

            if (isServiceProvider) {
                binding.actvServiceType.setText(user.serviceType?.capitalizeFirst(), false)
                binding.etExperience.setText(user.about?.capitalizeFirst())
                user.portfolio?.let {
                    portfolioUris.addAll(it.map { Uri.parse(it) })
                }
            }
        }
    }

    private fun setupServiceTypeDropdown() {
        val serviceTypes = AppData.serviceTypes
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, serviceTypes)
        binding.actvServiceType.setAdapter(adapter)
    }

    private fun setupPortfolio() {
        portfolioAdapter = PortfolioUploadAdapter(portfolioUris) {
            launchImagePicker(pickPortfolioImage)
        }
        binding.recyclerViewPortfolioUpload.adapter = portfolioAdapter
        binding.recyclerViewPortfolioUpload.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupClickListeners() {
        binding.fabEditImage.setOnClickListener {
            launchImagePicker(pickProfileImage)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfile()
        }
    }

    private fun launchImagePicker(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        launcher.launch(intent)
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        val dataToUpdate = mutableMapOf<String, Any?>()

        AppData.userProfile?.let {
            if (fullName != it.name) dataToUpdate["name"] = fullName
            if (address != it.address) dataToUpdate["address"] = address
            if (phone != it.phone) dataToUpdate["phone"] = phone
            if (location != it.city) dataToUpdate["city"] = location
        }

        if (isServiceProvider) {
            val serviceType = binding.actvServiceType.text.toString().lowercase()
            val experience = binding.etExperience.text.toString().trim()

            AppData.userProfile?.let {
                if (serviceType != it.serviceType) dataToUpdate["serviceType"] = serviceType
                if (experience != it.about) dataToUpdate["about"] = experience
            }
        }

        val newImagePicked = imageUri != null
        val portfolioChanged = portfolioUris.map { it.toString() }.toSet() != AppData.userProfile?.portfolio?.toSet()

        if (dataToUpdate.isEmpty() && !newImagePicked && !portfolioChanged) {
            showToast(this, "No changes to save")
            return
        }

        showLoading("Saving profile...")

        val performFinalUpdate = { finalData: Map<String, Any?> ->
            if (finalData.isNotEmpty()) {
                updateProfileInViewModel(finalData)
            } else {
                hideLoading()
            }
        }

        if (newImagePicked) {
            showLoading("Uploading profile picture...")
            CloudinaryService.uploadToCloudinary(imageUri) { profilePicUrl ->
                if (profilePicUrl.isEmpty()) {
                    hideLoading()
                    showToast(this, "Failed to upload profile picture. Aborting save.")
                    return@uploadToCloudinary
                }
                dataToUpdate["profilePicUrl"] = profilePicUrl

                if (portfolioChanged) {
                    uploadPortfolioAndFinalize(dataToUpdate, performFinalUpdate)
                } else {
                    performFinalUpdate(dataToUpdate)
                }
            }
        } else if (portfolioChanged) {
            uploadPortfolioAndFinalize(dataToUpdate, performFinalUpdate)
        } else {
            performFinalUpdate(dataToUpdate)
        }
    }

    private fun uploadPortfolioAndFinalize(
        data: MutableMap<String, Any?>,
        onComplete: (Map<String, Any?>) -> Unit
    ) {
        showLoading("Uploading portfolio images...")
        val newPortfolioUris = portfolioUris.filter { !it.toString().startsWith("http") }
        val existingPortfolioUrls = portfolioUris.filter { it.toString().startsWith("http") }.map { it.toString() }

        if (newPortfolioUris.isNotEmpty()) {
            CloudinaryService.uploadImages(newPortfolioUris) { newUrls ->
                if (newUrls.size < newPortfolioUris.size) {
                    hideLoading()
                    showToast(this, "Some portfolio images failed to upload. Aborting save.")
                    return@uploadImages
                }
                data["portfolioPic"] = existingPortfolioUrls + newUrls
                onComplete(data)
            }
        } else {
            data["portfolioPic"] = existingPortfolioUrls
            onComplete(data)
        }
    }


    private fun updateProfileInViewModel(dataToUpdate: Map<String, Any?>) {
        if (dataToUpdate.isEmpty()) {
            hideLoading()
            showToast(this, "Failed to upload image. Please try again.")
            return
        }

        showLoading("Saving profile...")
        lifecycleScope.launch {
            try {
                AppData.authToken?.let { userId ->
                    profileViewModel.updateProfile(userId, dataToUpdate)

                    val currentUserProfile = AppData.userProfile
                    if (currentUserProfile != null) {
                        val newUserProfile = currentUserProfile.copy(
                            name = dataToUpdate["name"] as? String
                                ?: currentUserProfile.name?.capitalizeEachWord(),
                            address = dataToUpdate["address"] as? String
                                ?: currentUserProfile.address?.capitalizeEachWord(),
                            phone = dataToUpdate["phone"] as? String ?: currentUserProfile.phone,
                            city = dataToUpdate["city"] as? String
                                ?: currentUserProfile.city?.capitalizeFirst(),
                            serviceType = dataToUpdate["serviceType"] as? String
                                ?: currentUserProfile.serviceType?.capitalizeFirst(),
                            profileImage = dataToUpdate["profilePicUrl"] as? String
                                ?: currentUserProfile.profileImage,
                            about = dataToUpdate["about"] as? String ?: currentUserProfile.about,
							portfolio = dataToUpdate["portfolioPic"] as? List<String> ?: currentUserProfile.portfolio
                        )
                        AppData.userProfile = newUserProfile
                    }

                    val targetId = RealtimeNotificationService(applicationContext).stableTargetId()
                    runCatching {
                        withContext(Dispatchers.IO) {
                            AppwriteManager.functions.syncSubscriptions(targetId)
                        }
                    }.onFailure {
                        logError("Profile", "syncSubscriptions failed", it as Exception?)
                    }

                    hideLoading()
                    showToast(this@EditProfileActivity, "Profile saved successfully")
                    finish()
                } ?: run {
                    hideLoading()
                    showToast(this@EditProfileActivity, "User not logged in")
                }
            } catch (e: Exception) {
                hideLoading()
                showToast(this@EditProfileActivity, "Error saving profile: ${e.message}", false)
            }
        }
    }

    private fun showLoading(message: String) {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.loadingText.text = message
        setUiEnabled(false)
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        setUiEnabled(true)
    }

    private fun setUiEnabled(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
        binding.etFullName.isEnabled = enabled
        binding.etAddress.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.etLocation.isEnabled = enabled
        binding.fabEditImage.isEnabled = enabled
        binding.btnSaveChanges.isEnabled = enabled
        binding.actvServiceType.isEnabled = enabled
        binding.recyclerViewPortfolioUpload.isEnabled = enabled
    }
}
