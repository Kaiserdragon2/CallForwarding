package de.kaiserdragon.callforwardingstatus;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_PHONE_STATE_PERMISSION = 1;


    final DatabaseHelper databaseHelper = new DatabaseHelper(this);
    //SQLiteDatabase database = databaseHelper.getWritableDatabase();
    Context context;
    Activity activity;
    final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;



        checkPermission(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
           ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 3);
        }

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
        phoneNumber1EditText.setText(getPhoneNumber(1));
        phoneNumber2EditText.setText(getPhoneNumber(2));
        phoneNumber3EditText.setText(getPhoneNumber(3));
        setCheckedRadioButton();



        // Set OnClickListeners on the ImageButton views
        saveButton1.setOnClickListener(v -> saveSQLData(phoneNumber1EditText,1));
        saveButton2.setOnClickListener(v -> saveSQLData(phoneNumber2EditText,2));
        saveButton3.setOnClickListener(v -> saveSQLData(phoneNumber3EditText,3));
        deleteButton1.setOnClickListener(v -> deleteSQLData(phoneNumber1EditText,1));
        deleteButton2.setOnClickListener(v -> deleteSQLData(phoneNumber2EditText,2));
        deleteButton3.setOnClickListener(v -> deleteSQLData(phoneNumber3EditText,3));


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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Open the settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCheckedRadioButton(){
       String[] array= databaseHelper.getSelected();
        RadioButton radioButton1= findViewById(R.id.radioButton1);
        RadioButton radioButton2= findViewById(R.id.radioButton2);
        RadioButton radioButton3= findViewById(R.id.radioButton3);
        if (Objects.equals(array[0], "1"))radioButton1.setChecked(true);
        if (Objects.equals(array[0], "2"))radioButton2.setChecked(true);
        if (Objects.equals(array[0], "3"))radioButton3.setChecked(true);
        Log.i(TAG, "Retrieve Data"+ array[0]+"phone"+array[1]);
    }

    private String getPhoneNumber(int row) {
        // Query the database to retrieve the data
        Log.i(TAG, "Retrieve Data");
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
            if (phoneNumberColumnIndex != -1) phoneNumber = cursor.getString(phoneNumberColumnIndex);
        }

        // Close the cursor
        cursor.close();
        return phoneNumber;
    }

    private void saveSQLData(EditText numberInput,int row){
        String phoneNumber = numberInput.getText().toString();

        // Insert the data into the database
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(databaseHelper.getColumnId(), row);
        values.put(databaseHelper.getColumnPhoneNumber(), phoneNumber);
        String whereClause = databaseHelper.getColumnId() + " = ?";
        String[] whereArgs = {String.valueOf(row)}; // Replace "1" with the ID of the row you want to update
        int ok = database.update(databaseHelper.getTableName(), values, whereClause, whereArgs);
        long insOk = 0;
        if (ok == 0) {
            values.put(databaseHelper.getColumnId(), row);
            values.put(databaseHelper.getColumnSelected(),"false");
            insOk = database.insert(databaseHelper.getTableName(), null, values);
        }
        if (insOk == 0 && ok == 1)Toast.makeText(context, getString(R.string.PhoneNumberSaved), Toast.LENGTH_SHORT).show();
    }

    private void deleteSQLData(EditText numberInput, int row){
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String whereClause = databaseHelper.getColumnId() + " = ?";
        String[] whereArgs = {String.valueOf(row)}; // Replace "1" with the ID of the row you want to delete
        database.delete(databaseHelper.getTableName(), whereClause, whereArgs);
        numberInput.setText(getPhoneNumber(row));
    }

    public void checkPermission(Activity activity) {
        // Check Permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE_PERMISSION);
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
                Log.i(TAG, "Try Again");
                // Request the permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE_PERMISSION);
                return;
            }
           // return;
        }
        //Permission already granted start service
        Intent serviceIntent = new Intent(context, PhoneStateService.class);
        context.startForegroundService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_READ_PHONE_STATE_PERMISSION) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can do the required task
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 3);
                }
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
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
