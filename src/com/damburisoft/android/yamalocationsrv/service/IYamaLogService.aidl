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
     * @return true if both GPS and azimuth sensors are ruuning, otherwise false.
     */
    boolean isMonitoring();

    /**
     * Return the current azimuth from true north as degree.
     * @return the crrent azimuth
     */
    double getAzimuth();

    /**
     * Returns the current Location, which GPS sensor being recorded.
     * @return the currnet Location.
     */
    Location getCurrentLocation();

}
