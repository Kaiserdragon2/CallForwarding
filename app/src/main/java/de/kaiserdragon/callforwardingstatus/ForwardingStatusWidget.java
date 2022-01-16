package de.kaiserdragon.callforwardingstatus;


import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class ForwardingStatusWidget extends AppWidgetProvider {
    boolean cfi;


    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {
        String TAG = "Widget";
        CharSequence widgetText;

        if (!cfi) {
            widgetText = context.getString(R.string.appwidget_text2);
        } else widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        Log.i(TAG, "onCallForwardingIndicatorChanged  CFI =" + widgetText);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_status_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewTextSize(appWidgetId, COMPLEX_UNIT_SP, 100);
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        cfi = intent.getBooleanExtra("cfi", false);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {

        // Enter relevant functionality for when the first widget is created


    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}