package com.example.alena.sms_gps_30;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.alena.sms_gps_30.help_classes.DBHelperProvider;
import com.example.alena.sms_gps_30.help_classes.HistoryAdapter;
import com.example.alena.sms_gps_30.help_classes.ItemHistory;

import java.util.List;


public class FragmentHistory extends Fragment {

    private OnFragmentInteractionListener mListener;
    private final String TAG = ActivityMap.TAG + " FragmentHist";
    ListView listHistory;
    TextView textView;
    onSomeEventListener someEventListener;

    public FragmentHistory() {
        // Required empty public constructor
    }

    public interface onSomeEventListener {
        public void someEvent(String s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listHistory = (ListView)view.findViewById(R.id.listViewHistory);
        textView =(TextView) view.findViewById(R.id.textViewHistoryEmpty);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (onSomeEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
        List<ItemHistory> itemHistoryList = dbHelperProvider.getAllHistory();

        Log.d(TAG, String.valueOf(itemHistoryList.size()));

        HistoryAdapter historyAdapter = new HistoryAdapter(getActivity(), itemHistoryList);
        listHistory.setAdapter(historyAdapter);
        if(itemHistoryList.size() == 0){
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            listHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String name = ((TextView)view.findViewById(R.id.textViewItemHistoryName)).getText().toString();
                    String number = ((TextView)view.findViewById(R.id.textViewItemHistoryNumber)).getText().toString();
                    String data = ((TextView)view.findViewById(R.id.textViewItemHistoryData)).getText().toString();
                    String accuracy = ((TextView)view.findViewById(R.id.textViewItemHistoryAcc)).getText().toString();
                    String latitude = ((TextView)view.findViewById(R.id.textViewItemHistoryLat)).getText().toString();
                    String longitude = ((TextView)view.findViewById(R.id.textViewItemHistoryLon)).getText().toString();
                    someEventListener.someEvent(name + "#" +
                                                number + "#" +
                                                data + "#" +
                                                accuracy + "#" +
                                                latitude + "#" +
                                                longitude);
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
