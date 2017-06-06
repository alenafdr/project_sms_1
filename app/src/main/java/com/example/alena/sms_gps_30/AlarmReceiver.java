package com.example.alena.sms_gps_30;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private final String TAG = ActivityMap.TAG + " Alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm");
        if(!ServiceGPS.sentGPS) {
            ServiceGPS.sentGPS = true;
            Log.d(TAG, "Запустил selectLocation");
            ServiceGPS.myLocationListener.selectLocation();
        }
    }
}
