package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StaffDashboard(
    employeeId: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Staff Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Employee ID: $employeeId",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                // Camera will be connected later.
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("MARK ATTENDANCE")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Today's Status",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Attendance not marked")
    }
}