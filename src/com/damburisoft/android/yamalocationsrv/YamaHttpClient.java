package com.damburisoft.android.yamalocationsrv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import net.iharder.Base64;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriEvent;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriRole;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class YamaHttpClient implements Runnable {

    private final static String TAG = "YamaHttpClient";

    private long mDateTime;
    private double mAzimuth;
    private double mBatteryLevel;
    private Location mLocation;
    private Context mContext;
    private String mNickname;

    public YamaHttpClient(Context context, long datetime, double azimuth, 
            Location location, double batteryLevel) {
        mContext  = context;
        mDateTime = datetime;
        mAzimuth  = azimuth;
        mLocation = location;
        mBatteryLevel = batteryLevel;
        mNickname = YamaPreferenceActivity.getNickName(mContext);
    }

    public YamaHttpClient(Context context, YamaInfo info) {
        mContext = context;
        mDateTime = info.getTime();
        mAzimuth  = info.getAzimuth();
        mLocation = info.getLocation();
        mBatteryLevel = info.getBatteryLevel();
        mNickname = YamaPreferenceActivity.getNickName(mContext);
    }

    public JSONObject createJsonObject() {
        JSONObject retObj = new JSONObject();
        JSONObject valueObj = new JSONObject();
        try {
            valueObj.put("heading_accuracy", 0.0);
            valueObj.put("device_nickname", mNickname);
            valueObj.put("horizontal_accuracy", mLocation.getAccuracy());
            valueObj.put("longitude", mLocation.getLongitude());
            valueObj.put("latitude", mLocation.getLatitude());
            valueObj.put("heading", mAzimuth);
            valueObj.put("timestamp",
                    DateTimeUtilities.getDateAndTime(mDateTime));
            valueObj.put("battery_level", mBatteryLevel);
            retObj.put("location", valueObj);
            retObj.put("now", DateTimeUtilities.getDateAndTime(new Date()));
            return retObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void run() {
        JSONObject sendObject = createJsonObject();

        DefaultHttpClient objHttp = new DefaultHttpClient();
        DefaultHttpRequestRetryHandler handler = new DefaultHttpRequestRetryHandler(5, false) {

            @Override
            public boolean retryRequest(IOException exception,
                    int executionCount, HttpContext context) {
                boolean retry = super.retryRequest(exception, executionCount, context);
                if (retry) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return retry;
            }
        };
        objHttp.setHttpRequestRetryHandler(handler);
        
        String serverName = YamaPreferenceActivity.getServer(mContext);
        String event_id_str = YamaPreferenceActivity.getEventID(mContext);
        String role_id_str = YamaPreferenceActivity.getRoleID(mContext);
        String username = YamaPreferenceActivity.getUsername(mContext);
        String password = YamaPreferenceActivity.getPassword(mContext);

        if (serverName == null || event_id_str == null || role_id_str == null
                || username == null || password == null) {
            return;
        }
        
        Uri uri = Uri.parse(serverName);
        Uri.Builder builder = uri.buildUpon();
        builder.path(OmatsuriEvent.Columns.PATH + "/" + event_id_str
                + "/" + OmatsuriRole.Columns.PATH + "/" + role_id_str 
                + "/locations.json");

        
        HttpPost httpPost = new HttpPost(builder.build().toString());

        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(":");
        sb.append(password);
        httpPost.setHeader("Authorization", "Basic " + Base64.encodeBytes(sb.toString().getBytes()));
        httpPost.setHeader("Content-Type", "application/json");

        try {

            HttpEntity entity = new StringEntity(sendObject.toString());
            httpPost.setEntity(entity);
            HttpResponse objResponse = objHttp.execute(httpPost);
            if (objResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
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
