package com.thekirankumarv.newsync.profile.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.authentication.presentation.state.AuthState
import com.thekirankumarv.newsync.authentication.presentation.state.AuthViewModel
import com.thekirankumarv.newsync.authentication.presentation.state.BiometricPromptManager
import com.thekirankumarv.newsync.authentication.presentation.state.ProfileInfo
import com.thekirankumarv.newsync.navigation.Dest

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    promptManager: BiometricPromptManager,
    context: Context = LocalContext.current
) {
    val user by authViewModel.user.collectAsState()
    val authState = authViewModel.authState.observeAsState()

    // State for editing mode
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingEmail by remember { mutableStateOf(false) }
    var pendingEditField by remember { mutableStateOf<String?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val userName = user?.name ?: ""
    var tempName by remember { mutableStateOf(userName) }

    val userEmail = user?.email ?: ""
    var tempEmail by remember { mutableStateOf(userEmail) }

    val biometricResult by promptManager.promptResults.collectAsState(initial = null)

    // Handle biometric result
    LaunchedEffect(biometricResult) {
        when (biometricResult) {
            is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                when (pendingEditField) {
                    "name" -> {
                        isEditingName = true
                        isEditingEmail = false
                    }
                    "email" -> {
                        isEditingEmail = true
                        isEditingName = false
                    }
                }
                // Clear the result after handling
                promptManager.clearResults()
            }
            is BiometricPromptManager.BiometricResult.Cleared -> {
                // Do nothing, this is just the reset state
            }
            is BiometricPromptManager.BiometricResult.AuthenticationError,
            is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                isEditingName = false
                isEditingEmail = false
                pendingEditField = null
                // Clear the result after handling
                promptManager.clearResults()
            }
            else -> {}
        }
    }

    // Function to handle edit requests
    fun handleEditRequest(field: String) {
        pendingEditField = field
        isEditingName = false
        isEditingEmail = false
        promptManager.showBiometricPrompt(
            title = "Authenticate to Edit",
            description = "Confirm your identity to edit your profile"
        )
    }

    // Reset states when editing is done
    fun handleEditingComplete() {
        isEditingName = false
        isEditingEmail = false
        pendingEditField = null
        // Reset the biometric result by triggering a recomposition
        promptManager.clearResults()
    }


    LaunchedEffect(user) {
        tempName = user?.name ?: ""
        tempEmail = user?.email ?: ""
    }

    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
    }

    // Monitor auth state changes
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                // Navigate to LoginScreen and clear the entire back stack
                navController.navigate(Dest.LoginScreen) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
            else -> {}
        }
    }

    // Validation functions
    fun validateName(name: String): Boolean {
        return when {
            name.isEmpty() -> {
                nameError = "Name cannot be empty"
                false
            }
            name.any { it.isDigit() } -> {
                nameError = "Name cannot contain numbers"
                false
            }
            name.length < 2 -> {
                nameError = "Name must be at least 2 characters"
                false
            }
            else -> {
                nameError = null
                true
            }
        }
    }

    fun validateEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            email.isEmpty() -> {
                emailError = "Email cannot be empty"
                false
            }
            !email.matches(emailPattern.toRegex()) -> {
                emailError = "Invalid email format"
                false
            }
            else -> {
                emailError = null
                true
            }
        }
    }

    Box(modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(),
            painter = painterResource(id = R.drawable.profile_screen_app_bar_bg),
            contentDescription = "App bar background",
            contentScale = ContentScale.FillWidth
        )
        IconButton(
            onClick = {
                navController.navigate(Dest.HomeScreen)
             },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                tint = Color.White,
                contentDescription = "Arrow Back",
                modifier = Modifier.size(35.dp)
            )
        }
        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 43.dp),
            text = "Profile",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(600),
                color = Color.White
            )
        )
        Card(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 106.dp,
                    bottom = 16.dp
                )
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(vertical = 24.dp),
            ) {
                // Profile Picture Section
                Box {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = Color.Black,
                                shape = CircleShape
                            )
                    ) {
                        val profilePicture = when (user) {
                            is ProfileInfo.GoogleUser -> (user as ProfileInfo.GoogleUser).profilePic
                            is ProfileInfo.FacebookUser -> (user as ProfileInfo.FacebookUser).profilePic
                            is ProfileInfo.ManualUser -> (user as ProfileInfo.ManualUser).profilePic
                            else -> null
                        }

                        if (profilePicture.isNullOrEmpty()) {
                            Image(
                                painter = painterResource(R.drawable.avatar),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            AsyncImage(
                                model = profilePicture,
                                contentDescription = "Profile Picture",
                                placeholder = painterResource(R.drawable.avatar),
                                error = painterResource(R.drawable.avatar),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .size(34.dp)
                            .offset(y = (-4).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.inversePrimary)
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)

                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                user?.let { currentUser ->
                    val userName = currentUser.name
                    val userEmail = currentUser.email

                    if (isEditingName) {
                        EditableProfileField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = "Name",
                            error = nameError,
                            keyboardType = KeyboardType.Text,
                            onDone = {
                                if (validateName(tempName)) {
                                    authViewModel.updateUserField(
                                        field = "name",
                                        value = tempName,
                                        onSuccess = { handleEditingComplete() },
                                        onFailure = { exception ->
                                            Toast.makeText(
                                                context,
                                                "Failed to update name: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            handleEditingComplete()
                                        }
                                    )
                                }
                            },
                            onCancel = {
                                tempName = userName
                                nameError = null
                                handleEditingComplete()
                            }
                        )
                    } else {
                        ProfileItem(
                            title = "Name",
                            subtitle = userName,
                            onEditClick = { handleEditRequest("name") }
                        )
                    }

                    if (isEditingEmail) {
                        EditableProfileField(
                            value = tempEmail,
                            onValueChange = { tempEmail = it },
                            label = "Email",
                            error = emailError,
                            keyboardType = KeyboardType.Email,
                            onDone = {
                                if (validateEmail(tempEmail)) {
                                    authViewModel.updateUserField(
                                        field = "email",
                                        value = tempEmail,
                                        onSuccess = { handleEditingComplete() },
                                        onFailure = { exception ->
                                            Toast.makeText(
                                                context,
                                                "Failed to update email: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            handleEditingComplete()
                                        }
                                    )
                                }
                            },
                            onCancel = {
                                tempEmail = userEmail
                                emailError = null
                                handleEditingComplete()
                            }
                        )
                    } else {
                        ProfileItem(
                            title = "Email",
                            subtitle = userEmail,
                            onEditClick = { handleEditRequest("email") }
                        )
                    }
                }

                val isNameValid = validateName(tempName)
                val isEmailValid = validateEmail(tempEmail)

                Button(
                    onClick = {
                        authViewModel.signout(context, navController as NavHostController)
                    },
                    enabled = isNameValid && isEmailValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Log out",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight(600),
                            color = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun EditableProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            isError = error != null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) Color.Red else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (error != null) Color.Red else Color.Gray
            )
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            TextButton(onClick = onDone) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ProfileItem(
    title: String,
    subtitle: String,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFA5A5A5)
                )
            )
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $title",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
    }
}