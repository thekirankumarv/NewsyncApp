package com.thekirankumarv.newsync.chat.presentation

import com.google.firebase.Timestamp

data class ChatState(
    val imageUrl: String ?= null,
    val isImageLoading: Set<String> = emptySet(),
    val uploadingMessageId: String? = null,
    val users: List<ChatUser> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val chatUser: ChatUser? = null,
    val error: String? = null
)

data class ChatUser(
    val userId: String = "",
    val email: String = "",
    val name: String? = null,
    val lastMessage: String? = null
) {
    val displayName: String
        get() = name ?: "Unknown"

    // Explicitly define equality based on userId
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChatUser) return false
        return userId == other.userId
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val timestamp: Timestamp? = null,
    val imageKey: String? = null
)

/*
Firebase Firestore

collection - chats
document Id - auto Id
field - participants =  type -> array
0 -> type string, value userId1
1 -> type string, value userId2

sub collection - messages
document Id - auto Id
field - isRead -> type Boolean
message -> string
receiverId ->string
senderId -> string

*/
