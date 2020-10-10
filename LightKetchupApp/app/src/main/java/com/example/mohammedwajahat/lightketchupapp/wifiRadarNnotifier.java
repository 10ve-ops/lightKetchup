package com.example.mohammedwajahat.lightketchupapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;

public class wifiRadarNnotifier extends Service {

    public WifiManager wifiManager;
    String TAG = MainActivity.TAG;
    boolean notifierStatus = false;
    Ringtone r;
    List<String> ssidList = new ArrayList<>();
    int alarmUserSetVol = 0;
    AudioManager am;
    boolean SILENT_MODE=false;
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
        if (!wifiManager.isWifiEnabled())
        {
            Toast.makeText(this, "Turning Wifi ON", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        mHandler = new Handler();
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                try { wifiManager.setWifiEnabled(true);
                    Log.w(TAG, "Starting Scan...");
                    Log.d(TAG, "Scan res:" + wifiManager.startScan());
                } finally {
                    // 100% guarantee that this always happens, even if
                    // your update method throws an exception
                    mHandler.postDelayed(scanRunnable, 5000);
                }
            }
        };
        scanRunnable.run();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"wifiRadar Service Ended");
        unregisterReceiver(receiver);
        if(r!=null)
        r.stop();
        mHandler.removeCallbacks(scanRunnable);
        wifiManager.setWifiEnabled(false);
        if (am != null) {    //restore user vol
            am.setStreamVolume(AudioManager.STREAM_RING,alarmUserSetVol,0);
        }
        super.onDestroy();
    }



     final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
              List<ScanResult> results = wifiManager.getScanResults();
              Log.d(TAG, "********WIFI-SCAN RESULTS*********");
              Log.d(TAG, String.valueOf(results));
              Log.d(TAG, ">>>>>>>>>" + results.toString() + "<<<<<<<<<");
            if(results.toString().contains("Ghaznavi")||results.toString().contains("Wan"))
                    setNotifierStatus(true);
                else{
                    setNotifierStatus(false);
                }
        }
    };



    void setNotifierStatus(boolean status){
        if(status&&!notifierStatus){
        if(!SILENT_MODE){
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(this, alarm);
        r.play();
        }
            Log.d(TAG,"**********ALARM RINGING STARTS*********");
        }else if(status){
            Log.d(TAG,"********ALARM RETAINS********");
        }else{
            Log.d(TAG,"********ALARM DISABLED*******");
            if (r!=null&&r.isPlaying())
            r.stop();
        }
        notifierStatus = status;
    }


}
