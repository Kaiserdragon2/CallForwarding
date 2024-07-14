package de.kaiserdragon.callforwardingstatus;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.kaiserdragon.callforwardingstatus.helper.CallForwardingListener;

public class PhoneStateService extends Service {
    private static final String CHANNEL_ID = "CallForwardingServiceID";
    public static boolean currentState;
    Context appcontext;
    static final String TAG = "Service";

    // Define NOTIFICATION_ID as a constant
    private static final int NOTIFICATION_ID = 1;
    private CallForwardingListener callForwardingListener;


    /** @noinspection deprecation*/
    @TargetApi(Build.VERSION_CODES.R)
    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            if (DEBUG)Log.i(TAG, "onCallForwardingIndicatorChanged  CFI  Old=" + cfi);
            // Create an Intent with the android.appwidget.action.APPWIDGET_UPDATE action
            Intent intent = new Intent(appcontext, ForwardingStatusWidget.class);
            intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI");
            // Add the CFI value as an extra
            intent.putExtra("cfi", cfi);
            // Send the broadcast
            sendBroadcast(intent);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(appcontext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    super.onCallForwardingIndicatorChanged(cfi);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        appcontext = getApplicationContext();
        createNotificationChannel();
        startForegroundService();

    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name), // Use string resources for text
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.NotificationTitle))
                .setContentText(getString(R.string.NotificationText))
                .setSmallIcon(R.drawable.ic_call_forwarding)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            if (ActivityCompat.checkSelfPermission(appcontext, Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE)
                    == PackageManager.PERMISSION_GRANTED) {
                startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                // Handle permission not granted (request permission, inform user, etc.)
                Log.w(TAG, "FOREGROUND_SERVICE_SPECIAL_USE permission not granted");
                // Consider gracefully stopping the service if this permission is critical
            }
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerPhoneStateListener();
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void registerPhoneStateListener() {
        TelephonyManager telephonyManager = getSystemService(TelephonyManager.class); // Use class reference for type safety
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (callForwardingListener == null) { // Check if already registered
                callForwardingListener = new CallForwardingListener(appcontext);
                telephonyManager.registerTelephonyCallback(Executors. newSingleThreadExecutor(), callForwardingListener);
                if (DEBUG) Log.i(TAG, "Registered TelephonyCallback");
            }
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
            //if (DEBUG) Log.i(TAG, "Registered PhoneStateListener");
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onDestroy() {
        Log.d(TAG, "Destroy");
        TelephonyManager telephonyManager = getSystemService(TelephonyManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&callForwardingListener != null) {
            telephonyManager.unregisterTelephonyCallback(callForwardingListener); // Unregister explicitly
            callForwardingListener = null;
            if (DEBUG) Log.i(TAG, "Unregistered TelephonyCallback");
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
