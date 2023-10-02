package com.devnex.simvirtuallocation

import android.location.Location

interface IMockLocationProvider {
    fun pushLocation(lat: Double, lon: Double, alt: Double, accuracy: Float, speed: Double)
    fun pushLocation(location: Location)

    fun createLocation(lat: Double, lon: Double, alt: Double, accuracy: Float, speed: Double): Location

    fun shutdown()
}