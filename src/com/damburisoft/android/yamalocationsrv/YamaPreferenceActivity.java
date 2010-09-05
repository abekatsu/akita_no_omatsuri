package com.damburisoft.android.yamalocationsrv;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

public class YamaPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "YamaPreferenceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        
        debug_EditTextPreference_set();
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setOmatsuriSummary(settings);
        setHikiyamaSummary(settings);
        setDebugMinDistanceSummary(settings);
        setDebugMinRequiredAccuracySummary(settings);
        setDebugPollingIntervalSummary(settings);
        setDebugPushingIntervalSummary(settings);
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
        } else if (key.equals(getString(R.string.device_nickname_key))) {
            setDeviceNicknameSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.debug_gps_update_minDistance_key))) {
            setDebugMinDistanceSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.debug_gps_min_required_accuracy_key))) {
            setDebugMinRequiredAccuracySummary(sharedPreferences);
        } else if (key.equals(getString(R.string.debug_polling_interval_key))) {
            setDebugPollingIntervalSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.debug_pushing_interval_key))) {
            setDebugPushingIntervalSummary(sharedPreferences);
        }

    }

    private void debug_EditTextPreference_set() {
        int[] editTextPreferenceKey = {R.string.debug_gps_update_minDistance_key, 
                R.string.debug_gps_min_required_accuracy_key,
                R.string.debug_polling_interval_key,
                R.string.debug_pushing_interval_key};
        
        for (int key : editTextPreferenceKey) {
            EditTextPreference etp = (EditTextPreference)findPreference(getString(key));
            EditText et = etp.getEditText();
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
    
    
    private void setDeviceNicknameSummary(SharedPreferences settings) {
        String key = getString(R.string.device_nickname_key);
        String value = getPreferencesString(this, key, Build.MODEL); 
        Preference pref = findPreference(key);
        pref.setSummary(value);
    }

    private void setDebugMinDistanceSummary(SharedPreferences settings) {
        String key = getString(R.string.debug_gps_update_minDistance_key);
        String value = getPreferencesString(this, key, getString(R.string.default_gps_update_minDistance));
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        sb.append(" m");
        Preference pref = findPreference(key);
        pref.setSummary(sb.toString());
        
        // Modify MinDistanceSummary also if key is contained.
        /*
        if (settings.contains(key)) {
            String gps_update_minDistance_key = getString(R.string.gps_update_minDistance_key);
            Preference gps_update_minDistance_pref = findPreference(gps_update_minDistance_key);
            gps_update_minDistance_pref.setSummary(sb.toString());
        }
        */
    }
    
    private void setDebugMinRequiredAccuracySummary(SharedPreferences settings) {
        String key = getString(R.string.debug_gps_min_required_accuracy_key);
        String value = getPreferencesString(this, key, getString(R.string.default_gps_min_required_accuracy));
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        sb.append(" m");
        Preference pref = findPreference(key);
        pref.setSummary(sb.toString());
        
        // Modify MinRequiredAccuracySummary also if key is contained.
        /*
        if (settings.contains(key)) {
            String gps_min_required_accuracy_key = getString(R.string.gps_min_required_accuracy_key);
            Preference gps_min_required_accuracy_pref = findPreference(gps_min_required_accuracy_key);
            gps_min_required_accuracy_pref.setSummary(sb.toString());
        }
        */
    }

    private void setDebugPollingIntervalSummary(SharedPreferences settings) {
        
        String debug_key = getString(R.string.debug_polling_interval_key);
        String debug_value = getPreferencesString(this, debug_key, getString(R.string.default_polling_interval));
        int debug_interval = Integer.parseInt(debug_value);
        String debug_summary = getIntervalString(debug_interval);
        Preference debug_pref = findPreference(debug_key);
        debug_pref.setSummary(debug_summary);

        /*
        // Modify PollingIntervalSummary (only summary, not value) also if key is contained.
        if (settings.contains(debug_key)) {
            String key = getString(R.string.polling_interval_key);
            Preference pref = findPreference(key);
            pref.setSummary(debug_summary);
            // SharedPreferences.Editor editor = settings.edit();
            // editor.putString(key, debug_value);
            // editor.commit();
        }
        */
    }
    
    private void setDebugPushingIntervalSummary(SharedPreferences settings) {
        String debug_key = getString(R.string.debug_pushing_interval_key);
        String debug_value = getPreferencesString(this, debug_key, getString(R.string.default_pushing_interval));
        String intervalStr = getIntervalString(Integer.parseInt(debug_value));
        Preference debug_pref = findPreference(debug_key);
        debug_pref.setSummary(intervalStr);
        
        // Modify PushingIntervalSummary also if key is contained.
        /*
        if (settings.contains(debug_key)) {
            String key = getString(R.string.pushing_interval_key);
            Preference pref = findPreference(key);
            pref.setSummary(intervalStr);
        }
        */
    }
    
    private String getIntervalString(int interval) {
        int minutes = interval / 60;
        int seconds = interval - (minutes * 60);
        StringBuffer sb = new StringBuffer();

        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" " + getString(R.string.minute) + " ");
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" " + getString(R.string.second));
        }
        
        return sb.toString();
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int strId = 0;
        if (settings.contains(context.getString(R.string.debug_polling_interval_key))) {
            strId = R.string.debug_polling_interval_key;
        } else {
            strId = R.string.polling_interval_key;
        }
        
        String pollingIntStr = getPreferencesString(context, context.getString(strId),
                context.getString(R.string.default_polling_interval));
        return Long.parseLong(pollingIntStr) * 1000;
    }

    public static long getPushingInterval(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int strId = 0;
        if (settings.contains(context.getString(R.string.debug_pushing_interval_key))) {
            strId = R.string.debug_pushing_interval_key;
        } else {
            strId = R.string.pushing_interval_key;
        }

        String pushingIntStr = getPreferencesString(context, context.getString(strId),
                context.getString(R.string.default_pushing_interval));
        return Long.parseLong(pushingIntStr) * 1000;
    }
    
    public static double getGpsUpdateMinDistance(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int strId = 0;
        if (settings.contains(context.getString(R.string.debug_gps_update_minDistance_key))) {
            strId = R.string.debug_gps_update_minDistance_key;
        } else {
            strId = R.string.gps_update_minDistance_key;
        }

        String key = context.getString(strId);
        String minDistanceStr = getPreferencesString(context, key,
                context.getString(R.string.default_gps_update_minDistance));
        return Double.parseDouble(minDistanceStr);
    }

    public static double getMinRequiredAccuracy(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int strId = 0;
        if (settings.contains(context.getString(R.string.debug_gps_min_required_accuracy_key))) {
            strId = R.string.debug_gps_min_required_accuracy_key;
        } else {
            strId = R.string.gps_min_required_accuracy_key;
        }

        String key = context.getString(strId);
        String minAccuracyStr = getPreferencesString(context, key,
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
