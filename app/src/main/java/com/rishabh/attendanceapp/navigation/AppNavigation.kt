package com.rishabh.attendanceapp.navigation

import com.rishabh.attendanceapp.face.FaceMatcher
import android.widget.Toast
import com.rishabh.attendanceapp.face.EmbeddingResult
import com.rishabh.attendanceapp.face.FaceRecognitionHelper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rishabh.attendanceapp.data.DatabaseProvider
import com.rishabh.attendanceapp.data.Staff
import com.rishabh.attendanceapp.ui.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val context = LocalContext.current

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    val staffDao = database.staffDao()

    val staffList by staffDao
        .getAllStaff()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // ------------------------------------------------
        // LOGIN
        // ------------------------------------------------

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

                    navController.navigate("staff/$employeeId") {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // ------------------------------------------------
        // ADMIN DASHBOARD
        // ------------------------------------------------

        composable("admin") {

            AdminDashboard(

                staffList = staffList,

                onAddStaff = {
                    navController.navigate("addStaff")
                },

                onStaffClick = { staff ->
                    navController.navigate(
                        "staffProfile/${staff.id}"
                    )
                },

                onLogout = {

                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }


        // ------------------------------------------------
        // ADD STAFF
        // ------------------------------------------------

        composable("addStaff") {

            AddStaffScreen(

                onSave = { name, employeeId ->

                    coroutineScope.launch {

                        val existingStaff =
                            staffDao.getStaffByEmployeeId(employeeId)

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


        // ------------------------------------------------
        // STAFF PROFILE
        // ------------------------------------------------

        composable(
            route = "staffProfile/{staffId}",

            arguments = listOf(
                navArgument("staffId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val staffId =
                backStackEntry.arguments?.getInt("staffId")

            val staff =
                staffList.find { it.id == staffId }

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
        // ------------------------------------------------
        // FACE ENROLLMENT
        // ------------------------------------------------

        composable(
            route = "faceEnrollment/{staffId}",

            arguments = listOf(
                navArgument("staffId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val staffId =
                backStackEntry.arguments?.getInt("staffId")

            val staff =
                staffList.find { it.id == staffId }

            if (staff != null) {

                FaceEnrollmentScreen(

                    staff = staff,

                    onPhotoCaptured = { photoFile ->

                        FaceRecognitionHelper.generateEmbedding(
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


                                        navController.popBackStack()
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


        // ------------------------------------------------
        // STAFF DASHBOARD
        // ------------------------------------------------

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


            StaffDashboard(

                staff = staff,

                employeeId = employeeId,

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

        // ------------------------------------------------
// ATTENDANCE FACE VERIFICATION
// ------------------------------------------------

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


                        if (enrolledEmbedding == null) {

                            Toast.makeText(
                                context,
                                "No enrolled face found.",
                                Toast.LENGTH_LONG
                            ).show()

                            photoFile.delete()

                            return@AttendanceCameraScreen
                        }


                        FaceRecognitionHelper
                            .generateEmbedding(

                                context = context,

                                photoFile =
                                    photoFile

                            ) { embeddingResult ->


                                when (
                                    embeddingResult
                                ) {


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


                                        if (
                                            matchResult.isMatch
                                        ) {

                                            Toast.makeText(
                                                context,
                                                "Face verified successfully ($similarityPercent% similarity)",
                                                Toast.LENGTH_LONG
                                            ).show()


                                            // Keep the image for now.
                                            // In the next phase we will
                                            // permanently store it as
                                            // the attendance selfie.


                                            navController
                                                .popBackStack()

                                        } else {

                                            Toast.makeText(
                                                context,
                                                "Face does not match enrolled employee ($similarityPercent% similarity)",
                                                Toast.LENGTH_LONG
                                            ).show()


                                            // Rejected photos do not
                                            // need to be retained.

                                            photoFile.delete()


                                            navController
                                                .popBackStack()
                                        }
                                    }


                                    is EmbeddingResult.Error -> {


                                        Toast.makeText(
                                            context,
                                            "Face verification failed: ${embeddingResult.message}",
                                            Toast.LENGTH_LONG
                                        ).show()


                                        photoFile.delete()
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