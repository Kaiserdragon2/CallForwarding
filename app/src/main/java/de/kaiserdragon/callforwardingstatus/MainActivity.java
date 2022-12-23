package de.kaiserdragon.callforwardingstatus;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    Context context;
    String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1);
                return;
            }
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               //Intent serviceIntent = new Intent(context, MyPhoneStateService.class);
                //context.startForegroundService(serviceIntent);
                //startService(serviceIntent);
                //Start the MyPhoneStateService
               //context.startService(serviceIntent);


                TelephonyManager manager = (TelephonyManager)
                        context.getSystemService(TELEPHONY_SERVICE);
                Log.i(TAG, "onCallForwarding");
                Handler handler = new Handler();
                TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
                    @Override
                    public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                        super.onReceiveUssdResponse(telephonyManager, request, response);
                        Log.i(TAG, "ok");
                        //Intent sendintent = new Intent(context,CheckService.class);
                        //context.startService(sendintent);//
                        Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                        super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                        Toast.makeText(MainActivity.this, String.valueOf(failureCode), Toast.LENGTH_SHORT).show();
                    }
                };
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    //if MyPhoneStateListener)
                    manager.sendUssdRequest("**21*3311#", responseCallback, handler);
                }

            }

        });
    }



}
