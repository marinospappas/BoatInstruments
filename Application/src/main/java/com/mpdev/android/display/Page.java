package com.mpdev.android.display;

import android.transition.Scene;
import android.view.View;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boat.BoatDataMax;

interface Page {

    /** gets the text views for this page from the container view */
    void getTextViews();

    /** sets the values of the txt views for this page from boat data */
    void setTextValues(BoatData boatData);

    /** sets the values of the text views to max readings from boat data max */
    void setTextValuesMax(BoatDataMax boatDataMax);

    /** sets the values of the text views to max readings timestamps from boat data max */
    void setTextValuesMaxTime(BoatDataMax boatDataMax);

    /** sets the colours for these text views */
    void setColours();

    /** changes the colour of out-of-date values */
    void setOutOfDate();

    /** sets the text sizes according to the display height */
    void setTextSize(int dispHeight);

    /** sets the scene */
    void setScene(Scene scene);

    /** get the scene */
    Scene getScene();

    /** set the container view */
    void setContainer(View container);

    /** set the container view id */
    void setContainerId(int containerId);

    /** get the container id view */
    int getContainerId();
}
