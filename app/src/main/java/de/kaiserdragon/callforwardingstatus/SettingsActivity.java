package de.kaiserdragon.callforwardingstatus;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    final String TAG = "Settings";
    // Key for the shared preferences file
    private static final String PREFS_NAME = "MyPrefsFile";
    // Key for the background color preference
    private static final String PREF_BACKGROUND_COLOR = "background_color";

    private int mBackgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get the current value of the background color preference
        //SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //mBackgroundColor = settings.getInt(PREF_BACKGROUND_COLOR, Color.WHITE);

        SeekBar colorSeekBar = findViewById(R.id.color_seek_bar);
        colorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                updateWidgetColor(progress,90);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
        SeekBar SaturationSeekBar = findViewById(R.id.color_seek_bar2);
        SaturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                updateWidgetColor(10,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });



    }


    private void updateWidgetColor(int progressHue,int progressSaturation) {

           float saturation =  (float) progressSaturation /100;
        // Convert the progress value (0-360) to a hue value (0-1)

        Log.v(TAG, String.valueOf(saturation));
        // Create a new color using the hue value
        int color = Color.HSVToColor(new float[]{progressHue, saturation, 1});
        TextView showColor = findViewById(R.id.color_preview);
        showColor.setBackgroundColor(color);
        Log.v(TAG, String.valueOf(color));

        // Update the background of the widget
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.forwarding_status_widget);
        views.setInt(R.id.widget_button, "setBackgroundColor", color);
        AppWidgetManager.getInstance(this).updateAppWidget(new ComponentName(this, ForwardingStatusWidget.class), views);
        Intent intent = new Intent(this, ForwardingStatusWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE_COLOR");
        intent.putExtra("cfi", color);
        sendBroadcast(intent);
    }

}
