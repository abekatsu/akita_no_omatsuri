package com.damburisoft.android.yamalocationsrv.service;

/**
 * Interface
 * 
 */

interface IYamaLogService {

    /**
     * Starts logging a GPS and an azimuth.
     */
    boolean startLogService();
    
    /**
     * Stops logging service.
     */
    void stopLogService();

    /**
     * Return the sensors (GPS, azimuth) are running.
     * @return true if both GPS and azimuth sensors are running, otherwise false.
     */
    boolean isSensorRunning();
    
    /**
     * Return the current azimuth from true north as degree.
     * @return the current azimuth
     */
    double getAzimuth();

    /**
     * Returns the current Location, which GPS sensor being recorded.
     * @return the current Location.
     */
    Location getCurrentLocation();
    
    /**
     * Returns the service is running
     */
    boolean isRunning();

}
