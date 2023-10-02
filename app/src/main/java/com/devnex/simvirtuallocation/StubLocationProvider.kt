package com.devnex.simvirtuallocation

import android.location.Location

class StubLocationProvider : IMockLocationProvider {
    override fun pushLocation(
        lat: Double,
        lon: Double,
        alt: Double,
        accuracy: Float,
        speed: Double
    ) {
        println("Mock location is not enabled")
    }

    override fun pushLocation(location: Location) {
        println("Mock location is not enabled")
    }

    override fun createLocation(
        lat: Double,
        lon: Double,
        alt: Double,
        accuracy: Float,
        speed: Double
    ): Location {
        return Location("");
    }

    override fun shutdown() {
        println("Mock location is not enabled. Shutdown")
    }
}