package com.devnex.simvirtuallocation

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import com.devnex.simvirtuallocation.LocationSocketServiceUtils.startLocationSocketForeground
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Optional
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocationSocketService : Service() {

    private val subject = BehaviorSubject.create<String>()

    private var lastLocation: Location = Location("empty")
        get() {
            if (field.provider == "empty")
                return field
            val newLocation = Location(field)
            newLocation.latitude = randMove(newLocation.latitude)
            newLocation.longitude = randMove(newLocation.longitude)
            return newLocation
        }
    var locationProvider: IMockLocationProvider = StubLocationProvider()
    var disposable: Disposable? = null
    override fun onCreate() {
        super.onCreate()
        startLocationSocketForeground()
        if (locationProvider is StubLocationProvider) {
            locationProvider = MockLocationProvider(
                LocationManager.GPS_PROVIDER,
                this.application.applicationContext
            )
        }
        disposable = subject
            .map { hostAndPort ->
                val splitted = hostAndPort.split(":")
                    if (splitted.size == 2) {
                        val host = splitted[0]
                        val port = splitted[1].toInt()
                        host to port
                    } else {
                        throw IllegalArgumentException(hostAndPort)
                    }
            }
            .doOnNext {(host, port) ->
                println("parsed $host:$port")
            }
            .doOnError { it.printStackTrace() }
            .onErrorReturnItem("" to -1)
            .filter {(host, port) -> host.isNotEmpty() && port != -1}
            .switchMap {(host, port) ->
                createSocketObservable(host, port)
                    .retry()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .map {
                        val location = convertRawLocation(it)
                        Optional.ofNullable(location)
                    }
                    .doOnError {
                        it.printStackTrace()
                    }
                    .onErrorReturnItem(Optional.empty())
                    .filter { it.isPresent }
                    .map { it.get() }
                    .window(1, TimeUnit.SECONDS)
                    .switchMap { it.defaultIfEmpty(lastLocation) }
                    .filter { it.provider != "empty" }
            }.subscribe(::submitLocation)
    }

    private fun createSocketObservable(host: String, port: Int) = Observable.create { emitter ->
        val inputSocket = Socket()
        println("try to connect $host:$port by timeout 10s")
        inputSocket.connect(InetSocketAddress(host, port), 5000)
        inputSocket.use { socket ->
            if (socket.isConnected) {
                println("connected")
                val inputStream = socket.getInputStream()
                val byteArray = ByteArray(1024)

                while (socket.isConnected && !socket.isInputShutdown) {
                    val read = inputStream.available()
                    if (read > 0) {
                        println("try to read $read")
                        inputStream.read(byteArray, 0, read)
                        val rawLocations = String(byteArray, 0, read)
                        println(rawLocations)
                        rawLocations.dropLast(1).split("|")
                            .forEach { emitter.onNext(it) }
                    }
                }



                inputStream.close()
            } else {
                emitter.onError(IOException())
            }
        }

        emitter.setCancellable {
            inputSocket.shutdownInput()
            inputSocket.close()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("start command ${disposable == null}")
        val hostAndPort = intent?.extras?.getString("host")
        if(!hostAndPort.isNullOrEmpty()) {
            subject.onNext(hostAndPort)
        }
        return START_STICKY
    }

    private fun randMove(coord: Double): Double {
        val diff = 0.0000000001
        val multi = Random.nextInt(10) - 5
        return coord + diff * multi
    }

    private fun convertRawLocation(rawLocation: String): Location? {
        try {
            val splitted = rawLocation.split(";").mapNotNull {
                it.split("=").let { rawArgList ->
                    if (rawArgList.size != 2) return@mapNotNull null
                    rawArgList[0] to rawArgList[1]
                }
            }.toTypedArray()
            val args = mapOf(*splitted)
            val lat by args
            val lon by args
            val speed by args
            return locationProvider.createLocation(
                lat.toDouble(),
                lon.toDouble(),
                0.0,
                1f,
                speed.toDouble()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun submitLocation(location: Location) {
        println("submit ${location.latitude} ${location.longitude}")
        lastLocation = location
        locationProvider.pushLocation(location)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}