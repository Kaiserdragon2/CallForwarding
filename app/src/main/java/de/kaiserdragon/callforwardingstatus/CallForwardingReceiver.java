package de.kaiserdragon.callforwardingstatus;

import static android.content.Context.TELEPHONY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;
import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class CallForwardingReceiver extends BroadcastReceiver {
    final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("de.kaiserdragon.callforwardingstatus.TOGGLE_CALL_FORWARDING".equals(intent.getAction())) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            String[] array = databaseHelper.getSelected();
            if (DEBUG) Log.v(TAG, "Number = " + array[1]);
            if (!array[1].equals("")) {
                Toast.makeText(context, context.getString(R.string.setupCallForwarding), Toast.LENGTH_LONG).show();
                setCallForwarding(context, PhoneStateService.currentState, array[1]);
            } else
                Toast.makeText(context, context.getString(R.string.NoNumber), Toast.LENGTH_SHORT).show();
        }
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            Intent serviceIntent = new Intent(context, PhoneStateService.class);
            context.startService(serviceIntent);
        }
    }

    public int getSavedSelectedSimId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SIM_PREFERENCES", Context.MODE_PRIVATE);
        return preferences.getInt("SELECTED_SIM_ID", -1); // -1 is a default value if the preference is not found
    }


    private void setCallForwarding(Context context, boolean cfi, String phoneNumber) {
        if (DEBUG) Log.i(TAG, String.valueOf(cfi));
        if (DEBUG) Log.v(TAG, phoneNumber);
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);

        int defaultSubId = getSavedSelectedSimId(context);
        if (defaultSubId <= 0) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            // Determine the default subscription ID
            defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
            // Check if the device supports multiple SIMs and retrieve active subscriptions
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
                    // Choose the first active subscription
                    SubscriptionInfo subscriptionInfo = activeSubscriptions.get(0);
                    defaultSubId = subscriptionInfo.getSubscriptionId();
                }
            }
        }


        Handler handler = new Handler();
        TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                super.onReceiveUssdResponse(telephonyManager, request, response);
                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                Log.v(TAG, request);

            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                Toast.makeText(context, String.valueOf(failureCode), Toast.LENGTH_SHORT).show();
            }
        };
        TelephonyManager manager1;
        if (!cfi) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                String ussdRequest = "*21*" + phoneNumber + "#";

                // Set the subscription ID for call forwarding
                manager1 = manager.createForSubscriptionId(defaultSubId);

                manager1.sendUssdRequest(ussdRequest, responseCallback, handler);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                // Set the subscription ID for call forwarding
                manager1 = manager.createForSubscriptionId(defaultSubId);

                manager1.sendUssdRequest("#21#", responseCallback, handler);
            }
        }
    }
}