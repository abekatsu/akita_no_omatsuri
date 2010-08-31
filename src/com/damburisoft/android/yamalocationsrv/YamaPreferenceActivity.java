package com.damburisoft.android.yamalocationsrv;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class YamaPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "YamaPreferenceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setOmatsuriSummary(settings);
        setHikiyamaSummary(settings);
        setMinDistanceSummary(settings);
        setMinRequiredAccuracySummary(settings);
        setPollingIntervalSummary(settings);
        setPushingIntervalSummary(settings);
        setDeviceNicknameSummary(settings);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        Log.d(TAG, "onSharedPreferenceChanged with key: " + key);
        if (key.equals(getString(R.string.omatsuri_name_key)) 
                || key.equals(getString(R.string.hikiyama_key))) {
            setOmatsuriSummary(sharedPreferences);
            setHikiyamaSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.gps_update_minDistance_key))) {        
            setMinDistanceSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.gps_min_required_accuracy_key))) {
            setMinRequiredAccuracySummary(sharedPreferences);
        } else if (key.equals(getString(R.string.polling_interval_key))) {
            setPollingIntervalSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.pushing_interval_key))) {
            setPushingIntervalSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.device_nickname_key))) {
            setDeviceNicknameSummary(sharedPreferences);
        }

    }

    private void setOmatsuriSummary(SharedPreferences settings) {
        String key = getString(R.string.omatsuri_name_key);
        String value = getPreferencesString(this, key, getString(R.string.default_omatsuri_name));
        String summary;

        if (!settings.contains(key)) {
            summary = getString(R.string.omatsuri_name_summary);
        } else {
            summary = getEntryFromList(key, value, R.array.omatsuri_name_values, 
                    R.array.omatsuri_name_entries, settings);            
        }
        
        Preference pref = findPreference(key);
        pref.setSummary(summary);
    }
    
    private void setHikiyamaSummary(SharedPreferences settings) {
        String key = getString(R.string.hikiyama_key);
        String value = getPreferencesString(this, key, getString(R.string.default_hikiyama));
        String summary;
        
        if (!settings.contains(key)) {
            if (!settings.contains(getString(R.string.omatsuri_name_key))) {
                summary = getString(R.string.hikiyama_summary);
            } else {
                summary = getString(R.string.hikiyama_summary_2nd);
            }
        } else {
            summary = getEntryFromList(key, value, R.array.hikiyama_values, R.array.hikiyama_entries, settings);
        }

        Preference pref = findPreference(key);
        pref.setSummary(summary);
    }
    
    private void setMinDistanceSummary(SharedPreferences settings) {
        String key = getString(R.string.gps_update_minDistance_key);
        String value = getPreferencesString(this, key, getString(R.string.default_gps_update_minDistance));
        String summary = getEntryFromList(key, value, R.array.gps_update_minDistance_values, 
                R.array.gps_update_minDistance_entries, settings);
        Preference pref = findPreference(key);
        pref.setSummary(summary);

    }
    
    private void setMinRequiredAccuracySummary(SharedPreferences settings) {
        String key = getString(R.string.gps_min_required_accuracy_key);
        String value = getPreferencesString(this, key, getString(R.string.default_gps_min_required_accuracy));
        String summary = getEntryFromList(key, value, R.array.gps_min_required_accuracy_values,
                R.array.gps_min_required_accuracy_entries, settings);
        Preference pref = findPreference(key);
        pref.setSummary(summary);
    }
    
    private void setPollingIntervalSummary(SharedPreferences settings) {
        String key = getString(R.string.polling_interval_key);
        String value = getPreferencesString(this, key, getString(R.string.default_polling_interval));
        String summary = getEntryFromList(key, value, R.array.polling_interval_values,
                R.array.polling_interval_entries, settings);
        Preference pref = findPreference(key);
        pref.setSummary(summary);
    }
    
    private void setPushingIntervalSummary(SharedPreferences settings) {
        String key = getString(R.string.pushing_interval_key);
        String value = getPreferencesString(this, key, getString(R.string.default_pushing_interval));
        String summary = getEntryFromList(key, value, R.array.pushing_interval_values,
                R.array.pushing_interval_entries, settings);
        Preference pref = findPreference(key);
        pref.setSummary(summary);
    }
    
    private void setDeviceNicknameSummary(SharedPreferences settings) {
        String key = getString(R.string.device_nickname_key);
        String value = getPreferencesString(this, key, Build.MODEL); 
        Preference pref = findPreference(key);
        pref.setSummary(value);
    }
    
    private String getEntryFromList(String key, String value, int values_id, int entries_id,
            SharedPreferences settings) {
        return getEntryFromList(key, value, values_id, entries_id, settings, ""); 
    }

    private String getEntryFromList(String key, String value, int values_id, int entries_id, 
            SharedPreferences settings, 
            String suffix_str) {
        StringBuffer sb = new StringBuffer();
        List<String> values = Arrays.asList(getResources().getStringArray(values_id));
        List<String> entries = Arrays.asList(getResources().getStringArray(entries_id));
        try {
            sb.append(entries.get(values.indexOf(value)));
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
        }
        sb.append(" ");
        sb.append(suffix_str);
        return sb.toString();
    }

    private static String getPreferencesString(Context context, String key, String defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defValue);
    }

    private static boolean getPreferencesBoolean(Context context, String key, boolean defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, defValue);
    }

    public static String getOmatsuriName(Context context) {
        String defaultOmatsuriName = context.getString(R.string.default_omatsuri_name);
        String key = context.getString(R.string.omatsuri_name_key);
        return getPreferencesString(context, key, defaultOmatsuriName);
    }

    public static String getHikiyamaName(Context context) {
        String defaultHikiyamaName = context.getString(R.string.default_hikiyama);
        String key = context.getString(R.string.hikiyama_key);
        return getPreferencesString(context, key, defaultHikiyamaName);
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

    public static long getPushingInterval(Context context) {
        String pushingIntStr = getPreferencesString(context, "pushing_interval", 
                context.getString(R.string.default_pushing_interval));
        return Long.parseLong(pushingIntStr);
    }
    
    public static double getGpsUpdateMinDistance(Context context) {
        String key = context.getString(R.string.gps_update_minDistance_key);
        String minDistanceStr = getPreferencesString(context, key,
                context.getString(R.string.default_gps_update_minDistance));
        return Double.parseDouble(minDistanceStr);
    }

    public static double getMinRequiredAccuracy(Context context) {
        String minAccuracyStr = getPreferencesString(context,
                "gps_min_required_accuracy", 
                context.getString(R.string.default_gps_min_required_accuracy));
        return Double.parseDouble(minAccuracyStr);
    }

    public static String getNickName(Context context) {
        return getPreferencesString(context, "device_nickname", Build.MODEL); 
    }

    public static String getSdcardLogFileName(Context context) {
        return getPreferencesString(context, "sdcard_logname", DateTimeUtilities.getFilenameFromDateAndTime() + ".log");
    }


}
