package com.example.alena.sms_gps_30;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;


public class FragmentSendLogs extends Fragment {

    private OnFragmentInteractionListener mListener;
    private final String TAG = ActivityMap.TAG + " logFragm";

    EditText editTextMessageForDev, editTextMesageInLogs;
    Button buttonSendLogs, buttonSaveInLogs;
    onSomeEventListener someEventListener;

    public interface onSomeEventListener {
        public void someEvent(String s);
    }

    public FragmentSendLogs() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (FragmentSendLogs.onSomeEventListener) activity;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send_logs, container, false);

        editTextMesageInLogs = (EditText) view.findViewById(R.id.editTextMessageLogs);
        buttonSaveInLogs = (Button) view.findViewById(R.id.buttonMessageLogs);
        buttonSaveInLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile(editTextMesageInLogs.getText().toString());
                editTextMesageInLogs.setText("");
                Toast.makeText(getActivity(), "Сообщение успешно добавлено", Toast.LENGTH_SHORT).show();
            }
        });

        editTextMessageForDev = (EditText) view.findViewById(R.id.editTextLogs);
        buttonSendLogs = (Button) view.findViewById(R.id.buttonSendLogs);
        buttonSendLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTask requestTask = new RequestTask();
                requestTask.execute(editTextMessageForDev.getText().toString());
            }
        });

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void stopSelf(){
        someEventListener.someEvent("end");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.animator.fragment_exit);
        ft.remove(this);
        ft.commit();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class RequestTask extends AsyncTask<String,Void,String>{
        String lineAnswer = "";

        public RequestTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder builder = null;
            File gpxfile = null;
            try {
                //читать
                File file = new File(getActivity().getFilesDir(), "LOGS");
                gpxfile = new File(file, ServiceIntentSaveLOGs.FILE_NAME);

                FileReader fileReader1 = new FileReader(gpxfile);
                BufferedReader reader = new BufferedReader(fileReader1);
                String line;
                builder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                fileReader1.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
                cancel(true);
            }

            //отправить

            try {
                HttpURLConnection connection = null;
                if (builder != null) {
                    String encodedQueryVersion = URLEncoder.encode(getVersion(), "UTF-8");
                    String encodedQueryInfo = URLEncoder.encode(getIdTelephone(), "UTF-8");
                    String encodedQueryData = URLEncoder.encode(builder.toString(), "UTF-8");
                    String encodedQueryMessage = URLEncoder.encode(params[0], "UTF-8");

                    String postData = "{\"version\":\"" + encodedQueryVersion +
                            "\",\"info\":\""+encodedQueryInfo+
                            "\",\"data\":\""+ encodedQueryData +
                            "\",\"message\":\"" + encodedQueryMessage + "\"}";

                    URL url = new URL("http://gps.aiprof.ru/data.php");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));

                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.close();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuffer sb = new StringBuffer("");

                        while((lineAnswer = in.readLine()) != null) {
                            sb.append(lineAnswer);
                            break;
                        }
                        in.close();
                        if (lineAnswer.equals("ok")){
                            gpxfile.delete(); //очистить логи если отправка успешная

                        }
                        Log.d(TAG, "lineAnswer: " + sb.toString());
                    }
                    else {
                        Log.d(TAG, "false : " + responseCode);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
            return lineAnswer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("ok")){
                Toast.makeText(getActivity(), "логи успешно отправлены", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Спасибо за участие!", Toast.LENGTH_SHORT).show();
                editTextMessageForDev.setText("");
            } else {
                if (!isOnline(getActivity())){
                    Toast.makeText(getActivity(), "нет подключения к интернету", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            Toast.makeText(getActivity(), "логи пусты", Toast.LENGTH_SHORT).show();
        }
    }

    private String getVersion(){
        SharedPreferences sPref = getActivity().getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
        return sPref.getString(ActivityMap.VERSION, "0");
    }

    private String getIdTelephone(){
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();
        return phoneNumber;
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    private void saveFile(String text) {
        Intent logsIntent = new Intent(getActivity(), ServiceIntentSaveLOGs.class);
        logsIntent.putExtra(ServiceIntentSaveLOGs.TIME_LOGS, System.currentTimeMillis());
        logsIntent.putExtra(ServiceIntentSaveLOGs.TEXT_LOGS, text);
        getActivity().startService(logsIntent);
    }
}
