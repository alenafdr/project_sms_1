package com.aiprof.alena.get_gps_from_sms.help_classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aiprof.alena.get_gps_from_sms.ActivityMap;
import com.example.alena.sms_gps_30.R;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private final String TAG = ActivityMap.TAG + " histAdapt";

    List<ItemHistory>listHistory;
    private LayoutInflater lInflater;
    Context mContext;

    public HistoryAdapter(Context context, List<ItemHistory> list) {
        this.listHistory = list;
        mContext = context;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
/*
        Log.d(TAG, "position " + position);*/
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_history, parent, false);
        }

        ItemHistory itemHistory = getItemHistory(position);
/*
        Log.d(TAG, itemHistory.getType());*/

        if (itemHistory.getType().equals(ItemHistory.TYPE_SENT)){
            ((ImageView) view.findViewById(R.id.ivImage)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sent));
        } else {
            ((ImageView) view.findViewById(R.id.ivImage)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_received));
        }

        if (!itemHistory.getName().equals("")) {
            ((TextView) view.findViewById(R.id.textViewItemHistoryName)).setText(itemHistory.getName());
        } else {
            ((TextView) view.findViewById(R.id.textViewItemHistoryName)).setText(itemHistory.getNumber());
        }
        ((TextView) view.findViewById(R.id.textViewItemHistoryNumber)).setText(itemHistory.getNumber());
        ((TextView) view.findViewById(R.id.textViewItemHistoryAddress)).setText(itemHistory.getAddress());
        ((TextView) view.findViewById(R.id.textViewItemHistoryData)).setText(itemHistory.getData());
        ((TextView) view.findViewById(R.id.textViewItemHistoryAcc)).setText(String.valueOf(itemHistory.getAccuracy()));
        ((TextView) view.findViewById(R.id.textViewItemHistoryLat)).setText(String.valueOf(itemHistory.getLatitude()));
        ((TextView) view.findViewById(R.id.textViewItemHistoryLon)).setText(String.valueOf(itemHistory.getLongitude()));
        /*
        Log.d(TAG, "отдал " + itemHistory.getData());*/

        return view;
    }

    ItemHistory getItemHistory(int position) {
        return ((ItemHistory) getItem(position));
    }

    @Override
    public Object getItem(int position) {
        return listHistory.get(position);
    }

    @Override
    public int getCount() {
        return listHistory.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
