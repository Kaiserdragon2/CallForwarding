package de.kaiserdragon.callforwardingstatus;


import static android.content.Context.TELEPHONY_SERVICE;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


/**
 * Implementation of App Widget functionality.
 */
public class ForwardingStatusWidget extends AppWidgetProvider {
    boolean cfi;
    private  PhoneStateReceiver phoneStateReceiver  = null;
    String TAG = "hjkfgskahjsdgj";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {
        String TAG = "Widget";
        CharSequence widgetText;

        if (!cfi) {
            widgetText = context.getString(R.string.appwidget_text3);
        } else widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        Log.i(TAG, "onCallForwardingIndicatorChanged  CFI =" + widgetText);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        //views.setTextViewTextSize(appWidgetId, COMPLEX_UNIT_SP, 14);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        cfi = intent.getBooleanExtra("cfi", false);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}