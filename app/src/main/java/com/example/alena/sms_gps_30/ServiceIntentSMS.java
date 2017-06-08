package com.example.alena.sms_gps_30;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.alena.sms_gps_30.help_classes.ItemHistory;
import com.example.alena.sms_gps_30.help_classes.SaveInHistoryTask;
import com.google.android.gms.maps.model.LatLng;

public class ServiceIntentSMS extends IntentService {

    public static final String ACTION = ServiceIntentSMS.class.getName() + "ERR";
    final String command_get = "GET";
    final String command_show = "SHOW";
    final String command_err = "ERR";
    private static final String TAG = ActivityMap.TAG + " serviceSMS";
    String sms_body;
    String sms_from;

    public ServiceIntentSMS() {
        super("ServiceIntentSMS");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sms_body = intent.getExtras().getString("sms_body");
        sms_from = intent.getExtras().getString("sms_from");
        String[] messages = sms_body.split("&");

        Log.d(TAG, "стартовал ServiceIntentSMS");

        if (messages[1].equals(command_get)) {
            //запустить сервис с запросом GPS
            if (!isWhiteListEnable() || isNumberInWhiteList(sms_from)){
                Intent intentServiceGPS = new Intent(getApplicationContext(), ServiceGPS.class);
                intentServiceGPS.putExtra("phoneNumber", sms_from);
                getApplicationContext().startService(intentServiceGPS);
                Log.d(TAG, "стартовал сервис");
            } else {
                Log.d(TAG, "номера нет в белом списке");
            }
        }

        if (messages[1].equals(command_show)) {
            saveMessageInHistory(sms_from, sms_body); //сохраняет в истории, настройках и открывает карту
        }

        if (messages[1].equals(command_err)) {
            Intent intentMap = new Intent(getApplicationContext(), ActivityMap.class);
            intentMap.setAction(ACTION);
            intentMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentMap);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void saveMessageInHistory(String phoneNumber, String message){

        SaveInHistoryTask historyTask = new SaveInHistoryTask(getApplicationContext(), ItemHistory.TYPE_RECEIVED, message, phoneNumber);
        historyTask.execute();
    }

    public boolean isWhiteListEnable(){
        SharedPreferences sPref = getApplicationContext().getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        return sPref.getBoolean(FragmentSettings.CHECK_BOX_WHITE_LIST, false);
    }

    public boolean isNumberInWhiteList(String number) {
        String numberForSearch;
        if(number.contains("+")){
            numberForSearch = number.substring(2);
        } else {
            numberForSearch = number.substring(1);
        }

        SharedPreferences sPref = getApplication().getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        String whiteList = sPref.getString(FragmentWhiteList.WHITE_NUMBERS, "");

        /*Log.d(TAG, "number " + number + "whitelist " + whiteList);*/
        return whiteList.contains(numberForSearch);
    }
}
