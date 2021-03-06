package com.aiprof.alena.get_gps_from_sms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aiprof.alena.get_gps_from_sms.help_classes.Contact;
import com.aiprof.alena.get_gps_from_sms.help_classes.DBHelperProvider;
import com.example.alena.sms_gps_30.R;
import com.aiprof.alena.get_gps_from_sms.help_classes.ContactsAdapter;
import com.aiprof.alena.get_gps_from_sms.help_classes.ContactsAdapterAutoComplete;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FragmentWhiteList extends Fragment {

    public static final String WHITE_NUMBERS = "whiteNumbers";

    private OnFragmentInteractionListener mListener;
    private SharedPreferences sPref;
    private final String TAG = ActivityMap.TAG + " FrWhiteList";
    private boolean isAutoCompleteTextView = false;
    private AutoCompleteTextView autoCompleteTextView;
    private ImageButton imageButtonAddContact;
    private ListView whiteListListView;
    private String number;

    onSomeEventListener someEventListener;

    public interface onSomeEventListener {
        public void someEvent(String s);
    }

    public FragmentWhiteList() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (FragmentWhiteList.onSomeEventListener) activity;
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
        View view = inflater.inflate(R.layout.fragment_white_list, container, false);
        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewWhiteList);
        imageButtonAddContact = (ImageButton) view.findViewById(R.id.imageButtonAddContact);
        whiteListListView = (ListView) view.findViewById(R.id.listViewWhiteList);
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

        initAutoCompleteTextView();

        final DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
        updateList();
        imageButtonAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAutoCompleteTextView) {
                    Toast.makeText(getActivity(), "Введите корректное имя или номер абонента (номер должен быть выбран из выпадающего списка)", Toast.LENGTH_LONG).show();
                } else if (isNumberInList(number)) {
                    Toast.makeText(getActivity(), "Этот номер уже есть в списке", Toast.LENGTH_SHORT).show();
                } else {
                    String idForSave = getIdByNumber(number);
                    dbHelperProvider.addIdWhiteList(Integer.valueOf(idForSave));
                    updateList();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        String stringForSaveInShP = "";
        for (int i = 0; i < whiteListListView.getChildCount(); i++){
            View view = whiteListListView.getChildAt(i);
            TextView numberTextView = (TextView) view.findViewById(R.id.textViewItemContactNumber);
            try {
                stringForSaveInShP = stringForSaveInShP + numberTextView.getText().toString() + " ";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!stringForSaveInShP.equals("")){
            sPref = getActivity().getSharedPreferences(ActivityMap.APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.putString(WHITE_NUMBERS, stringForSaveInShP);
            editor.apply();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean isNumberInList (String number) {
        for (int i = 0; i < whiteListListView.getChildCount(); i++){
            View view = whiteListListView.getChildAt(i);
            TextView numberTextView = null;
            try {
                numberTextView = (TextView) view.findViewById(R.id.textViewItemContactNumber);
                if (number.equals(numberTextView.getText().toString())) return  true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private void updateList(){

        final DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
        List<String> idWhiteList = dbHelperProvider.getWhiteList();

        if (idWhiteList.size() == 0) {
            ArrayList<String> myStringArray1 = new ArrayList<>();
            myStringArray1.add("Список номеров пуст");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, myStringArray1);
            whiteListListView.setAdapter(adapter);
        } else {
            List<Contact> whiteList = findContactsById(idWhiteList);
            whiteListListView.setAdapter(new ContactsAdapter(whiteList, getActivity()));
        }

        whiteListListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //диалоговое окно "удалить"?
                final String removeNumber = ((TextView) view.findViewById(R.id.textViewItemContactNumber)).getText().toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle("Внимание!")
                        .setMessage("Удалить номер из списка?")
                        .setCancelable(true);
                alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String idRemoveNumber = getIdByNumber(removeNumber);
                        DBHelperProvider dbHelperProvider = new DBHelperProvider(getActivity());
                        dbHelperProvider.removeNumberFromList(idRemoveNumber);
                        updateList();
                        dialog.cancel();
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

    private void initAutoCompleteTextView() {
        autoCompleteTextView.setAdapter(new ContactsAdapterAutoComplete(getActivity()));
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Contact contact = (Contact) adapterView.getItemAtPosition(position);
                autoCompleteTextView.setText(contact.getName());
                number = contact.getNumber();
                isAutoCompleteTextView = true;
            }
        });
    }

    public String getIdByNumber(String number) {
        String id = "";
        ContentResolver cr = getActivity().getContentResolver();
        String numberForSearch = "";
        if (number.contains("+")) {
            numberForSearch = number.substring(2);
        } else {
            numberForSearch = number.substring(1);
        }

        String[] columns = {ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + numberForSearch + "%"};

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns, selection, selectionArgs, null);

        try {
            while (phones.moveToNext() && id.equals("")) {
                id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return id;
        }
        phones.close();
        return id;
    }

    public List<Contact> findContactsById(List<String> listForSearch){

        ContentResolver cr = getActivity().getContentResolver();
        List<Contact> resultList = new ArrayList<Contact>();

        String[] columns = {ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone._ID + " = ?";
        String[] selectionArgs;
        for (int i = 0; i < listForSearch.size(); i++) {
            selectionArgs = new String[]{listForSearch.get(i)};
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

            try {
                phones.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Ошибка " + e.toString());
            }
        }
        return resultList;
    }

    final public Bitmap GetContactPhoto(int contactId)
    {
        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_avatar);
            return bitmap;
        }
        return BitmapFactory.decodeStream(input);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void stopSelf(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();


        if (ActivityMap.transitionSettingsWhiteList) { //проверяет, открыт ли белый лист через настройки
            ft.setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit);
            ActivityMap.menuItemId = R.id.menu_settings;
            ActivityMap.transitionSettingsWhiteList = false;
            getActivity().getFragmentManager().popBackStack();
        } else {
            someEventListener.someEvent("end");
            ft.setCustomAnimations(0, R.animator.fragment_exit);
            ft.remove(this);
            ft.commit();
        }



    }
}
