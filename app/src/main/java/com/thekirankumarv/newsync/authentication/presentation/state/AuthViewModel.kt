package com.thekirankumarv.newsync.authentication.presentation.state

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.navigation.Dest
import com.thekirankumarv.newsync.sendWelcomeNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    // Firebase and Authentication instances
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val callbackManager = CallbackManager.Factory.create()

    // State management
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    val user = MutableStateFlow<ProfileInfo?>(null)

    init {
        checkAuthState()
    }

    // AUTH STATE MANAGEMENT
    fun checkAuthState() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            fetchUserProfile(auth.currentUser)
        }
    }

    // EMAIL/PASSWORD AUTHENTICATION
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    fetchUserProfile(auth.currentUser)
                    // Send welcome notification
                    val userName = auth.currentUser?.displayName ?: "User"
                    sendWelcomeNotification(application, userName)
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login Failed")
                }
            }
    }

    fun signup(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Fields cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userData = ProfileInfo.ManualUser(
                        id = firebaseUser?.uid ?: "",
                        name = name,
                        email = email,
                        profilePic = ""
                    )
                    saveUserToFirestore(userData)
                    // Send welcome notification
                    sendWelcomeNotification(application, name)
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign-up Failed")
                }
            }
    }

    // GOOGLE AUTHENTICATION
    fun handleGoogleSignIn(context: Context, navController: NavController) {
        viewModelScope.launch {
            googleSignIn(context).collect { result ->
                result.fold(
                    onSuccess = { authResult ->
                        val currentUser = authResult.user
                        if (currentUser != null) {
                            user.value = ProfileInfo.GoogleUser(
                                id = currentUser.uid,
                                profilePic = currentUser.photoUrl.toString(),
                                email = currentUser.email ?: "",
                                name = currentUser.displayName ?: ""
                            )
                            _authState.value = AuthState.Authenticated
                            // Send welcome notification
                            sendWelcomeNotification(context, currentUser.displayName ?: "User")
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_LONG).show()
                            navController.navigate(Dest.HomeScreen) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onFailure = { e ->
                        _authState.value = AuthState.Error(e.message ?: "Google Sign In Failed")
                        Toast.makeText(context, "Something went wrong: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.d("GoogleSignIn", "handleGoogleSignIn: ${e.message}")
                    }
                )
            }
        }
    }

    private suspend fun googleSignIn(context: Context): Flow<Result<AuthResult>> {
        return callbackFlow {
            try {
                val credentialManager = CredentialManager.create(context)
                val nonce = UUID.randomUUID().toString()
                val hashedNonce = MessageDigest.getInstance("SHA-256")
                    .digest(nonce.toByteArray())
                    .fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    trySend(Result.success(authResult))
                } else {
                    throw RuntimeException("Invalid credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                trySend(Result.failure(Exception("Sign-in was canceled")))
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose()
        }
    }

    // FACEBOOK AUTHENTICATION
    fun handleFacebookSignIn(context: Context, navController: NavController) {
        viewModelScope.launch {
            facebookSignIn(context).collect { result ->
                result.fold(
                    onSuccess = { authResult ->
                        val currentUser = authResult.user
                        if (currentUser != null) {
                            user.value = ProfileInfo.FacebookUser(
                                id = currentUser.uid,
                                profilePic = currentUser.photoUrl.toString(),
                                email = currentUser.email ?: "",
                                name = currentUser.displayName ?: ""
                            )
                            _authState.value = AuthState.Authenticated
                            // Send welcome notification
                            sendWelcomeNotification(context, currentUser.displayName ?: "User")
                            Toast.makeText(context, "Facebook Login Successful!", Toast.LENGTH_LONG).show()
                            navController.navigate(Dest.HomeScreen) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onFailure = { e ->
                        _authState.value = AuthState.Error(e.message ?: "Facebook Sign In Failed")
                        Toast.makeText(context, "Facebook Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.d("FacebookSignIn", "handleFacebookSignIn: ${e.message}")
                    }
                )
            }
        }
    }

    private suspend fun facebookSignIn(context: Context): Flow<Result<AuthResult>> {
        return callbackFlow {
            try {
                val accessToken = AccessToken.getCurrentAccessToken()
                if (accessToken != null && !accessToken.isExpired) {
                    val credential = FacebookAuthProvider.getCredential(accessToken.token)
                    val authResult = auth.signInWithCredential(credential).await()
                    trySend(Result.success(authResult))
                } else {
                    LoginManager.getInstance().logInWithReadPermissions(
                        context as Activity,
                        listOf("email", "public_profile")
                    )

                    LoginManager.getInstance().registerCallback(callbackManager,
                        object : FacebookCallback<LoginResult> {
                            override fun onSuccess(result: LoginResult) {
                                viewModelScope.launch {
                                    try {
                                        val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                                        val authResult = auth.signInWithCredential(credential).await()
                                        trySend(Result.success(authResult))
                                    } catch (e: Exception) {
                                        trySend(Result.failure(e))
                                    }
                                }
                            }

                            override fun onCancel() {
                                trySend(Result.failure(Exception("Facebook Login Cancelled")))
                            }

                            override fun onError(error: FacebookException) {
                                trySend(Result.failure(error))
                            }
                        })
                }
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // USER PROFILE MANAGEMENT
    private fun fetchUserProfile(firebaseUser: FirebaseUser?) {
        firebaseUser?.let { user ->
            val providerId = user.providerData.lastOrNull()?.providerId

            when (providerId) {
                GoogleAuthProvider.PROVIDER_ID -> {
                    this.user.value = ProfileInfo.GoogleUser(
                        id = user.uid,
                        profilePic = user.photoUrl?.toString() ?: "",
                        email = user.email ?: "",
                        name = user.displayName ?: ""
                    )
                }
                FacebookAuthProvider.PROVIDER_ID -> {
                    this.user.value = ProfileInfo.FacebookUser(
                        id = user.uid,
                        profilePic = user.photoUrl?.toString() ?: "",
                        email = user.email ?: "",
                        name = user.displayName ?: ""
                    )
                }
                else -> {
                    firestore.collection("users").document(user.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val name = document.getString("name") ?: ""
                                this.user.value = ProfileInfo.ManualUser(
                                    id = user.uid,
                                    name = name,
                                    email = user.email ?: "",
                                    profilePic = ""
                                )
                            }
                        }
                }
            }
        }
    }

    private fun saveUserToFirestore(profileInfo: ProfileInfo) {
        val userId = profileInfo.id
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .set(profileInfo)
                .addOnSuccessListener {
                    _authState.value = AuthState.Authenticated
                    user.value = profileInfo
                }
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Error("Failed to save user: ${e.message}")
                }
        } else {
            _authState.value = AuthState.Error("Invalid user ID")
        }
    }

    fun updateUserField(
        field: String,
        value: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = user.value?.id ?: return
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.update(field, value)
            .addOnSuccessListener {
                val updatedUser = when (val currentUser = user.value) {
                    is ProfileInfo.ManualUser -> currentUser.copy(
                        name = if (field == "name") value else currentUser.name,
                        email = if (field == "email") value else currentUser.email
                    )
                    is ProfileInfo.GoogleUser -> currentUser.copy(
                        name = if (field == "name") value else currentUser.name,
                        email = if (field == "email") value else currentUser.email
                    )
                    is ProfileInfo.FacebookUser -> currentUser.copy(
                        name = if (field == "name") value else currentUser.name,
                        email = if (field == "email") value else currentUser.email
                    )
                    null -> null
                    else -> throw IllegalStateException("Unknown user type: $currentUser")
                }
                user.value = updatedUser
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // SIGN OUT
    fun signout(context: Context, navController: NavHostController) {
        LoginManager.getInstance().logOut() // Clear Facebook login state
        auth.signOut() // Sign out from Firebase
        user.value = null
        _authState.value = AuthState.Unauthenticated
        Toast.makeText(context, "Logout successful", Toast.LENGTH_LONG).show()
        navController.navigate(Dest.LoginScreen) {
            popUpTo(0) { inclusive = true }
        }
    }
}

// Auth State sealed class
sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}