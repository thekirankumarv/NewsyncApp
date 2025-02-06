package com.thekirankumarv.newsync.authentication.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thekirankumarv.newsync.authentication.presentation.components.SocialLoginSection
import com.thekirankumarv.newsync.authentication.presentation.components.TermsAndConditionsDialog
import com.thekirankumarv.newsync.authentication.presentation.components.TermsAndConditionsRow
import com.thekirankumarv.newsync.authentication.presentation.state.AuthState
import com.thekirankumarv.newsync.authentication.presentation.state.AuthViewModel
import com.thekirankumarv.newsync.navigation.Dest

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showTermsDialog by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 50.dp)
                ) {
                Text(
                    text = "Create Account",
                    fontFamily = FontFamily.Cursive,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Create an Account to Explore All the News",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Card(
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(12.dp),
            ){
                // Input Fields
                Column(
                    modifier = Modifier
                        .padding(top = 35.dp, bottom = 35.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val focusManager = LocalFocusManager.current

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text("Name",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email= it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    // Phone Verification Section
                    Card(
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon and Text Label
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f) // Take available space
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    text = "Verify your phone number",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            // Verify Button
                            Text(
                                text = "Verify",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { navController.navigate(Dest.OtpVerificationScreen) }
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }


                }

            }
            // Terms and Conditions Checkbox
            TermsAndConditionsRow(
                termsAccepted = termsAccepted,
                onTermsAcceptedChange = { termsAccepted = it },
                onShowTerms = { showTermsDialog = true }
            )

            if (showTermsDialog) {
                TermsAndConditionsDialog(
                    onAccept = {
                        termsAccepted = true
                        showTermsDialog = false
                    },
                    onDismiss = {
                        showTermsDialog = false
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Sign Up Button
                Button(
                    onClick = {
                        authViewModel.signup(email, password, name)

                    },
                    enabled = authState.value != AuthState.Loading && termsAccepted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Get Started",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Blue,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable { navController.navigate(Dest.LoginScreen){
                                navController.navigate(Dest.WelcomeScreen)
                            } }
                    )
                }
                Box{
                    Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                        SocialLoginSection(
                            viewModel = authViewModel,
                            navController = navController,
                            context = LocalContext.current
                        )
                    }
                }
            }
        }
    }
}

