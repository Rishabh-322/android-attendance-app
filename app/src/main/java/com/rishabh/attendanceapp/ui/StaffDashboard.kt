package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.attendanceapp.data.Staff

@Composable
fun StaffDashboard(
    staff: Staff?,
    employeeId: String,
    onMarkAttendance: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "Staff Dashboard",
                style =
                    MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = onLogout
            ) {

                Text("Logout")
            }
        }


        Spacer(
            modifier = Modifier.height(32.dp)
        )


        if (staff == null) {

            Text(
                text = "Employee not found.",
                color =
                    MaterialTheme.colorScheme.error
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Employee ID: $employeeId"
            )

            return@Column
        }


        Text(
            text = "Welcome, ${staff.name}",
            style =
                MaterialTheme.typography.titleLarge
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text =
                "Employee ID: ${staff.employeeId}",
            style =
                MaterialTheme.typography.bodyLarge
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        Text(
            text =
                if (staff.faceEmbedding != null)
                    "Face enrollment: Ready"
                else
                    "Face enrollment: Not completed",
            style =
                MaterialTheme.typography.bodyMedium
        )


        Spacer(
            modifier = Modifier.height(40.dp)
        )


        Button(

            onClick =
                onMarkAttendance,

            enabled =
                staff.faceEmbedding != null,

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)

        ) {

            Text("MARK ATTENDANCE")
        }


        if (staff.faceEmbedding == null) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "Ask an administrator to enrol your face before marking attendance.",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }


        Spacer(
            modifier = Modifier.height(32.dp)
        )


        Text(
            text = "Today's Status",
            style =
                MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = "Attendance not marked"
        )
    }
}