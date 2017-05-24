package com.example.alena.sms_gps_30.help_classes;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapterAutoComplete extends BaseAdapter implements Filterable {

    final String TAG = ActivityMap.TAG + " CAACTW";

    private List<Contact> listContacts;
    private Context mContext;
    private ContentResolver cr;
    private LayoutInflater lInflater;

    public ContactsAdapterAutoComplete(Context context) {
        this.mContext = context;
        this.lInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.cr = context.getContentResolver();
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

    public List<Contact> findContacts(String stringForSearch){
        List<Contact> resultList = new ArrayList<Contact>();

        String[] columns = {ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
        String[] selectionArgs = new String[] {"%" + stringForSearch + "%"};

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns, selection, selectionArgs, null);

        try {
            while (phones.moveToNext())
            {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int contactId = phones.getInt(phones.getColumnIndex(ContactsContract.Contacts._ID));
                Bitmap image = GetContactPhoto(contactId);
                resultList.add(new Contact(image, name, phoneNumber));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Ошибка " + e.toString());
        }

        selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
        phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns, selection, selectionArgs, null);

        try {
            while (phones.moveToNext())
            {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int contactId = phones.getInt(phones.getColumnIndex(ContactsContract.Contacts._ID));
                Bitmap image = GetContactPhoto(contactId);
                resultList.add(new Contact(image, name, phoneNumber));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Ошибка " + e.toString());
        }

        try {
            phones.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    final public Bitmap GetContactPhoto(int contactId)
    {
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_avatar);
            return bitmap;
        }
        return BitmapFactory.decodeStream(input);
    }
}
