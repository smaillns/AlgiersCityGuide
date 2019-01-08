package com.example.smail.algiers_city_guide.utility

import com.example.smail.algiers_city_guide.CityGuideConfig


object Logcat {
    val TAG = "CITYGUIDE"


    fun d(msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.d(TAG, msg)
    }

    fun d(tag: String, msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.d(tag, msg)
    }

    fun e(msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.e(TAG, msg)
    }

    fun e(tag: String, msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.e(tag, msg)
    }

    fun e(msg: String, tr: Throwable) {
        if (CityGuideConfig.LOGS) android.util.Log.e(TAG, msg, tr)
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        if (CityGuideConfig.LOGS) android.util.Log.e(tag, msg, tr)
    }

    fun i(msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.i(TAG, msg)
    }

    fun i(tag: String, msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.i(tag, msg)
    }

    fun v(msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.v(TAG, msg)
    }

    fun v(tag: String, msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.v(tag, msg)
    }

    fun w(msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.w(TAG, msg)
    }

    fun w(tag: String, msg: String) {
        if (CityGuideConfig.LOGS) android.util.Log.w(tag, msg)
    }

}