package com.thekirankumarv.newsync.chat.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thekirankumarv.newsync.chat.presentation.utils.formatTime
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    userId: String,
    onNavigateBack: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    // Extract values from uiState
    val chatMessages = uiState.chatMessages
    val chatUser = uiState.chatUser
    val isImageLoading = uiState.isImageLoading
    val uploadingMessageId = uiState.uploadingMessageId

    LaunchedEffect(Unit) { viewModel.initializeChat(userId) }
    LaunchedEffect(chatMessages) {
        if (chatMessages.isNotEmpty()) scrollState.animateScrollToItem(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(chatUser?.name ?: "Chat") }, // Use chatUser from uiState
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton({ launcher.launch("image/*") }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Image",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message") },
                        maxLines = 2,
                        trailingIcon = {
                            IconButton(onClick = {
                                if (message.text.isNotBlank()) {
                                    viewModel.sendMessage(message.text)
                                    message = TextFieldValue("")
                                }
                            }) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f),
                state = scrollState,
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    MessageItem(
                        message = message,
                        isCurrentUser = viewModel.isCurrentUser(message.senderId),
                        isUploading = message.messageId == uploadingMessageId,
                        isImageLoading = isImageLoading.contains(message.imageKey)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    isUploading: Boolean,
    isImageLoading: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    var imageAspectRatio by remember { mutableStateOf(1f) }
    val maxChar = 100

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Show image from Firestore URL or uploading state
                    if (message.imageKey != null || isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (isUploading || isImageLoading) {
                                // Loading state with fixed height
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                                        val alpha by infiniteTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 0.7f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1000),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "alpha"
                                        )

                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = if (isCurrentUser)
                                                MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary,
                                            strokeWidth = 3.dp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isUploading) "Uploading image..." else "Loading image...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isCurrentUser)
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = alpha)
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                                        )
                                    }
                                }
                            } else if (message.imageUrl != null) {
                                AsyncImage(
                                    model = message.imageUrl,
                                    contentDescription = "Chat Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 100.dp)
                                        .heightIn(max = 300.dp), 
                                    contentScale = ContentScale.FillWidth,
                                    onState = { state ->
                                        when (state) {
                                            is AsyncImagePainter.State.Success -> {
                                                // Calculate and store aspect ratio from the loaded image
                                                val painter = state.painter
                                                val intrinsicSize = painter.intrinsicSize
                                                if (intrinsicSize.width > 0 && intrinsicSize.height > 0) {
                                                    imageAspectRatio = intrinsicSize.width / intrinsicSize.height
                                                }
                                            }
                                            is AsyncImagePainter.State.Error -> {
                                                println("Error loading image: ${state.result.throwable.message}")
                                            }
                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Message text
                    if (message.message.isNotBlank()) {
                        val displayText = buildAnnotatedString {
                            append(if (isExpanded || message.message.length <= maxChar) {
                                message.message
                            } else {
                                message.message.take(maxChar) + "..."
                            })

                            if (message.message.length > maxChar) {
                                val readMoreText = if (isExpanded) " Read Less" else " Read More"
                                val startIndex = length

                                pushStyle(
                                    SpanStyle(
                                        color = Color.Cyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                append(readMoreText)
                                addStringAnnotation(
                                    tag = "expand",
                                    annotation = readMoreText,
                                    start = startIndex,
                                    end = startIndex + readMoreText.length
                                )
                                pop()
                            }
                        }

                        ClickableText(
                            text = displayText,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = if (isCurrentUser)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            onClick = { offset ->
                                if (message.message.length > maxChar) {
                                    isExpanded = !isExpanded
                                }
                            }
                        )
                    }

                    // Timestamp and read status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.timestamp?.toDate()?.formatTime() ?: "",
                            fontSize = 12.sp,
                            color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.isRead) Icons.Default.Done else Icons.Default.Close,
                                contentDescription = if (message.isRead) "Read" else "Sent",
                                modifier = Modifier.size(16.dp),
                                tint = if (message.isRead) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
