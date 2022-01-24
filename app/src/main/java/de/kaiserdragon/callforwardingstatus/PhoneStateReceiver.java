package de.kaiserdragon.callforwardingstatus;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PhoneStateReceiver extends BroadcastReceiver {
Context context;
static final String TAG = "BroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.i(TAG, "Recive");
        Intent sendintent = new Intent(context,CheckService.class);
        context.startService(sendintent);//
        // Checker();
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
    }
    public void Checker(){

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // } else {
            TelephonyManager manager = (TelephonyManager)
                    context.getSystemService(TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                manager.listen(new MyPhoneStateListener(),
                        PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
                //  }
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            Log.i(TAG, "onCallForwardingIndicatorChanged  CFI =" + cfi);

            Intent intent = new Intent(context, ForwardingStatusWidget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ForwardingStatusWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            intent.putExtra("cfi", cfi);
            context.sendBroadcast(intent);


            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    super.onCallForwardingIndicatorChanged(cfi);
                }
            }
        }
    }
}