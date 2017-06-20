package com.example.alena.sms_gps_30;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.alena.sms_gps_30.help_classes.Contact;
import com.example.alena.sms_gps_30.help_classes.ContactsAdapterAutoComplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityMap extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        FragmentHistory.onSomeEventListener,
        FragmentSettings.onSomeEventListener,
        FragmentWhiteList.onSomeEventListener
{


    public static int menuItemId;
    public static boolean transitionSettingsWhiteList = false;//запущен ли белый список из настроек

    public static final String VERSION = "version";
    public static final String TAG = "SMSGPS3.0";
    public static final String APP_PREFERENCES = "com.example.alena.sms_gps_30";
    public static final String LAST_NAME = "last name";
    public static final String LAST_NUMBER = "last number";
    public static final String LAST_DATA = "last data";
    public static final String LAST_LAT = "last latitude";
    public static final String LAST_LNG = "last longitude";
    public static final String LAST_ACCURACY = "last accuracy";
    public static final String ACTION = ActivityMap.class.getName() + "ACTION";

    private ProgressBar progressBar;
    private ImageButton imageButtonGet;
    private SharedPreferences sPref;
    private DrawerLayout mDrawerLayout;
    private AutoCompleteTextView mAutoCompleteTextView;
    private GoogleMap mGoogleMap;
    private boolean isAutoCompleteTextView = false;
    private FragmentHistory mFragmentHistory;
    private FragmentWhiteList mFragmentWhiteList;
    private FragmentSettings mFragmentSettings;
    private FragmentSendLogs mFragmentSendLogs;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.menu_map);
        menuItemId = R.id.menu_map;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) { //закрывает клавиатуру если открыта шторка
                super.onDrawerOpened(drawerView);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        };
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_view);
        initAutoCompleteTextView();

        imageButtonGet = (ImageButton) findViewById(R.id.imageButtonGet);
        initImageButton(imageButtonGet);
        ImageButton imageButtonFindMeGet = (ImageButton) findViewById(R.id.imageButtonFindMe);
        initImageButton(imageButtonFindMeGet);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        initProgressBar();

        initMap();
        mFragmentHistory = new FragmentHistory();
        mFragmentWhiteList = new FragmentWhiteList();
        mFragmentSettings = new FragmentSettings();
        mFragmentSendLogs = new FragmentSendLogs();

        saveVersion();
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent " + intent.getAction());
        String action = intent.getAction();
        imageButtonGet.setVisibility(ImageButton.VISIBLE);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        if (!action.equals(ServiceIntentSMS.ACTION)){
            showLastLocation();
        }

        if (mFragmentHistory.isVisible()) {
            mFragmentHistory.updateTableHistory();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (id == R.id.menu_map) {

            ft.setCustomAnimations(0, R.animator.fragment_exit);
            ft.remove(mFragmentHistory);
            ft.remove(mFragmentSettings);
            ft.remove(mFragmentWhiteList);
            ft.remove(mFragmentSendLogs);
            ft.commit();
            menuItemId = R.id.menu_map;
        }

        if (id == R.id.menu_history){

            if(menuItemId == R.id.menu_history){
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(FragmentHistory.llBottomSheet);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            ft.setCustomAnimations(R.animator.fragment_enter, 0);
            ft.replace(R.id.container, mFragmentHistory);
            ft.commit();
            menuItemId = R.id.menu_history;
        }

        if (id == R.id.menu_white_list) {
            ft.setCustomAnimations(R.animator.fragment_enter, 0);
            ft.replace(R.id.container, mFragmentWhiteList);
            ft.commit();
            menuItemId = R.id.menu_white_list;
        }

        if (id == R.id.menu_settings) {
            ft.setCustomAnimations(R.animator.fragment_enter, 0);
            ft.replace(R.id.container, mFragmentSettings);
            ft.commit();
            menuItemId = R.id.menu_settings;
        }

        if (id == R.id.menu_logs) {
            ft.setCustomAnimations(R.animator.fragment_enter, 0);
            ft.replace(R.id.container, mFragmentSendLogs);
            ft.commit();
            menuItemId = R.id.menu_logs;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        UiSettings mSettingsMap = googleMap.getUiSettings();
        mSettingsMap.setCompassEnabled(true); //Включили компас
        mSettingsMap.setZoomControlsEnabled(true); //Включили кнопки зума
        mSettingsMap.setMyLocationButtonEnabled(true); //Включили кнопку определения своего местоположения
        mSettingsMap.setAllGesturesEnabled(true);
        mSettingsMap.setMapToolbarEnabled(true);

        showLastLocation();

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void someEvent(String s) {

        if (!s.equals("end")) {
            String[] strings = s.split("#");
        /*for (int i = 0; i < strings.length; i++) {
            Log.d(TAG, String.valueOf(strings[i]));
        }*/
            saveName(strings[0]);
            saveNumber(strings[1]);
            saveData(strings[2]);
            saveAccuracy(Float.valueOf(strings[3]));
            saveLatitude(Float.valueOf(strings[4]));
            saveLongitude(Float.valueOf(strings[5]));
            showLastLocation();
        } else {
            menuItemId = R.id.menu_map;
            navigationView.setCheckedItem(R.id.menu_map);
        }
    }

    public void onBackPressed() {

        if (transitionSettingsWhiteList) { //проверяет, открыт ли белый лист через настройки
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit);
            ft.replace(R.id.container, mFragmentSettings);
            ft.commit();
            menuItemId = R.id.menu_settings;
            transitionSettingsWhiteList = false;
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (menuItemId != R.id.menu_map) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.fragment_exit);
            ft.remove(mFragmentHistory);
            ft.remove(mFragmentSettings);
            ft.remove(mFragmentWhiteList);
            ft.remove(mFragmentSendLogs);
            ft.commit();
            menuItemId = R.id.menu_map;
            navigationView.setCheckedItem(R.id.menu_map);
        } else {
            finish();
        }
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    private void initImageButton(ImageButton imageButton) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAutoCompleteTextView) {
                    final int id = v.getId();
                    final String mMassage;
                    if (id == R.id.imageButtonGet) {
                        mMassage = "Запросить местоположение абонента ";
                    } else {
                        mMassage = "Отправить местоположение абоненту ";
                    }
                    String name = loadName();
                    if (name.contains("Я => ")){
                        name = name.substring(5);
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityMap.this);
                    alertDialog.setTitle("Внимание!")
                            .setMessage(mMassage + name + " по номеру " + loadNumber() + "?")
                            .setCancelable(true);
                    alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (id == R.id.imageButtonGet) {
                                sendSMS(loadNumber());
                                progressBar.setVisibility(ProgressBar.VISIBLE);
                                imageButtonGet.setVisibility(ImageButton.INVISIBLE);

                                /*AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                Intent broadcastIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
                                broadcastIntent.setAction(ACTION);
                                PendingIntent piAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, broadcastIntent, PendingIntent.FLAG_ONE_SHOT);

                                long timeForAlarm = System.currentTimeMillis() + maxTimeWaitAnswer;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                {
                                    final AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timeForAlarm, piAlarm);
                                    am.setAlarmClock(alarmClockInfo, piAlarm);
                                }
                                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                    am.setExact(AlarmManager.RTC_WAKEUP,timeForAlarm, piAlarm);
                                else
                                    am.set(AlarmManager.RTC_WAKEUP, timeForAlarm, piAlarm);*/

                            } else {
                                Intent intentSEND = new Intent(getApplicationContext(), ServiceGPS.class);
                                intentSEND.putExtra("phoneNumber", loadNumber());
                                getApplicationContext().startService(intentSEND);
                            }
                            dialog.cancel();
                        }
                    });
                    alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Введите корректное имя или номер абонента(номер должен быть выбран из выпадающего списка)", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initAutoCompleteTextView() {
        mAutoCompleteTextView.setAdapter(new ContactsAdapterAutoComplete(getApplicationContext()));
        String name = loadName();

        if (!name.equals("")){
            if (name.contains("Я => ")){
                name = name.substring(5);
            }
            mAutoCompleteTextView.setText(name);
            isAutoCompleteTextView = true;
        }

        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Contact contact = (Contact) adapterView.getItemAtPosition(position);
                mAutoCompleteTextView.setText(contact.getName());
                saveName(contact.getName());
                saveNumber(contact.getNumber());
                isAutoCompleteTextView = true;
            }
        });
    }

    private void initProgressBar() {
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButtonGet.setVisibility(ImageButton.VISIBLE);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    private void showLastLocation(){

        mGoogleMap.clear();
        LatLng nullLatLng = new LatLng(0, 0);
        if (!loadLatLng().equals(nullLatLng)) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(loadLatLng())
                    .zoom(mGoogleMap.getMaxZoomLevel() - 5)
                    .bearing(0)
                    .tilt(0)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mGoogleMap.animateCamera(cameraUpdate);

            if (loadName().contains("Я =>")){
                mGoogleMap.addMarker(new MarkerOptions() //мое местоположение
                        .position(loadLatLng())
                        .title(loadData())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            } else {

                mGoogleMap.addMarker(new MarkerOptions() //чужое актуальное
                        .position(loadLatLng())
                        .title(loadData())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }

            CircleOptions circleOptions = new CircleOptions()
                    .center(loadLatLng())
                    .radius(loadAccuracy())
                    .fillColor(Color.TRANSPARENT)
                    .strokeColor(Color.RED)
                    .strokeWidth(4);

            mGoogleMap.addCircle(circleOptions);
        }
    }

    private void saveName(String name) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LAST_NAME, name);
        editor.apply();
    }

    private void saveNumber (String number) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LAST_NUMBER, number);
        editor.apply();
    }

    private void saveData (String data) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LAST_DATA, data);
        editor.apply();
    }

    private void saveAccuracy (float accuracy) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat(LAST_ACCURACY, accuracy);
        editor.apply();
    }

    private void saveLatitude (float latitude) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat(LAST_LAT, latitude);
        editor.apply();
    }

    private void saveLongitude (float longitude) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat(LAST_LNG, longitude);
        editor.apply();
    }

    private void saveVersion() {
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.putString(VERSION, version);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    private String loadName(){
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return sPref.getString(LAST_NAME, "");
    }

    private LatLng loadLatLng() {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        float latitude = sPref.getFloat(LAST_LAT, 0);
        float longitude = sPref.getFloat(LAST_LNG, 0);

        return new LatLng(latitude, longitude);
    }

    private float loadAccuracy() {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        float accuracy = sPref.getFloat(LAST_ACCURACY, 50);

        return accuracy;
    }

    private String loadNumber(){
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return sPref.getString(LAST_NUMBER, "");
    }

    private String loadData(){
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return sPref.getString(LAST_DATA, "");
    }

    private void sendSMS(String phoneNumber){
        String message = "&GET&";
        Intent smsSendIntentService = new Intent(getApplicationContext(), ServiceSendSms.class);
        smsSendIntentService.putExtra("message", message);
        smsSendIntentService.putExtra("phoneNumber", phoneNumber);
        getApplicationContext().startService(smsSendIntentService);
    }
}
