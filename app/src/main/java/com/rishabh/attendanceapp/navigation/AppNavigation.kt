package com.rishabh.attendanceapp.navigation

import kotlinx.coroutines.flow.flowOf
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rishabh.attendanceapp.data.Attendance
import com.rishabh.attendanceapp.data.AttendanceStorageHelper
import com.rishabh.attendanceapp.data.DatabaseProvider
import com.rishabh.attendanceapp.data.Staff
import com.rishabh.attendanceapp.face.EmbeddingResult
import com.rishabh.attendanceapp.face.FaceMatcher
import com.rishabh.attendanceapp.face.FaceRecognitionHelper
import com.rishabh.attendanceapp.location.LocationHelper
import com.rishabh.attendanceapp.location.LocationResult
import com.rishabh.attendanceapp.ui.AddStaffScreen
import com.rishabh.attendanceapp.ui.AdminDashboard
import com.rishabh.attendanceapp.ui.AttendanceCameraScreen
import com.rishabh.attendanceapp.ui.FaceEnrollmentScreen
import com.rishabh.attendanceapp.ui.LoginScreen
import com.rishabh.attendanceapp.ui.StaffDashboard
import com.rishabh.attendanceapp.ui.StaffProfileScreen
import kotlinx.coroutines.launch


@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val context = LocalContext.current

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    val staffDao = database.staffDao()

    val attendanceDao = database.attendanceDao()

    val staffList by staffDao
        .getAllStaff()
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    val coroutineScope = rememberCoroutineScope()


    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // ============================================================
        // LOGIN
        // ============================================================

        composable("login") {

            LoginScreen(

                onAdminLogin = {

                    navController.navigate("admin") {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },

                onStaffLogin = { employeeId ->

                    navController.navigate(
                        "staff/$employeeId"
                    ) {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // ============================================================
        // ADMIN DASHBOARD
        // ============================================================

        composable("admin") {

            AdminDashboard(

                staffList = staffList,

                onAddStaff = {

                    navController.navigate(
                        "addStaff"
                    )
                },

                onStaffClick = { staff ->

                    navController.navigate(
                        "staffProfile/${staff.id}"
                    )
                },

                onLogout = {

                    navController.navigate(
                        "login"
                    ) {

                        popUpTo(0)
                    }
                }
            )
        }


        // ============================================================
        // ADD STAFF
        // ============================================================

        composable("addStaff") {

            AddStaffScreen(

                onSave = { name, employeeId ->

                    coroutineScope.launch {

                        val existingStaff =
                            staffDao.getStaffByEmployeeId(
                                employeeId
                            )

                        if (existingStaff == null) {

                            staffDao.insertStaff(
                                Staff(
                                    name = name,
                                    employeeId = employeeId
                                )
                            )
                        }

                        navController.popBackStack()
                    }
                },

                onBack = {

                    navController.popBackStack()
                }
            )
        }


        // ============================================================
        // STAFF PROFILE
        // ============================================================

        composable(
            route = "staffProfile/{staffId}",

            arguments = listOf(

                navArgument("staffId") {

                    type = NavType.IntType
                }
            )

        ) { backStackEntry ->

            val staffId =
                backStackEntry.arguments
                    ?.getInt("staffId")

            val staff =
                staffList.find {

                    it.id == staffId
                }


            if (staff != null) {

                StaffProfileScreen(

                    staff = staff,

                    onEnrollFace = {

                        navController.navigate(
                            "faceEnrollment/${staff.id}"
                        )
                    },

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }
        }


        // ============================================================
        // FACE ENROLLMENT
        // ============================================================

        composable(
            route = "faceEnrollment/{staffId}",

            arguments = listOf(

                navArgument("staffId") {

                    type = NavType.IntType
                }
            )

        ) { backStackEntry ->

            val staffId =
                backStackEntry.arguments
                    ?.getInt("staffId")

            val staff =
                staffList.find {

                    it.id == staffId
                }


            if (staff != null) {

                FaceEnrollmentScreen(

                    staff = staff,

                    onPhotoCaptured = { photoFile ->

                        FaceRecognitionHelper
                            .generateEmbedding(

                                context = context,

                                photoFile = photoFile

                            ) { result ->

                                when (result) {


                                    is EmbeddingResult.Success -> {

                                        val embeddingString =
                                            result.embedding
                                                .joinToString(",")


                                        coroutineScope.launch {

                                            staffDao.updateStaff(

                                                staff.copy(

                                                    faceEmbedding =
                                                        embeddingString
                                                )
                                            )


                                            Toast.makeText(

                                                context,

                                                "Face enrolled successfully",

                                                Toast.LENGTH_SHORT

                                            ).show()


                                            navController
                                                .popBackStack()
                                        }
                                    }


                                    is EmbeddingResult.Error -> {

                                        Toast.makeText(

                                            context,

                                            "Enrollment failed: ${result.message}",

                                            Toast.LENGTH_LONG

                                        ).show()
                                    }
                                }
                            }
                    },

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }
        }


        // ============================================================
// STAFF DASHBOARD
// ============================================================

        composable(
            route = "staff/{employeeId}",

            arguments = listOf(
                navArgument("employeeId") {
                    type = NavType.StringType
                }
            )

        ) { backStackEntry ->

            val employeeId =
                backStackEntry.arguments
                    ?.getString("employeeId")
                    ?.uppercase()
                    ?: ""

            val staff =
                staffList.find {
                    it.employeeId.equals(
                        employeeId,
                        ignoreCase = true
                    )
                }


            // --------------------------------------------------------
            // GET LATEST ATTENDANCE FOR THIS STAFF MEMBER
            // --------------------------------------------------------

            val latestAttendanceFlow =
                remember(staff?.id) {

                    if (staff != null) {

                        attendanceDao
                            .getLatestAttendanceForStaff(
                                staff.id
                            )

                    } else {

                        flowOf<Attendance?>(null)
                    }
                }


            val latestAttendance by
            latestAttendanceFlow
                .collectAsStateWithLifecycle(
                    initialValue = null
                )


            // --------------------------------------------------------
            // SHOW STAFF DASHBOARD
            // --------------------------------------------------------

            StaffDashboard(

                staff = staff,

                employeeId = employeeId,

                latestAttendance = latestAttendance,

                onMarkAttendance = {

                    if (staff != null) {

                        navController.navigate(
                            "attendanceCamera/${staff.id}"
                        )
                    }
                },

                onLogout = {

                    navController.navigate(
                        "login"
                    ) {

                        popUpTo(0)
                    }
                }
            )
        }

        // ============================================================
        // ATTENDANCE CAMERA / FACE VERIFICATION
        // ============================================================

        composable(
            route = "attendanceCamera/{staffId}",

            arguments = listOf(

                navArgument("staffId") {

                    type = NavType.IntType
                }
            )

        ) { backStackEntry ->


            val staffId =
                backStackEntry.arguments
                    ?.getInt("staffId")


            val staff =
                staffList.find {

                    it.id == staffId
                }


            if (staff != null) {

                AttendanceCameraScreen(

                    staff = staff,

                    onPhotoCaptured = { photoFile ->


                        val enrolledEmbedding =
                            staff.faceEmbedding


                        // ------------------------------------------------
                        // NO FACE ENROLLED
                        // ------------------------------------------------

                        if (enrolledEmbedding == null) {

                            Toast.makeText(

                                context,

                                "No enrolled face found.",

                                Toast.LENGTH_LONG

                            ).show()


                            photoFile.delete()


                        } else {


                            // ------------------------------------------------
                            // GENERATE EMBEDDING FROM CURRENT SELFIE
                            // ------------------------------------------------

                            FaceRecognitionHelper
                                .generateEmbedding(

                                    context = context,

                                    photoFile = photoFile

                                ) { embeddingResult ->


                                    when (embeddingResult) {


                                        // ====================================================
                                        // EMBEDDING SUCCESS
                                        // ====================================================

                                        is EmbeddingResult.Success -> {


                                            val matchResult =
                                                FaceMatcher.compare(

                                                    enrolledEmbeddingString =
                                                        enrolledEmbedding,

                                                    currentEmbedding =
                                                        embeddingResult.embedding
                                                )


                                            val similarityPercent =
                                                (
                                                        matchResult.similarity *
                                                                100
                                                        ).toInt()


                                            // --------------------------------------------
                                            // FACE MATCHED
                                            // --------------------------------------------

                                            if (matchResult.isMatch) {


                                                LocationHelper
                                                    .getCurrentLocation(

                                                        context = context

                                                    ) { locationResult ->


                                                        when (locationResult) {


                                                            // ========================================
                                                            // LOCATION SUCCESS
                                                            // ========================================

                                                            is LocationResult.Success -> {


                                                                val timestamp =
                                                                    System.currentTimeMillis()


                                                                coroutineScope.launch {


                                                                    try {


                                                                        // ------------------------------
                                                                        // SAVE SELFIE PERMANENTLY
                                                                        // ------------------------------

                                                                        val permanentSelfie =
                                                                            AttendanceStorageHelper
                                                                                .saveSelfie(

                                                                                    context =
                                                                                        context,

                                                                                    sourceFile =
                                                                                        photoFile,

                                                                                    employeeId =
                                                                                        staff.employeeId,

                                                                                    timestamp =
                                                                                        timestamp
                                                                                )


                                                                        // ------------------------------
                                                                        // SAVE ATTENDANCE TO ROOM
                                                                        // ------------------------------

                                                                        attendanceDao
                                                                            .insertAttendance(

                                                                                Attendance(

                                                                                    staffId =
                                                                                        staff.id,

                                                                                    timestamp =
                                                                                        timestamp,

                                                                                    latitude =
                                                                                        locationResult.latitude,

                                                                                    longitude =
                                                                                        locationResult.longitude,

                                                                                    selfiePath =
                                                                                        permanentSelfie.absolutePath
                                                                                )
                                                                            )


                                                                        Toast.makeText(

                                                                            context,

                                                                            "Attendance marked successfully ($similarityPercent% face match)",

                                                                            Toast.LENGTH_LONG

                                                                        ).show()


                                                                        navController
                                                                            .popBackStack()


                                                                    } catch (
                                                                        exception: Exception
                                                                    ) {


                                                                        photoFile.delete()


                                                                        Toast.makeText(

                                                                            context,

                                                                            "Unable to save attendance: ${exception.message}",

                                                                            Toast.LENGTH_LONG

                                                                        ).show()


                                                                        navController
                                                                            .popBackStack()
                                                                    }
                                                                }
                                                            }


                                                            // ========================================
                                                            // LOCATION FAILED
                                                            // ========================================

                                                            is LocationResult.Error -> {


                                                                photoFile.delete()


                                                                Toast.makeText(

                                                                    context,

                                                                    "Attendance not recorded: ${locationResult.message}",

                                                                    Toast.LENGTH_LONG

                                                                ).show()


                                                                navController
                                                                    .popBackStack()
                                                            }
                                                        }
                                                    }


                                            } else {


                                                // --------------------------------------------
                                                // FACE DID NOT MATCH
                                                // --------------------------------------------

                                                Toast.makeText(

                                                    context,

                                                    "Face does not match enrolled employee ($similarityPercent% similarity)",

                                                    Toast.LENGTH_LONG

                                                ).show()


                                                photoFile.delete()


                                                navController
                                                    .popBackStack()
                                            }
                                        }


                                        // ====================================================
                                        // EMBEDDING FAILED
                                        // ====================================================

                                        is EmbeddingResult.Error -> {


                                            Toast.makeText(

                                                context,

                                                "Face verification failed: ${embeddingResult.message}",

                                                Toast.LENGTH_LONG

                                            ).show()


                                            photoFile.delete()


                                            navController
                                                .popBackStack()
                                        }
                                    }
                                }
                        }
                    },


                    onBack = {

                        navController
                            .popBackStack()
                    }
                )
            }
        }
    }
}