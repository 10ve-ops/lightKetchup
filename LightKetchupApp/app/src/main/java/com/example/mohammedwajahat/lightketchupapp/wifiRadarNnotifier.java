package com.example.mohammedwajahat.lightketchupapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import androidx.core.app.NotificationCompat;

public class wifiRadarNnotifier extends Service {

    private static final String CHANNEL_ID = "LightKecthupScanNotif";
    public WifiManager wifiManager;
    String TAG = MainActivity.TAG;
    boolean notifierStatus = false;
    Ringtone r;
    List<String> ssidList = new ArrayList<>();
    int alarmUserSetVol = 0, userNotifStatus = 0;
    AudioManager am;
    boolean SILENT_MODE=false;
    private boolean wifi_status = false;


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            Log.d(TAG, "********WIFI-SCAN RESULTS*********");
            Log.d(TAG, String.valueOf(results));
            Log.d(TAG, ">>>>>>>>>" + results.toString() + "<<<<<<<<<");
            setNotifierStatus(results.toString().contains("Shahzaib zia") || results.toString().contains("Duha Khizar"));
        }
    };



    public wifiRadarNnotifier() {
    }


    @Override
    public void onCreate() {
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        userNotifStatus = mNotificationManager.getCurrentInterruptionFilter();
        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        wifiManager = (WifiManager)
                this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(receiver, intentFilter);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            alarmUserSetVol = am.getStreamVolume(AudioManager.STREAM_RING);
            am.setStreamVolume(AudioManager.STREAM_RING,am.getStreamMaxVolume
                    (AudioManager.STREAM_RING),0);
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

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(getText(R.string.notification_message))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        assert wifiManager != null;
        wifi_status = wifiManager.isWifiEnabled();
        if (!wifi_status)
        {
            Toast.makeText(this, "Turning Wifi ON", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        mHandler = new Handler();
        scanRunnable = () -> {
            try {
                Log.d(TAG, "Scan start attempt with: " + (wifiManager.startScan()?
                        "Success return":"Failure return"));
            } finally {
                mHandler.postDelayed(scanRunnable, 10000);
            }
        };
        scanRunnable.run();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"wifiRadar Service Ended");
        NotificationManager mNotifMgr = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifMgr.setInterruptionFilter(userNotifStatus);
        unregisterReceiver(receiver);
        if(r!=null)
        r.stop();
        mHandler.removeCallbacks(scanRunnable);
        wifiManager.setWifiEnabled(wifi_status);
        if (am != null) {    //restore user vol
            am.setStreamVolume(AudioManager.STREAM_RING,alarmUserSetVol,0);
        }
        super.onDestroy();
    }







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
