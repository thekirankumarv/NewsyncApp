package com.thekirankumarv.newsync.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thekirankumarv.newsync.navigation.Dest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSectionScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0,0,0,0),
                title = {
                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(Dest.HomeScreen)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Gray,
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Message") },
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedContentColor = Color.Gray,
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("About") },
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedContentColor = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> ChatListScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToChat = { chatScreen ->
                        navController.navigate(chatScreen.route)
                    }
                )
                1 -> AboutScreen()
            }
        }
    }
}




