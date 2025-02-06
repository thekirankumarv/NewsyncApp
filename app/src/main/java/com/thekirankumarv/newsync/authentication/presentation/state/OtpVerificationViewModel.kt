package com.thekirankumarv.newsync.authentication.presentation.state

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OtpVerificationViewModel @Inject constructor() : ViewModel() {
    private val mAuth = FirebaseAuth.getInstance()
    private var verificationOtp = ""

    private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Initial)
    val uiState: StateFlow<OtpUiState> = _uiState

    // Track whether OTP has been sent
    private var _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent

    // Track number of verification attempts
    private var verificationAttempts = 0
    private val MAX_ATTEMPTS = 3

    init {
        FirebaseAuth.getInstance().firebaseAuthSettings
            .setAppVerificationDisabledForTesting(false)
    }

    fun sendOtp(activity: ComponentActivity, phoneNumber: String) {
        // Validate phone number format
        if (!isValidPhoneNumber(phoneNumber)) {
            _uiState.value = OtpUiState.Error("Please enter a valid 10-digit phone number")
            return
        }

        _uiState.value = OtpUiState.Loading

        try {
            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+91$phoneNumber")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        _uiState.value = OtpUiState.VerificationCompleted
                        _isOtpSent.value = true
                    }

                    override fun onVerificationFailed(exception: FirebaseException) {
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                _uiState.value = OtpUiState.Error("Invalid phone number format")
                            }
                            is FirebaseTooManyRequestsException -> {
                                _uiState.value = OtpUiState.Error("Too many requests. Please try again later")
                            }
                            else -> {
                                _uiState.value = OtpUiState.Error(exception.message ?: "Verification failed")
                            }
                        }
                        _isOtpSent.value = false
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verificationId, token)
                        verificationOtp = verificationId
                        _isOtpSent.value = true
                        _uiState.value = OtpUiState.CodeSent
                        verificationAttempts = 0  // Reset attempts when new OTP is sent
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            _uiState.value = OtpUiState.Error("Failed to send OTP. Please try again")
            _isOtpSent.value = false
        }
    }

    fun verifyOtp(otp: String) {
        // Check if OTP was sent first
        if (!_isOtpSent.value) {
            _uiState.value = OtpUiState.Error("Please request OTP first")
            return
        }

        // Validate OTP format
        if (!isValidOtp(otp)) {
            _uiState.value = OtpUiState.Error("Please enter a valid 6-digit OTP")
            return
        }

        // Check verification attempts
        if (verificationAttempts >= MAX_ATTEMPTS) {
            _uiState.value = OtpUiState.Error("Too many failed attempts. Please request a new OTP")
            _isOtpSent.value = false
            return
        }

        _uiState.value = OtpUiState.Loading

        try {
            val credential = PhoneAuthProvider.getCredential(verificationOtp, otp)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = OtpUiState.Success
                    } else {
                        verificationAttempts++
                        val remainingAttempts = MAX_ATTEMPTS - verificationAttempts

                        when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                _uiState.value = OtpUiState.Error("Invalid OTP. $remainingAttempts attempts remaining")
                            }
                            else -> {
                                _uiState.value = OtpUiState.Error("Verification failed. Please try again")
                            }
                        }

                        if (verificationAttempts >= MAX_ATTEMPTS) {
                            _isOtpSent.value = false
                        }
                    }
                }
        } catch (e: Exception) {
            _uiState.value = OtpUiState.Error("Verification failed. Please try again")
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    private fun isValidOtp(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }

    fun resetState() {
        _uiState.value = OtpUiState.Initial
        _isOtpSent.value = false
        verificationAttempts = 0
    }
}




