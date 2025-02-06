package com.thekirankumarv.newsync

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thekirankumarv.newsync.home.domain.usecase.app_entry.AppEntryUseCases
import com.thekirankumarv.newsync.navigation.MainNavigation
import com.thekirankumarv.newsync.ui.theme.NewsyncTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.facebook.CallbackManager
import com.thekirankumarv.newsync.authentication.presentation.state.AuthViewModel
import com.thekirankumarv.newsync.chat.presentation.utils.LocaleManager
import com.thekirankumarv.newsync.chat.presentation.utils.PreferenceManager
import com.thekirankumarv.newsync.navigation.Dest
import android.Manifest


@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var appEntryUseCases: AppEntryUseCases

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var callbackManager: CallbackManager
    lateinit var navController : NavHostController
    private var pendingDeepLink: Uri? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Notification", "Notification permission granted")
                } else {
                    Log.d("Notification", "Notification permission denied")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        val savedLanguage = PreferenceManager.getLanguage(this)
        LocaleManager.setLocale(this, savedLanguage)

        callbackManager = CallbackManager.Factory.create()
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.4f,
                    0.0f
                )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }
                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    0.4f,
                    0.0f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }
        lifecycleScope.launch {
            appEntryUseCases.readAppEntry().collect{
                Log.d("Test",it.toString())
            }
        }

        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            NewsyncTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                navController = rememberNavController()
                MainNavigation(activity = this, navController)
                pendingDeepLink?.let {
                    navController.navigate(Dest.ProfileScreen)
                    pendingDeepLink = null
                }
            }
        }
        handleDeepLink(intent)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Delegate the result to the ViewModel
        val viewModel: AuthViewModel by viewModels()
        viewModel.onActivityResult(requestCode, resultCode, data)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    fun handleDeepLink(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            data?.let {
                Log.d("DeepLink", it.toString())
                if (::navController.isInitialized) {
                    navController.navigate(Dest.HomeScreen)
                } else {
                    pendingDeepLink = it
                }
            }
        }
    }
}

