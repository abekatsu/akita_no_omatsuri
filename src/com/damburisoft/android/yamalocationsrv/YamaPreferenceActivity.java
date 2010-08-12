package com.damburisoft.android.yamalocationsrv;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class YamaPreferenceActivity extends PreferenceActivity {

    static SharedPreferences mPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    
    public static String getHikiyamaName() {
        return mPreferences.getString("neighborhood_associate", "山根谷地町旭会若者");
        // return "山根谷地町旭会若者";
    }
    
    public static int getHikiyamaID() {
        String hikiyama = mPreferences.getString("neighborhood_associate", "");
        // TODO pickup ID from hikiyama Array List .
        return 0;
    }


}
