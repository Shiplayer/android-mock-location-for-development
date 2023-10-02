package com.devnex.simvirtuallocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log

class AdbBroadcastReceiver : BroadcastReceiver() {
    var mockGPS: MockLocationProvider? = null
    var mockWifi: MockLocationProvider? = null
    var logTag = "MockGpsAdbBroadcastReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(logTag, "received intent")
        if (intent.action == "stop.mock") {
            if (mockGPS != null) {
                mockGPS!!.shutdown()
            }
            if (mockWifi != null) {
                mockWifi!!.shutdown()
            }
        } else {
            if (mockGPS == null) {
                mockGPS = MockLocationProvider(LocationManager.GPS_PROVIDER, context)
            }
            if (mockWifi == null) {
                mockWifi = MockLocationProvider(LocationManager.NETWORK_PROVIDER, context)
            }
            val lat: Double
            val lon: Double
            val alt: Double
            val accurate: Float
            lat =
                (if (intent.getStringExtra("lat") != null) intent.getStringExtra("lat") else "0")!!.toDouble()
            lon =
                (if (intent.getStringExtra("lon") != null) intent.getStringExtra("lon") else "0")!!.toDouble()
            alt =
                (if (intent.getStringExtra("alt") != null) intent.getStringExtra("alt") else "0")!!.toDouble()
            accurate =
                (if (intent.getStringExtra("accurate") != null) intent.getStringExtra("accurate") else "1")!!.toFloat()
            Log.i(
                logTag,
                String.format(
                    "setting mock to Latitude=%f, Longitude=%f Altitude=%f Accuracy=%f",
                    lat,
                    lon,
                    alt,
                    accurate
                )
            )
            mockGPS!!.pushLocation(lat, lon, alt, accurate, 0.0)
            mockWifi!!.pushLocation(lat, lon, alt, accurate, 0.0)
        }
    }
}