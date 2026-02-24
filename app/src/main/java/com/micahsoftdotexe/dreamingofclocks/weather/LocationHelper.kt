package com.micahsoftdotexe.dreamingofclocks.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

object LocationHelper {
    private const val TAG = "WeatherAPI"

    private fun getAvailableProvider(lm: LocationManager): String? {
        val providers = lm.getProviders(true)
        return when {
            LocationManager.NETWORK_PROVIDER in providers -> LocationManager.NETWORK_PROVIDER
            LocationManager.GPS_PROVIDER in providers -> LocationManager.GPS_PROVIDER
            else -> null
        }
    }

    fun getLastKnownLocation(context: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Location permission not granted")
            return null
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = lm.getProviders(true)
        var location: android.location.Location? = null
        if (LocationManager.NETWORK_PROVIDER in providers) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (location == null && LocationManager.GPS_PROVIDER in providers) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        return if (location != null) {
            Log.d(TAG, "Last known location: (${location.latitude}, ${location.longitude})")
            Pair(location.latitude, location.longitude)
        } else {
            Log.d(TAG, "No last known location available")
            null
        }
    }

    fun requestLocationUpdate(context: Context, callback: (Pair<Double, Double>?) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
            return
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = getAvailableProvider(lm)
        if (provider == null) {
            Log.e(TAG, "No location provider available")
            callback(null)
            return
        }
        Log.d(TAG, "Using location provider: $provider")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                lm.getCurrentLocation(
                    provider,
                    CancellationSignal(),
                    context.mainExecutor
                ) { location ->
                    if (location != null) {
                        Log.d(TAG, "Location update: (${location.latitude}, ${location.longitude})")
                        callback(Pair(location.latitude, location.longitude))
                    } else {
                        Log.d(TAG, "getCurrentLocation returned null")
                        callback(null)
                    }
                }
            } else {
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        Log.d(TAG, "Location update: (${location.latitude}, ${location.longitude})")
                        lm.removeUpdates(this)
                        callback(Pair(location.latitude, location.longitude))
                    }
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {
                        lm.removeUpdates(this)
                        callback(null)
                    }
                }
                lm.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Location request failed", e)
            callback(null)
        }
    }
}
