package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddStaffScreen(
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Add Staff Member",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            label = { Text("Staff Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = employeeId,
            onValueChange = {
                employeeId = it.uppercase()
                errorMessage = ""
            },
            label = { Text("Employee ID") },
            placeholder = { Text("Example: EMP001") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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

                    name.isBlank() -> {
                        errorMessage = "Please enter the staff member's name"
                    }

                    employeeId.isBlank() -> {
                        errorMessage = "Please enter an employee ID"
                    }

                    else -> {
                        onSave(
                            name.trim(),
                            employeeId.trim().uppercase()
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE STAFF")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CANCEL")
        }
    }
}