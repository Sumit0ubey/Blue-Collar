package com.vibedev.bluecollar

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Person
import androidx.fragment.app.FragmentContainerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Work
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.width
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material3.IconButton
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Switch
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.fragment.app.Fragment
import androidx.compose.ui.Alignment
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.content.Intent
import android.os.Bundle
import android.view.View

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.ui.JobActivity
import com.vibedev.bluecollar.ui.theme.blue_500
import com.vibedev.bluecollar.ui.home.HomeFragment
import com.vibedev.bluecollar.ui.auth.LoginActivity
import com.vibedev.bluecollar.manager.SessionManager
import com.vibedev.bluecollar.viewModels.AuthViewModel
import com.vibedev.bluecollar.ui.NotificationsActivity
import com.vibedev.bluecollar.ui.theme.BlueCollarTheme
import com.vibedev.bluecollar.ui.profile.ProfileFragment
import com.vibedev.bluecollar.ui.service.ServiceFragment
import com.vibedev.bluecollar.viewModels.ProfileViewModel
import com.vibedev.bluecollar.ui.auth.AdditionalInfoActivity


class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isLoading by mutableStateOf(true)

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
                }
                isLoading = false
            } else {
                sessionManager.setProfileCompleted(false)
                startActivity(Intent(this@MainActivity, AdditionalInfoActivity::class.java))
                finish()
            }
        }

        enableEdgeToEdge()
        setContent {
            BlueCollarTheme {
                if (isLoading) {
                    LoadingScreen()
                } else {
                    BlueCollarMainScreen()
                }
            }
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
    val context = LocalActivity.current as FragmentActivity
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isOnline by rememberSaveable { mutableStateOf(false) }
    val userProfile = AppData.userProfile

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
                                Switch(
                                    checked = isOnline,
                                    onCheckedChange = { isOnline = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = blue_500
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        if (userProfile?.isServiceProvider == true && isOnline && currentDestination == AppDestinations.HOME) {
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
    SERVICE("Service", Icons.Filled.Menu),

    PROFILE("Profile", Icons.Filled.Person),
}
