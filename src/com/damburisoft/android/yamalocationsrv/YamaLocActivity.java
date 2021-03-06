package com.damburisoft.android.yamalocationsrv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.damburisoft.android.yamalocationsrv.service.IYamaLogService;
import com.damburisoft.android.yamalocationsrv.service.YamaLogService;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class YamaLocActivity extends MapActivity {
    private final static String TAG = "YamaLocActivity";
    private IYamaLogService yamaLogService = null;

    private MapView mMapView;
    private MapController mMapController = null;
    private YamaMapOverlay mYamaMapOverlay = null;

    private Handler mRedrawHandler = null;

    private Timer mTimer = null;
    private TimerTask checkServiceValues = null;
    
    private boolean isServiceRegisterd = false;
    
    /**
     * The connection to the Yama Log Service.
     * 
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "ServiceConnection.onServiceConnected");
            yamaLogService = IYamaLogService.Stub.asInterface(service);

        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "YamaLogActivity: Service now disconnected.");
            yamaLogService = null;
            Toast.makeText(YamaLocActivity.this,
                    R.string.status_service_disconnected, Toast.LENGTH_SHORT)
                    .show();
        }

    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mMapView = (MapView) findViewById(R.id.yama_mapview);
        mMapController = mMapView.getController();
        mMapController.setZoom(YamaLocationProviderConstants.defaultZoom);
        mMapView.setBuiltInZoomControls(true);

        /*
         * Set up the default device_nickname as Build.MODEL
         */
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        if (!settings.contains("device_nickname")) {
            editor.putString("device_nickname", Build.MODEL);
            editor.commit();
        }
        editor.putString("sdcard_logname", DateTimeUtilities.getFilenameFromDateAndTime() + ".log");
        editor.commit();

        createRedrawHandler();

        Intent startIntent = new Intent(YamaLocActivity.this,
                YamaLogService.class);
        startService(startIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tryUnbindLogService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryBindLogService();
    }

    @Override
    protected void onDestroy() {
        stopPollingService();
        tryUnbindLogService();
        Intent startIntent = new Intent(YamaLocActivity.this,
                YamaLogService.class);
        stopService(startIntent);
        super.onDestroy();
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
        if (yamaLogService == null) {
            Log.e(TAG, "YamaLogService is null");
            mi.setTitle(R.string.menu_start);
        } else {
            try {
                if (yamaLogService.isRunning()) {
                    mi.setTitle(R.string.menu_stop);
                    mi.setIcon(R.drawable.ic_menu_stop);
                } else {
                    mi.setTitle(R.string.menu_start);
                    mi.setIcon(R.drawable.ic_menu_mylocation);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "onPrepareOptionsMenu: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retvalue;
        switch (item.getItemId()) {
        case R.id.menu_startstop:
            if (yamaLogService == null) {
                Log.e(TAG, "YamaLogService is null");
                retvalue = true;
                break;
            }

            try {
                if (yamaLogService.isRunning()) {
                    stopPollingService();
                } else {
                    startPollingService();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            retvalue = true;
            break;
        case R.id.menu_save:
            YamaLocActivity.copyLogToSdCard((Context)this);
            Toast.makeText(YamaLocActivity.this, R.string.log_saved_to_sdcard,
                    Toast.LENGTH_SHORT).show();
            retvalue = true;
            break;
        case R.id.menu_quit:
            try {
                if (yamaLogService.isRunning()) {
                    stopPollingService();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                finish();
                retvalue = true;
            }
            break;
        case R.id.menu_preference:
            Intent intent = new Intent();
            intent.setClassName("com.damburisoft.android.yamalocationsrv",
                    "com.damburisoft.android.yamalocationsrv.YamaPreferenceActivity");
            startActivity(intent);
            retvalue = true;
            break;
        default:
            retvalue = super.onOptionsItemSelected(item);
        }

        return retvalue;
    }

    private void createRedrawHandler() {
        Log.d(TAG, "YamaLogActivity.createRedrawHandler");

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
                        tv = (TextView) findViewById(R.id.azimuth_tv);
                        sb.append(getText(R.string.azimuth));
                        sb.append(" ");
                        sb.append((String) msg.obj);
                        break;
                    case YamaLocationProviderConstants.UpdateInfo.LOCATION:
                        tv = (TextView) findViewById(R.id.location_tv);
                        sb.append(getText(R.string.location));
                        sb.append(" ");
                        sb.append((String) msg.obj);
                        break;
                    case YamaLocationProviderConstants.UpdateInfo.DATETIME:
                        tv = (TextView) findViewById(R.id.time_tv);
                        sb.append(getText(R.string.current_time));
                        sb.append(" ");
                        sb.append((String) msg.obj);
                        break;
                    }
                    if (tv != null) {
                        tv.setText(sb.toString());
                    }
                    break;
                case YamaLocationProviderConstants.RedrawType.MapView:
                    if (msg.arg1 == YamaLocationProviderConstants.UpdateInfo.LOCATION) {
                        Location loc = (Location) msg.obj;
                        GeoPoint gp = new GeoPoint(
                                (int) (loc.getLatitude() * 1E6),
                                (int) (loc.getLongitude() * 1E6));
                        mMapController.animateTo(gp);
                        List<Overlay> overlays = mMapView.getOverlays();
                        if (mYamaMapOverlay != null) {
                            overlays.remove(mYamaMapOverlay);
                            mYamaMapOverlay = null;
                        }
                        mYamaMapOverlay = new YamaMapOverlay(getResources()
                                .getDrawable(R.drawable.aquaball_blue), gp);
                        overlays.add(mYamaMapOverlay);
                    }
                    break;
                }
            }
        };

    }

    private void startPollingService() {
        if (yamaLogService == null) {
            Log.e(TAG, "yamaLogService is null");
            return;
        }

        try {
            yamaLogService.startLogService();
            Toast.makeText(YamaLocActivity.this, R.string.status_now_logging,
                    Toast.LENGTH_SHORT).show();

        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(YamaLocActivity.this,
                    R.string.error_unable_to_start_logging, Toast.LENGTH_SHORT)
                    .show();
        }

        mTimer = new Timer();
        checkServiceValues = new TimerTask() {
            @Override
            public void run() {
                Message msg_time = new Message();
                msg_time.what = YamaLocationProviderConstants.RedrawType.TextView;
                msg_time.arg1 = YamaLocationProviderConstants.UpdateInfo.DATETIME;
                msg_time.obj = (Object) DateTimeUtilities.getDateAndTime();
                mRedrawHandler.sendMessage(msg_time);

                if (yamaLogService == null) {
                    Log.e(TAG, "yamaLogService is null");
                    return;
                }

                try {
                    double azimuth = yamaLogService.getAzimuth();
                    if (azimuth != Double.NaN) {
                        Message msg = new Message();
                        msg.what = YamaLocationProviderConstants.RedrawType.TextView;
                        msg.arg1 = YamaLocationProviderConstants.UpdateInfo.AZIMUTH;
                        msg.obj = (Object) Double.toString(azimuth);
                        mRedrawHandler.sendMessage(msg);
                    }

                    Location loc = yamaLogService.getCurrentLocation();
                    if (loc != null) {
                        Message msg_tv = new Message();
                        msg_tv.what = YamaLocationProviderConstants.RedrawType.TextView;
                        msg_tv.arg1 = YamaLocationProviderConstants.UpdateInfo.LOCATION;
                        msg_tv.obj = (Object) getLocationStringForTextView(loc);
                        mRedrawHandler.sendMessage(msg_tv);

                        Message msg_mv = new Message();
                        msg_mv.what = YamaLocationProviderConstants.RedrawType.MapView;
                        msg_mv.arg1 = YamaLocationProviderConstants.UpdateInfo.LOCATION;
                        msg_mv.obj = (Object) loc;
                        mRedrawHandler.sendMessage(msg_mv);
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "Failed to get Azimuth", e);
                    e.printStackTrace();
                }
            }
        };

        mTimer.schedule(checkServiceValues, 0,
                YamaLocationProviderConstants.PollingInterval);

    }

    private void stopPollingService() {

        if (checkServiceValues != null) {
            checkServiceValues.cancel();
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        checkServiceValues = null;
        mTimer = null;

        tryUnbindLogService();

        try {
            yamaLogService.stopLogService();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException: " + e.toString());
            e.printStackTrace();
        }
    }

    
    private boolean tryBindLogService() {
        isServiceRegisterd = bindService(new Intent(this, YamaLogService.class),
                serviceConnection, Context.BIND_AUTO_CREATE); 
        return isServiceRegisterd;
    }

    private void tryUnbindLogService() {
        if (!isServiceRegisterd) {
            return ;
        }
        
        try {
            unbindService(serviceConnection);
            isServiceRegisterd = false;
        } catch (IllegalArgumentException e) {
            Log.d(TAG,
                    "Tried unbind, but service is not registered: "
                            + e.toString());
            e.printStackTrace();
        }

    }

    private String getLocationStringForTextView(Location loc) {
        StringBuffer sb = new StringBuffer();

        sb.append((float) loc.getLongitude());
        sb.append(",");
        sb.append((float) loc.getLatitude());
        sb.append(",");
        sb.append((float) loc.getAccuracy());

        return sb.toString();
    }
    

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public static void copyLogToSdCard(Context context) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }

        String parentDirStr = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator + "akita_no_omatsuri" + File.separator;
        File parentDir = new File(parentDirStr);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        String dst = parentDirStr + YamaPreferenceActivity.getSdcardLogFileName(context); 

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = context.openFileInput(
                    YamaLocationProviderConstants.logFileName).getChannel();
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

}