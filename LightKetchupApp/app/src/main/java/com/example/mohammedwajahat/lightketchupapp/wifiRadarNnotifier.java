package com.example.mohammedwajahat.lightketchupapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class wifiRadarNnotifier extends Service {

    public WifiManager wifiManager;
    String TAG = MainActivity.TAG;
    boolean notifierStatus = false;
    Ringtone r;
    List<String> ssidList = new ArrayList<>();
    int alarmUserSetVol = 0;
    AudioManager am;
    boolean SILENT_MODE=false;
    public boolean wifi_status = false;
    private BroadcastReceiver receiver;
    public boolean SCAN_PERMISSION_GRANTED = false;

    public wifiRadarNnotifier() {
    }


    @Override
    public void onCreate() {
        wifiManager = (WifiManager)
                this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(receiver, intentFilter);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            alarmUserSetVol = am.getStreamVolume(AudioManager.STREAM_RING);
            am.setStreamVolume(AudioManager.STREAM_RING,am.getStreamMaxVolume(AudioManager.STREAM_RING),0);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public Handler mHandler;
    public Runnable scanRunnable;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        assert wifiManager != null;
        wifi_status = wifiManager.isWifiEnabled();
        if (!wifi_status) {
            Toast.makeText(this, "Turning Wifi ON", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        mHandler = new Handler();
        scanRunnable = () -> {
            try {
                Log.w(TAG, "Starting Scan...");
                Log.d(TAG, "Scan res:" + wifiManager.startScan());
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(scanRunnable, 5000);
            }
        };
        scanRunnable.run(); //Start scanning for wifi
        return START_STICKY;
    }

    }
