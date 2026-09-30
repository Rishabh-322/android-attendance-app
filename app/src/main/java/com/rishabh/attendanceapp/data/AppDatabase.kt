package com.rishabh.attendanceapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Staff::class,
        Attendance::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun staffDao(): StaffDao

    abstract fun attendanceDao(): AttendanceDao
}