package com.example.alena.sms_gps_30;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.alena.sms_gps_30.help_classes.DBHelperProvider;

public class FragmentSettings extends Fragment {

    public static final String CHECK_BOX_WHITE_LIST = "CheckBoxWhiteList";

    CheckBox checkBox;
    Button buttonClearHistory;

    private OnFragmentInteractionListener mListener;

    public FragmentSettings() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings1, container, false);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        buttonClearHistory = (Button) view.findViewById(R.id.buttonClearHistory);
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
                DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
                dbHelperProvider.clearHistory();
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


}
