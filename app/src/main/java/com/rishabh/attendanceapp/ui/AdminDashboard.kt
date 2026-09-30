package com.rishabh.attendanceapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rishabh.attendanceapp.data.Staff

@Composable
fun AdminDashboard(
    staffList: List<Staff>,
    onAddStaff: () -> Unit,
    onStaffClick: (Staff) -> Unit,
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
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Staff Members",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (staffList.isEmpty()) {

            Text("No staff members added yet.")

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(staffList) { staff ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                onStaffClick(staff)
                            }
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = staff.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Employee ID: ${staff.employeeId}"
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (staff.faceEmbedding == null)
                                    "Face: Not enrolled"
                                else
                                    "Face: Enrolled"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddStaff,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ ADD STAFF")
        }
    }
}