package com.entropy.david.openweather2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by david on 2/04/2016.
 */
public class MyLocationListener implements LocationListener {

    public MyLocationListener(){}


    @Override
    public void onLocationChanged(Location loc) {

        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();

        Log.d("GPS", "Coordenadas: latitud: " + latitude + " longitud: " + longitude);


    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}