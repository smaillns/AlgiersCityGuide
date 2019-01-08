package com.example.smail.algiers_city_guide

object CityGuideConfig{
    // true for enabling debug logs, should be false in production release
    val LOGS = false

    // true for enabling Google Analytics, should be true in production release
    val ANALYTICS = false

    // true for enabling Google AdMob banner on POI list screen, should be true in production release
    val ADMOB_POI_LIST_BANNER = true

    // true for enabling Google AdMob on POI detail screen, should be true in production release
    val ADMOB_POI_DETAIL_BANNER = true

    // true for enabling Google AdMob on map screen, should be true in production release
    val ADMOB_MAP_BANNER = true

    // file name of the SQLite database, this file should be placed in assets folder
    val DATABASE_NAME = "cityguide.db"

    // database version, should be incremented if database has been changed
    val DATABASE_VERSION = 2


}