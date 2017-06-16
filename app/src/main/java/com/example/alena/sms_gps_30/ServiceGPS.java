package com.example.alena.sms_gps_30;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServiceGPS extends Service {

    public static boolean sentGPS; //Флаг "Координаты отправлены"
    public static String formatForTine = "yy.MM.dd, HH:mm";
    public static MyLocationListener myLocationListener;
    public static final String ACTION = ServiceGPS.class.getName() + ".ACTION";


    private final String TAG = ActivityMap.TAG + " GPS";
    private String phoneNumber;
    private double timeStart; //Для отслеживания времени работы сервиса
    private final long maxTimeWaitAnswer = 45 * 1000;

    public ServiceGPS() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "служба GPS создана");

        sentGPS = false;

        myLocationListener = new MyLocationListener();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent broadcastIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        broadcastIntent.setAction(ACTION);
        PendingIntent piAlarm = PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_ONE_SHOT);

        long timeForAlarm = System.currentTimeMillis() + maxTimeWaitAnswer;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timeForAlarm, piAlarm);
            am.setAlarmClock(alarmClockInfo, piAlarm);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            am.setExact(AlarmManager.RTC_WAKEUP,timeForAlarm, piAlarm);
        else
            am.set(AlarmManager.RTC_WAKEUP, timeForAlarm, piAlarm);


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String time = sdf.format(new Date(timeForAlarm));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        phoneNumber = intent.getStringExtra("phoneNumber");
        timeStart = System.currentTimeMillis();
        return Service.START_STICKY ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myLocationListener = null;
        Log.d(TAG, "!!!!!!!служба GPS уничтожена ");
    }

    public class MyLocationListener implements LocationListener {
        private Location lastLocationGPS;
        private Location lastLocationNetwork;
        private Location currentLocationGPS;
        private Location currentLocationNetwork;
        private LocationManager locationManager;
        private int countChangeLocation;

        MyLocationListener(){
            locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);

            countChangeLocation = 0;

            //получаем последнее известное местоположение
            try {
                lastLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(TAG, "Последнее известное GPS" + Thread.currentThread().getName());
            } catch (SecurityException e) {
                Log.d(TAG, "Последнее известное GPS" + e.toString());
            } catch (Exception e) {
                Log.d(TAG, "Последнее известное GPS" + e.toString());
            }
            try {
                lastLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d(TAG, "Последнее известное нетворк" + Thread.currentThread().getName());
            } catch (SecurityException e) {
                Log.d(TAG, "Последнее известное нетворк" + e.toString());
            } catch (Exception e) {
                Log.d(TAG, "Последнее известное нетворк" + e.toString());
            }

            //Подписываемся на обновления
            listenerRegistration(LocationManager.GPS_PROVIDER);
            listenerRegistration(LocationManager.NETWORK_PROVIDER);

        }

        private void listenerRegistration(String provider) {
            try {
                locationManager.requestLocationUpdates(provider, 0, 0, this);
                Log.d(TAG, "Подписались на " + provider + Thread.currentThread().getName());
                Toast.makeText(getApplicationContext(), "Подписались на " + provider, Toast.LENGTH_LONG).show();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        synchronized void stopUsingGPS(){
            if(locationManager != null){
                try {

                    locationManager.removeUpdates(this);
                    locationManager = null;
                    Log.d(TAG, "!!!!!!!!!!!stopUsingGPS " + Thread.currentThread().getName());

                } catch (SecurityException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d(TAG, "stopUsingGPS" + e.toString());
                }
            }
        }

        public synchronized void selectLocation(){
            try {
                sentGPS = true;
                int provider = 0;
                Location locationForSend = null;
                if (currentLocationGPS != null) {
                    locationForSend = currentLocationGPS;
                    provider = 1;
                } else if (currentLocationNetwork != null) {
                    locationForSend = currentLocationNetwork;
                    provider = 2;
                } else {
                    if ((lastLocationGPS != null) && (lastLocationNetwork != null)) {
                        if (lastLocationGPS.getTime() > lastLocationNetwork.getTime()) {
                            locationForSend = lastLocationGPS;
                            provider = 3;
                        } else {
                            locationForSend = lastLocationNetwork;
                            provider = 4;
                        }
                    } else {
                        if (lastLocationGPS != null) {
                            locationForSend = lastLocationGPS;
                            provider = 3;
                        }
                        if (lastLocationNetwork != null) {
                            locationForSend = lastLocationNetwork;
                            provider = 4;
                        }
                    }
                }
                sendGPS(locationForSend, provider) ;
                stopUsingGPS();
            } catch (Exception e) {
                e.printStackTrace();
                saveFile(e.toString());
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                currentLocationGPS = location;
                Toast.makeText(getApplicationContext(), "Получил новое местоположение " + location.getProvider(), Toast.LENGTH_LONG).show();
                if (!sentGPS) {
                    selectLocation();
                }
            } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                currentLocationNetwork = location;
                countChangeLocation++;
                if(countChangeLocation > 1){
                    selectLocation();
                }
            }
            Log.d(TAG, "Получил новое местоположение " + location.getProvider() + " " + Thread.currentThread().getName());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private synchronized void sendGPS(Location location, int provider) {

        String message;

        if (location != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(formatForTine, Locale.US);
            String time = sdf.format(new Date(location.getTime()));
            float latitude =(float) location.getLatitude();
            float longitude = (float) location.getLongitude();
            message = "&SHOW"
                    + "&" + time
                    + "&" + provider
                    + "&" + location.getAccuracy()
                    + "&" + latitude
                    + "&" + longitude
                    + "&" +"https://maps.google.com/maps?q="+  latitude + "," + longitude;

            saveFile(message);

            Log.d(TAG, "Сформировал смс, поток " + Thread.currentThread().getName());
            Intent smsSendIntentService = new Intent(getApplicationContext(), ServiceIntentSendSms.class);
            smsSendIntentService.putExtra("message", message);
            smsSendIntentService.putExtra("phoneNumber", phoneNumber);
            getApplicationContext().startService(smsSendIntentService);
            Log.d(TAG, "!!!!!!!!!!!stopSelf " + Thread.currentThread().getName());
        } else {
            int timeForAnswer =(int) (System.currentTimeMillis() - timeStart) / 1000;
            LocationManager locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
            message = "&ERR&location is null, GPS " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) + " " + timeForAnswer;

            saveFile(message);

            Intent smsSendIntentService = new Intent(getApplicationContext(), ServiceIntentSendSms.class);
            smsSendIntentService.putExtra("message", message);
            smsSendIntentService.putExtra("phoneNumber", phoneNumber);
            getApplicationContext().startService(smsSendIntentService);
        }
        stopSelf();
    }

    private void saveFile(String text) {
        Intent logsIntent = new Intent(getApplicationContext(), ServiceIntentSaveLOGs.class);
        logsIntent.putExtra(ServiceIntentSaveLOGs.TIME_LOGS, System.currentTimeMillis());
        logsIntent.putExtra(ServiceIntentSaveLOGs.TEXT_LOGS, text);
        getApplicationContext().startService(logsIntent);
    }
}