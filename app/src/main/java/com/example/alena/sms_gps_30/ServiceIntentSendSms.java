package com.example.alena.sms_gps_30;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.alena.sms_gps_30.help_classes.ItemHistory;
import com.example.alena.sms_gps_30.help_classes.SaveInHistoryTask;

public class ServiceIntentSendSms extends Service {

    private static final String TAG = ActivityMap.TAG + " sendSMS";
    BroadcastReceiver broadcastReceiverSend;
    BroadcastReceiver broadcastReceiverDelivered;

    public ServiceIntentSendSms() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String message = intent.getStringExtra("message");
        sendSMS(phoneNumber, message);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendSMS(String phoneNumber, String message)
    {
        saveMessageInHistory(phoneNumber, message);
        String SENT="SMS_SENT";
        String DELIVERED="SMS_DELIVERED";
        PendingIntent sentPI= PendingIntent.getBroadcast(this,0,new Intent(SENT),0);//отправка смс

        PendingIntent deliveredPI= PendingIntent.getBroadcast(this,0,new Intent(DELIVERED),0);//отчет о доставке
        broadcastReceiverSend = new ReceiverSend();
        registerReceiver(broadcastReceiverSend, new IntentFilter(SENT));

        broadcastReceiverDelivered = new ReceiverDelivered();
        registerReceiver(broadcastReceiverDelivered, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();

        sms.sendTextMessage(phoneNumber,null, message,sentPI,deliveredPI);
        Log.d(TAG, "Отправил смс по номеру " + phoneNumber + "\nСообщение " + message);
    }

    public void saveMessageInHistory(String phoneNumber, String message){
        if (!message.equals("&GET&") && message.contains("SHOW")) {
            SaveInHistoryTask historyTask = new SaveInHistoryTask(getApplicationContext(), ItemHistory.TYPE_SENT, message, phoneNumber);
            historyTask.execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverSend);
        unregisterReceiver(broadcastReceiverDelivered);
        Log.d(TAG, "ServiceSendSms уничтожен");
    }

    class ReceiverSend extends BroadcastReceiver{

        public ReceiverSend() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(getResultCode())
            {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(),"SMS sent",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SMS sent");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(getBaseContext(),"Generic failure",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Generic failure");
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(getBaseContext(),"No service",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No service");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(getBaseContext(),"Null PDU",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Null PDU");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(getBaseContext(),"Radio off",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Radio off");
                    break;
            }
        }
    }

    class ReceiverDelivered extends BroadcastReceiver{
        @Override
        public void onReceive(Context arg0, Intent arg1){
            switch(getResultCode())
            {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(),"SMS delivered",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SMS delivered");
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getBaseContext(),"SMS not delivered",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SMS not delivered");
                    break;
            }
            stopSelf();
        }
    }
}
