package com.damburisoft.android.yamalocationsrv.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import com.damburisoft.android.yamalocationsrv.DateTimeUtilities;
import com.damburisoft.android.yamalocationsrv.YamaHttpClient;
import com.damburisoft.android.yamalocationsrv.YamaInfo;
import com.damburisoft.android.yamalocationsrv.YamaLocActivity;
import com.damburisoft.android.yamalocationsrv.YamaLocationProviderConstants;
import com.damburisoft.android.yamalocationsrv.YamaPreferenceActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class YamaLogService extends Service {

    private static final String TAG = "YamaLogService";

    private FileOutputStream mFos = null;
    private Timer mTimer = null;

    private SensorManager mSensorManager = null;
    private LocationManager mLocationManager = null;
    private GeomagneticField mGeomagneticField = null;

    private boolean isMagneticFieldSensorRegistered = false;
    private boolean isAccelerometerSensorRegistered = false;
    private boolean isOrientationSensorRegistered   = false;
    private boolean isLocationListenerRegistered    = false;
    
    private Sensor mMagneticFieldSensor;
    private Sensor mAccelerometerSensor;
    private Sensor mOrientationSensor;

    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private double mCurrentAzimuth = Double.NaN;
    private Location mCurrentLocation = null;
    private double mBatteryLevel = 1.0;
    
    private ArrayBlockingQueue<YamaInfo> mQueue;

    /** The timer posts a runnable to the main thread via this handler. */
    private TimerTask checkSensorValues;
    private boolean ischeckSensorValuesRunning = false;
    private TimerTask checkLocationListener;
    private boolean ischeckLocationListenerRunning = false;
    private TimerTask sendInfoServer; 
    private boolean isSendInfoServerRunning = false;
 
    private WakeLock mWakeLock;
    
    private BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                mBatteryLevel = (double)(level / 100.0);
                Log.d(TAG, "Battery level: " + mBatteryLevel);
            } else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                Log.d(TAG, "Battery Low broadcast received.");
                YamaLocActivity.copyLogToSdCard(YamaLogService.this);
            }
        }
        
    };
    
    private boolean isBatteryBroadcastReceiverRegistered = false;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "YamaLogService.onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "YamaLogService.onCreate");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        acquireWakeLock();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(mBatteryBroadcastReceiver, filter);
        isBatteryBroadcastReceiverRegistered = true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "YamaLogService.onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (isBatteryBroadcastReceiverRegistered) {
            unregisterReceiver(mBatteryBroadcastReceiver);
        }
        unregisterSensorEventListener();
        unregisterLocationListener();
        closeLogFile();
        stopTimerTask();
        super.onDestroy();
    }

    private final LocationListener myLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            Log.d(TAG, "myLocationListener.onLocationChanged()");
            mCurrentLocation = location;

            float latitude = new Double(mCurrentLocation.getLatitude()).floatValue();
            float longitude = new Double(mCurrentLocation.getLongitude()).floatValue();
            float altitude = new Double(mCurrentLocation.getAltitude()).floatValue();
            mGeomagneticField = new GeomagneticField(latitude, longitude,
                    altitude, new Date().getTime());
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

    };

    private final SensorEventListener mySensorEventListener = new SensorEventListener() {

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

    };

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
                        .registerListener(mySensorEventListener, s,
                                SensorManager.SENSOR_DELAY_UI);
                mMagneticFieldSensor = s;
            } else if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
                isAccelerometerSensorRegistered = mSensorManager
                        .registerListener(mySensorEventListener, s,
                                SensorManager.SENSOR_DELAY_UI);
                mAccelerometerSensor = s;
            } else if (s.getType() == Sensor.TYPE_ORIENTATION) {
                isOrientationSensorRegistered = mSensorManager
                        .registerListener(mySensorEventListener, s,
                                SensorManager.SENSOR_DELAY_UI);
                mOrientationSensor = s;
            }
        }

        if (isMagneticFieldSensorRegistered == false
                || isAccelerometerSensorRegistered == false
                || isOrientationSensorRegistered == false) {
            unregisterSensorEventListener();
            return false;
        }

        return true;
    }

    private void unregisterSensorEventListener() {
        Log.d(TAG, "YamaLogService.unregisterSensorEventListener()");
        if (mSensorManager == null) {
            Log.e(TAG, "No Sensor Manager");
            return;
        }
        
        if (isMagneticFieldSensorRegistered) {
            mSensorManager.unregisterListener(mySensorEventListener, mMagneticFieldSensor);
            mMagneticFieldSensor = null;
            isMagneticFieldSensorRegistered = false;
        }

        if (isAccelerometerSensorRegistered) {
            mSensorManager.unregisterListener(mySensorEventListener, mAccelerometerSensor);
            mAccelerometerSensor = null;
            isAccelerometerSensorRegistered = false;
        }
        
        if (isOrientationSensorRegistered) {
            mSensorManager.unregisterListener(mySensorEventListener, mOrientationSensor);
            mOrientationSensor = null;
            isOrientationSensorRegistered = false;
        }
    }

    private boolean registerLocationListener() {
        if (mLocationManager == null) {
            Log.e(TAG, "YamaLocationService: No Location Manager.");
            return false;
        }

        Log.d(TAG,
                "Preparing to register location listener with YamaLogService");

        HandlerThread th = new HandlerThread("GPS Thread");
        th.start();
        new Handler(th.getLooper()).post(new Runnable() {
            public void run() {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 
                        YamaPreferenceActivity.getPollingInterval((Context)YamaLogService.this), 
                        (float)YamaPreferenceActivity.getGpsUpdateMinDistance((Context)YamaLogService.this),
                        myLocationListener);
            }

        });
        isLocationListenerRegistered = true;
        return true;
    }

    private void unregisterLocationListener() {
        Log.d(TAG, "YamaLogService.unregisterLocationListener()");
        if (mLocationManager == null) {
            Log.e(TAG, "YamaLogService: No Location Manager.");
            return;
        }

        if (isLocationListenerRegistered) {
            mLocationManager.removeUpdates(myLocationListener);
            isLocationListenerRegistered = false;
        }

    }
    
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(mBatteryBroadcastReceiver, filter);
        isBatteryBroadcastReceiverRegistered = true;
    }
    
    private void unregisterBroadcastReceiver() {
        if (isBatteryBroadcastReceiverRegistered) {
            unregisterReceiver(mBatteryBroadcastReceiver);
            isBatteryBroadcastReceiverRegistered = false;
        }
    }

    /**
     * Tries to acquire a partial wake lock if not already acquired. Logs errors
     * and gives up trying in case the wake lock cannot be acquired.
     */
    public void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            Log.e(TAG, "No Pawer Management found.");
            return;
        }

        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "WakeLock of YamaLogService");
            if (mWakeLock == null) {
                Log.e(TAG, "Could not create wake lock (null).");
                return;
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
            Log.d(TAG, "IYamaLogService.Stub.startLogService");
            registerBroadcastReceiver();
            registerSensorEventListener();
            registerLocationListener();
            openLogFile();
            createTimerTask();

            mQueue = new ArrayBlockingQueue<YamaInfo>(100);
            long pollingInterval = YamaPreferenceActivity.getPollingInterval((Context)YamaLogService.this);
            mTimer = new Timer();
            mTimer.schedule(checkSensorValues, 0, pollingInterval);
            ischeckSensorValuesRunning = true;
            
            /*
             * after 5 minutes later, starts a task to re-register LocationListener. 
             */
            mTimer.schedule(checkLocationListener,  5 * 60 * 1000 + (pollingInterval / 2), pollingInterval);
            ischeckLocationListenerRunning = true;
            
            long pushingInterval = YamaPreferenceActivity.getPushingInterval((Context)YamaLogService.this);
            mTimer.schedule(sendInfoServer, pushingInterval / 2, pushingInterval);
            isSendInfoServerRunning = true;

            return true;
        }

        public void stopLogService() throws RemoteException {
            Log.d(TAG, "IYamaLogService.Stub.stopLogService");
            unregisterBroadcastReceiver();
            unregisterSensorEventListener();
            unregisterLocationListener();
            closeLogFile();
            stopTimerTask();
        }

        public boolean isSensorRunning() throws RemoteException {
            return ischeckSensorValuesRunning;
        }

        public Location getCurrentLocation() throws RemoteException {
            return mCurrentLocation;
        }

        public double getAzimuth() throws RemoteException {
            return mCurrentAzimuth;
        }

        public boolean isRunning() {
            return ischeckSensorValuesRunning;
        }

    };

    private void createTimerTask() {
        
        if (ischeckLocationListenerRunning) {
            checkLocationListener.cancel();
            checkLocationListener = null;
            ischeckLocationListenerRunning = false;
        }

        checkLocationListener = new TimerTask() {

            @Override
            public void run() {
                unregisterLocationListener();
                registerLocationListener();
            }

        };
        
        if (isSendInfoServerRunning) {
            sendInfoServer.cancel();
            sendInfoServer = null;
            isSendInfoServerRunning = false;
        }
        
        sendInfoServer = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "sendInfoServer.run");
                Log.d(TAG, "mQueue.isEmpty()? " + mQueue.isEmpty());
                while (!mQueue.isEmpty()) {
                    YamaInfo info = mQueue.poll();
                    if (info == null) {
                        break;
                    }

                    try {
                        YamaHttpClient httpClient = new YamaHttpClient((Context)YamaLogService.this, info); 
                        Thread th = new Thread(httpClient);
                        th.start();
                        Thread.sleep(50); // Wait 50 ms here
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    Log.d(TAG, "mQueue.isEmpty()? " + mQueue.isEmpty());
                }
            }
        };

        if (ischeckSensorValuesRunning) {
            checkSensorValues.cancel();
            checkSensorValues = null;
            ischeckSensorValuesRunning = false;
        }

        checkSensorValues = new TimerTask() {

            private long mPreviousGPSTime = -1;

            @Override
            public void run() {
                long currentDateTime = (new Date()).getTime();
                
                // Anyway, write location and azimuth information to log file.
                if (mCurrentLocation == null) {
                    Log.d(TAG, "Location has not been settled by GPS.");
                    mCurrentLocation = mLocationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                mCurrentAzimuth = calcurateAzimuth();
                if (mCurrentAzimuth == Double.NaN) {
                    Log.d(TAG, "cannot obtain azimuth.");
                    return;
                }
                
                String logString = createLogInfo(currentDateTime);
                try {
                    mFos.write(logString.getBytes());
                    YamaLocActivity.copyLogToSdCard(YamaLogService.this);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
                
                double minRequiredAccuragy = YamaPreferenceActivity.getMinRequiredAccuracy((Context)YamaLogService.this);
                if (mPreviousGPSTime >= mCurrentLocation.getTime()) {
                    // If a time-stamp of GPS is not newer of previous one, 
                    // then it is not inserted to queue for pushing.
                    Log.d(TAG,
                            "GPS is not updated after previous pushing data. So we doesn't push data now.");
                    // TODO return ;
                } else if (mCurrentLocation.getAccuracy() >= (float)minRequiredAccuragy) {
                    // If GPS info doesn't have enough accuracy, then it is not inserted to queue for pushing.
                    Log.d(TAG, "Current accuracy is more than required accuracy. So use the previous value.");
                    // TODO return ;
                }
                
                // insert YamaInfo to queue.
                String hikiyama = YamaPreferenceActivity.getHikiyamaName(YamaLogService.this);
                String omomatsuri = YamaPreferenceActivity.getOmatsuriName(YamaLogService.this);
                YamaInfo info = new YamaInfo(omomatsuri, hikiyama, currentDateTime, 
                        mCurrentAzimuth, mCurrentLocation, mBatteryLevel);
                try {
                    mQueue.put(info);
                } catch (InterruptedException e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
                mPreviousGPSTime = mCurrentLocation.getTime();
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

            private String createLogInfo(long currentDateTime) {
                StringBuffer sb = new StringBuffer();
                sb.append(DateTimeUtilities.getDateAndTime(currentDateTime));
                sb.append(",");
                sb.append((float) mCurrentLocation.getLongitude());
                sb.append(",");
                sb.append((float) mCurrentLocation.getLatitude());
                sb.append(",");
                sb.append((float) mCurrentLocation.getAccuracy());
                sb.append(",");
                sb.append((float) mCurrentAzimuth);
                sb.append(",");
                sb.append((float) 0.0);
                sb.append(",");
                sb.append((float) mBatteryLevel);
                sb.append("\n");

                return sb.toString();
            }
        };

    }

    private void stopTimerTask() {
        if (ischeckLocationListenerRunning) {
            checkLocationListener.cancel();
            checkLocationListener = null;
        }
        ischeckLocationListenerRunning = false;
        
        if (isSendInfoServerRunning) {
            sendInfoServer.cancel();
            sendInfoServer = null;
        }
        isSendInfoServerRunning = false;

        if (checkSensorValues != null) {
            checkSensorValues.cancel();
            checkSensorValues = null;
        }
        ischeckSensorValuesRunning = false;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void openLogFile() {
        try {
            mFos = openFileOutput(YamaLocationProviderConstants.logFileName,
                    MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "YamaLogService.openFileOutput: ", e);
        }
    }

    private void closeLogFile() {
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
    }

}
