package com.example.alena.sms_gps_30;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.alena.sms_gps_30.help_classes.DBHelperProvider;
import com.example.alena.sms_gps_30.help_classes.HistoryAdapter;
import com.example.alena.sms_gps_30.help_classes.ItemHistory;

import java.util.ArrayList;
import java.util.List;


public class FragmentHistory extends Fragment {

    public static LinearLayout llBottomSheet;
    private OnFragmentInteractionListener mListener;
    private final String TAG = ActivityMap.TAG + " FragmentHist";
    ListView listHistory;
    TextView textViewHeader;

    onSomeEventListener someEventListener;
    ArrayAdapter<String> adapter;
    HistoryAdapter historyAdapter;

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
        llBottomSheet = (LinearLayout) view.findViewById(R.id.bottom_sheet);
        textViewHeader = (TextView) view.findViewById(R.id.textViewHeaderBottomSheet);
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

        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            boolean canCollapse;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                canCollapse = listHistory.getFirstVisiblePosition () == 0;
                if (newState == BottomSheetBehavior.STATE_DRAGGING && !canCollapse) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        textViewHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        updateTableHistory();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateTableHistory(){
        DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
        List<ItemHistory> itemHistoryList = dbHelperProvider.getAllHistory();

        if(itemHistoryList.size() == 0){
            ArrayList<String> myStringArray1 = new ArrayList<>();
            myStringArray1.add("Нет данных");
            adapter = new ArrayAdapter<>(getActivity(), R.layout.item_1_list, myStringArray1);
            listHistory.setAdapter(adapter);

        } else {
            historyAdapter = new HistoryAdapter(getActivity(), itemHistoryList);
            listHistory.setAdapter(historyAdapter);
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

                    BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}