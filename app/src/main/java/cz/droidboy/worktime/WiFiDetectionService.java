package cz.droidboy.worktime;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.squareup.leakcanary.RefWatcher;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

import cz.droidboy.worktime.data.WorkTimeManager;
import cz.droidboy.worktime.model.WorkTime;
import cz.droidboy.worktime.util.ProximityUtils;
import cz.droidboy.worktime.wifi.range.ProximityScanner;
import cz.droidboy.worktime.wifi.range.ScanFilter;

/**
 * @author Jonas Sevcik
 */
public class WiFiDetectionService extends Service implements ProximityScanner.MonitoringListener {

    private static final String TAG = WiFiDetectionService.class.getSimpleName();

    public static final String UPDATE_COMMAND_KEY = "update";
    public static final int UPDATE_COMMAND = 0;

    private static final int UPDATE_INTERVAL = 60_000; //millis
    private static final int NOTIFICATION_ID = 0;

    private WorkTimeManager mWorkTimeManager;
    private ProximityScanner scanner;
    private WifiManager.WifiLock wifiLock;
    private BroadcastReceiver wifiDisabledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_DISABLING || state == WifiManager.WIFI_STATE_DISABLED) {
                    stopSelf();
                }
            }
        }
    };

    private WorkTime mWorkTime = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mWorkTimeManager = new WorkTimeManager(this);

        if (Build.VERSION.SDK_INT < 18) {
            acquireWifiLock();
            registerReceiver(wifiDisabledReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }

        scanner = new ProximityScanner(this);
        scanner.setMonitoringListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        scanner.startMonitoringAPs(ProximityUtils.prepareFilter(this), UPDATE_INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        scanner.stopMonitoringAPs();

        if (Build.VERSION.SDK_INT < 18) {
            releaseWifiLock();
            unregisterReceiver(wifiDisabledReceiver);
        }

        if (mWorkTime != null) {
            endWorkTime();
        }

        RefWatcher refWatcher = App.getRefWatcher(this);
        refWatcher.watch(this);
    }

    private void acquireWifiLock() {
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = manager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, TAG);
        wifiLock.setReferenceCounted(false);
        wifiLock.acquire();
    }

    private void releaseWifiLock() {
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
            wifiLock = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onEnterRegion(List<ScanResult> results) {
        Log.d(TAG, "onEnterRegion");
        if (mWorkTime != null) {
            endWorkTime();
        }
        mWorkTime = new WorkTime();
        mWorkTime.setStartDate(DateTime.now());
        mWorkTimeManager.createWorkTime(mWorkTime);
    }

    @Override
    public void onDwellRegion(List<ScanResult> results) {
        Log.d(TAG, "onDwellRegion");
        if (mWorkTime != null) {
            updateEndTime();
        } else {
            throw new IllegalStateException("Enter region is missing");
        }
    }

    @Override
    public void onExitRegion(ScanFilter filter) {
        Log.d(TAG, "onExitRegion");
        if (mWorkTime != null) {
            endWorkTime();
        }
    }

    private void updateEndTime() {
        mWorkTime.setEndDate(DateTime.now());
        mWorkTimeManager.updateWorkTime(mWorkTime);
    }

    private void endWorkTime() {
        updateEndTime();
        mWorkTime = null;
    }

}
