package de.kaiserdragon.callforwardingstatus;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;



public class ForwardingStatusWidget extends AppWidgetProvider {
    private static boolean currentState;
    final static String TAG = "Widget";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v(TAG,"onReceive got called");
        if ("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI".equals(intent.getAction())) {
            // Get the current CFI value from the Intent extra
            currentState = intent.getBooleanExtra("cfi", currentState);
            if(DEBUG)Log.v(TAG,"CFI:"+currentState);
            PhoneStateService.currentState = currentState;
            // Update the widget with the current CFI value
            updateWidget(context, currentState);
        }

    }

    public static void updateWidget(Context context, boolean cfi) {
        // Update the widget's views with the current CFI value
        if(DEBUG)Log.v(TAG,"...updating");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        if (cfi) views.setInt(R.id.ImageCallForwarding, "setColorFilter", Color.GREEN);
        else views.setInt(R.id.ImageCallForwarding, "setColorFilter", Color.RED);
        // Create an Intent to activate or deactivate call forwarding
        Intent intent = new Intent("de.kaiserdragon.callforwardingstatus.TOGGLE_CALL_FORWARDING");
        intent.setClass(context, CallForwardingReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        //Set Intent to on Widget click
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ForwardingStatusWidget.class));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Check if the READ_PHONE_STATE permission has been granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setClass(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }
        if (DEBUG) Log.v(TAG,"onUpdate got called");
        updateWidget(context, currentState);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}