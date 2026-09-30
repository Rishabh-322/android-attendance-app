package com.rishabh.attendanceapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rishabh.attendanceapp.data.Staff
import com.rishabh.attendanceapp.face.FaceDetectionResult
import com.rishabh.attendanceapp.face.FaceDetectorHelper
import java.io.File

@Composable
fun AttendanceCameraScreen(
    staff: Staff,
    onPhotoCaptured: (File) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun cameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun locationPermissionGranted(): Boolean {

        val fine =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarse =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }


    var hasCameraPermission by remember {
        mutableStateOf(
            cameraPermissionGranted()
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            locationPermissionGranted()
        )
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var isCapturing by remember {
        mutableStateOf(false)
    }

    var statusMessage by remember {
        mutableStateOf(
            "Look directly at the camera to verify your identity."
        )
    }


    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {

            hasCameraPermission =
                cameraPermissionGranted()

            hasLocationPermission =
                locationPermissionGranted()

            if (!hasCameraPermission) {

                statusMessage =
                    "Camera permission is required."

            } else if (!hasLocationPermission) {

                statusMessage =
                    "Location permission is required to record attendance."

            } else {

                statusMessage =
                    "Look directly at the camera to verify your identity."
            }
        }


    LaunchedEffect(Unit) {

        val requiredPermissions =
            mutableListOf<String>()

        if (!cameraPermissionGranted()) {

            requiredPermissions.add(
                Manifest.permission.CAMERA
            )
        }

        if (!locationPermissionGranted()) {

            requiredPermissions.add(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            requiredPermissions.add(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        if (requiredPermissions.isNotEmpty()) {

            permissionLauncher.launch(
                requiredPermissions.toTypedArray()
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Mark Attendance",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "${staff.name} • ${staff.employeeId}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        if (hasCameraPermission) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                AndroidView(

                    factory = { ctx ->

                        val previewView =
                            PreviewView(ctx)

                        val cameraProviderFuture =
                            ProcessCameraProvider
                                .getInstance(ctx)

                        cameraProviderFuture
                            .addListener({

                                try {

                                    val cameraProvider =
                                        cameraProviderFuture.get()

                                    val preview =
                                        Preview.Builder()
                                            .build()
                                            .also {

                                                it.surfaceProvider =
                                                    previewView.surfaceProvider
                                            }

                                    val capture =
                                        ImageCapture.Builder()
                                            .build()

                                    imageCapture =
                                        capture

                                    val cameraSelector =
                                        CameraSelector
                                            .DEFAULT_FRONT_CAMERA

                                    cameraProvider.unbindAll()

                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        capture
                                    )

                                } catch (
                                    exception: Exception
                                ) {

                                    statusMessage =
                                        "Unable to start camera: ${exception.message}"
                                }

                            },
                                ContextCompat.getMainExecutor(ctx)
                            )

                        previewView
                    },

                    modifier =
                        Modifier.fillMaxSize()
                )
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    "Camera permission is required."
                )
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        if (!hasLocationPermission) {

            Text(
                text = "Location permission is required before attendance can be recorded.",
                color = MaterialTheme.colorScheme.error
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }


        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        Button(

            onClick = {

                val capture =
                    imageCapture

                if (capture == null) {

                    statusMessage =
                        "Camera is not ready yet."

                    return@Button
                }

                isCapturing = true

                statusMessage =
                    "Capturing selfie..."


                val photoFile =
                    File(
                        context.cacheDir,
                        "attendance_${staff.employeeId}_${System.currentTimeMillis()}.jpg"
                    )


                val outputOptions =
                    ImageCapture
                        .OutputFileOptions
                        .Builder(photoFile)
                        .build()


                capture.takePicture(

                    outputOptions,

                    ContextCompat
                        .getMainExecutor(context),

                    object :
                        ImageCapture.OnImageSavedCallback {

                        override fun onImageSaved(
                            outputFileResults:
                            ImageCapture.OutputFileResults
                        ) {

                            statusMessage =
                                "Checking face..."


                            FaceDetectorHelper.detectFace(

                                context = context,

                                photoFile = photoFile

                            ) { result ->


                                when (result) {

                                    is FaceDetectionResult.Success -> {

                                        statusMessage =
                                            "Face detected. Verifying identity..."

                                        onPhotoCaptured(
                                            photoFile
                                        )
                                    }


                                    FaceDetectionResult.NoFace -> {

                                        isCapturing =
                                            false

                                        statusMessage =
                                            "No face detected. Please look directly at the camera."

                                        photoFile.delete()
                                    }


                                    is FaceDetectionResult.MultipleFaces -> {

                                        isCapturing =
                                            false

                                        statusMessage =
                                            "Multiple faces detected. Only the employee should be visible."

                                        photoFile.delete()
                                    }


                                    is FaceDetectionResult.Error -> {

                                        isCapturing =
                                            false

                                        statusMessage =
                                            "Face detection failed: ${result.message}"

                                        photoFile.delete()
                                    }
                                }
                            }
                        }


                        override fun onError(
                            exception:
                            ImageCaptureException
                        ) {

                            isCapturing =
                                false

                            statusMessage =
                                "Capture failed: ${exception.message}"
                        }
                    }
                )
            },

            enabled =
                hasCameraPermission &&
                        hasLocationPermission &&
                        !isCapturing,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(
                if (isCapturing)
                    "VERIFYING..."
                else
                    "VERIFY FACE"
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        OutlinedButton(
            onClick = onBack,
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("CANCEL")
        }
    }
}