package com.damburisoft.android.yamalocationsrv.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AbstractOmatsuriObject {
    
    protected String getJSONObjectStringValue(String key, JSONObject obj) {
        try {
            String value = obj.getString(key);
            if (value.equals("null")) {
                return null;
            } else {
                return value;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

}
