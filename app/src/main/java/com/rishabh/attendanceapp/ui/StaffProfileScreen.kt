package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.attendanceapp.data.Attendance
import com.rishabh.attendanceapp.data.Staff
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffProfileScreen(
    staff: Staff,
    attendanceList: List<Attendance>,
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = staff.name,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Employee ID: ${staff.employeeId}"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Face Enrollment",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text =
                if (staff.faceEmbedding == null)
                    "Status: Not enrolled"
                else
                    "Status: Enrolled"
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Attendance History",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (attendanceList.isEmpty()) {

            Text("No attendance records yet.")

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(attendanceList) { attendance ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {

                            Text(
                                text =
                                    formatDate(attendance.timestamp),
                                style =
                                    MaterialTheme.typography.titleSmall
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "Location: %.5f, %.5f".format(
                                        attendance.latitude,
                                        attendance.longitude
                                    ),
                                style =
                                    MaterialTheme.typography.bodySmall
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "Selfie recorded",
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
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


private fun formatDate(
    timestamp: Long
): String {

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )
}