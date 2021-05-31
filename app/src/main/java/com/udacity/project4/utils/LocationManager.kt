package com.mahmoud.todoapp.util


import android.location.Location

interface LocationManager {
    fun onLocationChanged(location: Location?)

    fun getLastKnownLocation(location: Location?)
}