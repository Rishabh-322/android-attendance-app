package com.rishabh.attendanceapp.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {

    fun getCurrentLocation(
        context: Context,
        onResult: (LocationResult) -> Unit
    ) {

        val finePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val coarsePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )


        if (
            finePermission != PackageManager.PERMISSION_GRANTED &&
            coarsePermission != PackageManager.PERMISSION_GRANTED
        ) {

            onResult(
                LocationResult.Error(
                    "Location permission has not been granted."
                )
            )

            return
        }


        try {

            val fusedLocationClient =
                LocationServices
                    .getFusedLocationProviderClient(
                        context
                    )


            val cancellationTokenSource =
                CancellationTokenSource()


            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                .addOnSuccessListener { location ->

                    if (location != null) {

                        onResult(
                            LocationResult.Success(
                                latitude =
                                    location.latitude,

                                longitude =
                                    location.longitude
                            )
                        )

                    } else {

                        onResult(
                            LocationResult.Error(
                                "Current location is unavailable."
                            )
                        )
                    }
                }

                .addOnFailureListener { exception ->

                    onResult(
                        LocationResult.Error(
                            exception.message
                                ?: "Unable to obtain location."
                        )
                    )
                }

        } catch (
            exception: SecurityException
        ) {

            onResult(
                LocationResult.Error(
                    "Location permission error."
                )
            )
        }
    }
}


sealed class LocationResult {

    data class Success(
        val latitude: Double,
        val longitude: Double
    ) : LocationResult()


    data class Error(
        val message: String
    ) : LocationResult()
}