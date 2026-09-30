package com.rishabh.attendanceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rishabh.attendanceapp.navigation.AppNavigation
import com.rishabh.attendanceapp.ui.theme.AttendanceAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AttendanceAppTheme {
                AppNavigation()
            }
        }
    }
}