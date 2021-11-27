package com.mpdev.android.boatinstruments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mpdev.android.boat.BoatDataMax;
import com.mpdev.android.logger.Log;

/**
 * User interface for the application setup
 * All items that are configurable through the config file are available to set here
 * The new settings can either be saved to file or just be used while the app is running
 */
public class SetupActivity extends AppCompatActivity {

    private final String TAG = "SetupActivity";

    public static final String LOG_PARAMS = "com.mpdev.android.boatinstruments.LOG_SEVERITY";

    // display fields //
    TextView gwIpAddress, gwIpPort;
    TextView socketTimeout;
    Switch demoMode;
    TextView demoFile;
    RadioGroup displayMode;
    TextView instrColourDay, bgndColourDay, instrColourDayOut, instrColourMaxDay;
    TextView instrColourNight, bgndColourNight, instrColourNightOut, instrColourMaxNight;
    Switch nightMode, autoNightMode;
    TextView maxValuesFile;
    Switch calcTrueWind, calcGndWind;
    TextView polarFile;
    TextView timeoutOutofdate;
    Switch validateChksum;
    // previous display mode is saved here
    AppConfig.InstView prevDispMode;
    ////////////////////
    // log viewer
    TextView logSeverity;
    TextView includeTag, excludeTag;
    TextView fromTime;
    Switch reverseOrder;
    ////////////////////

    Context context;

    @Override
    protected  void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate - started");

        context = getApplicationContext();
        // get the view and the container for the fields
        setContentView(R.layout.setup_activity);
        FrameLayout container = findViewById(R.id.setup_container);
        // and inflate the view with all the fields in it
        getLayoutInflater().inflate(R.layout.setup_fields, container, true);
        // set the actions for when the buttons are clicked
        findViewById(R.id.get_log_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getLog(v); }
        });
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { buttonSave(v); }
        });
        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { buttonCancel(v); }
        });
        findViewById(R.id.keep_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { buttonKeep(v); }
        });
        findViewById(R.id.reset_max_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { buttonResetMax(v); }
        });
        // setup display views
        setupDispViews();
        // update the values on the screen
        updateConfig2Display();
        // save previous display mode
        prevDispMode = AppConfig.DISPLAY_MODE;
        Log.d(TAG,"onCreate - finished");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update the values on the screen
        updateConfig2Display();
    }

    /** action when get log button is clicked */
    public void getLog(View v) {
        Log.i(TAG, "onClick - get log: will launch LogView activity");
        // setup the parameters for the logview class
        String[] params = new String[5];
        params[0] = logSeverity.getText().toString();
        params[1] = includeTag.getText().toString();
        params[2] = excludeTag.getText().toString();
        params[3] = fromTime.getText().toString();
        params[4] = (reverseOrder.isChecked()) ? "yes" : "no" ;
        Intent intent = new Intent(v.getContext(), LogviewActivity.class);
        intent.putExtra(LOG_PARAMS, params);
        // and call the logview activity
        startActivity(intent);
    }

    /** action when save button is clicked */
    public void buttonSave(View v) {
        Log.i(TAG, "onClick - saving config");
        updateDisplay2Config();
        // write config to file
        AppConfig.writeConfig();
        if (AppConfig.DISPLAY_MODE != prevDispMode) {
            Toast message = Toast.makeText(context,"Please restart the Application to activate the new Display Layout", Toast.LENGTH_LONG);
            message.show();
        }
        // restore the previous display mode for this instance (the new one needs restart)
        AppConfig.DISPLAY_MODE = prevDispMode;
        onBackPressed();
    }

    /** action when cancel button is clicked */
    public void buttonCancel(View v) {
        Log.i(TAG, "onClick - cancelling");
        onBackPressed();
    }

    /** action when keep button is clicked */
    public void buttonKeep(View v) {
        Log.i(TAG, "onClick - keep");
        updateDisplay2Config();
        // if it's "keep for this instance only" then restore the previous display mode
        AppConfig.DISPLAY_MODE = prevDispMode;
        onBackPressed();
    }

    /** action when reset max values button is clicked */
    public void buttonResetMax(View v) {
        Log.i(TAG, "onClick - reset max values");
        // set the flag to reset the maximum values
        AppConfig.resetMaxValues = true;
        Toast message = Toast.makeText(context,"Maximum values are being reset", Toast.LENGTH_LONG);
        message.show();
        onBackPressed();
    }

    /** sets up the various view objects for the data entry */
    void setupDispViews() {
        gwIpAddress = findViewById(R.id.ip_address);
        gwIpPort = findViewById(R.id.port);
        socketTimeout = findViewById(R.id.socket_timeout);
        validateChksum = findViewById(R.id.validate_chksum);
        demoMode = findViewById(R.id.demo_mode);
        demoFile = findViewById(R.id.demo_file);
        displayMode = findViewById(R.id.disp_mode);
        instrColourDay = findViewById(R.id.instr_colour_day);
        bgndColourDay = findViewById(R.id.bgnd_colour_day);
        instrColourDayOut = findViewById(R.id.instr_colour_day_out);
        instrColourMaxDay = findViewById(R.id.instr_colour_max_day);
        instrColourNight = findViewById(R.id.instr_colour_night);
        bgndColourNight = findViewById(R.id.bgnd_colour_night);
        instrColourNightOut = findViewById(R.id.instr_colour_night_out);
        instrColourMaxNight = findViewById(R.id.instr_colour_max_night);
        nightMode = findViewById(R.id.night_mode);
        autoNightMode = findViewById(R.id.auto_night_mode);
        maxValuesFile = findViewById(R.id.max_file);
        calcTrueWind = findViewById(R.id.calc_true_wind);
        calcGndWind = findViewById(R.id.calc_gnd_wind);
        polarFile = findViewById(R.id.polar_file);
        timeoutOutofdate = findViewById(R.id.timeout_outofdate);
        // log viewer
        logSeverity = findViewById(R.id.log_severity);
        includeTag = findViewById(R.id.inc_tag);
        excludeTag = findViewById(R.id.exc_tag);
        fromTime = findViewById(R.id.from_time);
        reverseOrder = findViewById(R.id.reverse_order);
    }

    /** update values from config to display */
    void updateConfig2Display() {
        gwIpAddress.setText(AppConfig.NMEA_GW_IP);
        gwIpPort.setText(String.valueOf(AppConfig.NMEA_GW_PORT));
        socketTimeout.setText(String.valueOf(AppConfig.NMEA_GW_TIMEOUT));
        validateChksum.setChecked(AppConfig.NMEA_VALIDATE_CHKSUM);
        demoMode.setChecked(AppConfig.DEMO_MODE);
        demoFile.setText(AppConfig.DEMO_FILE);
        if (AppConfig.DISPLAY_MODE == AppConfig.InstView.SCROLL_VERTICAL)
            displayMode.check(R.id.disp_vert_scroll);
        else if (AppConfig.DISPLAY_MODE == AppConfig.InstView.SCROLL_HORIZONTAL)
            displayMode.check(R.id.disp_hor_scroll);
        else // transition
            displayMode.check(R.id.disp_transition);
        instrColourDay.setText(AppConfig.INSTR_COLOUR_DAY);
        bgndColourDay.setText(AppConfig.BGND_COLOUR_DAY);
        instrColourDayOut.setText(AppConfig.INSTR_COLOUR_DAY_OUT);
        instrColourMaxDay.setText(AppConfig.INSTR_COLOUR_MAX_DAY);
        instrColourNight.setText(AppConfig.INSTR_COLOUR_NIGHT);
        bgndColourNight.setText(AppConfig.BGND_COLOUR_NIGHT);
        instrColourNightOut.setText(AppConfig.INSTR_COLOUR_NIGHT_OUT);
        instrColourMaxNight.setText(AppConfig.INSTR_COLOUR_MAX_NIGHT);
        nightMode.setChecked(AppConfig.NIGHT_MODE);
        autoNightMode.setChecked(AppConfig.NIGHT_MODE_AUTO);
        maxValuesFile.setText(AppConfig.MAX_VALUES_FILENAME);
        calcTrueWind.setChecked(AppConfig.CALCULATE_TRUE_WIND);
        calcGndWind.setChecked(AppConfig.CALCULATE_GND_WIND);
        polarFile.setText(AppConfig.POLAR_FILE);
        timeoutOutofdate.setText(String.valueOf(AppConfig.TIMEOUT_OUT_OF_DATE));
        // logviewer
        logSeverity.setText(String.valueOf(0));
        includeTag.setText(".*");
        excludeTag.setText("");
        fromTime.setText("00:00:00");
    }

    /** update values from config to display */
    void updateDisplay2Config() {
        AppConfig.NMEA_GW_IP = gwIpAddress.getText().toString();
        AppConfig.NMEA_GW_PORT = Integer.parseInt(gwIpPort.getText().toString());
        AppConfig.NMEA_GW_TIMEOUT = Integer.parseInt(socketTimeout.getText().toString());
        AppConfig.NMEA_VALIDATE_CHKSUM = validateChksum.isChecked();
        AppConfig.DEMO_MODE = demoMode.isChecked();
        AppConfig.DEMO_FILE = demoFile.getText().toString();
        int selectedId = displayMode.getCheckedRadioButtonId();
        if (selectedId == R.id.disp_vert_scroll)
            AppConfig.DISPLAY_MODE = AppConfig.InstView.SCROLL_VERTICAL;
        else if (selectedId == R.id.disp_hor_scroll)
            AppConfig.DISPLAY_MODE = AppConfig.InstView.SCROLL_HORIZONTAL;
        else // transition
            AppConfig.DISPLAY_MODE = AppConfig.InstView.TRANSITION;
        AppConfig.INSTR_COLOUR_DAY = instrColourDay.getText().toString();
        AppConfig.BGND_COLOUR_DAY = bgndColourDay.getText().toString();
        AppConfig.INSTR_COLOUR_DAY_OUT = instrColourDayOut.getText().toString();
        AppConfig.INSTR_COLOUR_MAX_DAY = instrColourMaxDay.getText().toString();
        AppConfig.INSTR_COLOUR_NIGHT = instrColourNight.getText().toString();
        AppConfig.BGND_COLOUR_NIGHT = bgndColourNight.getText().toString();
        AppConfig.INSTR_COLOUR_NIGHT_OUT = instrColourNightOut.getText().toString();
        AppConfig.INSTR_COLOUR_MAX_NIGHT = instrColourMaxNight.getText().toString();
        AppConfig.NIGHT_MODE = nightMode.isChecked();
        AppConfig.NIGHT_MODE_AUTO = autoNightMode.isChecked();
        AppConfig.MAX_VALUES_FILENAME = maxValuesFile.getText().toString();
        AppConfig.CALCULATE_TRUE_WIND = calcTrueWind.isChecked();
        AppConfig.CALCULATE_GND_WIND = calcGndWind.isChecked();
        AppConfig.POLAR_FILE = polarFile.getText().toString();
        AppConfig.TIMEOUT_OUT_OF_DATE = Integer.parseInt(timeoutOutofdate.getText().toString());
    }

}