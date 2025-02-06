package com.thekirankumarv.newsync.authentication.presentation.intent

sealed class WelcomeIntent {
    // Intent to load welcome text
    data object LoadWelcomeText : WelcomeIntent()

    // Intent triggered when the login button is clicked
    data object LoginClicked : WelcomeIntent()

    // Intent triggered when the register button is clicked
    data object RegisterClicked : WelcomeIntent()
}