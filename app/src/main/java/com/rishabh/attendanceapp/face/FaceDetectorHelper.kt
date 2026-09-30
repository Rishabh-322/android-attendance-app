package com.rishabh.attendanceapp.face

import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File

object FaceDetectorHelper {

    fun detectFace(
        context: android.content.Context,
        photoFile: File,
        onResult: (FaceDetectionResult) -> Unit
    ) {

        try {

            val image = InputImage.fromFilePath(
                context,
                Uri.fromFile(photoFile)
            )

            val options =
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(
                        FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
                    )
                    .build()

            val detector =
                FaceDetection.getClient(options)

            detector.process(image)

                .addOnSuccessListener { faces ->

                    when {

                        faces.isEmpty() -> {

                            onResult(
                                FaceDetectionResult.NoFace
                            )
                        }

                        faces.size > 1 -> {

                            onResult(
                                FaceDetectionResult.MultipleFaces(
                                    faces.size
                                )
                            )
                        }

                        else -> {

                            val face = faces.first()

                            onResult(
                                FaceDetectionResult.Success(
                                    boundingBoxLeft =
                                        face.boundingBox.left,

                                    boundingBoxTop =
                                        face.boundingBox.top,

                                    boundingBoxRight =
                                        face.boundingBox.right,

                                    boundingBoxBottom =
                                        face.boundingBox.bottom
                                )
                            )
                        }
                    }

                    detector.close()
                }

                .addOnFailureListener { exception ->

                    onResult(
                        FaceDetectionResult.Error(
                            exception.message
                                ?: "Unknown face detection error"
                        )
                    )

                    detector.close()
                }

        } catch (exception: Exception) {

            onResult(
                FaceDetectionResult.Error(
                    exception.message
                        ?: "Unable to process image"
                )
            )
        }
    }
}


sealed class FaceDetectionResult {

    data class Success(
        val boundingBoxLeft: Int,
        val boundingBoxTop: Int,
        val boundingBoxRight: Int,
        val boundingBoxBottom: Int
    ) : FaceDetectionResult()


    data object NoFace :
        FaceDetectionResult()


    data class MultipleFaces(
        val count: Int
    ) : FaceDetectionResult()


    data class Error(
        val message: String
    ) : FaceDetectionResult()
}