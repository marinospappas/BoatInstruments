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

import com.mpdev.android.logger.Log;


/**
 * This is actually the main activity that displays the NMEA data on the screen
 */
public class DisplayActivity extends FragmentActivity implements SensorEventListener {

    public static final String TAG = "DisplayActivity";

    // the display fragment
    InstrumentsDisplayFragment fragment;

    // light sensor if auto-switch day/night is enabled
    private Sensor lightSensor = null;
    private SensorManager lightSensorManager;

    /** onCreate - main entry point for the activity */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG,"onCreate: started");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // get a new instance of the Instruments Transition Fragment
            fragment = new InstrumentsDisplayFragment();
            // and put it in our instruments view
            transaction.replace(R.id.instruments_content_fragment, fragment);
            transaction.commit();
        }

        // get an instance of SensorManager
        lightSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // check for a light sensor
        lightSensor = lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null)
            Log.e(TAG, "could not set light sensor object");
        else
            Log.i(TAG, "light sensor set up");

        Log.d(TAG,"onCreate: finished");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        Log.d(TAG,"onResume: started");
        super.onResume();
        // if the light sensor is available on the current device then register a listener
        if (AppConfig.NIGHT_MODE_AUTO && lightSensor != null) {
            lightSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.i(TAG, "auto day/night switching enabled");
        }
        // call the display fragment resumeread to resume the background thread
        if (fragment != null)
            fragment.resumeRead();
        Log.d(TAG,"onResume: finished");
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause: started");
        super.onPause();
        // check auto day-night switching
        if (AppConfig.NIGHT_MODE_AUTO && lightSensor != null) {
            lightSensorManager.unregisterListener(this);
            Log.i(TAG, "auto day/night switching disabled");
        }
        // call the display fragment pauseread to stop the background thread
        if (fragment != null)
            fragment.pauseRead();
        Log.d(TAG,"onPause: finished");
    }

    /** light sensor has data */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the light intensity
        double currentValue = sensorEvent.values[0];
        if (AppConfig.NIGHT_MODE && currentValue > AppConfig.DAY_THRESHOLD) {
            AppConfig.NIGHT_MODE = false;
            Log.i(TAG, "onSensorChanged: switching to day mode");
        }
        else
        if (!AppConfig.NIGHT_MODE && currentValue < AppConfig.NIGHT_THRESHOLD) {
            AppConfig.NIGHT_MODE = true;
            Log.i(TAG, "onSensorChanged: switching to night mode");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing here for now
    }

    /** back button - exit app */
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed - exiting application");
        super.onBackPressed();
        Intent exitApp = new Intent(this,QuitAppActivity.class);
        exitApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exitApp);
        finish();
    }
}
