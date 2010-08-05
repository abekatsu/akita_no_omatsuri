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
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class YamaLocActivity extends MapActivity {
    private final static String TAG = "YamaLocActivity";
    private IYamaLogService yamaLogService = null;
    private boolean isYamaLogServiceConnected = false;
    private boolean isNewLoggingRequested = false;

    private MapView mMapView;
    private MapController mMapController = null;
    private YamaMapOverlay mYamaMapOverlay = null;

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
            yamaLogService = IYamaLogService.Stub.asInterface(service);
            if (isNewLoggingRequested) {
                isNewLoggingRequested = false;
                try {
                    isYamaLogServiceConnected = yamaLogService
                            .startLogService();
                    Toast.makeText(YamaLocActivity.this,
                            R.string.status_now_logging, Toast.LENGTH_SHORT)
                            .show();
                } catch (RemoteException e) {
                    Toast.makeText(YamaLocActivity.this,
                            R.string.error_unable_to_start_logging,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Unable to start logging", e);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "YamaLogActivity: Service now disconnected.");
            try {
                yamaLogService.stopLogService();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            yamaLogService = null;
            isYamaLogServiceConnected = false;
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

        createRedrawHandler();
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
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
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
        if (isMonitoring()) {
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
                isNewLoggingRequested = true;
                startPollingService();
            } else {
                isNewLoggingRequested = false;
                stopPollingService();
            }
            retvalue = true;
            break;
        case R.id.menu_save:
            copyLogToSdCard();
            Toast.makeText(YamaLocActivity.this, R.string.log_saved_to_sdcard,
                    Toast.LENGTH_SHORT).show();
            retvalue = true;
            break;
        case R.id.menu_quit:
            finish();
            retvalue = true;
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
    
    /**
     * @return true if the activity is bound to the track recording service and
     * the service is recording a track.
     */
    private boolean isMonitoring() {
        if (yamaLogService == null) {
            return false;
        }

        try {
            return yamaLogService.isMonitoring();
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception.", e);
            return false;
        }
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
        if (isNewLoggingRequested) {
            if (yamaLogService == null) {
                Intent startIntent = new Intent(YamaLocActivity.this,
                        YamaLogService.class);
                startService(startIntent);
                tryBindLogService();
            } else {
                // TODO
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
                        return;
                    }

                    double azimuth;
                    try {
                        azimuth = yamaLogService.getAzimuth();
                        if (azimuth != Double.NaN) {
                            Message msg = new Message();
                            msg.what = YamaLocationProviderConstants.RedrawType.TextView;
                            msg.arg1 = YamaLocationProviderConstants.UpdateInfo.AZIMUTH;
                            msg.obj = (Object) Double.toString(azimuth);
                            mRedrawHandler.sendMessage(msg);
                        }
                    } catch (RemoteException e) {
                        Log.d(TAG, "Failed to get Azimuth", e);
                        e.printStackTrace();
                    }

                    Location loc;
                    try {
                        loc = yamaLogService.getCurrentLocation();
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
                        Log.d(TAG, "Failed to get Location", e);
                        e.printStackTrace();
                    }
                }
            };

            mTimer.schedule(checkServiceValues, 0,
                    YamaLocationProviderConstants.PollingInterval);
        }
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
            stopService(new Intent(YamaLocActivity.this, YamaLogService.class));
        } catch (SecurityException e) {
            Log.e(TAG,
                    "Encountered a security exception when trying to stop service.",
                    e);
        }
        yamaLogService = null;
    }

    private void copyLogToSdCard() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }

        String dst = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + DateTimeUtilities.getFilenameFromDateAndTime() + ".log";

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = openFileInput(
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

    private void tryBindLogService() {
        if (isNewLoggingRequested) {
            isYamaLogServiceConnected = bindService(new Intent(this,
                    YamaLogService.class), serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void tryUnbindLogService() {
        if (!isYamaLogServiceConnected) {
            return;
        }

        unbindService(serviceConnection);
        isYamaLogServiceConnected = false;
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
}