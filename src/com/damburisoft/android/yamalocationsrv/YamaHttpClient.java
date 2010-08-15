package com.damburisoft.android.yamalocationsrv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

public class YamaHttpClient implements Runnable {

    private final static String TAG = "YamaHttpClient";

    private long mDateTime;
    private double mAzimuth;
    private double mBatteryLevel;
    private Location mLocation;
    private Context mContext;

    public YamaHttpClient(Context context, long datetime, double azimuth, 
            Location location, double batteryLevel) {
        mContext  = context;
        mDateTime = datetime;
        mAzimuth  = azimuth;
        mLocation = location;
        mBatteryLevel = batteryLevel;
    }

    public JSONObject createJsonObject() {
        JSONObject valueObj = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            valueObj.put("heading_accuracy", 0.0);
            valueObj.put("device_nickname", Build.MODEL);
            valueObj.put("horizontal_accuracy", mLocation.getAccuracy());
            valueObj.put("longitude", mLocation.getLongitude());
            valueObj.put("latitude", mLocation.getLatitude());
            valueObj.put("heading", mAzimuth);
            valueObj.put("timestamp",
                    DateTimeUtilities.getDateAndTime(mDateTime));
            valueObj.put("battery_level", mBatteryLevel);
            retObj.put("location", valueObj);
            retObj.put("hikiyama", YamaPreferenceActivity.getHikiyamaName(mContext));
            retObj.put("omomatsuri", YamaPreferenceActivity.getOmatsuriName(mContext));
        } catch (JSONException e) {
            Log.e(TAG, "createJsonObject() ", e);
            e.printStackTrace();
            return null;
        }

        return retObj;
    }

    public void run() {
        JSONObject sendObject = createJsonObject();

        HttpClient objHttp = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(
                YamaPreferenceActivity.getServerURIString(mContext));

        try {
            httpPost.setHeader("Content-Type", "application/json");
            HttpEntity entity = new StringEntity(sendObject.toString());
            httpPost.setEntity(entity);
            HttpResponse objResponse = objHttp.execute(httpPost);

            if (objResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                // TODO not created location object at Web Server. show warning? or retry?
                debugHeaderMessage(objResponse);
            }
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }
    
    private void debugHeaderMessage(HttpResponse httpResponse) {
        Log.d(TAG, "Headers");
        Header[] headers = httpResponse.getAllHeaders();
        for (int i = 0; i < headers.length; i++) {
            Log.d(TAG, "name: " + headers[i].getName() + "value:" + headers[i].getValue());
        }
        return;
        
    }

}
