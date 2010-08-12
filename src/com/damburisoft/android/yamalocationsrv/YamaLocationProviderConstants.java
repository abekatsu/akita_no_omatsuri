package com.damburisoft.android.yamalocationsrv;

public abstract class YamaLocationProviderConstants {

    public static final String logFileName = "YamaLocation.log";
    
    public static final String IsLoggingServiceRunning = "isLoggingServiceRunning";
    
    public static long PollingInterval = 60 * 1000;
    
    public abstract class RedrawType {
        public static final int TextView = 0;
        public static final int MapView  = 1;
    };
    
    public abstract class UpdateInfo {
        public static final int AZIMUTH  = 0;
        public static final int LOCATION = 1;
        public static final int DATETIME = 2;
    };
    
    public static final int defaultZoom = 19;
    
    
    public abstract class LocationService {
        /**
         * Command to the service to register a client, receiving callbacks
         * from the service.  The Message's replyTo field must be a Messenger of
         * the client where callbacks should be sent.
         */
        public static final int MSG_REGISTER_CLIENT = 0;
        /**
         * Command to the service to unregister a client, ot stop receiving callbacks
         * from the service.  The Message's replyTo field must be a Messenger of
         * the client as previously given with MSG_REGISTER_CLIENT.
         */
        public static final int MSG_UNREGISTER_CLIENT = 1;
    };
    
    // Web Server
    
    // public static final String webServer = "192.168.11.101"; // TODO configurable via PreferenceActivity.
    // public static final String webServer = "192.168.1.206"; // TODO configurable via PreferenceActivity.
    public static final String webServer   = "http://labs2.netpersons.co.jp/omatsuri/kakunodate/locations.json";
    public static final int    webPort   = 8080;               // TODO configurable via PreferenceActivity.
    
}

