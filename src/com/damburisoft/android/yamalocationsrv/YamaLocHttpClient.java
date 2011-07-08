package com.damburisoft.android.yamalocationsrv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.damburisoft.android.yamalocationsrv.model.YamaInfo;
import com.damburisoft.android.yamalocationsrv.model.YamaLocationColumn;

public class YamaLocHttpClient {

    private Context mContext;
    private static DefaultHttpClient mClient = new DefaultHttpClient();
    private static DefaultHttpRequestRetryHandler mHandler = new DefaultHttpRequestRetryHandler(5, false) {
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
    
    public YamaLocHttpClient(Context context, String url) {
        mContext = context;
        _setUp();
    }
    
    private void _setUp() {
        mClient.setHttpRequestRetryHandler(mHandler);
    }

    
    public boolean pushLocation(YamaInfo info) {

        // YamaHttpPost postRequest = info.buildPostRequest(mUrl);
        
        
        // httpPost.setHeader("Content-Type", "application/json");
        // HttpEntity entity = new StringEntity(sendObject.toString());
        // httpPost.setEntity(entity);
        // HttpResponse objResponse = objHttp.execute(httpPost);
        
//        HttpRequest request = null;
//        try {
//            request = requestFactory.buildPutRequest(url, content);
//            // TODO this is workaround to deal with GAE bug, which cannot handle etag rightly. 
//            request.headers.set("If-Match", "*");
//            updated_entry = request.execute().parseAs(CellEntry.class);
//            if (debug) {
//                updated_entry.writeLog(TAG);
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            // TODO this is workaround to deal with GAE bug, which cannot handle etag rightly. 
//            if (request != null && request.headers != null) {
//                request.headers.set("If-Match", null);
//            }
//        }
//        return updated_entry;
        
        return false;
    }
    
    public boolean pushLocations() {
        boolean retvalue = true;
        List<YamaInfo> infoList = getYamaInfo(false);
        for (YamaInfo info : infoList) {
            if (!pushLocation(info)) {
                retvalue = false;;
            }
        }
        
        return retvalue;
    }
    
    public List<YamaInfo> getYamaInfo(boolean pushed) {
        final ContentResolver resolver = mContext.getContentResolver();
        final Uri uri = YamaLocationColumn.Info.CONTENT_URI;

        String[] projection = new String[] {
                YamaLocationColumn.Info._ID,
                YamaLocationColumn.Info.BATTERY_LEVEL,
                YamaLocationColumn.Info.HEADING,
                YamaLocationColumn.Info.HEADING_ACCURACY,
                YamaLocationColumn.Info.LATITUDE,
                YamaLocationColumn.Info.LONGITUDE,
                YamaLocationColumn.Info.ALTITUDE,
                YamaLocationColumn.Info.TIMESTAMP,
                YamaLocationColumn.Info.PUSHED
        };

        String selection;
        if (pushed) {
            selection = YamaLocationColumn.Info.PUSHED + " = 1";
        } else {
            selection = YamaLocationColumn.Info.PUSHED + " = 0";
        }
        Cursor c = resolver.query(uri, projection, selection, null, null);
        if (c != null) {
            ArrayList<YamaInfo> retArray = null;
            if (c.moveToFirst()) {
                retArray = new ArrayList<YamaInfo>();
                do {
                    YamaInfo info = new YamaInfo();
                    info.id = c.getInt(c.getColumnIndex(YamaLocationColumn.Info._ID));
                    info.hikiyama = YamaPreferenceActivity.getHikiyamaName(mContext);
                    info.omatsuri = YamaPreferenceActivity.getOmatsuriName(mContext);
                    info.battery_level = c.getDouble(c.getColumnIndex(YamaLocationColumn.Info.BATTERY_LEVEL));
                    info.heading = c.getDouble(c.getColumnIndex(YamaLocationColumn.Info.HEADING));
                    info.heading_accuracy = c.getDouble(c.getColumnIndex(YamaLocationColumn.Info.HEADING_ACCURACY));
                    info.latitude = c.getDouble(c.getColumnIndex(YamaLocationColumn.Info.LATITUDE));
                    info.longitude = c.getDouble(c.getColumnIndex(YamaLocationColumn.Info.LONGITUDE));
                    info.altitude = c.getDouble(c.getColumnIndex(YamaLocationColumn.Info.ALTITUDE));
                    info.timestamp = c.getLong(c.getColumnIndex(YamaLocationColumn.Info.TIMESTAMP));
                    int pushed_int = c.getInt(c.getColumnIndex(YamaLocationColumn.Info.PUSHED));
                    if (pushed_int == 1) {
                        info.pushed = true;
                    } else {
                        info.pushed = false;
                    }
                    retArray.add(info);
                } while (c.moveToNext());
            }
            c.close();
            return retArray;
        }
        
        return null;
    }
}
