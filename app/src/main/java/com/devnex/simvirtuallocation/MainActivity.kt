package com.devnex.simvirtuallocation

import android.Manifest
import android.app.AppOpsManager
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.devnex.simvirtuallocation.LocationSocketServiceUtils.startLocationSocketService


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isMockSettingsON) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("In order to use this app you must enable mock location do you want to enable it now?")
                .setTitle("Mock location is not enable")
            builder.setPositiveButton("yes") { dialog: DialogInterface?, id: Int ->
                val i = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                startActivity(i)
            }
            builder.setNegativeButton("No") { dialog: DialogInterface?, id: Int -> finish() }
            val dialog = builder.create()
            dialog.show()
        }
        findViewById<Button>(R.id.button).setOnClickListener {
            val ipEditText = findViewById<EditText>(R.id.et_ip)
            val ipAddress = ipEditText.text.toString()
            if(ipAddress.isNotEmpty()) {
                ipEditText.error = null
                baseContext.startLocationSocketService(ipAddress)
            } else {
                ipEditText.error = "wrong format"
            }
        }
        val receiver = AdbBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("send.mock")
        intentFilter.addAction("stop.mock")
        applicationContext.registerReceiver(receiver, intentFilter)
    }

    private val isMockSettingsON: Boolean
        private get() {
            var isMockLocation = false
            isMockLocation = try {
                //if marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val opsManager = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
                    opsManager.checkOp(
                        AppOpsManager.OPSTR_MOCK_LOCATION,
                        Process.myUid(),
                        "com.devnex.simvirtuallocation",
                    ) == AppOpsManager.MODE_ALLOWED
                } else {
                    // in marshmallow this will always return true
                    Settings.Secure.getString(this.contentResolver, "mock_location") != "0"
                }
            } catch (e: Exception) {
                return isMockLocation
            }
            return isMockLocation
        }

    fun testMock(view: View?) { // On some version do like this
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askRunTimePermissions()
        }
    }

    private fun askRunTimePermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) { // If request is cancelled, the result arrays are empty.
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Got permission location ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No location permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1
    }
}