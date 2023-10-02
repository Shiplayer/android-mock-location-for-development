package com.devnex.simvirtuallocation

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val LOCATION_SOCKET_NOTIFICATION_ID = 10
const val LOCATION_SOCKET_CHANNEL_ID = "pro_location_service_channel_id"

object LocationSocketServiceUtils {
    fun Context.startLocationSocketService(ipAddress: String) {
        val intent = Intent(this, LocationSocketService::class.java)
        intent.putExtra("host", ipAddress)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun LocationSocketService.startLocationSocketForeground() {
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannelCompat.Builder(
                LOCATION_SOCKET_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
                .setName(getString(R.string.pro_service_channel_name))
                .build()
            val manager = NotificationManagerCompat.from(this)
            manager.getNotificationChannel(LOCATION_SOCKET_CHANNEL_ID)
            manager.createNotificationChannel(channel)
            NotificationCompat.Builder(this, LOCATION_SOCKET_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(this)
        }
        val notification = notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(
            LOCATION_SOCKET_NOTIFICATION_ID,
            notification
        )
    }

    fun LocationSocketService.stopLocationSocketForeground() {
        val manager = NotificationManagerCompat.from(this)
        manager.cancel(LOCATION_SOCKET_NOTIFICATION_ID)
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }
}