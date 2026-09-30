package com.rishabh.attendanceapp.face

import kotlin.math.sqrt

object FaceMatcher {

    // Demo threshold.
    // We can calibrate this after testing your face vs another face.
    const val MATCH_THRESHOLD = 0.65f

    data class MatchResult(
        val isMatch: Boolean,
        val similarity: Float
    )

    fun compare(
        enrolledEmbeddingString: String,
        currentEmbedding: FloatArray
    ): MatchResult {

        val enrolledEmbedding =
            enrolledEmbeddingString
                .split(",")
                .mapNotNull { value ->
                    value.toFloatOrNull()
                }
                .toFloatArray()

        if (
            enrolledEmbedding.isEmpty() ||
            enrolledEmbedding.size != currentEmbedding.size
        ) {
            return MatchResult(
                isMatch = false,
                similarity = 0f
            )
        }

        val similarity =
            cosineSimilarity(
                enrolledEmbedding,
                currentEmbedding
            )

        return MatchResult(
            isMatch = similarity >= MATCH_THRESHOLD,
            similarity = similarity
        )
    }


    private fun cosineSimilarity(
        first: FloatArray,
        second: FloatArray
    ): Float {

        var dotProduct = 0f
        var firstMagnitude = 0f
        var secondMagnitude = 0f

        for (index in first.indices) {

            dotProduct +=
                first[index] * second[index]

            firstMagnitude +=
                first[index] * first[index]

            secondMagnitude +=
                second[index] * second[index]
        }

        if (
            firstMagnitude == 0f ||
            secondMagnitude == 0f
        ) {
            return 0f
        }

        return (
                dotProduct /
                        (
                                sqrt(firstMagnitude) *
                                        sqrt(secondMagnitude)
                                )
                )
    }
}