package com.thekirankumarv.newsync.chat.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.util.Base64
import android.util.Log
import com.google.firebase.Timestamp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context

) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatState(
            imageUrl = "",
            isImageLoading = emptySet(),
            uploadingMessageId = null,
            users = emptyList(),
            messages = emptyList(),
            chatMessages = emptyList(),
            chatUser = null,
            error = null
        )
    )
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()

    init {
        listenToUserChats()
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                // Generate a temporary message ID for tracking the upload
                val tempMessageId = "uploading_${System.currentTimeMillis()}"
                _uiState.update { it.copy(uploadingMessageId = tempMessageId) }

                // Add a temporary message to the chat
                val tempMessage = ChatMessage(
                    messageId = tempMessageId,
                    senderId = auth.currentUser?.uid ?: return@launch,
                    receiverId = uiState.value.chatUser?.userId ?: return@launch,
                    message = "",
                    imageKey = null,
                    isRead = false,
                    timestamp = Timestamp.now()
                )

                // Add temporary message to the list
                _uiState.update { it.copy(chatMessages =  listOf(tempMessage) + it.messages) }

                val imageBytes = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes()
                } ?: throw Exception("Could not read image")

                // Rest of your upload logic...
                val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                val fileName = "chat_${System.currentTimeMillis()}.png"

                val response = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val json = JsonObject().apply {
                        addProperty("fileContent", base64Image)
                        addProperty("key", fileName)
                        addProperty("uploadedBy", auth.currentUser?.email)
                    }

                    val request = Request.Builder()
                        .url("S3_UPLOAD_URL")
                        .post(json.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val key = responseBody?.let { extractKeyFromResponse(it) }
                        ?: throw Exception("Failed to extract key from response")

                    // Remove temporary message and send actual message
                    _uiState.update {
                        it.copy(chatMessages = it.chatMessages.filter { msg -> msg.messageId != tempMessageId })
                    }
                    sendMessageWithImage(key)
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Failed to upload image: ${response.message}",
                            chatMessages = it.chatMessages.filter { msg -> msg.messageId != tempMessageId }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error uploading image: ${e.message}")
                }
            } finally {
                _uiState.update { it.copy(uploadingMessageId = null) }
            }
        }
    }


    private fun extractKeyFromResponse(responseBody: String): String? {
        return try {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            jsonObject.get("data")?.asString
        } catch (e: Exception) {
            null
        }
    }

    private fun sendMessageWithImage(key: String) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val currentChat = currentChatId ?: return@launch
                val receiver = uiState.value.chatUser?.userId ?: return@launch

                val messageRef = firestore.collection("chats")
                    .document(currentChat)
                    .collection("messages")
                    .document()

                val messageId = messageRef.id  // Generate a new message ID

                val message = hashMapOf(
                    "senderId" to currentUserId,
                    "receiverId" to receiver,
                    "message" to "",
                    "imageKey" to key,
                    "imageUrl" to null,
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                messageRef.set(message).addOnSuccessListener {
                    getImageUrl(key, messageId)  // Fetch signed URL and update Firestore
                }.addOnFailureListener { e ->
                    _uiState.update { it.copy(error = "Failed to send image: ${e.message}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error sending image: ${e.message}") }
            }
        }
    }

    fun getImageUrl(key: String, messageId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isImageLoading = it.isImageLoading + key) }

                val signedUrl = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("S3_SIGNED_URL=$key")
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        println("Response from S3 API: $responseBody")
                        val jsonObject = JsonParser.parseString(responseBody).asJsonObject
                        jsonObject.get("data")?.asString
                    } else {
                        println("Failed to fetch signed URL: ${response.message}")
                        null
                    }
                }
                signedUrl?.let { url ->
                    // Update Firestore with the image URL
                    firestore.collection("chats")
                        .document(currentChatId ?: return@launch)
                        .collection("messages")
                        .document(messageId)
                        .update("imageUrl", url)
                        .addOnSuccessListener {
                            println("Image URL successfully updated in Firestore")
                        }
                        .addOnFailureListener { e ->
                            println("Failed to update image URL: ${e.message}")
                        }
                }

                _uiState.update { it.copy(imageUrl = signedUrl) }
            } catch (e: Exception) {
                println("Error generating signed URL: ${e.message}")
            } finally {
                _uiState.update { it.copy(isImageLoading = it.isImageLoading - key) }
            }
        }
    }


    private fun listenToUserChats() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                viewModelScope.launch {
                    val usersList = mutableListOf<ChatUser>()

                    snapshot.documents.forEach { chatDoc ->
                        val participants = chatDoc.get("participants") as? List<String> ?: return@forEach
                        val otherUserId = participants.find { it != currentUserId } ?: return@forEach

                        try {
                            val userDoc = firestore.collection("users")
                                .document(otherUserId)
                                .get()
                                .await()

                            if (userDoc.exists()) {
                                val chatUser = ChatUser(
                                    userId = otherUserId,
                                    email = userDoc.getString("email") ?: "",
                                    name = userDoc.getString("name"),
                                    lastMessage = null
                                )
                                usersList.add(chatUser)
                            }
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                    _uiState.update { it.copy(users = usersList) }
                }
            }
    }

    fun searchUserByEmail(email: String, callback: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            callback(false, null)
            return
        }

        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch

            try {
                // First check if the user is already in chat list
                val existingChats = firestore.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()

                // Search for the user by email
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (userQuery.isEmpty) {
                    callback(false, null)
                    return@launch
                }

                val userDoc = userQuery.documents.first()
                val foundUserId = userDoc.id

                // Don't allow adding yourself
                if (foundUserId == currentUserId) {
                    callback(false, null)
                    return@launch
                }

                // Check if chat already exists with this user
                val chatExists = existingChats.documents.any { chatDoc ->
                    val participants = chatDoc.get("participants") as? List<String>
                    participants?.containsAll(listOf(currentUserId, foundUserId)) == true
                }

                if (chatExists) {
                    // User already exists in chat list
                    callback(false, "already_exists")
                } else {
                    callback(true, foundUserId)
                }
            } catch (e: Exception) {
                callback(false, null)
            }
        }
    }

    fun addUserToChat(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("chats")
            .add(hashMapOf(
                "participants" to listOf(currentUserId, otherUserId),
                "createdAt" to FieldValue.serverTimestamp()
            ))
    }

    fun deleteChat(user: ChatUser) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // First get all chats where current user is a participant
                val querySnapshot = firestore.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()

                for (chatDoc in querySnapshot.documents) {
                    val participants = chatDoc.get("participants") as? List<String>
                    // Check if this is the chat we want to delete
                    if (participants?.containsAll(listOf(currentUserId, user.userId)) == true) {
                        // Delete the chat document
                        chatDoc.reference.delete()
                            .addOnSuccessListener {
                                // Chat deleted successfully
                                // The UI will update automatically due to the listener in listenToUserChats
                            }
                            .addOnFailureListener { e ->
                                // Handle deletion error
                            }
                        break // Exit loop after finding and deleting the chat
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private var currentChatId: String? = null

    fun initializeChat(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Get user details
            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    _uiState.update {
                        it.copy(chatUser = ChatUser(
                            userId = userId,
                            email = userDoc.getString("email") ?: "",
                            name = userDoc.getString("name")))
                    }
                }

                // Find or create chat document
                val chatQuery = firestore.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()

                val chatDoc = chatQuery.documents.find { doc ->
                    val participants = doc.get("participants") as? List<String>
                    participants?.containsAll(listOf(currentUserId, userId)) == true
                }

                currentChatId = chatDoc?.id

                if (currentChatId != null) {
                    // Listen to messages
                    listenToMessages(currentChatId!!)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun listenToMessages(chatId: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(error = "Error fetching messages: ${error.message}") }
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val currentUserId = auth.currentUser?.uid
                    _uiState.update { state ->
                        state.copy(
                            chatMessages = querySnapshot.documents.mapNotNull { doc ->
                                val message = ChatMessage(
                                    messageId = doc.id,
                                    senderId = doc.getString("senderId") ?: "",
                                    receiverId = doc.getString("receiverId") ?: "",
                                    message = doc.getString("message") ?: "",
                                    imageKey = doc.getString("imageKey"),
                                    imageUrl = doc.getString("imageUrl"),
                                    isRead = doc.getBoolean("isRead") ?: false,
                                    timestamp = doc.getTimestamp("timestamp")
                                )
                                Log.d("ChatViewModel", "Message: ${message.messageId}, ImageUrl: ${message.imageUrl}")

                                // Mark message as read if it's received
                                if (message.receiverId == currentUserId && !message.isRead) {
                                    markMessageAsRead(message.messageId)
                                }

                                message
                            }
                        )
                    }
                }
            }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: run {
                    _uiState.update { it.copy(error = "Not authenticated") }
                    return@launch
                }
                val currentChat = currentChatId ?: run {
                    _uiState.update { it.copy(error = "Chat not initialized") }
                    return@launch
                }
                val receiver = uiState.value.chatUser?.userId ?: run {
                    _uiState.update { it.copy(error = "Receiver not found") }
                    return@launch
                }

                val message = hashMapOf(
                    "senderId" to currentUserId,
                    "receiverId" to receiver,
                    "message" to messageText,
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("chats")
                    .document(currentChat)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        _uiState.update { it.copy(error = null) }
                    }
                    .addOnFailureListener { e ->
                        _uiState.update { it.copy(error = "Failed to send message: ${e.message}") }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error: ${e.message}") }
            }
        }
    }

    fun isCurrentUser(userId: String): Boolean {
        return userId == auth.currentUser?.uid
    }


    private fun markMessageAsRead(messageId: String) {
        val currentChat = currentChatId ?: return

        firestore.collection("chats")
            .document(currentChat)
            .collection("messages")
            .document(messageId)
            .update("isRead", true)
    }

}