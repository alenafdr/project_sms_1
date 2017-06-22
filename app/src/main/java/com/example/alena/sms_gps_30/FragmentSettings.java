package com.example.alena.sms_gps_30;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alena.sms_gps_30.help_classes.DBHelperProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FragmentSettings extends Fragment {

    public static final String CHECK_BOX_WHITE_LIST = "CheckBoxWhiteList";
    final String TAG = ActivityMap.TAG + " settings";

    CheckBox checkBox;
    Button buttonClearHistory;

    onSomeEventListener someEventListener;

    public interface onSomeEventListener {
        public void someEvent(String s);
    }

    private OnFragmentInteractionListener mListener;

    public FragmentSettings() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (FragmentSettings.onSomeEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        buttonClearHistory = (Button) view.findViewById(R.id.buttonClearHistory);
        ImageButton buttonBack = (ImageButton) view.findViewById(R.id.imageButtonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkBox.setChecked(loadCheckBoxWhiteList());

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()) {
                    FragmentWhiteList fragmentWhiteList = new FragmentWhiteList();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit);
                    ft.replace(R.id.container, fragmentWhiteList);
                    ft.commit();
                    ActivityMap.menuItemId = R.id.menu_white_list;
                    ActivityMap.transitionSettingsWhiteList = true;
                }
            }
        });

        buttonClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle("Внимание!")
                        .setMessage("Вы уверены, что хотите очистить историю?")
                        .setCancelable(true);
                alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
                        dbHelperProvider.clearHistory();
                        dialog.cancel();
                        Toast.makeText(getActivity(), "История очищена", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        savePreferences(checkBox.isChecked());
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

    public void savePreferences(boolean checkBoxWhiteList){
        SharedPreferences sPref = getActivity().getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(CHECK_BOX_WHITE_LIST, checkBoxWhiteList);
        editor.apply();
    }

    private boolean loadCheckBoxWhiteList(){
        SharedPreferences sPref = getActivity().getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        return sPref.getBoolean(CHECK_BOX_WHITE_LIST, false);
    }

    private void stopSelf(){
        someEventListener.someEvent("end");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.animator.fragment_exit);
        ft.remove(this);
        ft.commit();
    }
}
