package com.thekirankumarv.newsync.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekirankumarv.newsync.profile.presentation.ProfileScreen
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.authentication.presentation.state.AuthState
import com.thekirankumarv.newsync.authentication.presentation.state.AuthViewModel
import com.thekirankumarv.newsync.authentication.presentation.state.BiometricPromptManager
import com.thekirankumarv.newsync.authentication.presentation.ui.LoginScreen
import com.thekirankumarv.newsync.chat.presentation.AboutScreen
import com.thekirankumarv.newsync.chat.presentation.ChatScreen
import com.thekirankumarv.newsync.chat.presentation.ChatSectionScreen
import com.thekirankumarv.newsync.chat.presentation.ChatViewModel
import com.thekirankumarv.newsync.home.domain.model.Article
import com.thekirankumarv.newsync.home.presentation.HomeScreen
import com.thekirankumarv.newsync.home.presentation.HomeViewModel
import com.thekirankumarv.newsync.home.presentation.details.DetailsScreen
import com.thekirankumarv.newsync.home.presentation.search.SearchScreen
import com.thekirankumarv.newsync.home.presentation.search.SearchViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomNavigation(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    activity: FragmentActivity
) {
    val bottomNavController = rememberNavController()
    var isBottomNavVisible by remember { mutableStateOf(true) }

    val items = listOf(
        BottomNavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Dest.HomeScreen
        ),
        BottomNavigationItem(
            title = "Chat",
            selectedIcon = ImageVector.vectorResource(R.drawable.chatfilled),
            unselectedIcon =  ImageVector.vectorResource(R.drawable.chatoutlined),
            route = Dest.ChatSectionScreen
        ),
        BottomNavigationItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = Dest.ProfileScreen
        ),
    )

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    // Monitor auth state changes
    val authState by authViewModel.authState.observeAsState()
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                navController.navigate(Dest.LoginScreen) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Scaffold(
            bottomBar = {
                // Conditionally render the bottom navigation bar
                if (isBottomNavVisible) {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = {
                                    selectedItemIndex = index
                                    bottomNavController.navigateToBottomBarRoute(item.route)
                                },
                                label = {
                                    Text(text = item.title)
                                },
                                icon = {
                                    BadgedBox(badge = {}) {
                                        Icon(
                                            imageVector = if (index == selectedItemIndex) {
                                                item.selectedIcon
                                            } else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            },
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                BottomNavigationGraph(
                    navController = bottomNavController,
                    authViewModel = authViewModel,
                    mainNavController = navController,
                    activity = activity,
                    onBottomNavVisibilityChange = { visible ->
                        isBottomNavVisible = visible
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainNavController: NavHostController,
    activity: FragmentActivity,
    onBottomNavVisibilityChange: (Boolean) -> Unit
) {
    val promptManager = remember { BiometricPromptManager(activity) }

    NavHost(
        navController = navController,
        startDestination = Dest.HomeScreen
    ) {
        composable<Dest.HomeScreen> {
            val viewModel: HomeViewModel = hiltViewModel()
            val articles = viewModel.news.collectAsLazyPagingItems()
            val state by viewModel.state
            HomeScreen(
                articles = articles,
                navigateToSearch = {
                    navigateToTap(
                        navController = navController,
                        route = Dest.SearchScreen
                    )
                },
                navigateToDetails = { article ->
                    navigateToDetails(
                        navController = navController,
                        article = article
                    )
                },
                state = state,
                event = { event ->
                    viewModel.onEvent(event)
                }
            )
        }

        composable<Dest.SearchScreen> {
            val viewModel: SearchViewModel = hiltViewModel()
            val state = viewModel.state.value
            SearchScreen(
                state = state,
                event = viewModel::onEvent,
                navigateToDetails = { article ->
                    navigateToDetails(
                        navController = navController,
                        article = article
                    )
                }
            )
        }

        composable<Dest.DetailsScreen>{
            navController.previousBackStackEntry?.savedStateHandle?.get<Article?>("article")
                ?.let { article ->
                    DetailsScreen(
                        article = article,
                        navigateUp = { navController.navigateUp() }
                    )
                }
        }

        composable<Dest.ChatSectionScreen> {
            ChatSectionScreen(navController)
        }

        composable(
            route = "chat_screen/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
                ?: return@composable
            val viewModel: ChatViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                onBottomNavVisibilityChange(false)
            }

            DisposableEffect(Unit) {
                onDispose {
                    onBottomNavVisibilityChange(true)
                }
            }

            ChatScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable<Dest.ProfileScreen> {
            ProfileScreen(
                authViewModel = authViewModel,
                navController = mainNavController,
                promptManager = promptManager
            )
        }

        composable<Dest.LoginScreen> {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel,
            )
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Dest
)


fun NavHostController.navigateToBottomBarRoute(dest: Dest) {
    navigate(dest) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}



private fun navigateToTap(navController: NavController, route: Dest) {
    navController.navigate(route) {
        navController.graph.startDestinationRoute?.let { homeScreen ->
            popUpTo(homeScreen) {
                saveState = true
            }
            restoreState = true
            launchSingleTop = true
        }
    }
}

private fun navigateToDetails(navController: NavController, article: Article) {
    navController.currentBackStackEntry?.savedStateHandle?.set("article", article)
    navController.navigate(
        route = Dest.DetailsScreen
    )
}

