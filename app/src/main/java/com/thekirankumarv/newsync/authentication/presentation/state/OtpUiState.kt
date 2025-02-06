package com.thekirankumarv.newsync.authentication.presentation.state

sealed class OtpUiState {
    object Initial : OtpUiState()
    object Loading : OtpUiState()
    object Success : OtpUiState()
    object CodeSent : OtpUiState()
    object VerificationCompleted : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}
