package de.kaiserdragon.callforwardingstatus;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class MyPhoneStateService extends Service {
    private static final String CHANNEL_ID = "my_phone_state_service_channel";
    public static boolean currentState;
    Context context;
    String TAG = "Service";
    private MyPhoneStateListener phoneStateListener;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Phone State Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        // Create a notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Phone State Service")
                .setContentText("Running in the foreground")
                .setSmallIcon(R.drawable.ic_callforwarding)
                .build();

        // Start the service as a foreground service
        startForeground(1, notification);
        context = this;
        Log.i(TAG, "Create");
        // Register MyPhoneStateListener as a phone state listener
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy");
        // Unregister MyPhoneStateListener as a phone state listener
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return null as this service is not bound to any activity
        return null;
    }

    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            Log.i(TAG, "onCallForwardingIndicatorChanged  CFI =" + cfi);
            // Get the current state of unconditional call forwarding
            currentState = cfi;
            // Create an Intent with the android.appwidget.action.APPWIDGET_UPDATE action
            Intent intent = new Intent(context, ForwardingStatusWidget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");

            // Add the app widget IDs as an extra
            int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ForwardingStatusWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

            // Add the CFI value as an extra
            intent.putExtra("cfi", currentState);

            // Send the broadcast
            sendBroadcast(intent);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    super.onCallForwardingIndicatorChanged(cfi);
                }
            }
        }

    }
}
