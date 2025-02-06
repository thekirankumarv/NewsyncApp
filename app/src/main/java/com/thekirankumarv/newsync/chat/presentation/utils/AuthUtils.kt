package com.thekirankumarv.newsync.chat.presentation.utils

import com.google.firebase.auth.FirebaseAuth

object AuthUtils {
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}