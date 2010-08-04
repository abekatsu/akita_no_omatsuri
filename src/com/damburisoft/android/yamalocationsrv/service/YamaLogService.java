package com.damburisoft.android.yamalocationsrv.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.damburisoft.android.yamalocationsrv.DateTimeUtilities;
import com.damburisoft.android.yamalocationsrv.YamaLocationProviderConstants;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class YamaLogService extends Service implements LocationListener,
        SensorEventListener {

    private static final String TAG = "YamaLogService";

    private FileOutputStream mFos = null;
    private Timer mTimer = null;

    private SensorManager mSensorManager = null;
    private LocationManager mLocationManager = null;
    private GeomagneticField mGeomagneticField = null;

    private boolean isOnCreateCalled = false;
    private boolean isMagneticFieldSensorRegistered = false;
    private boolean isAccelerometerSensorRegistered = false;
    private boolean isLocationListenerRegistered = false;

    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private double mCurrentAzimuth = Double.NaN;
    private Location mCurrentLocation = null;

    /** The timer posts a runnable to the main thread via this handler. */
    private final Handler handler = new Handler();

    private TimerTask checkSensorValues = new TimerTask() {

        @Override
        public void run() {
            mCurrentAzimuth = calcurateAzimuth();
            if (mCurrentAzimuth == Double.NaN) {
                Log.d(TAG, "cannot obtain azimuth.");
                return;
            }

            if (mCurrentLocation == null) {
                Log.d(TAG, "Location has not been settled by GPS.");
                return;
            }

            String logString = createLogInfo();
            try {
                mFos.write(logString.getBytes());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }

        }

        private double calcurateAzimuth() {
            final int MATRIX_SIZE = 16;
            float[] inR = new float[MATRIX_SIZE];
            float[] outR = new float[MATRIX_SIZE];
            float[] I = new float[MATRIX_SIZE];
            float[] orientationValues = new float[3];

            if (magneticFieldValues == null || accelerometerValues == null) {
                return Double.NaN;
            }

            SensorManager.getRotationMatrix(inR, I, accelerometerValues,
                    magneticFieldValues);
            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X,
                    SensorManager.AXIS_Y, outR);
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

        private String createLogInfo() {
            StringBuffer sb = new StringBuffer();
            sb.append(DateTimeUtilities.getDateAndTime());
            sb.append((float) mCurrentLocation.getLongitude());
            sb.append(",");
            sb.append((float) mCurrentLocation.getLatitude());
            sb.append(",");
            sb.append((float) mCurrentLocation.getAccuracy());
            sb.append(",");
            sb.append((float) mCurrentAzimuth);
            sb.append("\n");

            return sb.toString();
        }
    };

    private boolean ischeckSensorValuesRunning = false;

    /**
     * Task invoked by a timer periodically to make sure the location listener
     * is still registered.
     */
    private TimerTask checkLocationListener = new TimerTask() {

        @Override
        public void run() {
            if (!isOnCreateCalled) {
                Log.e(TAG,
                        "TrackRecordingService is running, but onCreate not called.");
            }

            if (isLocationListenerRegistered) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG,
                                "Re-registering location listener with YamLogService.");
                        unregisterLocationListener();
                        registerLocationListener();
                    }
                });
            } else {
                Log.w(TAG,
                        "Track recording service is paused. That should not be.");
            }
        }
    };

    private boolean ischeckLocationListenerRunning = false;

    private WakeLock mWakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "YamaLogService.onBind");
        // return mBinder;
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "YamaLogService.onCreate");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerSensorEventListener();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        registerLocationListener();
        
        acquireWakeLock();

        isOnCreateCalled = true;

    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        unregisterSensorEventListener();
        unregisterLocationListener();
        super.onDestroy();
    }

    
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        float latitude = new Double(location.getLatitude()).floatValue();
        float longitude = new Double(location.getLongitude()).floatValue();
        float altitude = new Double(location.getAltitude()).floatValue();
        mGeomagneticField = new GeomagneticField(latitude, longitude, altitude,
                new Date().getTime());
    }

    public void onProviderDisabled(String provider) {
        Log.d(TAG, "YamaLogService.onProviderDisabled: " + provider);
    }

    public void onProviderEnabled(String provider) {
        Log.d(TAG, "YamaLogService.onProviderEnabled: " + provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "YamaLogService.onStatusChanged: " + provider);
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

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*
         * Log.d(TAG, "YamaLogService.onAccuracyChanged. sensor: " +
         * sensor.getName() + ", accuracy :" + accuracy);
         */
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        }
    }
    
    

    @Override
    public ComponentName startService(Intent service) {
        Log.d(TAG, "YamaLogService.startService");
        return super.startService(service);
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "YamaLogService.stopService");
        return super.stopService(name);
    }

    private boolean registerSensorEventListener() {
        if (mSensorManager == null) {
            Log.e(TAG, "No Sensor Manager");
            return false;
        }

        List<Sensor> sensor_list = mSensorManager
                .getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensor_list) {
            if (s.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                isMagneticFieldSensorRegistered = mSensorManager
                        .registerListener(this, s,
                                SensorManager.SENSOR_DELAY_UI);
            } else if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
                isAccelerometerSensorRegistered = mSensorManager
                        .registerListener(this, s,
                                SensorManager.SENSOR_DELAY_UI);
            }
        }

        if (isMagneticFieldSensorRegistered == false
                || isAccelerometerSensorRegistered == false) {
            unregisterSensorEventListener();
            return false;
        }

        return true;
    }

    private void unregisterSensorEventListener() {
        if (mSensorManager == null) {
            Log.e(TAG, "No Sensor Manager");
            return;
        }

        if (isMagneticFieldSensorRegistered) {
            mSensorManager.unregisterListener(this);
            isMagneticFieldSensorRegistered = false;
        }

        if (isAccelerometerSensorRegistered) {
            mSensorManager.unregisterListener(this);
            isAccelerometerSensorRegistered = false;
        }
    }

    private boolean registerLocationListener() {
        if (mLocationManager == null) {
            Log.e(TAG, "YamaLocationService: No Location Manager.");
            return false;
        }

        Log.d(TAG,
                "Preparing to register location listener with YamaLogService");
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                60 * 1000, 0, this);
        isLocationListenerRegistered = true;
        return true;
    }

    private void unregisterLocationListener() {
        if (mLocationManager == null) {
            Log.e(TAG, "YamaLogService: No Location Manager.");
            return;
        }

        if (isLocationListenerRegistered) {
            mLocationManager.removeUpdates(this);
            isLocationListenerRegistered = false;
        }

    }


    public double getAzimuth() {
        return mCurrentAzimuth;
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * Tries to acquire a partial wake lock if not already acquired. Logs errors
     * and gives up trying in case the wake lock cannot be acquired.
     */
    public void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            Log.e(TAG, "No Pawer Management found.");
            return ;
        }
        
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock of YamaLogService");
            if (mWakeLock == null) {
                Log.e(TAG, "Could not create wake lock (null).");
                return ;
            }
        }
        
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            if (!mWakeLock.isHeld()) {
                Log.e(TAG, "Could not acquire wake lock.");
            }
        }
    }
    
    /**
     * The IYamaLogService is defined through IDL.
     */

    private final IYamaLogService.Stub binder = new IYamaLogService.Stub() {

        public boolean startLogService() throws RemoteException {
            boolean retValue = false;
            Log.d(TAG, "IYamaLogService.Stub.startLogService");
            retValue = registerSensorEventListener();

            if (!retValue) {
                return false;
            }

            retValue = registerLocationListener();

            try {
                mFos = openFileOutput(
                        YamaLocationProviderConstants.logFileName, MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                retValue = false;
                e.printStackTrace();
            }

            mTimer = new Timer();

            if (ischeckSensorValuesRunning) {
                checkSensorValues.cancel();
                ischeckSensorValuesRunning = false;
            }
            mTimer.schedule(checkSensorValues, 0, 10 * 1000);
            ischeckSensorValuesRunning = true;

            /** 
             * After 2 min, check every minute that location listener still is
             * registered and spit out additional debugging info to the logs:
             */
            // TODO determinate the interval for effective battery life.
            if (ischeckLocationListenerRunning) {
                checkLocationListener.cancel();
                ischeckLocationListenerRunning = false;
            }
            mTimer.schedule(checkLocationListener, 1000 * 60 * 2, 1000 * 60);
            ischeckLocationListenerRunning = true;

            return retValue;
        }

        public void stopLogService() throws RemoteException {
            // TODO Auto-generated method stub
            Log.d(TAG, "YamaLogService.stopLogService");
            unregisterSensorEventListener();
            unregisterLocationListener();

            try {
                if (mFos != null) {
                    mFos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            } finally {
                mFos = null;
            }

            checkSensorValues.cancel();
            ischeckSensorValuesRunning = false;
            checkLocationListener.cancel();
            ischeckLocationListenerRunning = false;

            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }

        public boolean isMonitoring() throws RemoteException {
            boolean retvalue = false;
            if (ischeckLocationListenerRunning && ischeckSensorValuesRunning) {
                retvalue = true;
            }
            return retvalue;
        }

        public Location getCurrentLocation() throws RemoteException {
            return mCurrentLocation;
        }

        public double getAzimuth() throws RemoteException {
            return mCurrentAzimuth;
        }

    };

}
