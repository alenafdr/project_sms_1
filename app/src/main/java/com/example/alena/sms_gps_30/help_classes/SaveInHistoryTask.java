package com.example.alena.sms_gps_30.help_classes;


import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.alena.sms_gps_30.ActivityMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class SaveInHistoryTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = ActivityMap.TAG + " task";
    private final String KEY = "AIzaSyBkQaKiuWQDSsrZE67xKXr5t5HMqpxNJ84";

    private String type, number, message;
    private Context context;

    public SaveInHistoryTask(Context context, String type, String message, String number) {
        this.type = type;
        this.message = message;
        this.number = number;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        saveMassageInHistory(type, number, message);
        return null;
    }

    private void saveMassageInHistory(String type, String phoneNumber, String message){
        String[] messages = message.split("&");
        String accuracyTime = messages[2];
        String provider = messages[3];
        String accuracy = messages[4];
        String latitude = messages[5];
        String longitude = messages[6];
        String link = messages[7];
        String address = getAddress(latitude, longitude);

        String name = getNameByNumber(phoneNumber);

        DBHelperProvider dbHelperProvider = new DBHelperProvider(context);
        dbHelperProvider.addItemHistoryInDB(new ItemHistory(Float.valueOf(accuracy),
                address,
                accuracyTime,
                Float.valueOf(latitude),
                Float.valueOf(longitude),
                name,
                phoneNumber,
                type));

        saveInSharPref(name,
                phoneNumber,
                accuracyTime,
                Float.valueOf(latitude),
                Float.valueOf(longitude),
                Float.valueOf(accuracy));
    }

    public String getNameByNumber(String number){
        String name = "";
        ContentResolver cr = context.getContentResolver();
        String numberForSearch = "";
        if(number.contains("+")){
            numberForSearch = number.substring(2);
        } else {
            numberForSearch = number.substring(1);
        }

        String[] columns = {ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
        String[] selectionArgs = new String[] {"%" + numberForSearch + "%"};

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns, selection, selectionArgs, null);

        try {
            while (phones.moveToNext() && name.equals(""))
            {
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
        phones.close();
        return name;
    }

    public void saveInSharPref(String name, String number, String data , float latitude, float longitude, float accuracy) {
        SharedPreferences sPref = context.getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(ActivityMap.LAST_NAME, name);
        ed.putString(ActivityMap.LAST_NUMBER, number);
        ed.putString(ActivityMap.LAST_DATA, data);
        ed.putFloat(ActivityMap.LAST_LAT, latitude);
        ed.putFloat(ActivityMap.LAST_LNG, longitude);
        ed.putFloat(ActivityMap.LAST_ACCURACY, accuracy);
        ed.apply();
        Log.d(TAG, "сохранил в Preferences");
    }


    private String getAddress(String lat, String lng){
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        String address = "";
        if (geoCoder.isPresent()) {
            address = getAddressGeocode(lat, lng);
        } else {
            address = getAddressHTTP(lat, lng);
        }
        return address;

    }

    private String getAddressGeocode(String lat, String lng){
        String addressString = "";

        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
            Address address = addresses.get(0);
            addressString = address.getLocality() + ", " + address.getThoroughfare() + ", " + address.getFeatureName();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return addressString;
    }

    private String getAddressHTTP(String lat, String lng){
        String  stringForJSON = "";
        JSONObject jsonObject;
        List<String> listAddresess = new ArrayList<>();
        String address = "";
        try {
            stringForJSON = getContent(lat, lng);
            Log.d(TAG,"Отправил координаты " + lat + lng);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        try {
            jsonObject = new JSONObject(stringForJSON);

            JSONArray resultJSONarray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultJSONarray.length(); i++){
                JSONObject itemJSON = resultJSONarray.getJSONObject(i);
                if (itemJSON.has("address_components")) {
                    JSONArray addressComp = itemJSON.getJSONArray("address_components");

                    for (int j = 0; j < addressComp.length(); j++) {
                        if (addressComp.getJSONObject(j).has("types")){
                            JSONArray JSONTypes = addressComp.getJSONObject(j).getJSONArray("types");
                            Log.d(TAG, JSONTypes.toString());
                            for (int t = 0; t < JSONTypes.length(); t++) {
                                String valueType = JSONTypes.getString(t);
                                if (valueType.equals("street_number")) listAddresess.add(addressComp.getJSONObject(j).getString("long_name"));
                                if (valueType.equals("route")) listAddresess.add(addressComp.getJSONObject(j).getString("long_name"));
                                if (valueType.equals("locality")) listAddresess.add(addressComp.getJSONObject(j).getString("long_name"));
                            }
                        }
                    }
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
        Log.d(TAG, listAddresess.toString());
        for (int i = listAddresess.size()-1; i >= 0; i--) {
            address = address + listAddresess.get(i) + ", ";
        }
        return address;


    }

    private String getContent(String lat, String lng) throws IOException {

        BufferedReader reader = null;
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?" +
                    "latlng=" + lat + "," + lng +
                    "&language="+ Locale.getDefault().getLanguage() +
                    "&key=" + KEY);
            HttpsURLConnection c = (HttpsURLConnection)url.openConnection();
            c.setRequestMethod("GET");
            c.setReadTimeout(10000);
            c.connect();
            reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            Log.d(TAG, "Отправил " + buf.toString());
            return(buf.toString());
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
