package com.example.alena.sms_gps_30;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServiceGPS extends Service {
    private static String SERVICE_ACTION = "com.example.alena.smsgps.ServiceGPS";
    final String TAG = "SMS_GPS_2";
    final int maxTimeWaitAnswer = 120; //Максимальное время ожидания координат
    MyLocationListener myLocationListener;
    String phoneNumber;
    String message;

    boolean sentGPS; //Флаг "Координаты отправлены"


    public ServiceGPS() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myLocationListener = new MyLocationListener();
        Log.d(TAG, "!!!!!!!служба GPS запущена " + Thread.currentThread().getName());

        sentGPS = false;
        timerThread timer = new timerThread(myLocationListener);
        Log.d(TAG, "служба GPS создана");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myLocationListener = null;
        Log.d(TAG, "!!!!!!!служба GPS уничтожена " + Thread.currentThread().getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        phoneNumber = intent.getStringExtra("phoneNumber");
        return Service.START_STICKY ;
    }

    public class MyLocationListener implements LocationListener {
        private Location lastLocationGPS;
        private Location lastLocationNetwork;
        private Location currentLocationGPS;
        private Location currentLocationNetwork;
        private LocationManager locationManager;

        MyLocationListener(){
            locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        public void listenerRegistration(String provider) {
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
                    if (this != null) {
                        locationManager.removeUpdates(this);
                        locationManager = null;
                        Log.d(TAG, "!!!!!!!!!!!stopUsingGPS " + Thread.currentThread().getName());
                    }
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

    public synchronized void sendGPS(Location location, int provider) {

        if (location != null) {
            //**************УДАЛИТЬ************
           /* Calendar currentTime = Calendar.getInstance();
            Date dateLocation = new Date(location.getTime());*/
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMM, HH:mm", Locale.US);
            String time = sdf.format(new Date(location.getTime()));

           /* String time = currentTime.get*/
            float latitude =(float) location.getLatitude();
            float longitude = (float) location.getLongitude();
            //**************УДАЛИТЬ************
            message = "&SHOW"
                    + "&" + time
                    + "&" + provider
                    + "&" + location.getAccuracy()
                    + "&" + latitude
                    + "&" + longitude
                    + "&" +"https://maps.google.com/maps?q="+  latitude + "," + longitude;

            Log.d(TAG, "Сформировал смс, поток " + Thread.currentThread().getName());
            Intent smsSendIntentService = new Intent(getApplicationContext(), ServiceIntentSendSms.class);
            smsSendIntentService.putExtra("message", message);
            smsSendIntentService.putExtra("phoneNumber", phoneNumber);
            getApplicationContext().startService(smsSendIntentService);
            Log.d(TAG, "!!!!!!!!!!!stopSelf " + Thread.currentThread().getName());

        }
        stopSelf();
    }

    class timerThread implements Runnable {
        Thread t;
        MyLocationListener LocationListener;
        timerThread(MyLocationListener myLocationListener) {
            t = new Thread(this, "Поток таймера");
            Log.d(TAG, "Поток таймера создан");
            LocationListener = myLocationListener;
            t.start();
        }
        @Override
        public void run() {
            int count = 0;
            do {
                try {
                    Thread.sleep(1000);
                    count++;
                    Log.d(TAG, "таймер " + count + "sentGPS " + sentGPS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while ((count < maxTimeWaitAnswer) && (!sentGPS));
            if (!sentGPS) {
                LocationListener.selectLocation();
            }
        }
    }

}