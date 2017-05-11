package com.example.alena.sms_gps_30.help_classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.alena.sms_gps_30.ActivityMap;

import java.util.ArrayList;
import java.util.List;


public class DBHelperProvider {
    private final String TAG = ActivityMap.TAG + " DBHelpProv";
    private Context mContext;

    public DBHelperProvider(Context context) {
        this.mContext = context;
    }

    public void addItemHistoryInDB(ItemHistory itemHistory){
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("type", itemHistory.getType());
        cv.put("name", itemHistory.getName());
        cv.put("number", itemHistory.getNumber());
        cv.put("address", itemHistory.getAddress());
        cv.put("data", itemHistory.getData());
        cv.put("latlng", String.valueOf(itemHistory.getLatitude()) + " " + String.valueOf(itemHistory.getLongitude()));
        cv.put("accuracy", String.valueOf(itemHistory.getAccuracy()));

        Log.d(TAG, "id = " + db.insertOrThrow(DBHelper.NAME_TABLE_HISTORY, null, cv));

        dbHelper.close();
    }

    public List<ItemHistory> getAllHistory(){
        List<ItemHistory> resultList = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String orderBy = "id";
        String [] colunms = new String[] {"name", "number", "address", "data", "latlng", "accuracy"};
        Cursor c = db.query(DBHelper.NAME_TABLE_HISTORY, null, null, null, null, null, orderBy + " DESC");
        if (c.moveToFirst()) {

            int typeColIndex = c.getColumnIndex("type");
            int nameColIndex = c.getColumnIndex("name");
            int numberColIndex = c.getColumnIndex("number");
            int addressColIndex = c.getColumnIndex("address");
            int addressColData = c.getColumnIndex("data");
            int addressColLatLng = c.getColumnIndex("latlng");
            int addressColAccuracy = c.getColumnIndex("accuracy");
            do {
                String type = c.getString(typeColIndex);
                String name = c.getString(nameColIndex);
                String number = c.getString(numberColIndex);
                String address = c.getString(addressColIndex);
                String data = c.getString(addressColData);
                String latlng = c.getString(addressColLatLng);
                String accuracy = c.getString(addressColAccuracy);

                String[] LatLng = latlng.split(" ");
                float lat = Float.valueOf(LatLng[0]);
                float lng = Float.valueOf(LatLng[1]);
                ItemHistory itemHistory = new ItemHistory(Float.valueOf(accuracy), address, data, lat, lng, name, number, type);
                resultList.add(itemHistory);

            } while (c.moveToNext());
        }
        db.close();

        return resultList;
    }

    public List<ItemHistory> getHistoryByNumber(String mNumber){
        List<ItemHistory> resultList = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"name", "number", "address", "data", "latlng", "accuracy"};
        String selection = "number = ?";
        String[] selectionArgs = {mNumber};
        String orderBy = "id";
        Cursor c = db.query(DBHelper.NAME_TABLE_HISTORY, columns, selection, selectionArgs, null, null, orderBy + " DESC");

        if (c.moveToFirst()) {

            int typeColIndex = c.getColumnIndex("type");
            int nameColIndex = c.getColumnIndex("name");
            int numberColIndex = c.getColumnIndex("number");
            int addressColIndex = c.getColumnIndex("address");
            int addressColData = c.getColumnIndex("data");
            int addressColLatLng = c.getColumnIndex("latlng");
            int addressColAccuracy = c.getColumnIndex("accuracy");
            do {
                String type = c.getString(typeColIndex);
                String name = c.getString(nameColIndex);
                String number = c.getString(numberColIndex);
                String address = c.getString(addressColIndex);
                String data = c.getString(addressColData);
                String latlng = c.getString(addressColLatLng);
                String accuracy = c.getString(addressColAccuracy);

                String[] LatLng = latlng.split(" ");
                float lat = Float.valueOf(LatLng[0]);
                float lng = Float.valueOf(LatLng[1]);
                ItemHistory itemHistory = new ItemHistory(Float.valueOf(accuracy), address, data, lat, lng, name, number, type);
                resultList.add(itemHistory);

            } while (c.moveToNext());

        }
        db.close();

        return resultList;
    }

    public ItemHistory getLastPositionByNumber(String mNumber){
        ItemHistory itemHistory = null;

        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"name", "number", "address", "data", "latlng", "accuracy"};
        String selection = "number = ?";
        String[] selectionArgs = {mNumber};
        String orderBy = "id";
        Cursor c = db.query(DBHelper.NAME_TABLE_HISTORY, columns, selection, selectionArgs, null, null, orderBy + " DESC");

        if (c.moveToFirst()) {

            int typeColIndex = c.getColumnIndex("type");
            int nameColIndex = c.getColumnIndex("name");
            int numberColIndex = c.getColumnIndex("number");
            int addressColIndex = c.getColumnIndex("address");
            int addressColData = c.getColumnIndex("data");
            int addressColLatLng = c.getColumnIndex("latlng");
            int addressColAccuracy = c.getColumnIndex("accuracy");

            String type = c.getString(typeColIndex);
            String name = c.getString(nameColIndex);
            String number = c.getString(numberColIndex);
            String address = c.getString(addressColIndex);
            String data = c.getString(addressColData);
            String latlng = c.getString(addressColLatLng);
            String accuracy = c.getString(addressColAccuracy);

            String[] LatLng = latlng.split(" ");
            float lat = Float.valueOf(LatLng[0]);
            float lng = Float.valueOf(LatLng[1]);
            itemHistory = new ItemHistory(Float.valueOf(accuracy), address, data, lat, lng, name, number, type);
        }

        return  itemHistory;
    }

    public void clearHistory(){
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int clearCount = db.delete(DBHelper.NAME_TABLE_HISTORY, null, null);
        Log.d(TAG, "deleted rows count = " + clearCount);
    }
}
