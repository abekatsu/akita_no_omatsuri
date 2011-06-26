package com.damburisoft.android.yamalocationsrv.service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.damburisoft.android.yamalocationsrv.YamaLocHttpClient;
import com.damburisoft.android.yamalocationsrv.YamaPreferenceActivity;
import com.damburisoft.android.yamalocationsrv.model.YamaInfo;
import com.damburisoft.android.yamalocationsrv.model.YamaLocationColumn;
import com.damburisoft.android.yamalocationsrv.provider.YamaLocationProvider;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service implements SensorEventListener, LocationListener {
    
    private static final String TAG = "LocationService";
    private SensorManager mSensorManager = null;
    private LocationManager mLocationManager = null;
    private boolean isLocationListenerRegistered    = false;
    private HashMap<Integer, Sensor> mSensors;
    
    private Location mCurrentLocation;
    private int mLocationProviderStatus;
    private GeomagneticField mGeomagneticField = null;
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private double mBatteryLevel = 1.0;
    
    private boolean debug = true;

    /** The timer posts a runnable to the main thread via this handler. */
    private Timer mTimer = null;
    private TimerTask collectInfoTimer;
    private boolean isCollectInfoTimerRunning = false;
    private TimerTask sendInfoTimer;
    private boolean isSendInfoTimerRunning = false;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSensors = new HashMap<Integer, Sensor>();
        mTimer = new Timer();
        super.onCreate();
        
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        
        startManagers();
        startTimerTasks();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        stopManagers();
        return super.onUnbind(intent);

    }
    
    private void startManagers() {
        startSensorManager(mSensorManager);
        startLocationManager(mLocationManager);
    }
    
    private void startSensorManager(SensorManager manager) {
        // TODO Auto-generated method stub
        if (manager == null) {
            Log.e(TAG, "given sensor manager is null");
            return;
        }
        // manager
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
        
        for (Sensor s : sensors) {
            if (s.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                if (mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)) {
                    mSensors.put(Sensor.TYPE_MAGNETIC_FIELD, s);
                }
            } else if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)) {
                    mSensors.put(Sensor.TYPE_ACCELEROMETER, s);
                }
            } else if (s.getType() == Sensor.TYPE_ORIENTATION) {
                if (mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)) {
                    mSensors.put(Sensor.TYPE_ORIENTATION, s);
                }
            }
        }
        
    }
    
    private void startLocationManager(LocationManager manager) {
        if (manager == null) {
            Log.e(TAG, "given location manager is null");
            return ;
        }
        
        long minTime = YamaPreferenceActivity.getPollingInterval(this);
        double minDistance = YamaPreferenceActivity.getGpsUpdateMinDistance(this);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // TODO show warning message.
            Log.d(TAG, "GPS Provider is disabled");
        } else {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, (float)minDistance, this);
            isLocationListenerRegistered = true;
        }

    }
    
    private void startTimerTasks() {
        if (isCollectInfoTimerRunning) {
            if (collectInfoTimer != null) {
                collectInfoTimer.cancel();
            }
            isCollectInfoTimerRunning = false;
        }
        
        if (isSendInfoTimerRunning) {
            if (sendInfoTimer != null) {
                sendInfoTimer.cancel();
            }
            isCollectInfoTimerRunning = false;
        }
        
        collectInfoTimer = new TimerTask() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mCurrentLocation == null) {
                    mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                if (mCurrentLocation == null) {
                    return;
                }
                // Store Info.
                ContentValues values = new ContentValues();
                values.put(YamaLocationColumn.Info.BATTERY_LEVEL, 0.99); // TODO obtain battery level.
                values.put(YamaLocationColumn.Info.HEADING, calcurateAzumith(mCurrentLocation));
                values.put(YamaLocationColumn.Info.HEADING_ACCURACY, 0.0);
                values.put(YamaLocationColumn.Info.HORIZONTAL_ACCURACY, mCurrentLocation.getAccuracy());
                values.put(YamaLocationColumn.Info.LATITUDE, mCurrentLocation.getLatitude());
                values.put(YamaLocationColumn.Info.LONGITUDE, mCurrentLocation.getLongitude());
                values.put(YamaLocationColumn.Info.ALTITUDE, mCurrentLocation.getAltitude());
                values.put(YamaLocationColumn.Info.PUSHED, false);

                final ContentResolver resolver = getContentResolver();
                resolver.insert(YamaLocationColumn.Info.CONTENT_URI, values);
                
            }
        };
        long pollingInterval = YamaPreferenceActivity.getPollingInterval(LocationService.this);
        mTimer.schedule(collectInfoTimer, 0, pollingInterval);
        isCollectInfoTimerRunning = true;
        
        sendInfoTimer = new TimerTask() {
            
            @Override
            public void run() {
                String server = YamaPreferenceActivity.getServerURIString(LocationService.this);
                YamaLocHttpClient client = new YamaLocHttpClient(LocationService.this, server);
                
                List<YamaInfo> infoList = client.getYamaInfo(false);
                for (YamaInfo info : infoList) {
                    if (client.pushLocation(info)) {
                        info.changePushed(LocationService.this);
                    }
                }
                
            }
            
        };
        long pushingInterval = YamaPreferenceActivity.getPushingInterval(LocationService.this);
        mTimer.schedule(sendInfoTimer, 0, pushingInterval);
        isSendInfoTimerRunning = true;
        
    }

    private void stopManagers() {
        stopSensorManager(mSensorManager);
        stopLocationManager(mLocationManager);
    }

    private void stopSensorManager(SensorManager manager) {
        if (manager == null) {
            Log.e(TAG, "given sensor manager is null");
        }
        Set<Integer> sensor_set = new HashSet<Integer>(mSensors.keySet());
        for (Integer sensor_type : sensor_set) {
            manager.unregisterListener(this, mSensors.get(sensor_type));
            mSensors.remove(sensor_type);
        }
    }
    
    private void stopLocationManager(LocationManager manager) {
        if (manager == null) {
            Log.e(TAG, "given location manager is null");
        }
        
        if (isLocationListenerRegistered) {
            mLocationManager.removeUpdates(this);
            isLocationListenerRegistered = false;
        }
    }
    
    private void stopTimerTasks() {
        if (isCollectInfoTimerRunning) {
            if (collectInfoTimer != null) {
                collectInfoTimer.cancel();
            }
            isCollectInfoTimerRunning = false;
        }
        
        if (isSendInfoTimerRunning) {
            if (sendInfoTimer != null) {
                sendInfoTimer.cancel();
            }
            isSendInfoTimerRunning = false;
        }

    }

    
    /*
     * (non-Javadoc)
     * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
     */

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (debug) {
            Log.d(TAG, "onAccuracyChanged(): sensor: " + sensor.getName() + ", accuracy: " + accuracy);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
     */

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        }
        
    }

    public void onLocationChanged(Location location) {
        if (debug) {
            Log.d(TAG, "onLocationChanged(): " + location.toString());
        }
        
        mCurrentLocation = location;
        
        float latitude = new Double(location.getLatitude()).floatValue();
        float longitude = new Double(location.getLongitude()).floatValue();
        float altitude = new Double(location.getAltitude()).floatValue();
        mGeomagneticField = new GeomagneticField(latitude, longitude, altitude, new Date().getTime());
    }

    public void onProviderDisabled(String provider) {
        if (debug) {
            Log.d(TAG, "onProviderDisabled:" + provider);
        }
        
//        if (provider.equals((LocationManager.GPS_PROVIDER)) {
//
//            stopLocationManager(mLocationManager);
//
//            Context c = getApplicationContext();
//            if (c != null) {
//                Looper.prepare();
//                Toast.makeText(getApplicationContext(), "Location Listener is registered", Toast.LENGTH_LONG);
//                Looper.loop();
//            }
//
//            startLocationManager(mLocationManager);
//        }
        
    }

    public void onProviderEnabled(String provider) {
        if (debug) {
            Log.d(TAG, "onProviderEnabled(): " + provider);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        int lastStatus = mLocationProviderStatus;
        switch (status) {
        case LocationProvider.OUT_OF_SERVICE:
            Log.d(TAG, "Status: Out of service");
            if (lastStatus == LocationProvider.AVAILABLE) {
                // TODO 
                Log.d(TAG, "Status: Available to Out of service");
            }
            break;
        case LocationProvider.TEMPORARILY_UNAVAILABLE:
            if (debug) {
                Log.d(TAG, "Status: Temporarily Unavailable");
            }
            if (lastStatus == LocationProvider.AVAILABLE) {
                // TODO 
                Log.d(TAG, "Status: Available to Temporarily Unavailable");
            }
            break;
        case LocationProvider.AVAILABLE:
            Log.d(TAG, "Status: Available");
            break;
        default:
            Log.d(TAG, "Unknown Status" + status);
        }
        mLocationProviderStatus = status;
        
    }
    
    public double calcurateAzumith(Location loc) {
        final int MATRIX_SIZE = 16;
        float[] inR = new float[MATRIX_SIZE];
        float[] outR = new float[MATRIX_SIZE];
        float[] I = new float[MATRIX_SIZE];
        float[] orientationValues = new float[3];

        if (magneticFieldValues == null || accelerometerValues == null) {
            return Double.NaN;
        }

        SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticFieldValues);
        SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
        SensorManager.getOrientation(outR, orientationValues);

        double azimuthDegree = Math.toDegrees(orientationValues[0]);
        if (mGeomagneticField != null) {
            azimuthDegree += mGeomagneticField.getDeclination();
        }
        
        while (azimuthDegree < 0.0) {
            azimuthDegree += 360.0;
        }

        return azimuthDegree;
    }

}
