package com.mpdev.android.display;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.transition.Scene;
import android.view.View;
import android.widget.TextView;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boat.BoatDataMax;
import com.mpdev.android.boatinstruments.AppConfig;
import com.mpdev.android.boatinstruments.MainActivity;
import com.mpdev.android.boatinstruments.R;

import java.util.Date;

public class Page1 implements Page {
    // data view variables
    // page 1
    private TextView boatSpeed, heading;
    private TextView appWindSpeed, appWindAngle;
    private TextView boatLog, tripLog;
    private TextView groundWind;
    // text sizes
    int textSize1, textSize2, textSize3;
    // data labels for max values
    private TextView boatSpeedLabel, appWindSpeedLabel, gndwindForceLabel;
    // data fields timestamps
    private Date boatSpeed_t, heading_t, appWind_t, log_t, trip_t, gndWind_t;

    // the corresponding scene object for Transition display
    private Scene scene;
    // the view this page is displayed in
    private View container;
    private int containerId;

    /** gets the text views for this page from the container view */
    @Override
    public void getTextViews() {
        // data views
        boatSpeed = container.findViewById(R.id.boat_speed);
        heading = container.findViewById(R.id.heading);
        appWindSpeed = container.findViewById(R.id.app_wind_speed);
        appWindAngle = container.findViewById(R.id.app_wind_angle);
        boatLog = container.findViewById(R.id.boat_log);
        tripLog = container.findViewById(R.id.trip_log);
        groundWind = container.findViewById(R.id.ground_wind);
        // labels
        boatSpeedLabel = container.findViewById(R.id.boat_speed_label);
        appWindSpeedLabel = container.findViewById(R.id.app_wind_speed_label);
        gndwindForceLabel = container.findViewById(R.id.ground_wind_label);
    }

    /** sets the values of the txt views and timestamps for this page from boat data */
    @SuppressLint("WrongConstant")
    @Override
    public void setTextValues(BoatData boatData) {
        // data
        boatSpeed.setText(boatData.boatSpeedString());
        heading.setText(boatData.headingString());
        appWindSpeed.setText(boatData.appWind.speedString());
        appWindAngle.setText(boatData.appWind.angleString());
        boatLog.setText(boatData.logString());
        tripLog.setText(boatData.tripString());
        groundWind.setText(boatData.gndWind.toString());
        // timestamps
        boatSpeed_t = boatData.boatSpeed_t;
        heading_t = boatData.heading_t;
        appWind_t = boatData.appWind_t;
        log_t = boatData.log_t;
        trip_t = boatData.trip_t;
        gndWind_t = boatData.gndWind_t;
        // labels
        boatSpeedLabel.setText(R.string.boat_speed);
        boatSpeedLabel.setTypeface(null, Typeface.NORMAL);
        appWindSpeedLabel.setText(R.string.app_wind_speed);
        appWindSpeedLabel.setTypeface(null, Typeface.NORMAL);
        gndwindForceLabel.setText(R.string.ground_wind);
        gndwindForceLabel.setTypeface(null, Typeface.NORMAL);
    }

    /** sets the values of the text views to max readings from boat data max */
    @Override
    public void setTextValuesMax(BoatDataMax boatDataMax) {
        // set values of max readings
        boatSpeed.setText(boatDataMax.boatSpeedMaxString());
        appWindSpeed.setText(boatDataMax.appWindSpeedMaxString());
        appWindAngle.setText("");
        groundWind.setText(boatDataMax.gndWindForceMaxString());
        // set labels
        CharSequence label;
        Context context = boatSpeedLabel.getContext();
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.boat_speed));
        boatSpeedLabel.setText(label);
        boatSpeedLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.app_wind_speed));
        appWindSpeedLabel.setText(label);
        appWindSpeedLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.ground_wind));
        gndwindForceLabel.setText(label);
        gndwindForceLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        // set digit colours
        String instrColour;
        if (AppConfig.NIGHT_MODE)
            instrColour = AppConfig.INSTR_COLOUR_MAX_NIGHT;
        else
            instrColour = AppConfig.INSTR_COLOUR_MAX_DAY;
        boatSpeed.setTextColor(Color.parseColor(instrColour));
        appWindSpeed.setTextColor(Color.parseColor(instrColour));
        groundWind.setTextColor(Color.parseColor(instrColour));
    }

    /** sets the values of the text views to max readings timestamps from boat data max */
    @Override
    public void setTextValuesMaxTime(BoatDataMax boatDataMax) {
        // set values of max readings timestamps
        boatSpeed.setText(boatDataMax.maxValueTimeString(boatDataMax.boatSpeedMax_t));
        boatSpeed.setTextSize((float) (textSize1/2.0+1.77));
        appWindSpeed.setText(boatDataMax.maxValueTimeString(boatDataMax.appWindMax_t));
        appWindSpeed.setTextSize((float) (textSize1/2.0+1.77));
        groundWind.setText(boatDataMax.maxValueTimeString(boatDataMax.gndWindMax_t));
        groundWind.setTextSize((float) (textSize2/2.0+1.7));
    }

    /** sets the colours for these text views */
    @Override
    public void setColours() {
        String instrColour, bgndColour;
        if (AppConfig.NIGHT_MODE) {
            instrColour = AppConfig.INSTR_COLOUR_NIGHT;
            bgndColour = AppConfig.BGND_COLOUR_NIGHT;
        }
        else {
            instrColour = AppConfig.INSTR_COLOUR_DAY;
            bgndColour = AppConfig.BGND_COLOUR_DAY;
        }
        // background
        container.setBackgroundColor(Color.parseColor(bgndColour));
        // digits
        boatSpeed.setTextColor(Color.parseColor(instrColour));
        heading.setTextColor(Color.parseColor(instrColour));
        appWindSpeed.setTextColor(Color.parseColor(instrColour));
        appWindAngle.setTextColor(Color.parseColor(instrColour));
        boatLog.setTextColor(Color.parseColor(instrColour));
        tripLog.setTextColor(Color.parseColor(instrColour));
        groundWind.setTextColor(Color.parseColor(instrColour));
    }

    /** changes the colour of out-of-date values */
    @Override
    public void setOutOfDate() {
        Date now = new Date();
        String instrColour;
        if (AppConfig.NIGHT_MODE)
            instrColour = AppConfig.INSTR_COLOUR_NIGHT_OUT;
        else
            instrColour = AppConfig.INSTR_COLOUR_DAY_OUT;
        // check each value on this page - if not recent then set the colour
        if (boatSpeed_t.getTime() > 0L && now.getTime() - boatSpeed_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            boatSpeed.setTextColor(Color.parseColor(instrColour));
        if (heading_t.getTime() > 0L && now.getTime() - heading_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            heading.setTextColor(Color.parseColor(instrColour));
        if (appWind_t.getTime() > 0L && now.getTime() - appWind_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE) {
            appWindSpeed.setTextColor(Color.parseColor(instrColour));
            appWindAngle.setTextColor(Color.parseColor(instrColour));
        }
        if (log_t.getTime() > 0L && now.getTime() - log_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            boatLog.setTextColor(Color.parseColor(instrColour));
        if (trip_t.getTime() > 0L && now.getTime() - trip_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            tripLog.setTextColor(Color.parseColor(instrColour));
        if (gndWind_t.getTime() > 0L && now.getTime() - gndWind_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            groundWind.setTextColor(Color.parseColor(instrColour));
    }

    /** sets the size of the txt views for this page */
    @Override
    public void setTextSize(int dispHeight) {
        textSize1 = dispHeight/36;
        textSize2 = dispHeight/40;
        textSize3 = dispHeight/50;
        boatSpeed.setTextSize(textSize1);
        heading.setTextSize(textSize1);
        appWindSpeed.setTextSize(textSize1);
        appWindAngle.setTextSize(textSize1);
        boatLog.setTextSize(textSize3);
        tripLog.setTextSize(textSize3);
        groundWind.setTextSize(textSize2);
    }

    /** set the Scene */
    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /** get the scene */
    @Override
    public Scene getScene() {
        return scene;
    }

    /** set the container view */
    @Override
    public void setContainer(View container) {
        this.container = container;
    }

    /** set the container view id */
    @Override
    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    /** get the container view id */
    @Override
    public int getContainerId() {
        return containerId;
    }
}
