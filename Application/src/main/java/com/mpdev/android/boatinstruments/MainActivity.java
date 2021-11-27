/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.mpdev.android.boatinstruments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mpdev.android.logger.Log;

/**
 * Displays the boat instrument measurements that are received from the NMEA Gateway via WiFi
 * A simple launcher activity using Fragment
 * @version 2.4.6 28.07.2021
 * @author Marinos Pappas
 *
 * Version History:
 *
 * 2.4.2 28.07.2021
 * Button in setup screen to reset max values and timestamps
 * Filename for saving max values across sessions configurable via setup parameter
 * Fixed bug with max value when measurement is 0.0 (introduced with the reset of max values)
 *
 * 2.4.1 26.07.2021
 * New digit colour introduced for max values (day and night - also added to the setup screen)
 * New labels introduced on all pages for the fields that support max values
 *   - these are used when max values or timestamp of max values are displayed
 *
 * 2.4 25.07.2021
 * Support for maximum values (class BoatDataMax)
 * Maximum values are saved in file so that they are retained across sessions until reset
 *
 * 2.3.3 24.07.2021
 * Fixed bug in setting out-of-date colour for the first time
 * Fixed bug in setting out-of-date colour when switching pages
 *
 * 2.3.2 14.07.2021
 * Applied out-of-date logic to page 2 and page 3
 * Added new NMEA message: $GPXTE - cross track error
 * Added XTE to page 3
 *
 * 2.3.1 13.07.2021
 * Updated next waypoint message parser to recognise GPBWC msg
 * Added next waypoint coordinates
 * Fixed bug in reading longitude degrees and minutes from NMEA message
 *
 * 2.3 11.07.2021
 * Added new NMEA messages: $GPVTG - SOG and COG
 * Fixed bug with out-of-date instrument digits colour config
 * Added setup fields for out-of-date instrument digits colour and out-of-date timeout
 * Removed redundant setup fields:
 *     max lines to read form network
 *     init timer
 *     loop timer
 *
 * 2.2.2 25.04.2021
 * Added timestamp for every intrument reading - change colour in the display for out-of-date values
 * (new config params - outdated colour, timeout that makes a value outdated, timer that checks all the fields)
 *
 * 2.2.1 23.4.2021
 * Added logic to display "--.-" instead of "00.0" for values that have not been received yet
 *
 * 2.2 19.04.2021
 * Added introductory screen (Zephyr photo) as separate activity (MainActivity)
 * What used to be the MainActivity has now become DisplayActivity
 *
 * 2.1.1 05.04.2021
 * Added logic in the background read thread to disconnect and reconnect to network in case of read timeout
 * Added GPS Date/Time fields on the screen
 *
 * 2.1 03.04.2021
 * Changed the network read to be entirely on a different thread
 * and to call the main thread every time a new message is received - removed the timer
 *
 * 2.0.1 02.04.2021
 * Added toast notification to user to restart when a different display layout has been selected
 * Minor bug fixes in the new disdplay classes and minor improvements
 *
 * 2.0 31.03.2021
 * Refactoring and separtion of all the display functions to separate classes
 * Simplification of the main Display Fragment
 *
 * 1.3.1 25.03.2021
 * Day and night mode theme
 * Auto-switching using the light sensor
 *
 * 1.3 24.03.2021
 * Implemented polar
 *
 * 1.2 22.03.2021
 * LogViewer added
 *
 * 1.1.1 21.03.2021
 * Connection to gateway opened and closed with every read to conserve battery
 *
 * 1.1 20.03.2021
 * Connection to the NMEA Gateway
 * Setup Screen
 * WiFi / Demo indicators on screen
 *
 * 1.0.3 18.03.2021
 * Transition with Net/Previous Buttons
 *
 * 1.0.2 16.03.2021
 * Horizontal scroll screen
 *
 * 1.0.1 15.03.2021
 * Verticall scroll screen to diasplay all the instrument values
 *
 * 1.0 13.03.2021
 * Basic code - NMEA decoder - display values from a test file
 *
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    // application file path
    public static String appFilePath = "./";

    // package version
    public static String appVersion = "a.b";

    /** onCreate - main entry point for the app */
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG,"onCreate: started");
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();

        // the introductory screen
        setContentView(R.layout.activity_main);

        try {
            appVersion= this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (Exception e) {
            Log.i(TAG, "could not get package version: "+ e.getMessage());
        }
        Log.a(TAG, getString(R.string.app_name) + " version " + appVersion );
        Log.a(TAG, getString(R.string.author));

        // read application config
        appFilePath = context.getFilesDir().getPath() + "/";
        Log.i(TAG, "data dir: " + appFilePath);
        String confInfo = AppConfig.readConfig();
        Log.i(TAG, "read config: " + confInfo);

        // set introductory screen fields
        ((TextView)findViewById(R.id.main_app_name)).setText(getString(R.string.app_name));
        ((TextView)findViewById(R.id.main_app_version)).setText("Version: " + appVersion);
        ((TextView)findViewById(R.id.main_author)).setText(getString(R.string.author));
        // and button click listener to proceed to the app
        ((Button)findViewById(R.id.buttonx)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(context, DisplayActivity.class));
            }
        });

        Log.d(TAG,"onCreate: finished");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
