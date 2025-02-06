package com.thekirankumarv.newsync.navigation

import kotlinx.serialization.Serializable

sealed class Dest{
    @Serializable
    data object WelcomeScreen : Dest()
    @Serializable
    data object LoginScreen : Dest()
    @Serializable
    data object RegisterScreen : Dest()
    @Serializable
    data object OtpVerificationScreen : Dest()
    @Serializable
    data object HomeScreen : Dest()
    @Serializable
    data object SearchScreen : Dest()
    @Serializable
    data object DetailsScreen : Dest()
    @Serializable
    data object ChatSectionScreen : Dest()
    @Serializable
    data class ChatScreen(val userId: String) : Dest()
    {
        val route = "chat_screen/$userId"
    }
    @Serializable
    data object ProfileScreen : Dest()

//    @Serializable
//    data object AboutScreen : Dest() {
//        const val route = "about_screen"
//        const val deepLinkPattern = "newsync://about"
//    }
}



