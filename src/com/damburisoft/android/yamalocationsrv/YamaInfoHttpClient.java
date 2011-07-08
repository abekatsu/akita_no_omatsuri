package com.damburisoft.android.yamalocationsrv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.iharder.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriEvent;

public class YamaInfoHttpClient {
    
    private Context mContext;
    private DefaultHttpClient mClient;
    private DefaultHttpRequestRetryHandler mHandler;

    public YamaInfoHttpClient(Context context) {
        mContext = context;
        mClient = new DefaultHttpClient();
        mHandler = new DefaultHttpRequestRetryHandler(1, false) {
            @Override
            public boolean retryRequest(IOException exception,
                    int executionCount, HttpContext context) {
                boolean retry = super.retryRequest(exception, executionCount, context);
                if (retry) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return retry;
            }
        };
        mClient.setHttpRequestRetryHandler(mHandler);
        
    }

    public List<OmatsuriEvent> getEvents() {
        // TODO Auto-generated method stub
        String serverName = YamaPreferenceActivity.getServer(mContext);
        Uri uri = Uri.parse(serverName);
        Uri.Builder builder = uri.buildUpon();
        builder.path("/events.json");
        Uri eventsUri = builder.build();

        HttpGet httpGet = new HttpGet(eventsUri.toString());
        // TODO add Basic Authentication
        setBasicAuthenticationHeader(httpGet);
        
        try {
            HttpResponse response = mClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() < HttpStatus.SC_BAD_REQUEST) {
                InputStream ins = response.getEntity().getContent();
                InputStreamReader inr = new InputStreamReader(ins);
                BufferedReader bufr = new BufferedReader(inr);
                StringBuilder sb = new StringBuilder();  
                String line;  
                while((line = bufr.readLine()) != null){  
                    sb.append(line);
                }  

                JSONArray jsonArray = new JSONArray(sb.toString());
                int length = jsonArray.length();
                List<OmatsuriEvent> events = new ArrayList<OmatsuriEvent>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject eventObj = jsonArray.getJSONObject(i).getJSONObject("event");
                    OmatsuriEvent event = new OmatsuriEvent(eventObj);
                    events.add(event);
                }
                return events;
            }

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return null;
    }
    
    private void setBasicAuthenticationHeader(AbstractHttpMessage message) {
        String username = YamaPreferenceActivity.getUsername(mContext);
        String password = YamaPreferenceActivity.getPassword(mContext);
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(":");
        sb.append(password);
        
        // Authorization: Basic aG9nZTpmdWdh
        message.addHeader("Authorization", "Basic " + Base64.encodeBytes(sb.toString().getBytes()));
    }

}
