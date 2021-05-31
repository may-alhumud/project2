package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R

private const val REQUEST_LOCATION_PERMISSION = 101

//Check the base location permissions required for all versions of Android
fun Activity.checkLocationPermissions(): Boolean {
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return true
    }
    return false
}

fun Activity.isPermissionsGranted(): Boolean {
    return checkLocationPermissions() && hasAndroidQPermissions(this)
}

//Check if the version of Android is Q, otherwise return true
@TargetApi(Build.VERSION_CODES.Q)
private fun hasAndroidQPermissions(activity: Activity): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return when (PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.checkSelfPermission(activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) -> {
            true
        }
        else -> false
    }
}


//Request for the base location permissions, add additional permission for Android Q
fun Activity.requestBaseLocationPermissions() {
    val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    ActivityCompat.requestPermissions(this,
            permissions.toTypedArray(), REQUEST_LOCATION_PERMISSION)
}


fun Activity.getLocationRequestTask(resolve: Boolean = true): Task<LocationSettingsResponse> {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(this)
    val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve){
            try { exception.startResolutionForResult(this,
                    1)
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d("LocSettingsResponse", "Error getting location settings resolution: " + sendEx.message)
            }
        } else {
            getLocationRequestTask()
        }
    }
    return locationSettingsResponseTask
}