package de.kaiserdragon.callforwardingstatus;


import static android.content.Context.MODE_PRIVATE;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;


/**
 * Implementation of App Widget functionality.
 */
public class ForwardingStatusWidget extends AppWidgetProvider {
    private static boolean currentState;
    private static int ViewText;
    private static int TextColor;
    private static int backColor;
    final static String TAG = "Widget";


    public void save(Context context) {
        // Get a SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppState", MODE_PRIVATE);
        // Save the CFI value
        sharedPreferences.edit().putBoolean("cfi", currentState).apply();
    }

    public void saveInt(Context context) {
        // Get a SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppState", MODE_PRIVATE);
        // Save the CFI value
        sharedPreferences.edit().putInt("visible", ViewText).apply();
    }

    public void load(Context context) {
        // Get an instance of SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppState", Context.MODE_PRIVATE);
        SharedPreferences settings = context.getSharedPreferences("WidgetSettings", Context.MODE_PRIVATE);
        // Load the CFI value from shared preferences
        currentState = sharedPreferences.getBoolean("cfi", false); // The second parameter is the default value to use if the CFI value is not found in shared preferences
        ViewText = sharedPreferences.getInt("visible",View.VISIBLE);
        TextColor = settings.getInt("text_color",Color.BLACK);
        backColor = settings.getInt("background_color",Color.BLACK);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        if (DEBUG) Log.v(TAG,"width"+minWidth);
        if ((float)(minWidth / minHeight) <= 1.5) {
            views.setViewVisibility(R.id.StatusText, View.GONE);
            ViewText = View.GONE;
        } else if (minWidth < 180) {
            views.setViewVisibility(R.id.StatusText, View.GONE);
            ViewText = View.GONE;
        } else {
            views.setViewVisibility(R.id.StatusText, View.VISIBLE);
            ViewText = View.VISIBLE;
        }
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
        saveInt(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        load(context);
        if ("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI".equals(intent.getAction())) {
            // Get the current CFI value from the Intent extra
            currentState = intent.getBooleanExtra("cfi", currentState);
            //currentState = cfi;

            save(context);
            // Update the widget with the current CFI value
            updateWidget(context, currentState);
        }
        if("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_COLOR".equals(intent.getAction())) {

            TextColor = intent.getIntExtra("color", 0);
            backColor = intent.getIntExtra("backColor", 0);
            if(DEBUG)Log.v(TAG,"backColorPos:"+backColor);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
            updateBackground(backColor,views);
            //Drawable drawable = ContextCompat.getDrawable(context,R.drawable.widget_background_round);
            // Set the color of the drawable
            //DrawableCompat.setTint(drawable, color);
            views.setInt(R.id.StatusText,"setTextColor",TextColor);
            // Update the widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ForwardingStatusWidget.class));
            appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, views);
        }

    }

    public static void updateBackground(int pos, RemoteViews views){
        if (pos==0) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_round);
        if (pos==1) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_black);
        if (pos==2) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_white);
        if (pos==3) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_blue);
        if (pos==4) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_yellow);
        if (pos==5) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_dark_grey);
        if (pos==6) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_dark_blue);
        if (pos==7) views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.widget_background_light_grey);


    }
    public static void updateWidget(Context context, boolean cfi) {
        // Update the widget's views with the current CFI value
        if(DEBUG)Log.v(TAG,"...updating");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        views.setTextViewText(R.id.StatusText, cfi ? context.getString(R.string.CallForwardingActive) : context.getString(R.string.CallForwardingInactive));
        views.setViewVisibility(R.id.StatusText, ViewText);
        views.setInt(R.id.StatusText,"setTextColor", TextColor);
        updateBackground(backColor, views);
        //views.setInt(R.id.widget_button, "setBackgroundColor",backColor);
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
        Intent serviceIntent = new Intent(context, PhoneStateService.class);
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