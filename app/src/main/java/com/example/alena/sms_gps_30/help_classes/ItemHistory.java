package com.example.alena.sms_gps_30.help_classes;


public class ItemHistory {
    public static String TYPE_SENT = "sent"; //исходящий
    public static String TYPE_RECEIVED = "received"; //входящий

    private String type;
    private String name;
    private String number;
    private String address;
    private String data;
    private float latitude;
    private float longitude;
    private float accuracy;

    public ItemHistory(float accuracy, String address, String data, float latitude, float longitude, String name, String number, String type) {
        this.accuracy = accuracy;
        this.address = address;
        this.data = data;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.number = number;
        this.type = type;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
