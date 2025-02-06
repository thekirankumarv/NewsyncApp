package com.thekirankumarv.newsync.authentication.presentation.ui

import android.annotation.SuppressLint
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.authentication.presentation.components.OTPTextFields
import com.thekirankumarv.newsync.authentication.presentation.state.OtpUiState
import com.thekirankumarv.newsync.authentication.presentation.state.OtpVerificationViewModel
import com.thekirankumarv.newsync.navigation.Dest
import androidx.compose.ui.platform.LocalFocusManager

//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    viewModel: OtpVerificationViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    var otpVal: String? by remember { mutableStateOf(null) }
    val phoneNumber = remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState() // Added this line
    var isResendEnabled by remember { mutableStateOf(false) }
    var timer by remember { mutableIntStateOf(30) }
    var showTimer by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val customView = remember { LottieAnimationView(context) }

    // Timer logic
    LaunchedEffect(key1 = showTimer, key2 = timer) {
        if (showTimer && timer > 0) {
            kotlinx.coroutines.delay(1000L)
            timer--
            if (timer == 0) {
                isResendEnabled = true
                showTimer = false
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is OtpUiState.Success -> {
                Toast.makeText(context, "Verification Successful", Toast.LENGTH_SHORT).show()
                navController.navigate(Dest.RegisterScreen)
            }
            is OtpUiState.Error -> {
                Toast.makeText(context, (uiState as OtpUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            is OtpUiState.CodeSent -> {
                Toast.makeText(context, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "Phone Verification",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight(400)
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        navController.navigate(Dest.RegisterScreen)
                    }) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)

            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AndroidView(
                    factory = { customView },
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)
                ) { view ->
                    with(view) {
                        setAnimation(R.raw.phone_number_verify)
                        playAnimation()
                        repeatCount = LottieDrawable.INFINITE
                        foregroundGravity = Gravity.CENTER
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                OutlinedTextField(
                    value = phoneNumber.value,
                    onValueChange = { newValue ->
                        if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                            phoneNumber.value = newValue
                        }
                    },
                    label = { Text(text = "Phone Number") },
                    placeholder = { Text(text = "Phone Number") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone Number") },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                if (isResendEnabled) {
                                    viewModel.sendOtp(activity, phoneNumber.value)
                                    isResendEnabled = false
                                    timer = 30
                                    showTimer = true
                                }
                            },
                            enabled = isResendEnabled
                        ) {
                            Text(
                                text = if (isResendEnabled) "Resend" else "Wait",
                                color = if (isResendEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.sendOtp(activity, phoneNumber.value)
                        isResendEnabled = false
                        timer = 30
                        showTimer = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(45.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    enabled = phoneNumber.value.length == 10 && !showTimer // Added validation
                ) {
                    Text(text = "Send OTP", fontSize = 15.sp, color = Color.White)
                }

                if (showTimer) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "You can resend OTP in $timer seconds",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Enter the OTP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OTPTextFields(
                    length = 6
                ) { getOpt ->
                    otpVal = getOpt
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Updated Verify OTP button with proper state handling
                Button(
                    onClick = { viewModel.verifyOtp(otpVal!!) },
                    enabled = isOtpSent && !otpVal.isNullOrEmpty() && otpVal?.length == 6,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(45.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "Verify OTP",
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                if (uiState is OtpUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}


