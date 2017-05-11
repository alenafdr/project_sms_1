package com.example.alena.sms_gps_30.help_classes;

import android.graphics.Bitmap;

import com.example.alena.sms_gps_30.ActivityMap;


public class Contact {

    final String TAG = ActivityMap.TAG + "FROM_CONTACTS";
    private String name;
    private String number;
    private Bitmap image;


    public Contact(Bitmap image, String name, String number) {
        this.image = image;
        this.name = name;
        this.number = number;
        /*Log.d(TAG, "new Contact. Name: " + name + ", Number: " + number);*/
    }


    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
