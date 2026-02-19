package com.vibedev.bluecollar.ui

import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.animation.ArgbEvaluator
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Color
import android.view.View
import android.os.*
import android.view.WindowManager

import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.utils.showToast
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.databinding.ActivityIncomingRequestBinding


class IncomingRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingRequestBinding
    private var timer: CountDownTimer? = null
    private val tag = "IncomingRequestActivity"
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Service Request"

        val jobId = intent.getStringExtra("jobId") ?: run { finish(); return }
        val name = intent.getStringExtra("name") ?: ""
        val number = intent.getStringExtra("number") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        val cost = intent.getStringExtra("cost") ?: ""
        val city = intent.getStringExtra("city") ?: ""
        val serviceType = intent.getStringExtra("serviceType") ?: ""
        val providerName = intent.getStringExtra("providerName") ?: ""
        val providerNumber = intent.getStringExtra("providerNumber") ?: ""

        binding.tvName.text = name
        binding.tvServiceType.text = getString(R.string.service_label, serviceType)
        binding.tvCost.text = getString(R.string.cost_label, cost)
        binding.tvCity.text = getString(R.string.city_label, city)
        binding.tvAddress.text = getString(R.string.address_label, address)
        binding.tvNumber.text = getString(R.string.number_label, number)
        binding.tvDescription.text = getString(R.string.description_label, description)

        startVibration()

        val startColor = Color.GREEN
        val endColor = Color.RED

        timer = object : CountDownTimer(20000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvTimer.text = secondsRemaining.toString()
                binding.circularProgressBar.progress = millisUntilFinished.toInt()

                val fraction = millisUntilFinished.toFloat() / 20000
                val color = ArgbEvaluator().evaluate(1 - fraction, startColor, endColor) as Int
                val drawable = DrawableCompat.wrap(binding.circularProgressBar.progressDrawable)
                DrawableCompat.setTint(drawable, color)
            }

            override fun onFinish() {
                binding.btnDecline.performClick()
            }
        }.start()

        binding.btnDecline.setOnClickListener {
            stopVibration()
            showLoading(true)
            finish()
        }

        binding.btnAccept.setOnClickListener {
            stopVibration()
            showLoading(true)
            lifecycleScope.launch {
                try {
                    AppwriteManager.functions.acceptJob(jobId, providerName, providerNumber)
                } catch (e: Exception) {
                    logError(tag, "Job already taken or cancelled", e)
                    showToast(this@IncomingRequestActivity, "Job already taken")
                } finally {
                    finish()
                }
            }
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 400, 200, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            logError(tag, "Error vibrating", e)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonsLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        timer?.cancel()
        stopVibration()
        super.onDestroy()
    }
}
