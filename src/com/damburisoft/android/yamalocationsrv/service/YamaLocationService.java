package com.damburisoft.android.yamalocationsrv.service;

import com.damburisoft.android.yamalocationsrv.R;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class YamaLocationService extends Service implements LocationListener {
    private static final String TAG = "YamaLocationService";
    private GeomagneticField mGeomagneticField = null;
    private LocationManager mLocationManager = null;
    private Location curLocation = null;
    @SuppressWarnings("unused")
    private boolean isLocationServiceRunning = false;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class YamaServiceBinder extends Binder {
        public YamaLocationService getService() {
            return YamaLocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void onCreate() {
        Log.d(TAG, "YamaLocationService.onCreate");
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        registerLocationListener();
        
        /**
         * After 5 min, check every minute that location listener still is
         * registered and spit out additional debugging info to the logs:
         */
        
        /*
        timer.schedule(checkLocationListener, 1000 * 60 * 5, 1000 * 60);
        isRecording = true;
        if (mTTSAvailable && (announcementFrequency != -1)) {
          if (executer == null) {
            SafeStatusAnnouncerTask announcer = new SafeStatusAnnouncerTask(this);
            executer = new PeriodicTaskExecuter(announcer, this);
          }
        }
         */
    }


    public void onLocationChanged(Location location) {
        this.curLocation = location;
    }

    public void onProviderDisabled(String provider) {
        Log.d(TAG, "YamaLocationService.onProviderDisabled: " + provider);
    }

    public void onProviderEnabled(String provider) {
        Log.d(TAG, "YamaLocationService.onProviderEnabled: " + provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "YamaLocationService.onStatusChanged: " + provider);
        switch (status) {
        case LocationProvider.OUT_OF_SERVICE:
            Log.d(TAG, "Status: Out of service");
            break;
        case LocationProvider.TEMPORARILY_UNAVAILABLE:
            Log.d(TAG, "Status: Temporarily Available");
            break;
        case LocationProvider.AVAILABLE:
            Log.d(TAG, "Status: Available");
            break;
        default:
            Log.d(TAG, "Unknown Status" + status);
        }

    }
    
    public void startLocationService() {
        Log.d(TAG, "YamaLocationService.startLocationService");
        registerLocationListener();
    }


    public void stopLocationService() {
        Log.d(TAG, "YamaLocationService.stopLocationService");
        unregisterLocationListener();
    }

    public Location getCurrentLocation() {
        return curLocation;
    }

    public GeomagneticField getGeomagneticField() {
        return mGeomagneticField;
    }
    
    private void registerLocationListener() {
        if (mLocationManager == null) {
            Log.e(TAG, "YamaLocationService: No Location Manager.");
            return;
        }
        
        Log.d(TAG, "Preparing to register location listener with YamaLocationService");
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                60 * 1000, 0, this);
        isLocationServiceRunning = true;
        Toast.makeText(this, R.string.yama_location_service_registered, 
                Toast.LENGTH_SHORT).show();

    }
    
    private void unregisterLocationListener() {
        if (mLocationManager == null) {
            Log.e(TAG, "YamaLocationService: No Location Manager.");
            return;
        }
        
        Toast.makeText(this, R.string.yama_location_service_unregistered, 
                Toast.LENGTH_SHORT).show();
        
        mLocationManager.removeUpdates(this);
        isLocationServiceRunning = false;
        Log.d(TAG, "Location Listener now unregistered with YamaLocationService");
    }

}
