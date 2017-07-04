package com.aiprof.alena.get_gps_from_sms;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServiceIntentSaveLOGs extends IntentService {

    final String TAG = ActivityMap.TAG + " saveLogs";
    public static String TIME_LOGS = "time logs";
    public static String TEXT_LOGS = "text logs";
    public static String FILE_NAME = "logs";

    public ServiceIntentSaveLOGs() {
        super("ServiceIntentSaveLOGs");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long timeLong = intent.getLongExtra(TIME_LOGS, System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
        String time = sdf.format(new Date(timeLong));
        String text = intent.getStringExtra(TEXT_LOGS);
        saveFile(time, text);
    }

    private void saveFile(String time, String text) {
        /*try {
            OutputStream outputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            osw.append(time + " " + text + "\r\n");
            osw.close();
        } catch (Throwable t) {
            Log.d(TAG, t.toString());
        }*/
        Log.d(TAG, "save " + text);
        File file = new File(getApplicationContext().getFilesDir(),"LOGS");
        if(!file.exists()){
            file.mkdir();
            Log.d(TAG, "create file");
        }


        try {
            File gpxfile = new File(file, FILE_NAME);
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(time + " " + text + "\r\n");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }
}
