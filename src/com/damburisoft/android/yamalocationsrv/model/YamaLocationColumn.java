package com.damburisoft.android.yamalocationsrv.model;

import com.damburisoft.android.yamalocationsrv.provider.YamaLocationProvider;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class YamaLocationColumn {

    private static final String TAG = "YamaLocation";
    
    // This class cannot be instantiated
    private YamaLocationColumn() {}
    
    /**
     * @author abekatsu
     *
     */
    public static final class Info implements BaseColumns {
        
        private static final String TAG = YamaLocationColumn.TAG + ".Info";
        
        // This class cannot be instantiated
        private Info() {}
        
        /**
         * The current battery level in integer from 0 to BatteryManager.EXTRA_LEVEL
         */
        public static final String BATTERY_LEVEL = "battery_level";
        
        /**
         * the device nickname
         */
        public static final String NICKNAME = "device_nickname";
        
        
        /**
         * The current heading in double from 0.0 to 360.0.
         */
        public static final String HEADING = "heading";
        
        /**
         * The current heading accuracy in double from -180.0 to 180.0.
         */
        public static final String HEADING_ACCURACY = "heading_accuracy";
        
        /**
         * The current horizontal accuracy in double from -180.0 to 180.0.
         */
        public static final String HORIZONTAL_ACCURACY = "horizontal_accuracy";
        
        /**
         * The current latitude in double from -180.0 to 180.0. 
         */
        public static final String LATITUDE = "latitude";

        /**
         * The current longitude in double from -180.0 to 180.0. 
         */
        public static final String LONGITUDE = "longitude";
        
        /** 
         * The current altitude in double from -180.0 to 180.0. 
         */
        public static final String ALTITUDE = "altitude";
        
        /**
         * The time data this info created, in milliseconds since the epoch.
         */
        public static final String TIMESTAMP = "timestamp";
        
        /**
         * The time has been pushed or not.
         */
        public static final String PUSHED = "pushed";
        
        /**
         * The content:// style URI for all data records of infos.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + YamaLocationProvider.AUTHORITY + "/infos");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of prices.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.damburisoft.yamalocation.info";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single price.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.damburisoft.yamalocation.info";
        /**
         * The paths for ContentProvider. 
         */
        public static final String PATH = "infos";

        /**
         * Check the ContentValues values is valid to insert ContentProvider;
         * @param values
         */
        public static boolean checkInsertValues(ContentValues values) {
            if (!_checkInsertValues(values, BATTERY_LEVEL, "battery level is not included.")) {
                return false;
            }
            
            if (!_checkInsertValues(values, NICKNAME, "device nickname is not included.")) {
                return false;
            }
            
            if (!_checkInsertValues(values, HEADING, "heading is not included.")) {
                return false;
            }
            
            if (!values.containsKey(HEADING_ACCURACY)) {
                values.put(HEADING_ACCURACY, 0.0); 
            }

            if (!values.containsKey(HORIZONTAL_ACCURACY)) {
                values.put(HORIZONTAL_ACCURACY, 0.0); 
            }

            if (!_checkInsertValues(values, LATITUDE, "latitude is not included.")) {
                return false;
            }
            
            if (!_checkInsertValues(values, LONGITUDE, "longitude is not included.")) {
                return false;
            }
            
            if (!_checkInsertValues(values, ALTITUDE, "altitude is not included.")) {
                return false;
            }
            
            if (!values.containsKey(PUSHED)) {
                values.put(PUSHED, false);
            }
            
            return true;
        }
        
        private static boolean _checkInsertValues(ContentValues values, String key, String errmsg) {
            if (!values.containsKey(key)) {
                Log.e(TAG, errmsg);
            }
            return true;
        }

        
    }
}
