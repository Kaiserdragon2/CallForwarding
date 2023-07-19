package de.kaiserdragon.callforwardingstatus;

import static android.content.Context.TELEPHONY_SERVICE;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class CallForwardingReceiver extends BroadcastReceiver {
    final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("de.kaiserdragon.callforwardingstatus.TOGGLE_CALL_FORWARDING".equals(intent.getAction())) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            String[] array = databaseHelper.getSelected();
            if (DEBUG) Log.v(TAG,"Number = "+array[1]);
            if (!array[1].equals("")) {
                Toast.makeText(context,context.getString(R.string.setupCallForwarding) , Toast.LENGTH_LONG).show();
                setCallForwarding(context, PhoneStateService.currentState, array[1]);
            }else  Toast.makeText(context,context.getString(R.string.NoNumber) , Toast.LENGTH_SHORT).show();
        }
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent serviceIntent = new Intent(context, PhoneStateService.class);
            context.startService(serviceIntent);
        }
    }

    private void setCallForwarding(Context context, boolean cfi, String phoneNumber) {
        if (DEBUG)Log.i(TAG, String.valueOf(cfi));
        if (DEBUG) Log.v(TAG,phoneNumber);
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);

        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);

        // Determine the default subscription ID
        int defaultSubId = SubscriptionManager.getDefaultSubscriptionId();

        // Check if the device supports multiple SIMs and retrieve active subscriptions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
                    // Choose the first active subscription
                    SubscriptionInfo subscriptionInfo = activeSubscriptions.get(0);
                    defaultSubId = subscriptionInfo.getSubscriptionId();
                }
            }
        }
        if (DEBUG) Log.v(TAG, String.valueOf(defaultSubId));

        Handler handler = new Handler();
        TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                super.onReceiveUssdResponse(telephonyManager, request, response);
                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                Toast.makeText(context, String.valueOf(failureCode), Toast.LENGTH_SHORT).show();
            }
        };

        if (!cfi) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                String ussdRequest = "*21*" + phoneNumber + "#";
                if (DEBUG) Log.v(TAG,ussdRequest);
                manager.sendUssdRequest(ussdRequest, responseCallback, handler);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                manager.sendUssdRequest("#21#", responseCallback, handler);
            }
        }
    }
}
