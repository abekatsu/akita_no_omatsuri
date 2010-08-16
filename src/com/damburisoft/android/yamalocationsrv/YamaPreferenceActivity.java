package com.damburisoft.android.yamalocationsrv;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class YamaPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

    private static String getPreferencesString(Context context, String key, String defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defValue);
    }

    private static boolean getPreferencesBoolean(Context context, String key, boolean defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, defValue);
    }

    public static String getHikiyamaName(Context context) {
        String defaultHikiyamaName = context.getString(R.string.default_choumei);

        return getPreferencesString(context, "neighborhood_associate", defaultHikiyamaName);
    }


    public static int getHikiyamaID(Context context) {
        String hikiyama = YamaPreferenceActivity.getHikiyamaName(context);
        List<CharSequence> list = Arrays.asList(context.getResources().getTextArray(R.array.neiglist_associate_values));
        return list.indexOf(hikiyama);
    }

    public static boolean isUseLocalServer(Context context) {
        return getPreferencesBoolean(context, "isLocalServer", false);
    }

    public static String getServerURIString(Context context) {
        StringBuffer sb = new StringBuffer();

        if (YamaPreferenceActivity.isUseLocalServer(context)) {
            /**
             *  Expected Result: http://192.168.x.x:8080/location.json";
             */
            sb.append(context.getString(R.string.default_test_local_server));
            sb.append(YamaLocationProviderConstants.jsonPost);
        } else {
            /**
             *  Expected Result: http://labs2.netpersons.co.jp/omatsuri/kakunodate/sugazawa/location.json
             */
            sb.append(YamaLocationProviderConstants.webServer);
            sb.append(YamaLocationProviderConstants.webLocation);
            sb.append(YamaPreferenceActivity.getHikiyamaName(context) + "/");
            sb.append(YamaLocationProviderConstants.jsonPost);
        }
        return sb.toString();
    }

    public static long getPollingInterval(Context context) {
        String pollingIntStr = getPreferencesString(context, "polling_interval", 
                context.getString(R.string.default_polling_interval));
        return Long.parseLong(pollingIntStr);
    }

    public static double getGpsUpdateMinDistance(Context context) {
        String minDistanceStr = getPreferencesString(context, "polling_interval", 
                context.getString(R.string.default_gps_update_minDistance));
        return Double.parseDouble(minDistanceStr);
    }

    public static double getMinRequiredAccuracy(Context context) {
        String minAccuracyStr = getPreferencesString(context,
                "gps_min_required_accuracy", 
                context.getString(R.string.default_gps_min_required_accuracy));
        return Double.parseDouble(minAccuracyStr);
    }

    public static String getOmatsuriName(Context context) {
        return context.getString(R.string.default_omatsuri_name);
    }

    public static String getNickName(Context context) {
        return getPreferencesString(context, "device_nickname", Build.MODEL); 
    }

    public static String getSdcardLogFileName(Context context) {
        return getPreferencesString(context, "sdcard_logname", DateTimeUtilities.getFilenameFromDateAndTime() + ".log");
    }

}
