package com.thekirankumarv.newsync.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.chat.presentation.utils.LocaleManager
import com.thekirankumarv.newsync.chat.presentation.utils.PreferenceManager

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(PreferenceManager.getLanguage(context)) }
    var expanded by remember { mutableStateOf(false) }
    val languages = mapOf(
        "en" to stringResource(id = R.string.language_english),
        "hi" to stringResource(id = R.string.language_hindi)
    )

    // Update locale when language changes
    LaunchedEffect(currentLanguage) {
        LocaleManager.setLocale(context, currentLanguage)
    }

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with title and dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.about_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box {
                    // Small dropdown menu
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .width(100.dp)
                            .height(36.dp)
                            .clickable { expanded = !expanded }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = languages[currentLanguage] ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    ) {
                        languages.forEach { (languageCode, languageName) ->
                            DropdownMenuItem(
                                onClick = {
                                    currentLanguage = languageCode
                                    PreferenceManager.saveLanguage(context, languageCode)
                                    LocaleManager.setLocale(context, languageCode)
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        text = languageName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.feature_realtime_updates_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.feature_realtime_updates_description),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = stringResource(id = R.string.feature_multilingual_support_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.feature_multilingual_support_description),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = stringResource(id = R.string.feature_chat_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.feature_chat_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.mission_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.mission_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            // Version info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = stringResource(id = R.string.version_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }


}

@Preview
@Composable
private fun AboutScreenPreview() {
    AboutScreen()
}

