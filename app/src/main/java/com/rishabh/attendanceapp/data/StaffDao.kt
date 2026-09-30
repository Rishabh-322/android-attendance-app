package com.rishabh.attendanceapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStaff(staff: Staff)

    @Update
    suspend fun updateStaff(staff: Staff)

    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id LIMIT 1")
    fun getStaffById(id: Int): Flow<Staff?>

    @Query("SELECT * FROM staff WHERE employeeId = :employeeId LIMIT 1")
    suspend fun getStaffByEmployeeId(employeeId: String): Staff?
}