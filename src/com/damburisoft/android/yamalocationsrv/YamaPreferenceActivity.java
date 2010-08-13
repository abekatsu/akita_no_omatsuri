package com.damburisoft.android.yamalocationsrv;

import java.util.Arrays;
import java.util.List;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mResources = getResources();
        defaultHikiyamaName = getString(R.string.default_choumei);
    }

    public static String getHikiyamaName() {
        return mPreferences.getString("neighborhood_associate",
                defaultHikiyamaName);
    }

    public static int getHikiyamaID() {
        String hikiyama = mPreferences.getString("neighborhood_associate",
                defaultHikiyamaName);
        List<CharSequence> list = Arrays.asList(mResources
                .getTextArray(R.array.neiglist_associate_values));
        return list.indexOf(hikiyama);
    }

    public static boolean isUseLocalServer() {
        return mPreferences.getBoolean("isLocalServer", false);
    }

    public static String getServerURIString() {
        StringBuffer sb = new StringBuffer();

        if (YamaPreferenceActivity.isUseLocalServer()) {
            // Expected Result: http://192.168.x.x:8080/location.json";
            sb.append(mResources.getString(R.string.default_test_local_server));
            sb.append(YamaLocationProviderConstants.jsonPost);
        } else {
            // Expected Result:
            // http://labs2.netpersons.co.jp/omatsuri/kakunodate/sugazawa/location.json
            sb.append(YamaLocationProviderConstants.webServer);
            sb.append(YamaLocationProviderConstants.webLocation);
            sb.append(YamaPreferenceActivity.getHikiyamaName() + "/");
            sb.append(YamaLocationProviderConstants.jsonPost);
        }
        return sb.toString();
    }

    public static long getPollingInterval() {
        StringBuffer sb = new StringBuffer();
        sb.append(mResources.getString(R.string.default_polling_interval));
        String pollingIntStr = mPreferences.getString("polling_interval",
                sb.toString());
        return Long.parseLong(pollingIntStr);
    }

    public static double getGpsUpdateMinDistance() {
        StringBuffer sb = new StringBuffer();
        sb.append(mResources.getString(R.string.default_gps_update_minDistance));
        String minDistanceStr = mPreferences.getString(
                "gps_update_minDistance", sb.toString());
        return Double.parseDouble(minDistanceStr);
    }

}
