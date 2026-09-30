package com.rishabh.attendanceapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insertAttendance(
        attendance: Attendance
    )

    @Query(
        """
        SELECT * FROM attendance
        WHERE staffId = :staffId
        ORDER BY timestamp DESC
        LIMIT 1
        """
    )
    fun getLatestAttendanceForStaff(
        staffId: Int
    ): Flow<Attendance?>

    @Query(
        """
        SELECT * FROM attendance
        WHERE staffId = :staffId
        ORDER BY timestamp DESC
        """
    )
    fun getAttendanceForStaff(
        staffId: Int
    ): Flow<List<Attendance>>
}