package com.example.alena.sms_gps_30;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class ReceiverSMS extends BroadcastReceiver {

    private static final String TAG = "SMS_GPS_2";

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
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
            //if (sms_from.equalsIgnoreCase(trackingNumber)) {
            StringBuilder bodyText = new StringBuilder();
            for (int i = 0; i < messages.length; i++) {
                bodyText.append(messages[i].getMessageBody());
            }
            String body = bodyText.toString();
            if (body.charAt(0) == '&') {
                Toast.makeText(context, "From to" + sms_from + "\n- " + body, Toast.LENGTH_LONG).show();
                Intent mIntent = new Intent(context, ServiceIntentSMS.class);
                mIntent.putExtra("sms_body", body);
                mIntent.putExtra("sms_from", sms_from);
                context.startService(mIntent);
                abortBroadcast();
            }

            //}
        }
    }
}