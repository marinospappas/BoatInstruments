package com.mpdev.android.boatinstruments;

import android.app.Activity;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boat.NmeaMessage;
import com.mpdev.android.display.InstrDisplay;
import com.mpdev.android.logger.Log;

import java.util.List;
import java.util.TimerTask;

/**
 * Timer Task extension that runs the scheduled task to read the instrument readings and update the display
 */
class OutdatedTimerTask extends TimerTask {

    Activity activity;
    InstrDisplay display;


    /** Constructor */
    public OutdatedTimerTask(Activity activity, InstrDisplay display) {
        this.activity = activity;
        this.display = display;
    }

    /** the periodic task */
    @Override
    public void run() {
        TimerTick();
    }


    /**
     * timer tick task
     * this is where we read the NMEA messages and update the display
     */
    private void TimerTick() {
        // called directly by the timer (runs in the same thread as the timer)
        // we call the method that will work with the UI through the runOnUiThread method
        Log.d("TimerThread", "out-of-date data check activated");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // read the next batch of NMEA messages
                display.setOutdatedColour();
            }
        });
    }
}
