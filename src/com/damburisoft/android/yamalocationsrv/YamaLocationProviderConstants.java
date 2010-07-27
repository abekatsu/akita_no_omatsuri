package com.damburisoft.android.yamalocationsrv;

public abstract class YamaLocationProviderConstants {

    public static final String logFileName = "YamaLocation.log";
    
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
    
}
