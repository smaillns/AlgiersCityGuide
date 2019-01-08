package com.example.smail.algiers_city_guide.gelocation

import android.location.Location

interface GeolocationListener {
    abstract fun onGeolocationRespond(geolocation: Geolocation, location: Location)
    abstract fun onGeolocationFail(geolocation: Geolocation)
}