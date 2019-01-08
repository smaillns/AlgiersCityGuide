package com.example.smail.algiers_city_guide.gelocation

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import com.example.smail.algiers_city_guide.utility.Logcat
import java.lang.ref.WeakReference
import java.util.*
import android.content.pm.PackageManager
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import com.example.smail.algiers_city_guide.App
import com.example.smail.algiers_city_guide.activity.MainActivity


class Geolocation():LocationListener{
    private val LOCATION_AGE = 60000 * 5 // milliseconds
    private val LOCATION_TIMEOUT = 30000 // milliseconds

    private lateinit var mListener: WeakReference<GeolocationListener>
    private var mLocationManager: LocationManager? = null
    private var mCurrentLocation: Location? = null
    private var mTimer: Timer? = null


    constructor (locationManager: LocationManager, listener: GeolocationListener) : this() {
        mLocationManager = locationManager // (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mListener = WeakReference<GeolocationListener>(listener)
        mTimer = Timer()
        init()
    }


    override fun onLocationChanged(location: Location?) {
        Logcat.d("Geolocation.onLocationChanged(): " + location!!.provider + " / " + location.latitude + " / " + location.longitude + " / " + Date(location.time).toString())

        // check location age
        val timeDelta = System.currentTimeMillis() - location.time
        if (timeDelta > LOCATION_AGE) {
            Logcat.d("Geolocation.onLocationChanged(): gotten location is too old")
            // gotten location is too old
            return
        }

        // return location
        mCurrentLocation = Location(location)
        stop()
        val listener = mListener.get()
        if (listener != null && location != null) listener!!.onGeolocationRespond(this@Geolocation, mCurrentLocation!!)
    }


    override fun onProviderDisabled(provider: String) {
        Logcat.d("Geolocation.onProviderDisabled(): $provider")
    }


    override fun onProviderEnabled(provider: String) {
        Logcat.d("Geolocation.onProviderEnabled(): $provider")
    }


    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Logcat.d("Geolocation.onStatusChanged(): $provider")
        when (status) {
            LocationProvider.OUT_OF_SERVICE -> Logcat.d("Geolocation.onStatusChanged(): status OUT_OF_SERVICE")
            LocationProvider.TEMPORARILY_UNAVAILABLE -> Logcat.d("Geolocation.onStatusChanged(): status TEMPORARILY_UNAVAILABLE")
            LocationProvider.AVAILABLE -> Logcat.d("Geolocation.onStatusChanged(): status AVAILABLE")
        }
    }


    fun stop() {
        Logcat.d("Geolocation.stop()")
        if (mTimer != null) mTimer!!.cancel()
        if (mLocationManager != null) {
            mLocationManager!!.removeUpdates(this)
            mLocationManager = null
        }
    }


    @SuppressLint("MissingPermission")
    private fun init() {
        // get last known location
        val lastKnownLocation = getLastKnownLocation(mLocationManager!!)

        // try to listen last known location
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        }

        if (mCurrentLocation == null) {
            // start timer to check timeout
            val task = object : TimerTask() {
                override fun run() {
                    if (mCurrentLocation == null) {
                        Logcat.d("Geolocation.timer: timeout")
                        stop()
                        val listener = mListener.get()
                        if (listener != null) listener!!.onGeolocationFail(this@Geolocation)
                    }
                }
            }
            mTimer!!.schedule(task, LOCATION_TIMEOUT.toLong())

            // register location updates
            try {
                mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.0f, this)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            try {
                mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0.0f, this)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

        }
    }


    // returns last known freshest location from network or GPS

    @SuppressLint("ResourceType")
    private fun getLastKnownLocation(locationManager: LocationManager): Location? {
        Logcat.d("Geolocation.getLastKnownLocation()")

        val locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        var timeNet = 0L
        var timeGps = 0L

        if (locationNet != null) {
            timeNet = locationNet.time
        }

        if (locationGps != null) {
            timeGps = locationGps.time
        }

        return if (timeNet > timeGps)
            locationNet
        else
            locationGps
    }




}