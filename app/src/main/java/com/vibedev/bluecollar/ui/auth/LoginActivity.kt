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
import com.vibedev.bluecollar.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tilEmail.error = "Email is required"
                binding.tilPassword.error = "Password is required"
                return@setOnClickListener
            }

            binding.tilEmail.error = null
            binding.tilPassword.error = null

            showLoading(true)
            lifecycleScope.launch {
                handleEmailLogin(email, password)
            }
        }

        binding.btnLoginGoogle.setOnClickListener {
            showLoading(true)
            lifecycleScope.launch {
                try {
                    authViewModel.googleLogin(this@LoginActivity)
                } catch (e: IllegalStateException) {
                    showLoading(false)
                    logError(TAG, "User cancelled Google login", e)
                    showToast(this@LoginActivity, "Login cancelled")
                }
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
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

    private suspend fun handleEmailLogin(email: String, password: String) {
        val success = authViewModel.login(email, password)

        if (success) {
            authViewModel.getCurrentUser()?.let {
                handleAuthenticatedUser(it)
            }
        } else {
            showLoading(false)
            showToast(this, "Invalid email or password")
        }
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
        binding.tilEmail.isEnabled = enabled
        binding.tilPassword.isEnabled = enabled
        binding.btnLogin.isEnabled = enabled
        binding.btnLoginGoogle.isEnabled = enabled
        binding.tvSignup.isEnabled = enabled
        binding.tvForgotPassword.isEnabled = enabled
    }
}
