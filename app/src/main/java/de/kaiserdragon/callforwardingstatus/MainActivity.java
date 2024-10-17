package de.kaiserdragon.callforwardingstatus;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Objects;

import de.kaiserdragon.callforwardingstatus.helper.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_PHONE_STATE_PERMISSION = 1;


    final DatabaseHelper databaseHelper = new DatabaseHelper(this);
    final String TAG = "Main";
    //SQLiteDatabase database = databaseHelper.getWritableDatabase();
    Context context;
    Activity activity;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;


        checkPermission(this);
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 3);

        // Find the EditText views and ImageButton views
        final EditText phoneNumber1EditText = findViewById(R.id.PhoneNumber1);
        final EditText phoneNumber2EditText = findViewById(R.id.PhoneNumber2);
        final EditText phoneNumber3EditText = findViewById(R.id.PhoneNumber3);
        final ImageButton saveButton1 = findViewById(R.id.save_row1);
        final ImageButton saveButton2 = findViewById(R.id.save_row2);
        final ImageButton saveButton3 = findViewById(R.id.save_row3);
        final ImageButton deleteButton1 = findViewById(R.id.delete_row1);
        final ImageButton deleteButton2 = findViewById(R.id.delete_row2);
        final ImageButton deleteButton3 = findViewById(R.id.delete_row3);
        radioButton1 = findViewById(R.id.radioButton1);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);

        phoneNumber1EditText.setText(getPhoneNumber(1));
        phoneNumber2EditText.setText(getPhoneNumber(2));
        phoneNumber3EditText.setText(getPhoneNumber(3));
        setCheckedRadioButton();
        MultiSim(this);
        updateMultiSimTxt();


        // Set OnClickListeners on the ImageButton views
        saveButton1.setOnClickListener(v -> saveSQLData(phoneNumber1EditText, 1));
        saveButton2.setOnClickListener(v -> saveSQLData(phoneNumber2EditText, 2));
        saveButton3.setOnClickListener(v -> saveSQLData(phoneNumber3EditText, 3));
        deleteButton1.setOnClickListener(v -> deleteSQLData(phoneNumber1EditText, 1));
        deleteButton2.setOnClickListener(v -> deleteSQLData(phoneNumber2EditText, 2));
        deleteButton3.setOnClickListener(v -> deleteSQLData(phoneNumber3EditText, 3));

        phoneNumber1EditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveSQLData(phoneNumber1EditText, 1);
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(this.getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
            return false;
        });
        phoneNumber2EditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveSQLData(phoneNumber2EditText, 2);
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(this.getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
            return false;
        });
        phoneNumber3EditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveSQLData(phoneNumber3EditText, 3);
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(this.getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
            return false;
        });
        phoneNumber1EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                colorTextSaveStatus(phoneNumber1EditText);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        phoneNumber2EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                colorTextSaveStatus(phoneNumber2EditText);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        phoneNumber3EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                colorTextSaveStatus(phoneNumber3EditText);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //saveSQLData(phoneNumber3EditText,3);
            }
        });


        findViewById(R.id.button).setOnClickListener(view -> {
            Intent intent = new Intent("de.kaiserdragon.callforwardingstatus.TOGGLE_CALL_FORWARDING");
            intent.setClass(context, CallForwardingReceiver.class);
            intent.putExtra("cfi", PhoneStateService.currentState);
            context.sendBroadcast(intent);
        });


        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            //group.clearCheck(); // Clear the previously selected radio button
            RadioButton selectedRadioButton = findViewById(checkedId);
            String selectedId = selectedRadioButton.getContentDescription().toString();
            selectedRadioButton.setChecked(true); // Set the current radio button as checked
            databaseHelper.changeSelected(selectedId);
            //String number = getPhoneNumber(Integer.parseInt(selectedId));
            // Save the selected option to a variable here
            // saveSQLData(number,selectedId)
        });

    }

    private void colorTextSaveStatus(EditText numberInput) {
        numberInput.setTextColor(Color.RED);
    }

    private void setCheckedRadioButton() {
        String[] array = databaseHelper.getSelected();
        if (Objects.equals(array[0], "1")) radioButton1.setChecked(true);
        if (Objects.equals(array[0], "2")) radioButton2.setChecked(true);
        if (Objects.equals(array[0], "3")) radioButton3.setChecked(true);
        //Log.i(TAG, "Retrieve Data" + array[0] + "phone" + array[1]);
    }

    public void MultiSim(Context context) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        if (subscriptionManager != null) {
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> subscriptionList = subscriptionManager.getActiveSubscriptionInfoList();
                if (DEBUG) Log.i(TAG, String.valueOf(subscriptionList.size()));
                if (subscriptionList.size() <= 1) {
                    findViewById(R.id.multisim_button).setVisibility(View.GONE);
                } else findViewById(R.id.multisim_button).setOnClickListener(view -> showSimSelectionPopup(this));
            }
        }
    }

    public void showSimSelectionPopup(Context context) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        if (subscriptionManager != null) {
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> subscriptionList = subscriptionManager.getActiveSubscriptionInfoList();
                if (subscriptionList != null && !subscriptionList.isEmpty()) {
                    SimSelectionDialog dialog = new SimSelectionDialog(context, subscriptionList);
                    dialog.show();
                } else {
                    Toast.makeText(context, "No SIM card available", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveSelectedSimId(Context context, int selectedSimId) {
        SharedPreferences preferences = context.getSharedPreferences("SIM_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SELECTED_SIM_ID", selectedSimId);
        editor.apply();
    }

    private void updateMultiSimTxt() {
        Button MultiSIM = findViewById(R.id.multisim_button);
        int SIMid = getSavedSelectedSimId(context);
        if (SIMid >= 0) {
            String simText = "SIM " + SIMid;
            MultiSIM.setText(simText);
        }

    }

    public int getSavedSelectedSimId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SIM_PREFERENCES", Context.MODE_PRIVATE);
        return preferences.getInt("SELECTED_SIM_ID", -1); // -1 is a default value if the preference is not found
    }

    private String getPhoneNumber(int row) {
        // Query the database to retrieve the data
        //Log.i(TAG, "Retrieve Data");
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String[] columns = {databaseHelper.getColumnPhoneNumber()};
        String selection = databaseHelper.getColumnId() + " = ?";
        String[] selectionArgs = {String.valueOf(row)}; // Replace "1" with the ID of the row you want to retrieve
        Cursor cursor = database.query(databaseHelper.getTableName(), columns, selection, selectionArgs, null, null, null);
        String phoneNumber = null;
        // If the cursor has at least one row, move to the first row and get the data
        if (cursor.moveToFirst()) {
            int phoneNumberColumnIndex = cursor.getColumnIndex(databaseHelper.getColumnPhoneNumber());
            // Check if the column was found and return data
            if (phoneNumberColumnIndex != -1)
                phoneNumber = cursor.getString(phoneNumberColumnIndex);
        }

        // Close the cursor
        cursor.close();
        return phoneNumber;
    }

    private void saveSQLData(EditText numberInput, int row) {
        String phoneNumber = numberInput.getText().toString();

        // Insert the data into the database
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(databaseHelper.getColumnId(), row);
        values.put(databaseHelper.getColumnPhoneNumber(), phoneNumber);
        String whereClause = databaseHelper.getColumnId() + " = ?";
        String[] whereArgs = {String.valueOf(row)}; // Replace "1" with the ID of the row you want to update
        int ok = 0;
        long insOk = 0;

        if (isIdExists(database, row)) {
            ok = database.update(databaseHelper.getTableName(), values, whereClause, whereArgs);
        } else {
            values.put(databaseHelper.getColumnId(), row);
            values.put(databaseHelper.getColumnSelected(), "false");
            insOk = database.insert(databaseHelper.getTableName(), null, values);
        }
        if ((insOk == row) || ok == 1) {
            Toast.makeText(context, getString(R.string.PhoneNumberSaved), Toast.LENGTH_SHORT).show();
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
            int color = ContextCompat.getColor(this, typedValue.resourceId);
            numberInput.setTextColor(color);
            if (isFirstEntry(database)) {
                if (Objects.equals(row, 1)) radioButton1.setChecked(true);
                if (Objects.equals(row, 2)) radioButton2.setChecked(true);
                if (Objects.equals(row, 3)) radioButton3.setChecked(true);
            }
        }
        database.close();
    }

    public boolean isIdExists(SQLiteDatabase db, int id) {
        String[] columns = {"id"};
        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(id)};

        try (Cursor cursor = db.query("phone_numbers", columns, selection, selectionArgs, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public boolean isFirstEntry(SQLiteDatabase db) {
        String[] columns = {"COUNT(*)"};

        try (Cursor cursor = db.query("phone_numbers", columns, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count == 1;
            }
        }

        return false; // Return false by default if an exception occurs or cursor is null
    }

    private void deleteSQLData(EditText numberInput, int row) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String whereClause = databaseHelper.getColumnId() + " = ?";
        String[] whereArgs = {String.valueOf(row)}; // Replace "1" with the ID of the row you want to delete
        database.delete(databaseHelper.getTableName(), whereClause, whereArgs);
        numberInput.setText(getPhoneNumber(row));
        database.close();
    }

    public void checkPermission(Activity activity) {
        // Check Permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE_PERMISSION);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                // Show a dialog explaining the need for the permission
                new AlertDialog.Builder(this)
                        .setTitle("Read Phone State Permission needed")
                        .setMessage("This permission is needed to read the phone state and update the widget accordingly. Without it the app can't function.")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            // Request the permission again
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE_PERMISSION);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            } else {
                Log.i(TAG, "Try Again");
                // Request the permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE_PERMISSION);
                return;
            }
            // return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            //Permission already granted start service
            Intent serviceIntent = new Intent(context, PhoneStateService.class);
            context.startForegroundService(serviceIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_READ_PHONE_STATE_PERMISSION) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can do the required task
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 2);
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 3);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                    // Show a dialog explaining the need for the permission
                    new AlertDialog.Builder(this)
                            .setTitle("Permission needed")
                            .setMessage("This permission is needed to read the phone state and update the widget accordingly.")
                            .setPositiveButton("Ok", (dialog, which) -> {
                                // Request the permission again
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE_PERMISSION);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                } else {
                    // Permission was denied, you can show a message to the user
                    new AlertDialog.Builder(this)
                            .setTitle("Permission needed")
                            .setMessage("This permission is needed to read the phone state and update the widget accordingly.")
                            .setPositiveButton("Ok", (dialog, which) -> {
                                // Redirect the user to the app's settings page
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            //.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                    return;
                }/*
                // Permission was denied, you can show a message to the user
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to read the phone state and update the widget accordingly.")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            // Redirect the user to the app's settings page
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        //.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }*/
            }
        }
        if (requestCode == 2) {

            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 4);
                } else {
                    Intent serviceIntent = new Intent(context, PhoneStateService.class);
                    context.startForegroundService(serviceIntent);
                }
            }
        }
        if (requestCode == 4) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent serviceIntent = new Intent(context, PhoneStateService.class);
                context.startForegroundService(serviceIntent);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public class SimSelectionDialog extends AlertDialog {
        private final List<SubscriptionInfo> subscriptionList;
        private ListView listView;

        protected SimSelectionDialog(Context context, List<SubscriptionInfo> subscriptionList) {
            super(context);
            this.subscriptionList = subscriptionList;
            init();
        }

        private void init() {
            Context context = getContext();
            listView = new ListView(context);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
            for (SubscriptionInfo subscriptionInfo : subscriptionList) {
                int subscriptionId = subscriptionInfo.getSubscriptionId();
                String displayName = subscriptionInfo.getDisplayName().toString();
                String simInfo = "SIM " + subscriptionId + ": \t " + displayName;
                adapter.add(simInfo);
            }
            listView.setAdapter(adapter);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            setView(listView);
            setButton(BUTTON_POSITIVE, "OK", (dialog, which) -> {
                int selectedItemPosition = listView.getCheckedItemPosition();
                if (selectedItemPosition != ListView.INVALID_POSITION) {
                    SubscriptionInfo selectedSubscription = subscriptionList.get(selectedItemPosition);
                    int selectedSimId = selectedSubscription.getSubscriptionId();
                    saveSelectedSimId(context, selectedSimId);
                    Toast.makeText(context, "Selected SIM ID: " + selectedSimId, Toast.LENGTH_SHORT).show();
                    updateMultiSimTxt();
                }
                dialog.dismiss();
            });
            setButton(BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        }
    }

}
