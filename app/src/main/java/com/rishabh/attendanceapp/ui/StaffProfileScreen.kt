package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.attendanceapp.data.Staff

@Composable
fun StaffProfileScreen(
    staff: Staff,
    onEnrollFace: () -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Staff Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = staff.name,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Employee ID: ${staff.employeeId}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Face Enrollment",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (staff.faceEmbedding == null)
                "Status: Not enrolled"
            else
                "Status: Enrolled"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEnrollFace,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (staff.faceEmbedding == null)
                    "ENROL FACE"
                else
                    "RE-ENROL FACE"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BACK")
        }
    }
}