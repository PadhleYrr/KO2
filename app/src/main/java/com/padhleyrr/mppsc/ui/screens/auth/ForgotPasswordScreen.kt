package com.padhleyrr.mppsc.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.padhleyrr.mppsc.ui.theme.DMSans
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors

@Composable
fun ForgotPasswordScreen(
    onResetClick: (email: String) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    
    val c = gkkColors
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = c.text
                    )
                }
            }
            
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge,
                    color = c.text,
                    fontFamily = Syne
                )
                Text(
                    text = "Enter your email to receive a password reset link",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.muted,
                    textAlign = TextAlign.Center
                )
            }
            
            // Success Message
            if (!successMessage.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = successMessage,
                        color = c.success,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Error Message
            if (!errorMessage.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = c.danger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = DMSans) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        if (email.isNotEmpty() && !isLoading) {
                            onResetClick(email)
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.navy,
                    unfocusedBorderColor = c.border,
                    focusedLabelColor = c.navy
                )
            )
            
            // Info text
            Text(
                text = "We'll send you an email with instructions to reset your password.",
                style = MaterialTheme.typography.bodySmall,
                color = c.muted,
                textAlign = TextAlign.Center
            )
            
            // Send Reset Email Button
            Button(
                onClick = { onResetClick(email) },
                enabled = !isLoading && email.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.navy,
                    contentColor = Color.White,
                    disabledContainerColor = c.border
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Send Reset Link",
                        fontFamily = DMSans,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
