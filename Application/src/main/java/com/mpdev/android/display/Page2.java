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

public class Page2 implements Page {
    // data view variables
    // page 2
    private TextView depth, waterTemp;
    private TextView trueWindSpeed, trueWindAngle;
    private TextView polarSpeed, polarEff;
    private TextView gpsDateGMT, gpsTimeGMT;
    // text sizes
    int textSize1, textSize2, textSize3;
    // data labels for max values
    private TextView waterTempLabel, trueWindSpeedLabel, polarEffLabel;
    // data fields timestamps
    private Date depth_t, waterTemp_t, trueWind_t, polarSpeed_t,
                polarEff_t, gpsTime_t;

    // the corresponding scene object for Transition display
    private Scene scene;
    // the view this page is displayed in
    private View container;
    private int containerId;

    /** gets the text views for this page from the container view */
    @Override
    public void getTextViews() {
        // data views
        depth = container.findViewById(R.id.depth);
        waterTemp = container.findViewById(R.id.water_temp);
        trueWindSpeed = container.findViewById(R.id.true_wind_speed);
        trueWindAngle = container.findViewById(R.id.true_wind_angle);
        polarSpeed = container.findViewById(R.id.polar_speed);
        polarEff = container.findViewById(R.id.polar_eff);
        gpsDateGMT = container.findViewById(R.id.gps_date);
        gpsTimeGMT = container.findViewById(R.id.gps_time);
        // labels
        waterTempLabel = container.findViewById(R.id.water_temp_label);
        trueWindSpeedLabel = container.findViewById(R.id.true_wind_speed_label);
        polarEffLabel = container.findViewById(R.id.polar_eff_label);
    }

    /** sets the values of the txt views for this page from boat data */
    @Override
    public void setTextValues(BoatData boatData) {
        // data
        depth.setText(boatData.depthString());
        waterTemp.setText(boatData.waterTempString());
        trueWindSpeed.setText(boatData.trueWind.speedString());
        trueWindAngle.setText(boatData.trueWind.angleString());
        polarSpeed.setText(boatData.polarSpeedString());
        polarEff.setText(boatData.polarEffString());
        gpsDateGMT.setText(boatData.gpsDate());
        gpsTimeGMT.setText((boatData.gpsTime()));
        // timestamps
        depth_t = boatData.depth_t;
        waterTemp_t = boatData.waterTemp_t;
        trueWind_t = boatData.trueWind_t;
        polarSpeed_t = boatData.polarSpeed_t;
        polarEff_t = boatData.polarEff_t;
        gpsTime_t = boatData.gpsTime_t;
        // labels
        waterTempLabel.setText(R.string.water_temp);
        trueWindSpeedLabel.setText(R.string.true_wind_speed);
        polarEffLabel.setText(R.string.polar_eff);
    }

    /** sets the values of the text views to max readings from boat data max */
    @Override
    public void setTextValuesMax(BoatDataMax boatDataMax) {
        // set values of max readings
        waterTemp.setText(boatDataMax.waterTempMaxString());
        trueWindSpeed.setText(boatDataMax.trueWindSpeedMaxString());
        trueWindAngle.setText("");
        polarEff.setText(boatDataMax.polarEffMaxString());
        // set labels
        CharSequence label;
        Context context = waterTempLabel.getContext();
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.water_temp));
        waterTempLabel.setText(label);
        waterTempLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.true_wind_speed));
        trueWindSpeedLabel.setText(label);
        trueWindSpeedLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        label = String.format (context.getString(R.string.max_label), context.getString(R.string.polar_eff));
        polarEffLabel.setText(label);
        polarEffLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        // set digit colours
        String instrColour;
        if (AppConfig.NIGHT_MODE)
            instrColour = AppConfig.INSTR_COLOUR_MAX_NIGHT;
        else
            instrColour = AppConfig.INSTR_COLOUR_MAX_DAY;
        waterTemp.setTextColor(Color.parseColor(instrColour));
        trueWindSpeed.setTextColor(Color.parseColor(instrColour));
        polarEff.setTextColor(Color.parseColor(instrColour));
    }

    /** sets the values of the text views to max readings timestamps from boat data max */
    @Override
    public void setTextValuesMaxTime(BoatDataMax boatDataMax) {
        // set values of max readings timestamps
        waterTemp.setText(boatDataMax.maxValueTimeString(boatDataMax.waterTempMax_t));
        waterTemp.setTextSize((float) (textSize1/2.0+1.77));
        trueWindSpeed.setText(boatDataMax.maxValueTimeString(boatDataMax.trueWindMax_t));
        trueWindSpeed.setTextSize((float) (textSize1/2.0+1.77));
        polarEff.setText(boatDataMax.maxValueTimeString(boatDataMax.polarEffMax_t));
        polarEff.setTextSize((float) (textSize1/2.0+1.77));
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
        depth.setTextColor(Color.parseColor(instrColour));
        waterTemp.setTextColor(Color.parseColor(instrColour));
        trueWindSpeed.setTextColor(Color.parseColor(instrColour));
        trueWindAngle.setTextColor(Color.parseColor(instrColour));
        polarSpeed.setTextColor(Color.parseColor(instrColour));
        polarEff.setTextColor(Color.parseColor(instrColour));
        gpsDateGMT.setTextColor(Color.parseColor(instrColour));
        gpsTimeGMT.setTextColor(Color.parseColor(instrColour));
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
        if (depth_t.getTime() > 0L &&  now.getTime() - depth_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            depth.setTextColor(Color.parseColor(instrColour));
        if (waterTemp_t.getTime() > 0L &&  now.getTime() - waterTemp_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            waterTemp.setTextColor(Color.parseColor(instrColour));
        if (trueWind_t.getTime() > 0L &&  now.getTime() - trueWind_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE) {
            trueWindSpeed.setTextColor(Color.parseColor(instrColour));
            trueWindAngle.setTextColor(Color.parseColor(instrColour));
        }
        if (polarSpeed_t.getTime() > 0L &&  now.getTime() - polarSpeed_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            polarSpeed.setTextColor(Color.parseColor(instrColour));
        if (polarEff_t.getTime() > 0L &&  now.getTime() - polarEff_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE)
            polarEff.setTextColor(Color.parseColor(instrColour));
        if (gpsTime_t.getTime() > 0L &&  now.getTime() - gpsTime_t.getTime() > AppConfig.TIMEOUT_OUT_OF_DATE) {
            gpsTimeGMT.setTextColor(Color.parseColor(instrColour));
            gpsDateGMT.setTextColor(Color.parseColor(instrColour));
        }
    }

    /** sets the size of the txt views for this page */
    @Override
    public void setTextSize(int dispHeight) {
        textSize1 = dispHeight/36;
        textSize2 = dispHeight/40;
        textSize3 = dispHeight/50;
        depth.setTextSize(textSize1);
        waterTemp.setTextSize(textSize1);
        trueWindSpeed.setTextSize(textSize1);
        trueWindAngle.setTextSize(textSize1);
        polarSpeed.setTextSize(textSize1);
        polarEff.setTextSize(textSize1);
        gpsDateGMT.setTextSize(textSize3);
        gpsTimeGMT.setTextSize(textSize3);
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
