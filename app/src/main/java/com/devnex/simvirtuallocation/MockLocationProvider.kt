package com.devnex.simvirtuallocation

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock

class MockLocationProvider(private val providerName: String, private val ctx: Context): IMockLocationProvider {
    init {
        val lm = ctx.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lm.addTestProvider(
                providerName, false, false, false, false, false,
                true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE
            )
        } else {
            lm.addTestProvider(
                providerName, false, false, false, false, false,
                true, true, 1, 1,
            )
        }
        lm.setTestProviderEnabled(providerName, true)
    }

    var lastLocation: Location? = null

    override fun pushLocation(lat: Double, lon: Double, alt: Double, accuracy: Float, speed: Double) {
        val lm = ctx.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
        val mockLocation = createLocation(lat, lon, alt, accuracy, speed)
        lm.setTestProviderLocation(providerName, mockLocation)
    }

    override fun pushLocation(location: Location) {
        val lm = ctx.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
        lm.setTestProviderLocation(providerName, location)
    }

    override fun createLocation(lat: Double, lon: Double, alt: Double, accuracy: Float, speed: Double): Location {
        val mockLocation = Location(providerName)
        mockLocation.latitude = lat
        mockLocation.longitude = lon
        mockLocation.altitude = alt
        mockLocation.accuracy = accuracy
        mockLocation.speed = speed.toFloat()
        mockLocation.time = System.currentTimeMillis()
        mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        return mockLocation
    }

    override fun shutdown() {
        val lm = ctx.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
        lm.removeTestProvider(providerName)
    }
}