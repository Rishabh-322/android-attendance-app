package com.rishabh.attendanceapp.ui

import com.rishabh.attendanceapp.face.FaceDetectionResult
import com.rishabh.attendanceapp.face.FaceDetectorHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.ImageCaptureException
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
import java.io.File

@Composable
fun FaceEnrollmentScreen(
    staff: Staff,
    onPhotoCaptured: (File) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var statusMessage by remember {
        mutableStateOf("Position your face clearly inside the camera.")
    }

    var isCapturing by remember {
        mutableStateOf(false)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted

            if (!granted) {
                statusMessage =
                    "Camera permission is required for face enrollment."
            }
        }

    LaunchedEffect(Unit) {

        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Enrol Face",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${staff.name} • ${staff.employeeId}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasCameraPermission) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                AndroidView(
                    factory = { ctx ->

                        val previewView = PreviewView(ctx)

                        val cameraProviderFuture =
                            ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({

                            try {

                                val cameraProvider =
                                    cameraProviderFuture.get()

                                val preview =
                                    Preview.Builder().build().also {
                                        it.surfaceProvider =
                                            previewView.surfaceProvider
                                    }

                                val capture =
                                    ImageCapture.Builder().build()

                                imageCapture = capture

                                val cameraSelector =
                                    CameraSelector.DEFAULT_FRONT_CAMERA

                                cameraProvider.unbindAll()

                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )

                            } catch (exception: Exception) {

                                statusMessage =
                                    "Unable to start camera: ${exception.message}"
                            }

                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },

                    modifier = Modifier.fillMaxSize()
                )
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Camera permission is required."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val capture = imageCapture

                if (capture == null) {

                    statusMessage = "Camera is not ready yet."

                    return@Button
                }

                isCapturing = true
                statusMessage = "Capturing..."

                val photoFile = File(
                    context.cacheDir,
                    "enrollment_${staff.employeeId}_${System.currentTimeMillis()}.jpg"
                )

                val outputOptions =
                    ImageCapture.OutputFileOptions.Builder(photoFile)
                        .build()

                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),

                    object : ImageCapture.OnImageSavedCallback {

                        override fun onImageSaved(
                            outputFileResults: ImageCapture.OutputFileResults
                        ) {

                            statusMessage = "Checking face..."

                            FaceDetectorHelper.detectFace(
                                context = context,
                                photoFile = photoFile
                            ) { result ->

                                isCapturing = false

                                when (result) {

                                    is FaceDetectionResult.Success -> {

                                        statusMessage =
                                            "Face detected successfully."

                                        onPhotoCaptured(photoFile)
                                    }

                                    FaceDetectionResult.NoFace -> {

                                        statusMessage =
                                            "No face detected. Please look directly at the camera and try again."

                                        photoFile.delete()
                                    }

                                    is FaceDetectionResult.MultipleFaces -> {

                                        statusMessage =
                                            "Multiple faces detected (${result.count}). Only one person should be visible."

                                        photoFile.delete()
                                    }

                                    is FaceDetectionResult.Error -> {

                                        statusMessage =
                                            "Face detection failed: ${result.message}"

                                        photoFile.delete()
                                    }
                                }
                            }
                        }

                        override fun onError(
                            exception: ImageCaptureException
                        ) {

                            isCapturing = false

                            statusMessage =
                                "Capture failed: ${exception.message}"
                        }
                    }
                )

            },

            enabled = hasCameraPermission && !isCapturing,

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (isCapturing)
                    "CAPTURING..."
                else
                    "CAPTURE SELFIE"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CANCEL")
        }
    }
}