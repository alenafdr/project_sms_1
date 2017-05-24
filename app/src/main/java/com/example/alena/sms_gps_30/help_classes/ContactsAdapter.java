package com.example.alena.sms_gps_30.help_classes;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alena.sms_gps_30.ActivityMap;
import com.example.alena.sms_gps_30.R;

import java.util.List;

public class ContactsAdapter extends BaseAdapter {

    final String TAG = ActivityMap.TAG + " ContAdapt";


    private List<Contact> listContacts;
    private Context mContext;
    private LayoutInflater lInflater;

    public ContactsAdapter(List<Contact> listContacts, Context mContext) {
        this.listContacts = listContacts;
        this.mContext = mContext;
        this.lInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listContacts.size();
    }

    @Nullable
    @Override
    public Contact getItem(int position) {
        return listContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_contact, parent, false);
        }

        Contact cont = getContact(position);

        ((TextView) view.findViewById(R.id.textViewItemContactName)).setText(cont.getName());
        ((TextView) view.findViewById(R.id.textViewItemContactNumber)).setText(cont.getNumber());
        ((ImageView) view.findViewById(R.id.imageView)).setImageBitmap(cont.getImage());

        return view;
    }

    Contact getContact(int position) {
        return ((Contact) getItem(position));
    }



}
