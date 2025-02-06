package com.thekirankumarv.newsync.authentication.presentation.state

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(private val activity: FragmentActivity) {

    private val resultChannel = Channel<BiometricResult>()
    val promptResults = resultChannel.receiveAsFlow()

    fun clearResults() {
        // Send null or a specific "Cleared" state to reset the authentication state
        resultChannel.trySend(BiometricResult.Cleared)
    }

    fun showBiometricPrompt(
        title: String,
        description: String
    ) {
        val manager = BiometricManager.from(activity)
        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else BIOMETRIC_STRONG

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .apply {
                if (Build.VERSION.SDK_INT < 30) {
                    setNegativeButtonText("Cancel")
                }
            }
            .build()

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resultChannel.trySend(BiometricResult.AuthenticationSuccess)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )
        prompt.authenticate(promptInfo)
    }

    sealed interface BiometricResult {
        data object HardwareUnavailable : BiometricResult
        data object FeatureUnavailable : BiometricResult
        data class AuthenticationError(val error: String) : BiometricResult
        data object AuthenticationFailed : BiometricResult
        data object AuthenticationSuccess : BiometricResult
        data object AuthenticationNotSet : BiometricResult
        data object Cleared : BiometricResult  // New state for clearing results
    }
}