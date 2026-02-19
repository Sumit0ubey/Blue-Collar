package com.vibedev.bluecollar

import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.manager.SessionManager
import com.vibedev.bluecollar.services.ProviderOnlineService
import com.vibedev.bluecollar.ui.JobActivity
import com.vibedev.bluecollar.ui.auth.AdditionalInfoActivity
import com.vibedev.bluecollar.ui.auth.LoginActivity
import com.vibedev.bluecollar.ui.home.HomeFragment
import com.vibedev.bluecollar.ui.myjobs.JobsFragment
import com.vibedev.bluecollar.ui.notification.NotificationsActivity
import com.vibedev.bluecollar.ui.profile.ProfileFragment
import com.vibedev.bluecollar.ui.service.ServiceFragment
import com.vibedev.bluecollar.ui.theme.BlueCollarTheme
import com.vibedev.bluecollar.ui.theme.blue_500
import com.vibedev.bluecollar.viewModels.AuthViewModel
import com.vibedev.bluecollar.viewModels.ProfileViewModel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private var isContentLoaded = false
    private var noInternetDialog: AlertDialog? = null
    private var onPermissionGranted: (() -> Unit)? = null

    private var isLoadingState by mutableStateOf(true)
    private var showMainState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BlueCollarTheme {
                if (isLoadingState) {
                    LoadingScreen()
                } else if (showMainState) {
                    BlueCollarMainScreen()
                } else {
                    LoadingScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isContentLoaded) return
        checkNetworkAndLoad()
    }

    override fun onPause() {
        super.onPause()
        noInternetDialog?.dismiss()
    }

    private fun checkNetworkAndLoad() {
        if (isNetworkAvailable()) {
            loadContent()
        } else {
            showNoInternetDialog()
        }
    }

    private fun loadContent() {
        isContentLoaded = true
        isLoadingState = true
        showMainState = false

        lifecycleScope.launch {
            val sessionManager = SessionManager(this@MainActivity)
            val user = authViewModel.getCurrentUser()

            if (user == null) {
                sessionManager.deleteAuthToken()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return@launch
            }

            sessionManager.saveAuthToken(user.id)
            AppData.authToken = user.id

            if (profileViewModel.doesProfileExist(user.id)) {
                val userProfile = profileViewModel.getProfile(user.id)
                sessionManager.setProfileCompleted(true)
                if (userProfile != null) {
                    AppData.userProfile = userProfile
                    if (userProfile.isServiceProvider) {
                        checkSystemAlertWindowPermission()
                    }
                }

                isLoadingState = false
                showMainState = true
            } else {
                sessionManager.setProfileCompleted(false)
                startActivity(Intent(this@MainActivity, AdditionalInfoActivity::class.java))
                finish()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun showNoInternetDialog() {
        noInternetDialog?.dismiss()

        noInternetDialog = MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please connect to the internet to continue.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
            .setNegativeButton("Retry") { _, _ ->
                checkNetworkAndLoad()
            }
            .setCancelable(false)
            .show()
    }

    fun ensureNotificationPermissionThen(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                onPermissionGranted = action
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                return
            }
        }
        action()
    }

    private fun checkSystemAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission Required")
                .setMessage("To receive incoming job alerts, please grant the 'draw over other apps' permission.")
                .setPositiveButton("Grant") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted?.invoke()
            onPermissionGranted = null
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "Setting up your profile...", color = MaterialTheme.colorScheme.onBackground)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun BlueCollarMainScreen() {

    val context = LocalActivity.current as MainActivity

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isOnline by rememberSaveable { mutableStateOf(false) }
    var isConnecting by rememberSaveable { mutableStateOf(false) }

    val userProfile = AppData.userProfile

    LaunchedEffect(Unit) {
        isOnline = isServiceRunning(context, ProviderOnlineService::class.java)
        isConnecting = false
    }

    val onSuccess = {
        isConnecting = false
        isOnline = true
    }
    val onError = {
        isConnecting = false
        isOnline = false
        Toast.makeText(context, "your are now offline.", Toast.LENGTH_SHORT).show()
    }

    val broadcastReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    ProviderOnlineService.ACTION_REALTIME_SUBSCRIPTION_SUCCESS -> onSuccess()
                    ProviderOnlineService.ACTION_REALTIME_SUBSCRIPTION_ERROR -> onError()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(ProviderOnlineService.ACTION_REALTIME_SUBSCRIPTION_SUCCESS)
            addAction(ProviderOnlineService.ACTION_REALTIME_SUBSCRIPTION_ERROR)
        }
        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }


    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    },
                    title = {
                        if (userProfile?.isServiceProvider == true && currentDestination == AppDestinations.HOME) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (isOnline) "Online" else "Offline")
                                Spacer(modifier = Modifier.width(8.dp))

                                if (isConnecting) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Switch(
                                        checked = isOnline,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                isConnecting = true
                                                context.ensureNotificationPermissionThen {
                                                    ContextCompat.startForegroundService(
                                                        context,
                                                        Intent(context, ProviderOnlineService::class.java)
                                                    )
                                                }
                                            } else {
                                                isOnline = false
                                                isConnecting = false
                                                context.stopService(
                                                    Intent(context, ProviderOnlineService::class.java)
                                                )
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = blue_500
                                        )
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (userProfile?.isServiceProvider == true &&
                            isOnline &&
                            currentDestination == AppDestinations.HOME
                        ) {
                            IconButton(onClick = {
                                context.startActivity(Intent(context, JobActivity::class.java))
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Work,
                                    contentDescription = "Jobs"
                                )
                            }
                        }

                        IconButton(onClick = {
                            context.startActivity(Intent(context, NotificationsActivity::class.java))
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notification"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            val currentFragment = remember(currentDestination) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeFragment()
                    AppDestinations.JOBS -> JobsFragment()
                    AppDestinations.SERVICE -> ServiceFragment()
                    AppDestinations.PROFILE -> ProfileFragment()
                }
            }

            FragmentContainer(
                modifier = Modifier.padding(innerPadding),
                fragment = currentFragment
            )
        }
    }
}

@Composable
fun FragmentContainer(
    modifier: Modifier = Modifier,
    fragment: Fragment
) {
    val context = LocalActivity.current as FragmentActivity
    val fragmentManager = context.supportFragmentManager
    val containerId = remember { View.generateViewId() }

    AndroidView(
        factory = { ctx ->
            FragmentContainerView(ctx).apply { id = containerId }
        },
        modifier = modifier.fillMaxSize(),
        update = {
            fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit()
        }
    )
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Filled.Home),
    JOBS("My Jobs", Icons.Filled.Window),
    SERVICE("Service", Icons.Filled.Menu),
    PROFILE("Profile", Icons.Filled.Person),
}


fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION")
    return am.getRunningServices(Int.MAX_VALUE)
        .any { it.service.className == serviceClass.name }
}
