package com.example.alena.sms_gps_30;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiverSMS extends BroadcastReceiver {

    private static final String TAG = ActivityMap.TAG + " receiver";

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.d(TAG, "Перехватил смс");
        if (intent != null && intent.getAction() != null &&
                ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pduArray.length];
            for (int i = 0; i < pduArray.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
            }

            String sms_from = messages[0].getDisplayOriginatingAddress();
            Log.d(TAG, "SmsBroadcast - From: " + sms_from);

            StringBuilder bodyText = new StringBuilder();
            for (int i = 0; i < messages.length; i++) {
                bodyText.append(messages[i].getMessageBody());
            }
            String body = bodyText.toString();

            saveFile("From: " + sms_from + " - " + body);
            if (body.charAt(0) == '&') {
                Intent mIntent = new Intent(context, ServiceIntentSMS.class);
                mIntent.putExtra("sms_body", body);
                mIntent.putExtra("sms_from", sms_from);
                context.startService(mIntent);
                Log.d(TAG, "запустил ServiceIntentSMS");
                abortBroadcast();
            }

        }
    }

    private void saveFile(String text) {
        String dirPath =  Environment.getExternalStorageDirectory() + File.separator + "LOGS" +File.separator;
        String name = "Logs.txt";
        File projDir = new File(dirPath);
        if (!projDir.exists()) projDir.mkdir();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
        String time = sdf.format(new Date(System.currentTimeMillis()));

        try {
            File logfile = new File(dirPath, name);
            FileWriter writer = new FileWriter(logfile,true);
            writer.write(time + " " + text + "\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
