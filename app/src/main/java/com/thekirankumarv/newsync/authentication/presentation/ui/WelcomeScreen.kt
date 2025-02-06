package com.thekirankumarv.newsync.authentication.presentation.ui

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.authentication.presentation.intent.WelcomeIntent
import com.thekirankumarv.newsync.authentication.presentation.state.WelcomeState
import com.thekirankumarv.newsync.authentication.presentation.state.WelcomeViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.thekirankumarv.newsync.authentication.presentation.state.BiometricPromptManager
import android.provider.Settings

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: WelcomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (state) {
            is WelcomeState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is WelcomeState.Success -> {
                val successState = state as WelcomeState.Success

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Lottie Animation
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reporter))
                    val progress by animateLottieCompositionAsState(
                        composition,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(325.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title and Subtitle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = successState.text,
                            fontFamily = FontFamily.Cursive,
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your gateway to breaking news, vibrant \n discussions, and accurate weather updates. \n Stay informed, connect with communities, and \n explore the world - all in one app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.processIntent(WelcomeIntent.LoginClicked)
                                onLoginClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.extraSmall,
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 10.dp,
                                pressedElevation = 5.dp
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Login",
                                color = MaterialTheme.colorScheme.onPrimary
                                )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.processIntent(WelcomeIntent.RegisterClicked)
                                onRegisterClick()
                            },
                            border =  BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Register",
                                color = MaterialTheme.colorScheme.primary
                                )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            is WelcomeState.Error -> {
                val errorState = state as WelcomeState.Error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

