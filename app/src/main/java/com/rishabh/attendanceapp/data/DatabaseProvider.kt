package com.rishabh.attendanceapp.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null


    private val MIGRATION_1_2 =
        object : Migration(1, 2) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `attendance` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `staffId` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `selfiePath` TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_attendance_staffId`
                    ON `attendance` (`staffId`)
                    """.trimIndent()
                )
            }
        }


    fun getDatabase(
        context: Context
    ): AppDatabase {

        return INSTANCE
            ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "attendance_database"
                    )
                        .addMigrations(
                            MIGRATION_1_2
                        )
                        .build()

                INSTANCE = instance

                instance
            }
    }
}