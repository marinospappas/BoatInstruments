/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mpdev.android.boatinstruments;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boat.BoatDataMax;
import com.mpdev.android.boat.BoatPolar;
import com.mpdev.android.boat.NmeaMessage;

import java.util.Timer;

import com.mpdev.android.logger.Log;
import com.mpdev.android.display.*;

/**
 * InstrumentsTransitionFragment
 * Reads the NMEA messages and displays the instrument readings on the screen
 */
public class InstrumentsDisplayFragment extends Fragment {

    private static final String STATE_CURRENT_PAGE = "current_page";

    /** Tag for the logger */
    private static final String TAG = "InstrDisplayFrgmnt";

    // display objects
    InstrDisplay display;
    Page1 page1;
    Page2 page2;
    Page3 page3;

    // boat / nmea objects
    NmeaGw nmeaGw = null;
    BoatData boatData;
    BoatDataMax maxData;
    NmeaMessage nmeaMessage;

    // the timer that checks for outdated data
    Timer outdatedTimer;

    // keep the activity object for this activity here
    Activity activity;

    /** Constructor */
    public InstrumentsDisplayFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView started");

        // get the activity
        activity = this.getActivity();
        if (activity == null)
            Log.e(TAG,"getActivity returned null");
        // initialise the display
        View view;
        display = new InstrDisplay(activity, inflater);
        display.insertPage(page1 = new Page1());
        display.insertPage(page2 = new Page2());
        display.insertPage(page3 = new Page3());
        // inflate the views
        view = display.inflateViews(container);

        Log.d(TAG, "onCreateView finished");
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated started");

        // set the app version
        ((TextView)view.findViewById(R.id.app_version)).setText(MainActivity.appVersion);

        Log.i(TAG, "setting up the page views");
        Context context = getContext();
        // setup the page views
        display.setPageView(page1, R.layout.boat_fields_page1, R.id.page_frame1, context);
        display.setPageView(page2, R.layout.boat_fields_page2, R.id.page_frame2, context);
        display.setPageView(page3, R.layout.boat_fields_page3, R.id.page_frame3, context);

        // get the current page (valid only for Transition - otherwise the page would be -1)
        if (null != savedInstanceState)
            display.setCurPage(savedInstanceState.getInt(STATE_CURRENT_PAGE));

        // initialise the boat, nmea message and nmea gateway objects
        nmeaGw = new NmeaGw(activity, this);
        nmeaMessage = new NmeaMessage();
        boatData = new BoatData();
        // setup the max values structure
        maxData = new BoatDataMax();
        boatData.maxValues = maxData;
        // get previously observed max values from file
        maxData.loadMaxBoatData();

        // load polar
        String polarFile = MainActivity.appFilePath + AppConfig.POLAR_FILE;
        String res = BoatPolar.loadPolarTable(polarFile);
        Log.i(TAG, res);
        if (!BoatPolar.polarLoaded)
            Log.e(TAG, "error loading polar");
        else
            Log.i(TAG, "loaded polar\n" + BoatPolar.polarToString());

        // setup the button listener
        display.setOnClickListener(new ButtonClickListener(activity, display, boatData));

        // start the background read thread
        nmeaGw.start();

        // setup the timer that will check for outdated data on the screen and will change the colour if required
        outdatedTimer = new Timer();
        outdatedTimer.schedule(new OutdatedTimerTask(activity, display),
                AppConfig.TIMEOUT_OUT_OF_DATE, AppConfig.TIMEOUT_OUT_OF_DATE);

        // display the view
        display.thisPage(boatData);
        // display the fields (most likely all 0's at this stage)
        display.thisPage(boatData);
        Log.d(TAG, "onViewCreated finished");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored called");
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState)
            display.setCurPage(savedInstanceState.getInt(STATE_CURRENT_PAGE));
        display.thisPage(boatData);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE, display.getCurPage());
    }

    public void pauseRead() {
        Log.i(TAG, "stopping background read thread");
        if (nmeaGw != null) {
            nmeaGw.stop();
            nmeaGw = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void resumeRead() {
        if (nmeaGw == null) {
            Log.i(TAG, "starting new background read thread");
            nmeaGw = new NmeaGw(activity, this);
            boatData = new BoatData();
            // retain the max values already recorded
            boatData.maxValues = maxData;
            nmeaGw.start();
        }
    }

    /** process an incoming nmea message and update the display */
    @RequiresApi(api = Build.VERSION_CODES.O)
    void processMessage(String nmeaMsgStr) {
        // build the nmea message object
        nmeaMessage.set(nmeaMsgStr, AppConfig.NMEA_VALIDATE_CHKSUM);
        if (boatData.updateBoatData(nmeaMessage)) {
            // if message processed then display values
            display.thisPage(boatData);
            // also update maximum values (if necessary)
            boatData.maxValues.updateMaxBoatData(boatData);
        }
        else
            Log.i(TAG, "message not recognised");
    }

}
