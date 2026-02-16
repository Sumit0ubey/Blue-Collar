package com.vibedev.bluecollar.ui.auth

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import kotlinx.coroutines.launch
import android.content.Intent
import android.os.Bundle
import android.view.View

import com.vibedev.bluecollar.MainActivity
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.utils.showToast
import com.vibedev.bluecollar.manager.SessionManager
import com.vibedev.bluecollar.viewModels.AuthViewModel
import com.vibedev.bluecollar.viewModels.ProfileViewModel
import com.vibedev.bluecollar.databinding.ActivitySignupBinding


class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var sessionManager: SessionManager
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var fullName: String
    private lateinit var email: String
    private lateinit var password: String
    private var tag = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignupWithEmail.setOnClickListener {
            binding.groupInitialSignup.visibility = View.GONE
            binding.groupAdditionalDetails.visibility = View.VISIBLE
        }

        binding.btnContinueGoogle.setOnClickListener {
            showLoading(true)
            lifecycleScope.launch {
                try {
                    authViewModel.googleLogin(this@SignupActivity)
                } catch (e: IllegalStateException) {
                    showLoading(false)
                    logError(tag, "User cancelled Google Signup", e)
                    showToast(this@SignupActivity, "Signup cancelled")
                }
            }
        }

        binding.btnCompleteSignup.setOnClickListener {
            if (validateAdditionalDetails()) {
                showLoading(true)
                lifecycleScope.launch {
                    handleEmailRegister(fullName, email, password)
                }
            }
        }

        binding.tvSignin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val user = authViewModel.getCurrentUser()
            if (user != null) {
                handleAuthenticatedUser(user)
            } else {
                showLoading(false)
            }
        }
    }

    private suspend fun handleEmailRegister(fullName: String, email: String, password: String) {
        val success = authViewModel.register(fullName, email, password)

        if (success) {
            authViewModel.getCurrentUser()?.let {
                handleAuthenticatedUser(it)
            }
        } else {
            showLoading(false)
            showToast(this, "Registration failed", false)
        }
    }

    private fun validateAdditionalDetails(): Boolean {
        fullName = binding.etFullName.text?.toString().orEmpty()
        email = binding.etEmail.text?.toString().orEmpty()
        password = binding.etPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            binding.tilFullName.error = "All fields are required"
            binding.tilEmail.error = "All fields are required"
            binding.tilPassword.error = "All fields are required"
            binding.tilConfirmPassword.error = "All fields are required"
            return false
        }

        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null


        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            return false
        }
        binding.tilConfirmPassword.error = null

        val passwordPattern =
            Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])\\S{8,}\$")

        if (!password.matches(passwordPattern)) {
            binding.tilPassword.error = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
            return false
        }
        binding.tilPassword.error = null

        return true
    }

    private suspend fun handleAuthenticatedUser(user: io.appwrite.models.User<Map<String, Any>>) {
        showLoading(true)
        val hasProfile = profileViewModel.doesProfileExist(user.id)

        if (hasProfile) {
            openMainActivity(user.id)
        } else {
            openNextActivity(user.id)
        }
    }

    private fun openNextActivity(userId: String) {
        sessionManager.saveAuthToken(userId)
        sessionManager.setProfileCompleted(false)
        startActivity(Intent(this, AdditionalInfoActivity::class.java))
        finish()
    }

    private fun openMainActivity(userId: String) {
        sessionManager.saveAuthToken(userId)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        setUiEnabled(!isLoading)
    }

    private fun setUiEnabled(enabled: Boolean) {
        binding.btnContinueGoogle.isEnabled = enabled
        binding.btnSignupWithEmail.isEnabled = enabled
        binding.btnCompleteSignup.isEnabled = enabled
        binding.tilFullName.isEnabled = enabled
        binding.tilEmail.isEnabled = enabled
        binding.tilPassword.isEnabled = enabled
        binding.tilConfirmPassword.isEnabled = enabled
        binding.tvSignin.isEnabled = enabled
    }

}
