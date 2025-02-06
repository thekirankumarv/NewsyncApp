package com.thekirankumarv.newsync.authentication.presentation.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import android.net.Uri
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.authentication.presentation.state.AuthViewModel

@Composable
fun SocialLoginSection(
    viewModel: AuthViewModel,
    navController: NavController,
    context: Context,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val url = "https://account.apple.com/sign-in"

        Text(
            text = "Or continue with",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Google button
            IconButton(
                onClick = { viewModel.handleGoogleSignIn(context, navController) },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icongoogle),
                    contentDescription = "Google Login",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Facebook button
            IconButton(
                onClick = { viewModel.handleFacebookSignIn(context, navController) },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconfacebook),
                    contentDescription = "Facebook Login",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Apple button
            IconButton(
                onClick = {
                    // Open the URL in the default browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconapple),
                    contentDescription = "Apple Login",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

