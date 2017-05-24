package com.example.alena.sms_gps_30;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.alena.sms_gps_30.help_classes.Contact;
import com.example.alena.sms_gps_30.help_classes.ContactsAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityMap extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, FragmentHistory.onSomeEventListener {

    private SharedPreferences sPref;
    private DrawerLayout mDrawerLayout;
    private AutoCompleteTextView mAutoCompleteTextView;
    private GoogleMap mGoogleMap;
    private boolean isAutoCompleteTextView = false;
    private int menuItemId;

    public static final String TAG = "SMSGPS3.0";
    public static final String APP_PREFERENCES = "com.example.alena.sms_gps_30";
    public static final String LAST_NAME = "last name";
    public static final String LAST_NUMBER = "last number";
    public static final String LAST_DATA = "last data";
    public static final String LAST_LAT = "last latitude";
    public static final String LAST_LNG = "last longitude";
    public static final String LAST_ACCURACY = "last accuracy";

    FragmentHistory mFragmentHistory;
    FragmentSettings mFragmentSettings;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.menu_map);

        toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_view);
        initAutoCompleteTextView();

        ImageButton imageButtonGet = (ImageButton) findViewById(R.id.imageButtonGet);
        initImageButton(imageButtonGet);
        ImageButton imageButtonFindMeGet = (ImageButton) findViewById(R.id.imageButtonFindMe);
        initImageButton(imageButtonFindMeGet);

        initMap();
        mFragmentHistory = new FragmentHistory();
        mFragmentSettings = new FragmentSettings();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showLastLocation();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (id == R.id.menu_map) {

            ft.setCustomAnimations(0, R.animator.fragment_exit);
            ft.remove(mFragmentHistory);
            ft.remove(mFragmentSettings);
            ft.commit();
            menuItemId = R.id.menu_map;
        }

        if (id == R.id.menu_history){

            ft.setCustomAnimations(R.animator.fragment_enter, 0);
            ft.replace(R.id.container, mFragmentHistory);
            ft.commit();
            menuItemId = R.id.menu_history;
        }

        if (id == R.id.menu_settings) {
            ft.setCustomAnimations(R.animator.fragment_enter, 0);
            ft.replace(R.id.container, mFragmentSettings);
            ft.commit();
            menuItemId = R.id.menu_settings;
          /*  (findViewById(R.id.container)).setBackgroundColor(getResources().getColor(R.color.white));*/
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Log.d(TAG, "158 id " + id);

        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
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

    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (menuItemId != R.id.menu_map) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.fragment_exit);
            ft.remove(mFragmentHistory);
            ft.remove(mFragmentSettings);
            ft.commit();
            menuItemId = R.id.menu_map;
            navigationView.setCheckedItem(R.id.menu_map);
        } else {
            super.onBackPressed();
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

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityMap.this);
                    alertDialog.setTitle("Внимание!")
                            .setMessage(mMassage + loadName() + " по номеру " + loadNumber() + "?")
                            .setCancelable(true);
                    alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (id == R.id.imageButtonGet) {
                                sendSMS(loadNumber());
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
                    Toast.makeText(getApplicationContext(), "Введите корректное имя или номер абонента", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initAutoCompleteTextView() {
        mAutoCompleteTextView.setAdapter(new ContactsAdapter(getApplicationContext()));

        /*if (!loadName().equals("")){
            mAutoCompleteTextView.setText(loadName());
        }*/

        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Contact contact = (Contact) adapterView.getItemAtPosition(position);
                mAutoCompleteTextView.setText(contact.getName());
                saveName(contact.getName());
                saveNumber(contact.getNumber());
                isAutoCompleteTextView = true;
                /*showLastLocation(contact.getNumber());*/
            }
        });
    }

    public void showLastLocation(){
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
            mGoogleMap.addMarker(new MarkerOptions().position(loadLatLng()).title(loadName() + " - " + loadData()));

            CircleOptions circleOptions = new CircleOptions()
                    .center(loadLatLng())
                    .radius(loadAccuracy())
                    .fillColor(Color.TRANSPARENT)
                    .strokeColor(Color.RED)
                    .strokeWidth(4);

            mGoogleMap.addCircle(circleOptions);
        }
    }

    public void saveName(String name) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LAST_NAME, name);
        editor.apply();
    }

    public void saveNumber (String number) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LAST_NUMBER, number);
        editor.apply();
    }

    public void saveData (String data) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LAST_DATA, data);
        editor.apply();
    }

    public void saveAccuracy (float accuracy) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat(LAST_ACCURACY, accuracy);
        editor.apply();
    }
    public void saveLatitude (float latitude) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat(LAST_LAT, latitude);
        editor.apply();
    }
    public void saveLongitude (float longitude) {
        sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat(LAST_LNG, longitude);
        editor.apply();
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
        Intent smsSendIntentService = new Intent(getApplicationContext(), ServiceIntentSendSms.class);
        smsSendIntentService.putExtra("message", message);
        smsSendIntentService.putExtra("phoneNumber", phoneNumber);
        getApplicationContext().startService(smsSendIntentService);
    }

    @Override
    public void someEvent(String s) {
        String[] strings = s.split("#");
        for (int i = 0; i < strings.length; i++) {
            Log.d(TAG, String.valueOf(strings[i]));
        }
        saveName(strings[0]);
        saveNumber(strings[1]);
        saveData(strings[2]);
        saveAccuracy(Float.valueOf(strings[3]));
        saveLatitude(Float.valueOf(strings[4]));
        saveLongitude(Float.valueOf(strings[5]));

        mAutoCompleteTextView.setText(strings[0]);

        showLastLocation();
    }
}
