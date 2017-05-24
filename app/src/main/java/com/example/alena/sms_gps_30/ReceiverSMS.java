package com.example.alena.sms_gps_30;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class ReceiverSMS extends BroadcastReceiver {

    private static final String TAG = "SMS_GPS_2";

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

            if (!isWhiteListEnable() || isNumberInWhiteList(sms_from)) {
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
            }
        }
    }

    public boolean isWhiteListEnable(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean("white_list", false);
    }

    public boolean isNumberInWhiteList(String number) {
        String numberForSearch;
        if(number.contains("+")){
            numberForSearch = number.substring(2);
        } else {
            numberForSearch = number.substring(1);
        }

        SharedPreferences sPref = mContext.getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        String whiteList = sPref.getString(FragmentWhiteList.WHITE_NUMBERS, "");
        return whiteList.contains(numberForSearch);
    }
}
