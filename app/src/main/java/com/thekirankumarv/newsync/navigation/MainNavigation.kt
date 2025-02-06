package com.thekirankumarv.newsync.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thekirankumarv.newsync.authentication.presentation.state.AuthState
import com.thekirankumarv.newsync.authentication.presentation.ui.OtpVerificationScreen
import com.thekirankumarv.newsync.authentication.presentation.state.AuthViewModel
import com.thekirankumarv.newsync.authentication.presentation.state.BiometricPromptManager
import com.thekirankumarv.newsync.authentication.presentation.state.WelcomeViewModel
import com.thekirankumarv.newsync.authentication.presentation.ui.LoginScreen
import com.thekirankumarv.newsync.authentication.presentation.ui.RegisterScreen
import com.thekirankumarv.newsync.authentication.presentation.ui.WelcomeScreen
import com.thekirankumarv.newsync.profile.presentation.ProfileScreen

@Composable
fun MainNavigation(activity: FragmentActivity, navController: NavHostController ) {

    val promptManager = remember { BiometricPromptManager(activity) }

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.observeAsState()


// Handle navigation based on authentication state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(Dest.HomeScreen) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(Dest.WelcomeScreen) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == Dest.HomeScreen::class.simpleName
    ) {
        // Do nothing, effectively disabling back press on HomeScreen
    }

    NavHost(navController = navController, startDestination = Dest.WelcomeScreen) {
        composable<Dest.WelcomeScreen> { navBackStackEntry ->
            val welcomeViewModel: WelcomeViewModel = viewModel(navBackStackEntry)
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(Dest.LoginScreen)
                },
                onRegisterClick = {
                    navController.navigate(Dest.RegisterScreen)
                },
                viewModel = welcomeViewModel,
            )
        }
        composable<Dest.LoginScreen> {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel,
            )
        }
        composable<Dest.RegisterScreen> {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel,
            )
        }
        composable<Dest.OtpVerificationScreen> {
            OtpVerificationScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }

        composable<Dest.HomeScreen> {
            BottomNavigation(
                authViewModel = authViewModel,
                navController = navController,
                activity = activity
            )
        }

        composable<Dest.ProfileScreen> {
            ProfileScreen(
                authViewModel = authViewModel,
                navController = navController,
                promptManager = promptManager
            )
        }

    }
}

