package com.rishabh.attendanceapp.data

import android.content.Context
import java.io.File

object AttendanceStorageHelper {

    fun saveSelfie(
        context: Context,
        sourceFile: File,
        employeeId: String,
        timestamp: Long
    ): File {

        val selfieDirectory =
            File(
                context.filesDir,
                "attendance_selfies"
            ).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

        val destinationFile =
            File(
                selfieDirectory,
                "${employeeId}_${timestamp}.jpg"
            )

        sourceFile.copyTo(
            destinationFile,
            overwrite = true
        )

        sourceFile.delete()

        return destinationFile
    }
}