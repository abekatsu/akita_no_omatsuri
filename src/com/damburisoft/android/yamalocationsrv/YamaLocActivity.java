package com.damburisoft.android.yamalocationsrv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import com.damburisoft.android.yamalocationsrv.service.YamaLogService;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class YamaLocActivity extends MapActivity {
    private final static String TAG = "YamaLocActivity";
    private YamaLogService yamaLogService = null;
    private boolean isYamaLogServiceConnected = false;
    
    private MapView mMapView;
    private MapController mMapController = null;
    private MyLocationOverlay mLocationOverlay = null;
    
    private Handler mRedrawHandler = null;
    
    private Timer mTimer = null;    
    private TimerTask checkServiceValues = null;

    /**
     * The connection to the Yama Log Service.
     * 
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "ServiceConnection.onServiceConnected");
            yamaLogService = ((YamaLogService.YamaLogServiceBinder)service).getService();
            isYamaLogServiceConnected = yamaLogService.startLogService();
        }

        public void onServiceDisconnected(ComponentName name) {
            yamaLogService.stopLogService();
            yamaLogService = null;
            isYamaLogServiceConnected = false;
        }
        
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mMapView = (MapView)findViewById(R.id.yama_mapview);
        mMapController = mMapView.getController();
        mMapController.setZoom(YamaLocationProviderConstants.defaultZoom);
        mMapView.setBuiltInZoomControls(true);

        createRedrawHandler();
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        stopPollingService();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        stopPollingService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        startPollingService();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainview_menu, menu);
        return true;
    }
    
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.findItem(R.id.menu_startstop);
        if (isYamaLogServiceConnected) {
            mi.setTitle(R.string.menu_stop);
        } else {
            mi.setTitle(R.string.menu_start);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retvalue;
        switch (item.getItemId()) {
        case R.id.menu_startstop:
            if (!isYamaLogServiceConnected) {
                startService(new Intent(YamaLocActivity.this, YamaLogService.class));
                tryBindLogService();
            } else {
                tryUnbindLogService();
                stopService(new Intent(YamaLocActivity.this, YamaLogService.class));
                yamaLogService = null;
            }
            retvalue = true;
            break;
        case R.id.menu_save:
            copyLogToSdCard();
            Toast.makeText(YamaLocActivity.this, 
                    R.string.log_saved_to_sdcard, Toast.LENGTH_SHORT).show();
            retvalue = true;
        case R.id.menu_quit:
            finish();
            retvalue = true;
        default:
            retvalue = super.onOptionsItemSelected(item); 
        }
        
        return retvalue;
    }

    private void createRedrawHandler() {
        mRedrawHandler = new Handler() {
            
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                case YamaLocationProviderConstants.RedrawType.TextView:
                    TextView tv = null;
                    StringBuffer sb = new StringBuffer();
                    switch (msg.arg1) {
                    case YamaLocationProviderConstants.UpdateInfo.AZIMUTH:
                        tv = (TextView)findViewById(R.id.azimuth_tv);
                        sb.append(getText(R.string.azimuth));
                        sb.append(" ");
                        sb.append((String)msg.obj);
                        break;
                    case YamaLocationProviderConstants.UpdateInfo.LOCATION:
                        tv = (TextView)findViewById(R.id.location_tv);
                        sb.append(getText(R.string.location));
                        sb.append(" ");
                        sb.append((String)msg.obj);
                        break;
                    case YamaLocationProviderConstants.UpdateInfo.DATETIME:
                        tv = (TextView)findViewById(R.id.time_tv);
                        sb.append(getText(R.string.current_time));
                        sb.append(" ");
                        sb.append((String)msg.obj);
                        break;
                    }
                    if (tv != null) {
                        tv.setText(sb.toString());
                    }
                    break;
                case YamaLocationProviderConstants.RedrawType.MapView:
                    if (msg.arg1 == YamaLocationProviderConstants.UpdateInfo.LOCATION) {
                        Location loc = (Location)msg.obj;
                        GeoPoint gp = new GeoPoint((int)(loc.getLatitude()*1E6), 
                                (int)(loc.getLongitude()*1E6));
                        mMapController.animateTo(gp);
                    }
                    break;
                }
            }
        };

    }
    
    private void startPollingService() {
        mTimer = new Timer();
        checkServiceValues = new TimerTask() {
            @Override
            public void run() {
                Message msg_time = new Message();
                msg_time.what = YamaLocationProviderConstants.RedrawType.TextView;
                msg_time.arg1 = YamaLocationProviderConstants.UpdateInfo.DATETIME;
                msg_time.obj  = (Object)DateTimeUtilities.getDateAndTime();
                mRedrawHandler.sendMessage(msg_time);

                if (yamaLogService == null) {
                    return ;
                }
                
                double azimuth = yamaLogService.getAzimuth();
                if (azimuth != Double.NaN) {
                    Message msg = new Message();
                    msg.what = YamaLocationProviderConstants.RedrawType.TextView;
                    msg.arg1 = YamaLocationProviderConstants.UpdateInfo.AZIMUTH;
                    msg.obj = (Object)Double.toString(azimuth);
                    mRedrawHandler.sendMessage(msg);
                }
                
                Location loc = yamaLogService.getCurrentLocation();
                if (loc != null) {
                    Message msg_tv = new Message();
                    msg_tv.what = YamaLocationProviderConstants.RedrawType.TextView;
                    msg_tv.arg1 = YamaLocationProviderConstants.UpdateInfo.LOCATION;
                    msg_tv.obj  = (Object)getLocationStringForTextView(loc);
                    mRedrawHandler.sendMessage(msg_tv);

                    Message msg_mv = new Message();
                    msg_mv.what = YamaLocationProviderConstants.RedrawType.MapView;
                    msg_mv.arg1 = YamaLocationProviderConstants.UpdateInfo.LOCATION;
                    msg_mv.obj  = (Object)loc;
                    // mRedrawHandler.sendMessage(msg_mv);
                }
            }
        };

        mTimer.schedule(checkServiceValues, 0, 10 * 1000);

        // mLocationOverlay = new MyLocationOverlay(this, mMapView);
        // mLocationOverlay.enableCompass();
        // mLocationOverlay.enableMyLocation();
        // mLocationOverlay.runOnFirstFix(new Runnable() {

            // public void run() {
                // TODO Auto-generated method stub
                // mMapController.animateTo(mLocationOverlay.getMyLocation());
            // }
            
        // });
        // mMapView.getOverlays().add(mLocationOverlay);
        // mMapView.invalidate();
        // setContentView(mMapView);

    }
    
    private void stopPollingService() {
        // mMapView.getOverlays().remove(mLocationOverlay);
        // mLocationOverlay = null;
        // mMapView.invalidate();
        // setContentView(mMapView);

        mLocationOverlay.disableCompass();
        mLocationOverlay.disableMyLocation();

        if (checkServiceValues != null) {
            checkServiceValues.cancel();
        }
        
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        
        checkServiceValues = null;
        mTimer = null;
    }
    
    private void copyLogToSdCard() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        
        String dst = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + DateTimeUtilities.getFilenameFromDateAndTime() + ".log";
        
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = openFileInput(YamaLocationProviderConstants.logFileName).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            try {
                srcChannel.close();
                dstChannel.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    private void tryBindLogService() {
        isYamaLogServiceConnected = bindService(new Intent(this, YamaLogService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void tryUnbindLogService() {
        if (!isYamaLogServiceConnected) {
            return ;
        }
        
        unbindService(serviceConnection);
        isYamaLogServiceConnected = false;
    }
    
    private String getLocationStringForTextView(Location loc) {
        StringBuffer sb = new StringBuffer();

        sb.append((float)loc.getLongitude());
        sb.append(",");
        sb.append((float)loc.getLatitude());
        sb.append(",");
        sb.append((float)loc.getAccuracy());

        return sb.toString();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}