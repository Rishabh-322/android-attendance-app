package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onAdminLogin: () -> Unit,
    onStaffLogin: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Attendance App",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = ""
            },
            label = { Text("Username / Employee ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    username == "admin" && password == "admin123" -> {
                        onAdminLogin()
                    }

                    username.startsWith("EMP", ignoreCase = true) &&
                            password == "staff123" -> {
                        onStaffLogin(username.uppercase())
                    }

                    else -> {
                        errorMessage = "Invalid username or password"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Demo Admin: admin / admin123",
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = "Demo Staff: EMP001 / staff123",
            style = MaterialTheme.typography.bodySmall
        )
    }
}