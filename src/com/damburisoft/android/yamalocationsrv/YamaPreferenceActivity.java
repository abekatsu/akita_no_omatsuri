package com.damburisoft.android.yamalocationsrv;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class YamaPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference);
    }
    
}
