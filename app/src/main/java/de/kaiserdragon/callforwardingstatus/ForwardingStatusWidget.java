package de.kaiserdragon.callforwardingstatus;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    String TAG = "Widget";
    private static boolean currentState;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void save(Context context){
        // Get a SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("my_preferences", MODE_PRIVATE);
        // Save the CFI value
        sharedPreferences.edit().putBoolean("cfi", currentState).apply();
    }
    public void load (Context context){
        // Get an instance of SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("your_prefs_name", Context.MODE_PRIVATE);

        // Load the CFI value from shared preferences
        currentState = sharedPreferences.getBoolean("cfi", false); // The second parameter is the default value to use if the CFI value is not found in shared preferences
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Get the current CFI value from the Intent extra
        boolean cfi = intent.getBooleanExtra("cfi", currentState);
        currentState = cfi;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);

        int color = Color.parseColor("white"); // red color
        views.setInt(R.id.imageView, "setColorFilter", color);
        save(context);
        // Update the widget with the current CFI value
        updateWidget(context, currentState);
    }

    private void updateWidget(Context context, boolean cfi) {
        // Update the widget's views with the current CFI value
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        views.setTextViewText(R.id.textView, cfi ? "Call forwarding is enabled" : "Call forwarding is disabled");

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,ForwardingStatusWidget.class));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Create an Intent to start the MyPhoneStateService
        Intent serviceIntent = new Intent(context, MyPhoneStateService.class);
        context.startForegroundService(serviceIntent);
        load(context);
        updateWidget(context, currentState);

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}