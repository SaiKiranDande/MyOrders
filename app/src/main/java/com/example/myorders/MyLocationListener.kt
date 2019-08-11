package com.example.myorders

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import java.io.IOException
import java.util.*

class MyLocationListener constructor(val context: Context, val listner: MyLocationListener.SetonLocationListner) :
    LocationListener {

    override fun onLocationChanged(loc: Location?) {
        /*------- To get city name from coordinates -------- */
        var cityName: String? = null
        var counteryName: String? = null
        var stateName: String? = null
        val gcd = Geocoder(context.applicationContext, Locale.getDefault());
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(loc!!.latitude, loc.longitude, 1)

            if (addresses.isNotEmpty()) {
                System.out.println(addresses[0].locality)
                cityName = addresses[0].locality
                counteryName = addresses[0].countryName
                stateName = addresses[0].adminArea
                listner.onSetLocation(cityName, stateName)
            }
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    interface SetonLocationListner {
        fun onSetLocation(city: String, state: String)
    }
}