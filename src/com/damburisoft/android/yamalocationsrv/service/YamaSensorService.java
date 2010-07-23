package com.damburisoft.android.yamalocationsrv.service;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class YamaSensorService extends Service implements SensorEventListener {
    private static final String TAG = "YamaSensorService";
    private SensorManager mSensorManager = null;

    private boolean isMagneticFieldSensorRegistered = false;
    private boolean isAccelerometerSensorRegistered = false;
    
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class YamaSensorServiceBinder extends Binder {
        public YamaSensorService getService() {
            return YamaSensorService.this;
        }
    }
    
    // refer to http://developer.android.com/reference/android/app/Service.html
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new YamaSensorServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "YamaLocationService.onBind");
        return mBinder;
    }
    

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG, "YamaSensorService.onCreate");
        super.onCreate();
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        registerSensorEventListener();
    }
    

    @Override
    public void onDestroy() {
        unregisterSensorEventListener();
        super.onDestroy();
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "YamaSensorService.onAccuracyChanged. sensor: " +
                sensor.getName() + ", accuracy :" + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            setMagneticFieldValues(event.values.clone());
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            setAccelerometerValues(event.values.clone());
        }

    }
    
    private void registerSensorEventListener() {
        if (mSensorManager == null) {
            Log.e(TAG, "No Sensor Manager");
            return ; 
        }
        
        List<Sensor> sensor_list = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensor_list) {
            if (s.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                isMagneticFieldSensorRegistered = mSensorManager.registerListener(this, s, 
                        SensorManager.SENSOR_DELAY_UI);
            } else if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
                isAccelerometerSensorRegistered = mSensorManager.registerListener(this, s, 
                        SensorManager.SENSOR_DELAY_UI);
            }
        }
    }
    
    private void unregisterSensorEventListener() {
        if (mSensorManager == null) {
            Log.e(TAG, "No Sensor Manager");
            return ;
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
    
    public void startSensorService() {
        Log.d(TAG, "YamaSensorService.startSensorService");
        registerSensorEventListener();
    }
    
    public void stopSensorService() {
        Log.d(TAG, "YamaSensorService.stopSensorService");
        unregisterSensorEventListener();
    }


    private void setMagneticFieldValues(float[] magneticFieldValues) {
        this.magneticFieldValues = magneticFieldValues;
    }


    public float[] getMagneticFieldValues() {
        return magneticFieldValues;
    }


    private void setAccelerometerValues(float[] accelerometerValues) {
        this.accelerometerValues = accelerometerValues;
    }


    public float[] getAccelerometerValues() {
        return accelerometerValues;
    }

}
