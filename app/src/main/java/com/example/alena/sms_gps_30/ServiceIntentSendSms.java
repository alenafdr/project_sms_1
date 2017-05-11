package com.example.alena.sms_gps_30;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.alena.sms_gps_30.help_classes.ItemHistory;
import com.example.alena.sms_gps_30.help_classes.SaveInHistoryTask;

public class ServiceIntentSendSms extends IntentService {


    private static final String TAG = "SMS_GPS_2";

    public ServiceIntentSendSms() {
        super("ServiceIntentSmsSend");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String message = intent.getStringExtra("message");
        sendSMS(phoneNumber, message);
    }

    private void sendSMS(String phoneNumber, String message)
    {
        saveMessageInHistory(phoneNumber, message);
        PendingIntent pi = PendingIntent.getActivity(this,0,
                new Intent(),0);
        SmsManager sms= SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber,null, message,pi,null);
        Toast.makeText(getApplicationContext(), "Отправлено сообщение абоненту " + phoneNumber, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Отправил смс по номеру " + phoneNumber + "\nСообщение " + message);
    }

    public void saveMessageInHistory(String phoneNumber, String message){
        if (!message.equals("&GET&")) {
            SaveInHistoryTask historyTask = new SaveInHistoryTask(getApplicationContext(), ItemHistory.TYPE_SENT, message, phoneNumber);
            historyTask.execute();

        }
    }
}
