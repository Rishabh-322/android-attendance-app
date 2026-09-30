package com.rishabh.attendanceapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["staffId"])
    ]
)
data class Attendance(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val staffId: Int,

    val timestamp: Long,

    val latitude: Double,

    val longitude: Double,

    val selfiePath: String
)