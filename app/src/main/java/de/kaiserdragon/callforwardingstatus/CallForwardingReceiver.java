package de.kaiserdragon.callforwardingstatus;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class CallForwardingReceiver extends BroadcastReceiver {
    String TAG = "Receiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the current CFI value from the intent
        if ("de.kaiserdragon.callforwardingstatus.TOGGLE_CALL_FORWARDING".equals(intent.getAction())) {

            // Toggle the CFI value


            // Set the new CFI value
            setCallForwarding(context, MyPhoneStateService.currentState);
            // Update the widget to reflect the new CFI value
            //ForwardingStatusWidget.updateWidget(context, cfi);
        }
    }

    private void setCallForwarding(Context context, boolean cfi) {
        // TODO: Implement code to set the call forwarding status here
        Log.i(TAG, String.valueOf(cfi));
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        Handler handler = new Handler();
        TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                super.onReceiveUssdResponse(telephonyManager, request, response);

                //Intent sendintent = new Intent(context,CheckService.class);
                //context.startService(sendintent);//
                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                Toast.makeText(context, String.valueOf(failureCode), Toast.LENGTH_SHORT).show();
            }
        };


        if (cfi == false) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                //if MyPhoneStateListener)
                manager.sendUssdRequest("*21*3311#", responseCallback, handler);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                //if MyPhoneStateListener)
                manager.sendUssdRequest("#21#", responseCallback, handler);
            }
        }
    }

    private void updateWidget(Context context, boolean cfi) {
        // Update the widget's views with the current CFI value
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        views.setTextViewText(R.id.textView, cfi ? "Call forwarding is enabled" : "Call forwarding is disabled");
        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ForwardingStatusWidget.class));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}
