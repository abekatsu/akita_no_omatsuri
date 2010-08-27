package com.damburisoft.android.yamalocationsrv;

import android.location.Location;

public class YamaInfo {

    private String mOmomatsuri;
    private String mHikiyama;
    private long mTime;
    private double mAzimuth;
    private Location mLocation;
    private double mBatterylevel;

    public YamaInfo(String omomatsuri, String hikiyama, long time,
            double azimuth, Location location, double batterylevel) {
        mOmomatsuri   = omomatsuri;
        mHikiyama     = hikiyama;
        mTime         = time;
        mAzimuth      = azimuth;
        mLocation     = location;
        mBatterylevel = batterylevel;
        
    }

    public long getTime() {
        return mTime;
    }

    public double getAzimuth() {
        return mAzimuth;
    }

    public Location getLocation() {
        return mLocation;
    }

    public double getBatteryLevel() {
        return mBatterylevel;
    }

    public String getHikiyama() {
        return mHikiyama;
    }

    public String getOmatsuri() {
        return mOmomatsuri;
    }

}
