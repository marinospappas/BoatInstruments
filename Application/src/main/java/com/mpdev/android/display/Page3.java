package com.mpdev.android.display;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.transition.Scene;
import android.view.View;
import android.widget.TextView;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boat.BoatDataMax;
import com.mpdev.android.boatinstruments.AppConfig;
import com.mpdev.android.boatinstruments.R;

import java.util.Date;

public class Page3 implements Page {
    // data view variables
    // page 3
    private TextView sog, cog;
    private TextView posLat, posLong;
    private TextView nextWpName, nextWpDist, nextWpBrng;
    private TextView nextWpLat, nextWpLong;
    private TextView xTrackError;
    // text sizes
    int textSize1, textSize2, textSize3, textSize4;
    // data labels for max values
    private TextView sogLabel;

    // data fields timestamps
    Date sog_t, cog_t, position_t, nextWp_t, xTrackError_t;

    // the corresponding scene object for Transition display
    private Scene scene;
    // the view this page is displayed in
    private View container;
    private int containerId;

    /** gets the text views for this page from the container view */
    @Override
    public void getTextViews() {
        // data views
        sog = container.findViewById(R.id.sog);
        cog = container.findViewById(R.id.cog);
        posLat = container.findViewById(R.id.pos_lat);
        posLong = container.findViewById(R.id.pos_long);
        nextWpName = container.findViewById(R.id.nextwp_name);
        nextWpDist = container.findViewById(R.id.distance);
        nextWpBrng = container.findViewById(R.id.bearing);
        nextWpLat = container.findViewById(R.id.wp_lat);
        nextWpLong = container.findViewById(R.id.wp_long);
        xTrackError = container.findViewById((R.id.xte));
        // labels
        sogLabel = container.findViewById(R.id.sog_label);
    }

    /** sets the values of the txt views for this page from boat data */
    @Override
    public void setTextValues(BoatData boatData) {
        // data
        sog.setText(boatData.sogString());
        cog.setText(boatData.cogString());
        posLat.setText(boatData.position.toString("lat"));
        posLong.setText(boatData.position.toString("long"));
        nextWpName.setText(boatData.nextWaypoint.name.substring(0,Math.min(boatData.nextWaypoint.name.length(),6)));
        nextWpDist.setText(boatData.nextWaypoint.distString());
        nextWpBrng.setText(boatData.nextWaypoint.bearingString());
        nextWpLat.setText(boatData.nextWaypoint.wpCoord.toString("lat"));
        nextWpLong.setText(boatData.nextWaypoint.wpCoord.toString("long"));
        xTrackError.setText(boatData.xteString());
        // timestamps
        sog_t = boatData.sog_t;
        cog_t = boatData.cog_t;
        position_t = boatData.position_t;
        nextWp_t = boatData.nextWaypoint_t;
        xTrackError_t = boatData.xte_t;
        // labels
        sogLabel.setText(R.string.sog);
        sogLabel.setTypeface(null, Typeface.NORMAL);
    }

    /** sets the values of the text views to max readings from boat data max */
    @Override
    public void setTextValuesMax(BoatDataMax boatDataMax) {
        // set values of max readings
        sog.setText(boatDataMax.sogMaxString());
        // set labels
        CharSequence label;
        Context context = sogLabel.getContext();
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.sog));
        sogLabel.setText(label);
        sogLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        // set digit colours
        String instrColour;
        if (AppConfig.NIGHT_MODE)
            instrColour = AppConfig.INSTR_COLOUR_MAX_NIGHT;
        else
            instrColour = AppConfig.INSTR_COLOUR_MAX_DAY;
        sog.setTextColor(Color.parseColor(instrColour));
    }

    /** sets the values of the text views to max readings timestamps from boat data max */
    @Override
    public void setTextValuesMaxTime(BoatDataMax boatDataMax) {
        // set values of max readings timestamps
        sog.setText(boatDataMax.maxValueTimeString(boatDataMax.boatSpeedMax_t));
        sog.setTextSize((float) (textSize2/2.0+1.7));
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
        sog.setTextColor(Color.parseColor(instrColour));
        cog.setTextColor(Color.parseColor(instrColour));
        posLat.setTextColor(Color.parseColor(instrColour));
        posLong.setTextColor(Color.parseColor(instrColour));
        nextWpName.setTextColor(Color.parseColor(instrColour));
        nextWpDist.setTextColor(Color.parseColor(instrColour));
        nextWpBrng.setTextColor(Color.parseColor(instrColour));
        nextWpLat.setTextColor(Color.parseColor(instrColour));
        nextWpLong.setTextColor(Color.parseColor(instrColour));
        xTrackError.setTextColor(Color.parseColor(instrColour));
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
        if (sog_t.getTime() > 0L &&  now.getTime() - sog_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            sog.setTextColor(Color.parseColor(instrColour));
        if (cog_t.getTime() > 0L &&  now.getTime() - cog_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            cog.setTextColor(Color.parseColor(instrColour));
        if (position_t.getTime() > 0L &&  now.getTime() - position_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE) {
            posLat.setTextColor(Color.parseColor(instrColour));
            posLong.setTextColor(Color.parseColor(instrColour));
        }
        if (nextWp_t.getTime() > 0L &&  now.getTime() - nextWp_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE) {
            nextWpName.setTextColor(Color.parseColor(instrColour));
            nextWpBrng.setTextColor(Color.parseColor(instrColour));
            nextWpDist.setTextColor(Color.parseColor(instrColour));
            nextWpLat.setTextColor(Color.parseColor(instrColour));
            nextWpLong.setTextColor(Color.parseColor(instrColour));
        }
        if (xTrackError_t.getTime() > 0L &&  now.getTime() - xTrackError_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            xTrackError.setTextColor(Color.parseColor(instrColour));
    }

    /** sets the size of the txt views for this page */
    @Override
    public void setTextSize(int dispHeight) {
        textSize1 = dispHeight/36;
        textSize2 = dispHeight/40;
        textSize3 = dispHeight/50;
        textSize4 = dispHeight/60;
        sog.setTextSize(textSize2);
        cog.setTextSize(textSize2);
        posLat.setTextSize(textSize3);
        posLong.setTextSize(textSize3);
        nextWpName.setTextSize(textSize4);
        xTrackError.setTextSize(textSize4);
        nextWpDist.setTextSize(textSize3);
        nextWpBrng.setTextSize(textSize3);
        nextWpLat.setTextSize(textSize4);
        nextWpLong.setTextSize(textSize4);
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
