package com.thekirankumarv.newsync.authentication.presentation.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekirankumarv.newsync.authentication.presentation.intent.WelcomeIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {

    // StateFlow to hold and expose the current UI state
    private val _state = MutableStateFlow<WelcomeState>(WelcomeState.Loading)
    val state: StateFlow<WelcomeState> get() = _state

    init {
        // Process the initial intent to load the welcome text
        processIntent(WelcomeIntent.LoadWelcomeText)
    }

    // Function to handle intents
    fun processIntent(intent: WelcomeIntent) {
        viewModelScope.launch {
            when (intent) {
                is WelcomeIntent.LoadWelcomeText -> loadWelcomeText()
                is WelcomeIntent.LoginClicked -> handleLoginClick()
                is WelcomeIntent.RegisterClicked -> handleRegisterClick()
            }
        }
    }

    // Private function to load welcome text
    private fun loadWelcomeText() {
        _state.value = WelcomeState.Success(
            "Welcome to Newsync",
            isLoginClicked = false,
            isRegisterClicked = false
        )
    }

    // Private function to handle login click
    private fun handleLoginClick() {
        val currentState = _state.value
        if (currentState is WelcomeState.Success) {
            _state.value = currentState.copy(isLoginClicked = true)
        }
    }

    // Private function to handle register click
    private fun handleRegisterClick() {
        val currentState = _state.value
        if (currentState is WelcomeState.Success) {
            _state.value = currentState.copy(isRegisterClicked = true)
        }
    }
}
