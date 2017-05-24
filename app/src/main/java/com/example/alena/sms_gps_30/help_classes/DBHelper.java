package com.example.alena.sms_gps_30.help_classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.alena.sms_gps_30.ActivityMap;


public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = ActivityMap.TAG + " DBHelp";
    final public static String NAME_DATA_BASE = "DataBaseSMSGPS3";
    final public static int version = 1;
    final public static String NAME_TABLE_HISTORY = "TableHistory";
    final public static String NAME_TABLE_WHITE_LIST = "TableWhiteList";
    Context mContext;

    public DBHelper(Context context) {
        super(context, NAME_DATA_BASE, null, version);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + NAME_TABLE_HISTORY + " ("
                + "id integer primary key autoincrement,"
                + "type text,"
                + "name text,"
                + "number text,"
                + "address text,"
                + "data text,"
                + "latlng text,"
                + "accuracy text"
                + ");");
        Log.d(TAG, "Создана база данных");
        db.execSQL("create table " + NAME_TABLE_WHITE_LIST + " ("
                + "id integer primary key autoincrement,"
                + "id_number integer"
                + ");");
        Log.d(TAG, "Создана база данных");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
