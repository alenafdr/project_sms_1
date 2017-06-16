package com.example.alena.sms_gps_30;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class FragmentSendLogs extends Fragment {

    private OnFragmentInteractionListener mListener;

    EditText editText;
    Button buttonSendLogs;

    public FragmentSendLogs() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send_logs, container, false);
        editText = (EditText) view.findViewById(R.id.editTextLogs);
        buttonSendLogs = (Button) view.findViewById(R.id.buttonSendLogs);
        buttonSendLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    //читать
                    File file = new File(getActivity().getFilesDir(),"LOGS");
                    File gpxfile = new File(file, ServiceIntentSaveLOGs.FILE_NAME);

                    FileReader fileReader1 = new FileReader(gpxfile);
                    BufferedReader reader = new BufferedReader(fileReader1);
                    String line;
                    StringBuilder builder = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }

                    fileReader1.close();

                    //отправить


                    //удалить если отправка успешая
                    gpxfile.delete();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
