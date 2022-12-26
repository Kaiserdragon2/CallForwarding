package de.kaiserdragon.callforwardingstatus;


import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;


/**
 * Implementation of App Widget functionality.
 */
public class ForwardingStatusWidget extends AppWidgetProvider {
    private static final int REQUEST_CODE_FOREGROUND_SERVICE_PERMISSION = 100;
    private static boolean currentState;
    String TAG = "Widget";


    public void save(Context context) {
        // Get a SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("my_preferences", MODE_PRIVATE);
        // Save the CFI value
        sharedPreferences.edit().putBoolean("cfi", currentState).apply();
    }

    public void load(Context context) {
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

        save(context);
        // Update the widget with the current CFI value
        updateWidget(context, currentState);
    }

    public static void  updateWidget(Context context, boolean cfi) {
        // Update the widget's views with the current CFI value
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        views.setTextViewText(R.id.textView, cfi ? "Call forwarding is enabled" : "Call forwarding is disabled");
        if (cfi) views.setInt(R.id.imageView2, "setColorFilter", Color.GREEN);
        else views.setInt(R.id.imageView2, "setColorFilter", Color.RED);
        // Create an Intent to activate or deactivate call forwarding
        Intent intent = new Intent("de.kaiserdragon.callforwardingstatus.TOGGLE_CALL_FORWARDING");
        intent.setClass(context,CallForwardingReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        //Set Intent to on Widget click
        views.setOnClickPendingIntent(R.id.widget_button,pendingIntent);

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
            intent.setClass(context,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }
        Intent serviceIntent = new Intent(context, MyPhoneStateService.class);
        context.startForegroundService(serviceIntent);
        load(context);
        //updateWidget(context, currentState);


    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}