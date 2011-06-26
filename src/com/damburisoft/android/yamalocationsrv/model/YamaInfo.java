package com.damburisoft.android.yamalocationsrv.model;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

public class YamaInfo {
    
    @Key
    public int id;

    @Key
    public String omatsuri;
    
    @Key
    public String hikiyama;
    
    @Key
    public String device_nickname;
    
    @Key
    public double battery_level;
    
    @Key
    public double heading;
    
    @Key
    public double heading_accuracy;
    
    @Key
    public double horizontal_accuracy;

    @Key
    public double longitude;
    
    @Key
    public double latitude;
    
    @Key
    public double altitude;
    
    @Key
    public long timestamp;
    
    @Key
    public boolean pushed;

    @Override
    public boolean equals(Object o) {
        if (o instanceof YamaInfo) {
            YamaInfo compareInfo = (YamaInfo)o;
            if (altitude != compareInfo.altitude) {
                // return false; // since server does not recognize "altitude" key.
            } else if (battery_level != compareInfo.battery_level) {
                return false;
            } else if (heading != compareInfo.heading) {
                return false;
            } else if (heading_accuracy != compareInfo.heading_accuracy) {
                return false;
            } else if (horizontal_accuracy != compareInfo.horizontal_accuracy) {
                return false;
            } else if (latitude != compareInfo.latitude) {
                return false;
            } else if (longitude != compareInfo.longitude) {
                return false;
            } else if (!omatsuri.equals(compareInfo.omatsuri)) {
                // return false;
            } else if (!hikiyama.equals(compareInfo.hikiyama)) {
                // return false;
            } else if (!device_nickname.equals(compareInfo.device_nickname)) {
                return false;
            } else if (timestamp != compareInfo.timestamp) {
                return false;
            } else if (pushed != compareInfo.pushed) {
                // return false;
            } else {
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    public JSONObject createJsonObject() throws JSONException {
        if (timestamp == 0) {
            Date date = new Date();
            timestamp = date.getTime();
        }

        JSONObject retObject = new JSONObject();

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        JSONObject locObject = new JSONObject();

        locObject.put("battery_level", battery_level);
        locObject.put("device_nickname", device_nickname);
        locObject.put("heading", heading);
        locObject.put("heading_accuracy", heading_accuracy);
        locObject.put("horizontal_accuracy", horizontal_accuracy);
        locObject.put("latitude", latitude);
        locObject.put("longitude", longitude);
        locObject.put("timestamp", sdf.format(new Date(timestamp)));

        retObject.put("location", locObject);
        retObject.put("now", sdf.format(date));

        return retObject;
        
    }
    
    public HttpPost buildPutRequest(String mUrl) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost();
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject jsonObject;
        try {
            jsonObject = createJsonObject();
            HttpEntity entity = new StringEntity(jsonObject.toString());
            httpPost.setEntity(entity);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return httpPost;
    }

    public int changePushed(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        ContentValues updateValue = new ContentValues();
        updateValue.put(YamaLocationColumn.Info.PUSHED, true);
        String where = YamaLocationColumn.Info._ID + " = " + id;
        return resolver.update(YamaLocationColumn.Info.CONTENT_URI, updateValue, where, null);
    }
    
    
}
