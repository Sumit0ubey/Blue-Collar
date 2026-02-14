package com.vibedev.bluecollar.ui.profile

import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import kotlinx.coroutines.launch
import android.view.ViewGroup
import android.content.Intent
import android.os.Bundle
import android.view.View

import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.data.UserProfile
import com.vibedev.bluecollar.utils.capitalizeFirst
import com.vibedev.bluecollar.ui.auth.LoginActivity
import com.vibedev.bluecollar.manager.SessionManager
import com.vibedev.bluecollar.adapter.PortfolioAdapter
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.services.GlideService
import com.vibedev.bluecollar.viewModels.AuthViewModel
import com.vibedev.bluecollar.databinding.ActivityProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: ActivityProfileBinding? = null
    private val accountViewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                accountViewModel.logout()
                sessionManager.deleteAuthToken()

                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        bindProfileData(AppData.userProfile)
    }

    private fun bindProfileData(profile: UserProfile?) {

        GlideService.loadCircularImage(
            requireContext(),
            profile?.profileImage,
            R.drawable.progress_animation,
            R.drawable.account_icon,
            binding.ivProfileImage
        )

        binding.userName.text = profile?.name?.capitalizeFirst()
        binding.location.text = profile?.city?.capitalizeFirst() ?: ""
        
        val isServiceProvider = profile?.isServiceProvider ?: false

        if (isServiceProvider) {
            binding.userSpecialty.visibility = View.VISIBLE
            binding.aboutHeader.visibility = View.VISIBLE
            binding.aboutContent.visibility = View.VISIBLE
            binding.portfolioHeader.visibility = View.VISIBLE
            binding.recyclerViewPortfolio.visibility = View.VISIBLE

            binding.userSpecialty.text = profile.serviceType?.capitalizeFirst() ?: "Service Provider"
            binding.rating.text = profile.rating ?: "3.5"
            binding.aboutContent.text = profile.about?.capitalizeFirst() ?: "Professional ${profile.serviceType}".capitalizeFirst()

            setupPortfolioRecycleView()
        } else {
            binding.ratingIcon.setImageResource(R.drawable.phone_icon)
            binding.rating.text = "${profile?.phone}"
        }
    }

    private fun setupPortfolioRecycleView(){
        val portfolioImageUrls = AppData.userProfile?.portfolio ?: emptyList()

        val portfolioAdapter = PortfolioAdapter(portfolioImageUrls)
        binding.recyclerViewPortfolio.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = portfolioAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
