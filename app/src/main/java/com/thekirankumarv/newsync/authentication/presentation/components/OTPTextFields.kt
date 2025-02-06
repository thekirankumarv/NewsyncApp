package com.thekirankumarv.newsync.authentication.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun OTPTextFields(
    modifier: Modifier = Modifier,
    length: Int,
    onFilled: (code: String) -> Unit
) {
    var otpValues by remember { mutableStateOf(List<String>(length) { "" }) }
    val focusRequesters = remember { List(length) { FocusRequester() } }

    Row(
        modifier = modifier.height(50.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        otpValues.forEachIndexed { index, value ->
            val textFieldValue by remember(value) {
                mutableStateOf(
                    TextFieldValue(
                        text = value,
                        selection = TextRange(index = value.length)
                    )
                )
            }

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 1) {
                        val newOtpValues = otpValues.toMutableList()
                        newOtpValues[index] = newValue.text
                        otpValues = newOtpValues

                        if (newValue.text.isNotEmpty() && index < length - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }

                        // Check if OTP is complete
                        if (newOtpValues.none { it.isEmpty() }) {
                            onFilled(newOtpValues.joinToString(""))
                        }
                    }
                },
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL && value.isEmpty() && index > 0) {
                            // Clear previous field and move focus back
                            val newOtpValues = otpValues.toMutableList()
                            newOtpValues[index - 1] = ""
                            otpValues = newOtpValues
                            focusRequesters[index - 1].requestFocus()
                            true
                        } else {
                            false
                        }
                    },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == length - 1) ImeAction.Done else ImeAction.Next
                )
            )

            if (index < length - 1) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }
}