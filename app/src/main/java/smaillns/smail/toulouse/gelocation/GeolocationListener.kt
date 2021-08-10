package smaillns.smail.toulouse.gelocation

import android.location.Location

interface GeolocationListener {
    abstract fun onGeolocationRespond(geolocation: Geolocation, location: Location)
    abstract fun onGeolocationFail(geolocation: Geolocation)
}