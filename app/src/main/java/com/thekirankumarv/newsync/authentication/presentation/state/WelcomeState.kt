package com.thekirankumarv.newsync.authentication.presentation.state

sealed class WelcomeState {
    data object Loading : WelcomeState()
    data class Success(
        val text: String,
        val isLoginClicked: Boolean = false,
        val isRegisterClicked: Boolean = false
    ) : WelcomeState()
    data class Error(val message: String) : WelcomeState()
}
