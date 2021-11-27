package com.mpdev.android.display;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boatinstruments.AppConfig;
import com.mpdev.android.boatinstruments.R;
import com.mpdev.android.boatinstruments.SetupActivity;
import com.mpdev.android.logger.Log;

/**
 * ButtonClickListener interface that listens for button clicks on the main page
 */
public class ButtonClickListener implements View.OnClickListener {

    private final String TAG = "ButtonClickListener";

    Activity activity;
    InstrDisplay display;
    BoatData boatData;

    /** Constructor */
    public ButtonClickListener(Activity activity, InstrDisplay display, BoatData boatData) {
        this.activity = activity;
        this.display = display;
        this.boatData = boatData;
    }

    /** button click listener */
    @Override
    public void onClick(View v) {
        // setup button
        if (v.getId() == R.id.action_button) {
            Log.i(TAG,"onClick - will launch SetupActivity");
            Intent intent = new Intent(v.getContext(), SetupActivity.class);
            activity.startActivity(intent);
        }
        // next page button
        else if (v.getId() == R.id.next_button) {
            display.nextPage(boatData);
        }
        // previous page button
        else if (v.getId() == R.id.prev_button) {
            display.prevPage(boatData);
        }
        // day/night button
        else if (v.getId() == R.id.bright_button) {
            display.toggleDayNight();
        }
        // maximum values button
        else if (v.getId() == R.id.max_button) {
            display.toggleMaxValues(boatData);
        }
    }
}
