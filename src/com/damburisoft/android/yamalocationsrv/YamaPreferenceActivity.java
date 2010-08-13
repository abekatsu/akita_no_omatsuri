package com.damburisoft.android.yamalocationsrv;

import java.util.Arrays;
import java.util.List;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class YamaPreferenceActivity extends PreferenceActivity {

    private static SharedPreferences mPreferences;
    private static Resources mResources;
    
    private static String defaultHikiyamaName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        
        setDefaultValuesForPreferences();
        
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mResources = getResources();
        defaultHikiyamaName = getString(R.string.default_choumei);
    }
    
    private void setDefaultValuesForPreferences() {
        StringBuffer sb;
        EditTextPreference editTextPreference = (EditTextPreference)findPreference("test_local_server");
        editTextPreference.setDefaultValue(YamaLocationProviderConstants.testWebServer);
        
        sb = new StringBuffer();
        ListPreference pollingIntervalListPreference = (ListPreference)findPreference("polling_interval");
        sb.append(YamaLocationProviderConstants.defaultPollingIntervale);
        pollingIntervalListPreference.setDefaultValue(sb.toString());
        sb = null;
    }
    
    public static String getHikiyamaName() {
        return mPreferences.getString("neighborhood_associate", defaultHikiyamaName);
    }
    
    public static int getHikiyamaID() {
        String hikiyama = mPreferences.getString("neighborhood_associate", defaultHikiyamaName);
        List<CharSequence> list = Arrays.asList(mResources.getTextArray(R.array.neiglist_associate_values));
        return list.indexOf(hikiyama);
    }

    public static boolean isUseLocalServer() {
        return mPreferences.getBoolean("isLocalServer", false);
    }
    
    public static String getServerURIString() {
        StringBuffer sb = new StringBuffer();

        if (YamaPreferenceActivity.isUseLocalServer()) {
            // Expected Result: http://192.168.x.x:8080/location.json";
            sb.append(YamaLocationProviderConstants.testWebServer);
            sb.append(YamaLocationProviderConstants.testWebLocation);
            sb.append(YamaLocationProviderConstants.jsonPost);
        } else {
            // Expected Result: http://labs2.netpersons.co.jp/omatsuri/kakunodate/sugazawa/location.json
            sb.append(YamaLocationProviderConstants.webServer);
            sb.append(YamaLocationProviderConstants.webLocation);
            sb.append(YamaPreferenceActivity.getHikiyamaName() + "/");
            sb.append(YamaLocationProviderConstants.jsonPost);
        }
        return sb.toString();
    }

    public static long getPollingInterval() {
        StringBuffer sb = new StringBuffer();
        sb.append(YamaLocationProviderConstants.defaultPollingIntervale);
        String pollingIntStr = mPreferences.getString("polling_interval", sb.toString());
        return Long.parseLong(pollingIntStr);
    }

}
