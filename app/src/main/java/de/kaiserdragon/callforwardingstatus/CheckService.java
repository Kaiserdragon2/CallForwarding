package de.kaiserdragon.callforwardingstatus;

import android.Manifest;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class CheckService extends Service {
    // private final LocalBinder mBinder = new LocalBinder();
    final Handler handler = new Handler() ;
    protected Toast mToast;
    Context context;
    String TAG = "Service";

    public CheckService() {
        context = this;
        Log.i(TAG, "Run");

            }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
                        }
                    }
                    return Service.START_STICKY;
    }

    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            Log.i(TAG, "onCallForwardingIndicatorChanged  CFI =" + cfi);

            Intent intent = new Intent(context, ForwardingStatusWidget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ForwardingStatusWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            intent.putExtra("cfi", cfi);
            sendBroadcast(intent);


            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    super.onCallForwardingIndicatorChanged(cfi);
                }
            }
        }
    }
}