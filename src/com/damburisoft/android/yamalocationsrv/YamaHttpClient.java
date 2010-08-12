package com.damburisoft.android.yamalocationsrv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.Build;
import android.util.Log;

import com.damburisoft.android.yamalocationsrv.service.YamaLogService;

public class YamaHttpClient implements Runnable {

    private final static String TAG = "YamaHttpClient";

    private long mDateTime;
    private double mAzimuth;
    private Location mLocation;

    public YamaHttpClient(long datetime, double azimuth, Location location) {
        mDateTime = datetime;
        mAzimuth = azimuth;
        mLocation = location;
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
            valueObj.put("battery_level", YamaLogService.getBatteryLevel());
            valueObj.put("id", 1);
            valueObj.put("hikiyama_id", YamaPreferenceActivity.getHikiyamaID());
            valueObj.put("hikiyama_name",
                    YamaPreferenceActivity.getHikiyamaName());
            retObj.put("location", valueObj);
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
                YamaPreferenceActivity.getServerURIString());

        try {
            httpPost.setHeader("Content-Type", "application/json");
            HttpEntity entity = new StringEntity(sendObject.toString());
            httpPost.setEntity(entity);
            HttpResponse objResponse = objHttp.execute(httpPost);
            // 戻りSTATUS: 201 (Created)
            Log.d(TAG, "HttpResponse: " + objResponse.toString());
            // TODO check response Status
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

}
