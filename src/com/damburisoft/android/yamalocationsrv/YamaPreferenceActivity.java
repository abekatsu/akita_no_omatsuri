package com.damburisoft.android.yamalocationsrv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriEvent;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriEventUpdateListener;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriRole;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriRoleUpdateListener;
import com.damburisoft.android.yamalocationsrv.provider.EventInfoSQLiteOpenHelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author abekatsu
 *
 */
public class YamaPreferenceActivity extends PreferenceActivity implements 
    SharedPreferences.OnSharedPreferenceChangeListener, OmatsuriEventUpdateListener, OmatsuriRoleUpdateListener {

    private static final String TAG = "YamaPreferenceActivity";
    private static final boolean debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setServerSummary(settings);
        setUsernameSummary(settings);
        setOmatsuriSummary(settings);
        setRoleSummary(settings);
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
        if (key.equals(getString(R.string.server_key))) {
            removeEventAndRole();
            setServerSummary(sharedPreferences);
            pickUpInfoFromServer(sharedPreferences);
        } else if (key.equals(getString(R.string.omatsuri_name_key))) {
            setOmatsuriSummary(sharedPreferences);
            pickUpRoleFromServer(sharedPreferences);
        } else if (key.equals(getString(R.string.hikiyama_key))) {
            setRoleSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.user_name_key))) {
            pickUpInfoFromServer(sharedPreferences);
            setUsernameSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.user_password_key))) {
            pickUpInfoFromServer(sharedPreferences);
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

    private boolean checkUsernameAndPasswordConfigured() {
        String username = getPreferencesString(this, getString(R.string.user_name_key), null);
        String password = getPreferencesString(this, getString(R.string.user_password_key), null);

        if (username != null && password != null) {
            return true;
        } else {
            return false;
        }
    }
    
    private void pickUpInfoFromServer(SharedPreferences sharedPreferences) {
        if (!checkUsernameAndPasswordConfigured()) {
            return ;
        }
        YamaOmatsuriEventHttpAsyncTask infoAsyncTask = new YamaOmatsuriEventHttpAsyncTask(this);
        Void[] params = {};
        infoAsyncTask.execute(params);
    }
    
    private void pickUpRoleFromServer(SharedPreferences sharedPreferences) {
        if (!checkUsernameAndPasswordConfigured()) {
            return ;
        }
        
        OmatsuriListPreference preference = (OmatsuriListPreference)findPreference(getString(R.string.omatsuri_name_key));
        String event_id_str = preference.getValue();
        if (event_id_str != null) {
            int event_id = Integer.parseInt(event_id_str);
            pickUpRoleFromServer(sharedPreferences, event_id);
        }
    }
    
    private void pickUpRoleFromServer(SharedPreferences sharedPreferences, int event_id) {
        if (!checkUsernameAndPasswordConfigured()) {
            return ;
        }
        
        YamaOmatsuriRoleHttpAsyncTask infoAsyncTask = new YamaOmatsuriRoleHttpAsyncTask(this);
        Integer[] params = {event_id};
        infoAsyncTask.execute(params);
    }

    private void setOmatsuriSummary(SharedPreferences settings) {
        
        String key = getString(R.string.omatsuri_name_key);
        OmatsuriListPreference pref = (OmatsuriListPreference)findPreference(key);
        CharSequence entry = pref.getEntry();
        String summary;
        if (entry == null) {
            if (pref.getEntries() == null) {
                summary = getString(R.string.omatsuri_name_summary_set_server);
                pref.setEnabled(false);
            } else {
                summary = getString(R.string.omatsuri_name_summary);
                pref.setEnabled(true);
            }
            
        } else {
            summary = entry.toString();
            pref.setEnabled(true);
            String event_id_str = pref.getValue().toString();
            if (event_id_str != null) {
                int event_id = Integer.parseInt(event_id_str);
                pickUpRoleFromServer(settings, event_id);
            }
        }
        pref.setSummary(summary);

    }
    
    private void setRoleSummary(SharedPreferences settings) {
        String key = getString(R.string.hikiyama_key);
        OmatsuriListPreference pref = (OmatsuriListPreference)findPreference(key);
        CharSequence entry = pref.getEntry();

        String summary;
        if (entry == null) {
            if (!settings.contains(getString(R.string.omatsuri_name_key))) {
                summary = getString(R.string.hikiyama_summary);
                pref.setEnabled(false);
            } else {
                summary = getString(R.string.hikiyama_summary_2nd);
                pref.setEnabled(true);
            }            
        } else {
            pref.setEnabled(true);
            summary = entry.toString();
        }
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
    
    private void setUsernameSummary(SharedPreferences settings) {
        String key = getString(R.string.user_name_key);
        String value = getPreferencesString(this, key, "");
        Preference pref = findPreference(key);
        String summary;
        if (settings.contains(key)) {
            summary = "Username: " + value;
        } else {
            summary = getString(R.string.user_name_summary);
        }
        pref.setSummary(summary);
    }

    
    private void setDeviceNicknameSummary(SharedPreferences settings) {
        String key = getString(R.string.device_nickname_key);
        String value = getPreferencesString(this, key, Build.MODEL); 
        Preference pref = findPreference(key);
        pref.setSummary(value);
    }
    
    private void setServerSummary(SharedPreferences settings) {
        String key = getString(R.string.server_key);
        String value = getPreferencesString(this, key, ""); 
        Preference pref = findPreference(key);
        if (settings.contains(key) 
                && !value.equalsIgnoreCase(getString(R.string.server_default_prefix))) {
            pref.setSummary("\t" + value);
        }
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
            e.printStackTrace();
            Log.e(TAG, "Message: " + e.getMessage());
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

    public static String getUsername(Context context) {
        String key = context.getString(R.string.user_name_key);
        return getPreferencesString(context, key, "");
    }

    public static String getPassword(Context context) {
        String key = context.getString(R.string.user_password_key);
        return getPreferencesString(context, key, "");
    }

    
    public static String getServer(Context context) {
        String key = context.getString(R.string.server_key);
        return getPreferencesString(context, key, "");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preference_menu, menu);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_sync:
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
            pickUpInfoFromServer(preference);
            pickUpRoleFromServer(preference);
            break;
        default:
            // nothing to do it here?
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see com.damburisoft.android.yamalocationsrv.model.OmatsuriUpdateListenner#getActivity()
     */
    public Activity getActivity() {
        return this;
    }

    /* (non-Javadoc)
     * @see com.damburisoft.android.yamalocationsrv.model.OmatsuriEventUpdateListener#updateEvents(java.util.List)
     */
    public void updateEvents(List<OmatsuriEvent> events) {
        if (events == null) {
            return ;
        }
        final ContentResolver resolver = getContentResolver();
        Cursor c;
        String[] projection = {
                OmatsuriEvent.Columns._ID,
                OmatsuriEvent.Columns.ID,
                OmatsuriEvent.Columns.TITLE,
        };

        for (OmatsuriEvent event : events) {
            String selection = OmatsuriEvent.Columns.ID + " = " + event.id;
            c = resolver.query(OmatsuriEvent.Columns.CONTENT_URI, projection, selection, null, null);
            if (c != null) {
                ContentValues cv = event.getContentValues();
                if (c.getCount() == 0) {
                    Uri uri = resolver.insert(OmatsuriEvent.Columns.CONTENT_URI, cv);
                    if (debug) {
                        Log.d(TAG, "insert: " + uri.toString());
                    }
                } else {
                    int update = resolver.update(OmatsuriEvent.Columns.CONTENT_URI, cv, selection, null);
                    if (debug) {
                        Log.d(TAG, "update: " + update);
                    }
                }
                c.close();
            }
        }
            
        setUpEventEntryAndValue();
    }

    /* (non-Javadoc)
     * @see com.damburisoft.android.yamalocationsrv.model.OmatsuriRoleUpdateListener#updateRoles(java.util.List)
     */
    public void updateRoles(int event_id, List<OmatsuriRole> roles) {
        if (roles == null) {
            return ;
        }

        final ContentResolver resolver = getContentResolver();
        Cursor c;
        String[] projection = {
                OmatsuriRole.Columns._ID,
                OmatsuriRole.Columns.ID,
                OmatsuriRole.Columns.NAME,
        };


        final Uri baseUri = Uri.withAppendedPath(OmatsuriEvent.Columns.CONTENT_URI, 
                "" + event_id + "/" + OmatsuriRole.Columns.PATH);
        
        for (OmatsuriRole role : roles) {
            String selection = OmatsuriRole.Columns.ID + " = " + role.id;
            c = resolver.query(baseUri, projection, selection, null, null);
            if (c != null) {
                ContentValues cv = role.getContentValues();
                if (c.getCount() == 0) {
                    Uri uri = resolver.insert(baseUri, cv);
                    if (debug) {
                        Log.d(TAG, "insert: " + uri.toString());
                    }
                } else {
                    int update = resolver.update(baseUri, cv, selection, null);
                    if (debug) {
                        Log.d(TAG, "update: " + update);
                    }
                }
                c.close();
            }
        }
        
        setUpRoleEntryAndValue(event_id);

    }
    
    private void setUpEventEntryAndValue() {
        final ContentResolver resolver = getContentResolver();
        Cursor c;
        String[] projection = {
                OmatsuriEvent.Columns._ID,
                OmatsuriEvent.Columns.ID,
                OmatsuriEvent.Columns.TITLE,
        };
        
        c = resolver.query(OmatsuriEvent.Columns.CONTENT_URI, projection, null, null, null);
        if (c != null) {
            String key = getString(R.string.omatsuri_name_key);
            final OmatsuriListPreference preference = (OmatsuriListPreference)findPreference(key);
            if (c.moveToFirst()) {
                ArrayList<CharSequence> entryList = new ArrayList<CharSequence>(c.getCount());
                ArrayList<CharSequence> entryValueList = new ArrayList<CharSequence>(c.getCount());

                do {
                    entryList.add(c.getString(c.getColumnIndex(OmatsuriEvent.Columns.TITLE)));
                    entryValueList.add(Integer.toString(c.getInt(c.getColumnIndex(OmatsuriEvent.Columns.ID))));
                } while (c.moveToNext());
                preference.setEnabled(true);
                preference.setEntries((CharSequence[])entryList.toArray(new CharSequence[0]));
                preference.setEntryValues((CharSequence[])entryValueList.toArray(new CharSequence[0]));
                preference.setSummary(R.string.omatsuri_name_summary);
            } else {
                preference.setEnabled(false);
            }
            c.close();
        }
    }
    
    private void setUpRoleEntryAndValue(int event_id) {
        final ContentResolver resolver = getContentResolver();
        Cursor c;
        String[] projection = {
                OmatsuriRole.Columns._ID,
                OmatsuriRole.Columns.ID,
                OmatsuriRole.Columns.NAME,
        };

        final Uri baseUri = Uri.withAppendedPath(OmatsuriEvent.Columns.CONTENT_URI, 
                "" + event_id + "/" + OmatsuriRole.Columns.PATH);

        c = resolver.query(baseUri, projection, null, null, null);
        if (c != null) {
            String key = getString(R.string.hikiyama_key);
            final OmatsuriListPreference preference = (OmatsuriListPreference)findPreference(key);
            if (c.moveToFirst()) {
                ArrayList<CharSequence> entryList = new ArrayList<CharSequence>(c.getCount());
                ArrayList<CharSequence> entryValueList = new ArrayList<CharSequence>(c.getCount());

                do {
                    entryList.add(c.getString(c.getColumnIndex(OmatsuriRole.Columns.NAME)));
                    entryValueList.add(Integer.toString(c.getInt(c.getColumnIndex(OmatsuriRole.Columns.ID))));
                } while (c.moveToNext());
                preference.setEnabled(true);
                preference.setEntries((CharSequence[])entryList.toArray(new CharSequence[0]));
                preference.setEntryValues((CharSequence[])entryValueList.toArray(new CharSequence[0]));
                preference.setSummary(R.string.hikiyama_summary_2nd);
            } else {
                preference.setSummary(R.string.hikiyama_summary);
                preference.setEnabled(false);
            }
            c.close();
        }


    }
    
    private void removeEventAndRole() {
        EventInfoSQLiteOpenHelper helper = new EventInfoSQLiteOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DROP TABLE IF EXISTS " + EventInfoSQLiteOpenHelper.DB_EVENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EventInfoSQLiteOpenHelper.DB_ROLE_TABLE_NAME);
        helper.onCreate(db);
        db.endTransaction();

        removeEntryAndValues(R.string.omatsuri_name_key, R.string.omatsuri_name_summary_set_server);
        removeEntryAndValues(R.string.hikiyama_key, R.string.hikiyama_summary);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.omatsuri_name_key));
        editor.remove(getString(R.string.hikiyama_key));
        editor.commit();
        
    }

    
    private void removeEntryAndValues(int key_id, int summary_string_id) {
        String key = getString(key_id);
        ListPreference preference = (ListPreference)findPreference(key);
        preference.setEntries(null);
        preference.setEntryValues(null);
        preference.setValue(null);
        preference.setSummary(summary_string_id);
        preference.setEnabled(false);
    }
    
    private static void setPreferenceValues(Context context, String key, String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        if (value == null) {
            editor.remove(key);
        } else {
            editor.putString(key, value);
        }
        
        editor.commit();
    }
    
    public static void setServer(Context context, String server) {
        String key = context.getString(R.string.server_key);
        setPreferenceValues(context, key, server);
    }

    public static void setUsername(Context context, String username) {
        String key = context.getString(R.string.user_name_key);
        setPreferenceValues(context, key, username);
    }

    public static void setPassword(Context context, String password) {
        String key = context.getString(R.string.user_password_key);
        setPreferenceValues(context, key, password);
    }

}
