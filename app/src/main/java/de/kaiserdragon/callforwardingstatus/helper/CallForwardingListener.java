package de.kaiserdragon.callforwardingstatus.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyCallback;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;

import de.kaiserdragon.callforwardingstatus.ForwardingStatusWidget;
import de.kaiserdragon.callforwardingstatus.PhoneStateService;

@RequiresApi(api = Build.VERSION_CODES.S)
public class CallForwardingListener extends TelephonyCallback implements TelephonyCallback.CallForwardingIndicatorListener {

    private static final String TAG = "CallForwardingListener";
    private static WeakReference<Context> contextRef;

    public CallForwardingListener(@NonNull Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    public void onCallForwardingIndicatorChanged(boolean callForwardingIndicator) {

        Log.i(TAG, "onCallForwardingIndicatorChanged - New CFI: " + callForwardingIndicator);
        PhoneStateService.currentState = callForwardingIndicator;
        sendWidgetUpdateBroadcast(callForwardingIndicator);

    }

    private void sendWidgetUpdateBroadcast(boolean callForwardingIndicator) {
        Context context = contextRef.get();
        Intent intent = new Intent(context, ForwardingStatusWidget.class);
        intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI");
        // Add the CFI value as an extra
        intent.putExtra("cfi", callForwardingIndicator);
        // Send the broadcast
        context.sendBroadcast(intent);
    }
}
