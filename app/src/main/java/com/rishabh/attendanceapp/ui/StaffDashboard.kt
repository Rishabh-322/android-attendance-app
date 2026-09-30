package com.rishabh.attendanceapp.ui

import kotlinx.coroutines.flow.flowOf
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.attendanceapp.data.Attendance
import com.rishabh.attendanceapp.data.Staff
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StaffDashboard(
    staff: Staff?,
    employeeId: String,
    latestAttendance: Attendance?,
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


        val attendanceMarkedToday =
            latestAttendance?.let {

                isSameDay(
                    it.timestamp,
                    System.currentTimeMillis()
                )

            } ?: false


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
                    "Face enrollment: Not completed"
        )


        Spacer(
            modifier = Modifier.height(40.dp)
        )


        Button(

            onClick = onMarkAttendance,

            enabled =
                staff.faceEmbedding != null &&
                        !attendanceMarkedToday,

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)

        ) {

            Text(
                if (attendanceMarkedToday)
                    "ATTENDANCE MARKED"
                else
                    "MARK ATTENDANCE"
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
            modifier = Modifier.height(12.dp)
        )


        if (
            attendanceMarkedToday &&
            latestAttendance != null
        ) {

            Text(
                text = "✓ Attendance marked",
                style =
                    MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "Time: ${formatAttendanceTime(latestAttendance.timestamp)}"
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "Location: %.5f, %.5f".format(
                        latestAttendance.latitude,
                        latestAttendance.longitude
                    )
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Selfie stored securely",
                style =
                    MaterialTheme.typography.bodySmall
            )

        } else {

            Text(
                text = "Attendance not marked"
            )
        }
    }
}


private fun formatAttendanceTime(
    timestamp: Long
): String {

    val formatter =
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

    return formatter.format(
        Date(timestamp)
    )
}


private fun isSameDay(
    firstTimestamp: Long,
    secondTimestamp: Long
): Boolean {

    val first =
        Calendar.getInstance().apply {
            timeInMillis = firstTimestamp
        }

    val second =
        Calendar.getInstance().apply {
            timeInMillis = secondTimestamp
        }

    return first.get(Calendar.YEAR) ==
            second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) ==
            second.get(Calendar.DAY_OF_YEAR)
}