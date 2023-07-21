package de.kaiserdragon.callforwardingstatus;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // Key for the shared preferences file
    private static final String PREFS_NAME = "WidgetSettings";
    // Key for the background color preference
    private static final String PREF_BACKGROUND_COLOR = "background_color";
    private static final String PREF_TEXT_COLOR = "text_color";
    final String TAG = "Settings";
    float saturation;
    int hue;
    float value;
    int alpha;
    int color;
    int backColor;
    SeekBar colorSeekBar;
    SeekBar SaturationSeekBar;
    SeekBar ValueSeekBar;
    SeekBar AlphaSeekBar;
    TextView showColor;
    int backColorPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initSettings();
        BarListener();

        // Enable the back button on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");


        Button apply = findViewById(R.id.applyColor);
        apply.setOnClickListener(v -> saveColor());
        Spinner spinner = findViewById(R.id.background_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.background_colors, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(backColorPos);


    }

    public void saveColor() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt((PREF_BACKGROUND_COLOR), backColorPos).apply();
        editor.putInt((PREF_TEXT_COLOR), color).apply();
        updateWidgetColor();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        backColorPos = pos;
        setColorBack(backColorPos);
    }

    public void setColorBack(int pos){
        if (pos == 0) backColor = Color.TRANSPARENT;
        if (pos == 1) backColor = Color.BLACK;
        if (pos == 2) backColor = Color.WHITE;
        if (pos == 3) backColor = Color.BLUE;
        if (pos == 4) backColor = Color.YELLOW;
        if (pos == 5) backColor = Color.DKGRAY;
        if (pos == 6) backColor = 0xFF041C45;
        if (pos == 7) backColor = Color.LTGRAY;
        if(DEBUG)Log.v(TAG, String.format("#%08X", backColor));

        showColor.setBackgroundColor(backColor);
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SaturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the background color of the widget based on the progress of the seek bar
                saturation = (float) progress / 100;
                updateColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void initSettings() {
        // init seekbars
        colorSeekBar = findViewById(R.id.hue_seek_bar);
        SaturationSeekBar = findViewById(R.id.saturation_seek_bar);
        ValueSeekBar = findViewById(R.id.value_seek_bar);
        AlphaSeekBar = findViewById(R.id.alpha_seek_bar);
        showColor = findViewById(R.id.color_preview);
        // Get the current value of the background color preference
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        backColorPos= settings.getInt(PREF_BACKGROUND_COLOR, 1);
        setColorBack(backColorPos);
        color = settings.getInt(PREF_TEXT_COLOR, 1);
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
        showColor.setTextColor(color);
    }

    private void updateWidgetColor() {

        if (DEBUG) Log.v(TAG, "Send Intend Color update" + color);
        // Update the background of the widget
        Intent intent = new Intent(this, ForwardingStatusWidget.class);
        intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_COLOR");
        intent.putExtra("color", color);
        intent.putExtra("backColor",backColorPos);
        sendBroadcast(intent);
    }


}
