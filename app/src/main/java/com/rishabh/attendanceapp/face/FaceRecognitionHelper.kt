package com.rishabh.attendanceapp.face

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object FaceRecognitionHelper {

    private const val MODEL_NAME = "mobile_face_net.tflite"

    private const val INPUT_SIZE = 112

    private const val EMBEDDING_SIZE = 192


    fun generateEmbedding(
        context: Context,
        photoFile: File,
        onResult: (EmbeddingResult) -> Unit
    ) {

        try {

            val bitmap =
                loadCorrectlyOrientedBitmap(photoFile)

            if (bitmap == null) {

                onResult(
                    EmbeddingResult.Error(
                        "Unable to read captured image."
                    )
                )

                return
            }

            val image =
                InputImage.fromBitmap(bitmap, 0)

            val detectorOptions =
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(
                        FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
                    )
                    .build()

            val detector =
                FaceDetection.getClient(detectorOptions)


            detector.process(image)

                .addOnSuccessListener { faces ->

                    try {

                        if (faces.size != 1) {

                            onResult(
                                EmbeddingResult.Error(
                                    "Exactly one face is required."
                                )
                            )

                            detector.close()
                            return@addOnSuccessListener
                        }


                        val box =
                            faces.first().boundingBox


                        // Add a little padding around the detected face.
                        val horizontalPadding =
                            (box.width() * 0.20f).toInt()

                        val verticalPadding =
                            (box.height() * 0.20f).toInt()


                        val left =
                            max(
                                0,
                                box.left - horizontalPadding
                            )

                        val top =
                            max(
                                0,
                                box.top - verticalPadding
                            )

                        val right =
                            min(
                                bitmap.width,
                                box.right + horizontalPadding
                            )

                        val bottom =
                            min(
                                bitmap.height,
                                box.bottom + verticalPadding
                            )


                        val cropWidth =
                            right - left

                        val cropHeight =
                            bottom - top


                        if (
                            cropWidth <= 0 ||
                            cropHeight <= 0
                        ) {

                            onResult(
                                EmbeddingResult.Error(
                                    "Unable to crop detected face."
                                )
                            )

                            detector.close()
                            return@addOnSuccessListener
                        }


                        val croppedFace =
                            Bitmap.createBitmap(
                                bitmap,
                                left,
                                top,
                                cropWidth,
                                cropHeight
                            )


                        val resizedFace =
                            Bitmap.createScaledBitmap(
                                croppedFace,
                                INPUT_SIZE,
                                INPUT_SIZE,
                                true
                            )


                        val embedding =
                            runMobileFaceNet(
                                context,
                                resizedFace
                            )


                        onResult(
                            EmbeddingResult.Success(
                                embedding
                            )
                        )


                    } catch (exception: Exception) {

                        onResult(
                            EmbeddingResult.Error(
                                exception.message
                                    ?: "Face embedding failed."
                            )
                        )
                    }

                    detector.close()
                }


                .addOnFailureListener { exception ->

                    detector.close()

                    onResult(
                        EmbeddingResult.Error(
                            exception.message
                                ?: "Unable to detect face."
                        )
                    )
                }


        } catch (exception: Exception) {

            onResult(
                EmbeddingResult.Error(
                    exception.message
                        ?: "Face recognition failed."
                )
            )
        }
    }


    private fun runMobileFaceNet(
        context: Context,
        faceBitmap: Bitmap
    ): FloatArray {

        val modelBytes =
            context.assets
                .open(MODEL_NAME)
                .use {
                    it.readBytes()
                }


        val modelBuffer =
            ByteBuffer.allocateDirect(
                modelBytes.size
            ).apply {

                order(
                    ByteOrder.nativeOrder()
                )

                put(modelBytes)

                rewind()
            }


        val interpreter =
            Interpreter(
                modelBuffer,
                Interpreter.Options()
                    .setNumThreads(4)
            )


        val inputBuffer =
            ByteBuffer.allocateDirect(
                1 *
                        INPUT_SIZE *
                        INPUT_SIZE *
                        3 *
                        4
            )

        inputBuffer.order(
            ByteOrder.nativeOrder()
        )


        for (y in 0 until INPUT_SIZE) {

            for (x in 0 until INPUT_SIZE) {

                val pixel =
                    faceBitmap.getPixel(x, y)


                val red =
                    (pixel shr 16) and 0xFF

                val green =
                    (pixel shr 8) and 0xFF

                val blue =
                    pixel and 0xFF


                inputBuffer.putFloat(
                    red / 128.0f - 1.0f
                )

                inputBuffer.putFloat(
                    green / 128.0f - 1.0f
                )

                inputBuffer.putFloat(
                    blue / 128.0f - 1.0f
                )
            }
        }


        inputBuffer.rewind()


        val output =
            Array(1) {
                FloatArray(
                    EMBEDDING_SIZE
                )
            }


        interpreter.run(
            inputBuffer,
            output
        )


        interpreter.close()


        return normalizeEmbedding(
            output[0]
        )
    }


    private fun normalizeEmbedding(
        embedding: FloatArray
    ): FloatArray {

        var sum = 0.0f

        for (value in embedding) {

            sum += value * value
        }


        val magnitude =
            sqrt(sum)


        if (magnitude == 0.0f) {

            return embedding
        }


        return FloatArray(
            embedding.size
        ) { index ->

            embedding[index] /
                    magnitude
        }
    }


    private fun loadCorrectlyOrientedBitmap(
        photoFile: File
    ): Bitmap? {

        val bitmap =
            BitmapFactory.decodeFile(
                photoFile.absolutePath
            )
                ?: return null


        val exif =
            ExifInterface(
                photoFile.absolutePath
            )


        val orientation =
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )


        val rotation =
            when (orientation) {

                ExifInterface.ORIENTATION_ROTATE_90 ->
                    90f

                ExifInterface.ORIENTATION_ROTATE_180 ->
                    180f

                ExifInterface.ORIENTATION_ROTATE_270 ->
                    270f

                else ->
                    0f
            }


        if (rotation == 0f) {

            return bitmap
        }


        val matrix =
            Matrix().apply {

                postRotate(rotation)
            }


        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}


sealed class EmbeddingResult {

    data class Success(
        val embedding: FloatArray
    ) : EmbeddingResult()


    data class Error(
        val message: String
    ) : EmbeddingResult()
}