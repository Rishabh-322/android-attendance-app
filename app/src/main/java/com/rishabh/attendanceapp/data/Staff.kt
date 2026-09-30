package com.rishabh.attendanceapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "staff",
    indices = [
        Index(value = ["employeeId"], unique = true)
    ]
)
data class Staff(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val employeeId: String,

    val faceEmbedding: String? = null
)