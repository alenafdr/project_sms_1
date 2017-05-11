package com.example.alena.sms_gps_30.help_classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alena.sms_gps_30.ActivityMap;
import com.example.alena.sms_gps_30.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends BaseAdapter implements Filterable {

    final String TAG = ActivityMap.TAG + " CONT_ADAPT";

    private List<Contact> listContacts;
    private List<Contact> listForFind;
    private Context mContext;
    /*private int resoursItem;*/
    private LayoutInflater lInflater;
    List<Contact> contactsForFind;

    public ContactsAdapter (Context context, List<Contact> contacts) {
        mContext = context;
        listForFind = contacts;
        lInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*for (Contact c: contacts){
            Log.d(TAG, "получено. Name: " + c.getName() + ", Number: " + c.getNumber());
        }*/
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

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    List<Contact> listResult = findContacts(constraint.toString());
                    filterResults.values = listResult;
                    filterResults.count = listResult.size();
                }
                /*Log.d(TAG, "constraint " + constraint);*/
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    listContacts = (List<Contact>) results.values;
                    notifyDataSetChanged();
                    /*Log.d(TAG, listContacts.toString());*/
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    public List<Contact> findContacts(String name){
        List<Contact> resultList = new ArrayList<Contact>();
        for (Contact c : listForFind){
            if ((c.getName().toLowerCase().contains(name.toLowerCase())) || (c.getNumber().contains(name))){
                resultList.add(c);
                /*Log.d(TAG, "добавлен в список результатов " + c.getName());*/
            }
        }
        return resultList;
    }


}
