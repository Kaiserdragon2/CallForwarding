package de.kaiserdragon.callforwardingstatus;

import static androidx.core.content.ContextCompat.getSystemService;
import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.Executors;

import de.kaiserdragon.callforwardingstatus.helper.CallForwardingListener;

public class PhoneStateWorker extends Worker {
    private static final String CHANNEL_ID = "CallForwardingServiceID";
    public static boolean currentState;
    private final Context appcontext;
    private static final String TAG = "Worker";
    private static final int NOTIFICATION_ID = 1;
    private CallForwardingListener callForwardingListener;

    public PhoneStateWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.appcontext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Result doWork() {
        registerPhoneStateListener();
        return Result.success();
    }

    private void registerPhoneStateListener() {
        TelephonyManager telephonyManager = appcontext.getSystemService(TelephonyManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (callForwardingListener == null) {
                callForwardingListener = new CallForwardingListener(appcontext);
                telephonyManager.registerTelephonyCallback(Executors.newSingleThreadExecutor(), callForwardingListener);
                if (DEBUG) Log.i(TAG, "Registered TelephonyCallback");
            }
        } else {
            // Ensure the Handler is initialized on a thread with a prepared Looper
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            Handler handler = new Handler(Looper.myLooper());
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallForwardingIndicatorChanged(boolean cfi) {
                    if (DEBUG) Log.i(TAG, "onCallForwardingIndicatorChanged  CFI  Old=" + cfi);
                    Intent intent = new Intent(appcontext, ForwardingStatusWidget.class);
                    intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI");
                    intent.putExtra("cfi", cfi);
                    appcontext.sendBroadcast(intent);

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                        if (ActivityCompat.checkSelfPermission(appcontext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            super.onCallForwardingIndicatorChanged(cfi);
                        }
                    }
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
            if (DEBUG) Log.i(TAG, "Registered PhoneStateListener");
        }
    }
}
