package de.kaiserdragon.callforwardingstatus;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    // Key for the shared preferences file
    private static final String PREFS_NAME = "WidgetSettings";
    // Key for the background color preference
    private static final String PREF_BACKGROUND_COLOR = "background_color";
    final String TAG = "Settings";
    float saturation;
    int hue;
    float value;
    int alpha;
    int color;
    SeekBar colorSeekBar;
    SeekBar SaturationSeekBar;
    SeekBar ValueSeekBar;
    SeekBar AlphaSeekBar;
    private int mBackgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initSettings();
        BarListener();


    }

    private void BarListener() {
        colorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                hue = progress;
                updateColor();
                //updateWidgetColor(alpha,progress,saturation,value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateWidgetColor();
            }
        });

        SaturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                saturation = (float) progress / 100;
                updateColor();
                //updateWidgetColor(10,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateWidgetColor();
            }
        });


        ValueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                value = (float) progress / 100;
                updateColor();
                //updateWidgetColor(10,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateWidgetColor();
            }
        });


        AlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                alpha = progress;
                updateColor();
                //updateWidgetColor(10,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateWidgetColor();
            }
        });

    }

    private void initSettings() {
        // init seekbars
        colorSeekBar = findViewById(R.id.hue_seek_bar);
        SaturationSeekBar = findViewById(R.id.saturation_seek_bar);
        ValueSeekBar = findViewById(R.id.value_seek_bar);
        AlphaSeekBar = findViewById(R.id.alpha_seek_bar);
        // Get the current value of the background color preference
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mBackgroundColor = settings.getInt(PREF_BACKGROUND_COLOR, Color.BLACK);
        color = mBackgroundColor;
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hue = (int) hsv[0];
        saturation = hsv[1];
        value = hsv[2];
        alpha = Color.alpha(color);
        colorSeekBar.setProgress(hue);
        SaturationSeekBar.setProgress((int) (saturation * 100));
        ValueSeekBar.setProgress((int) (value * 100));
        AlphaSeekBar.setProgress(alpha);
        updateColor();
    }

    private void updateColor() {
        color = Color.HSVToColor(alpha, new float[]{hue, saturation, value});
        TextView showColor = findViewById(R.id.color_preview);
        showColor.setBackgroundColor(color);
    }

    private void updateWidgetColor() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt((PREF_BACKGROUND_COLOR), color).apply();
        if(DEBUG) Log.v(TAG,"Send Intend Color update"+ color);
        // Update the background of the widget
        //RemoteViews views = new RemoteViews(getPackageName(), R.layout.forwarding_status_widget);
        //views.setInt(R.id.widget_button, "setBackgroundColor", color);
        //AppWidgetManager.getInstance(this).updateAppWidget(new ComponentName(this, ForwardingStatusWidget.class), views);
        Intent intent = new Intent(this, ForwardingStatusWidget.class);
        intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_COLOR");
        intent.putExtra("color", color);
        sendBroadcast(intent);
    }

}
