package com.thekirankumarv.newsync.authentication.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TermsAndConditionsDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    var expandedSection by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Terms and Conditions",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Scrollable content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Initial content
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Welcome to Newsync!")
                                }
                                append(" These Terms and Conditions govern your use of our news update and chat application.\n\n")

                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("1. Service Description\n")
                                }
                                append("Newsync is a mobile application that provides news updates and chat functionality to its users.\n\n")

                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("2. Data Collection\n")
                                }
                                append("We collect the following information:\n")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("• Full Name\n")
                                    append("• Email Address\n")
                                    append("• Phone Number\n\n")
                                }
                                append("This information is used to create and manage your account, provide personalized news content, and enable chat functionality.")
                            }
                        )


                        // Expandable content
                        if (expandedSection) {
                            Text(
                                buildAnnotatedString {
                                    append("\n\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("3. User Responsibilities\n")
                                    }
                                    append("• You must provide accurate information during registration\n")
                                    append("• You are responsible for maintaining the confidentiality of your account\n")
                                    append("• You agree not to use the service for any illegal purposes\n\n")

                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("4. Privacy Policy\n")
                                    }
                                    append("• We protect your personal information using ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("industry-standard security measures\n")
                                    }
                                    append("• Your data will not be shared with third parties without your consent\n")
                                    append("• You can request deletion of your account and associated data\n\n")

                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("5. Content Guidelines\n")
                                    }
                                    append("• Users must not post inappropriate, offensive, or illegal content\n")
                                    append("• We reserve the right to moderate chat content\n")
                                    append("• News content is curated from reliable sources\n\n")

                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("6. Service Availability\n")
                                    }
                                    append("• We strive to maintain ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("24/7 service availability\n")
                                    }
                                    append("• We may perform maintenance with prior notice\n")
                                    append("• Service interruptions may occur due to technical issues\n\n")

                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("7. Termination\n")
                                    }
                                    append("• We reserve the right to terminate accounts that violate our terms\n")
                                    append("• Users can terminate their account at any time\n\n")

                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("8. Changes to Terms\n")
                                    }
                                    append("• We may update these terms with notice to users\n")
                                    append("• Continued use of the service implies acceptance of new terms")
                                }
                            )

                        }
                    }
                }

                // Fixed bottom section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Read More button
                    TextButton(
                        onClick = { expandedSection = !expandedSection },
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (expandedSection) "Show Less" else "Read More",
                            textAlign = TextAlign.Center
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("Decline")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAccept
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TermsAndConditionsRow(
    termsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onShowTerms: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = termsAccepted,
            onCheckedChange = onTermsAcceptedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )

        Text(
            text = "I agree to the ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Terms and Conditions",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .clickable(onClick = onShowTerms)
                .padding(start = 4.dp)
        )
    }
}