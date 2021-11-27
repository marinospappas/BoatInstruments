package com.mpdev.android.display;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mpdev.android.boat.BoatData;
import com.mpdev.android.boatinstruments.AppConfig;
import com.mpdev.android.boatinstruments.AppConfig.InstView;
import static com.mpdev.android.boatinstruments.InstrumentsDisplayFragment.*;
import com.mpdev.android.boatinstruments.R;
import com.mpdev.android.logger.Log;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * handles the display of the instrument values
 * supports different views as per Display Mode
 * Horizontal Scrolling, Vertical Scrolling, Transition
 */
public class InstrDisplay {

    private final String TAG = "InstrDisplay";

    // the pages that display the instrument values
    List<Page> pageList = new ArrayList<>();
    // current page (-1 means all pages)
    int curPage = -1;
    // flag to freeze the display (e.g. when max values are displayed)
    boolean freezeDisplay = false;

    // the display mode
    public enum DisplayFlag {
        CURRENT,
        MAX_VALUES,
        MAX_VALUES_TIME
    }
    DisplayFlag displayFlag = DisplayFlag.CURRENT;

    // the main view object (that contains header, footer and the instrument frames)
    View mainView;

    // the transition page container for transition display
    FrameLayout transContainer;

    // prev/next buttons for the Transition view and day/night button
    ImageButton prevPage, nextPage, switchDayNight;
    // setup button
    ImageButton setupButton;
    // max values button
    ImageButton maxButton;

    // image for app status
    ImageView imgStatus = null;

    Activity activity;
    LayoutInflater inflater;

    // flag for first time a page is displayed
    boolean firstTimeDisplayed = true;

    // screen dimensions
    int dispHeight = 0;
    int dispWidth = 0;

    // custom transition for Transition display only - not used at this stage
    Transition mTransition = new Transition() {
        @Override
        public void captureStartValues(TransitionValues transitionValues) {}
        @Override
        public void captureEndValues(TransitionValues transitionValues) {}
        @Override
        public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
            return null;     // no animator - straight transition
        }
    };
    //////////////////

    /**
     * Constructor
     */
    public InstrDisplay(Activity activity, LayoutInflater inflater) {
        this.activity = activity;
        this.inflater = inflater;
    }

    /**
     * insert a new page in the list
     */
    public void insertPage(Page p) {
        pageList.add(p);
    }

    /**
     * inflate the views - called by OnCreateView
     */
    public View inflateViews(ViewGroup mainContainer) {

        // get display dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (activity != null) {
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dispHeight = displayMetrics.heightPixels;
            dispWidth = displayMetrics.widthPixels;
            Log.i(TAG, "Display dimensions " + dispWidth + " x " + dispHeight);
        }
        // inflate the main view as per display mode
        if (AppConfig.DISPLAY_MODE == InstView.SCROLL_VERTICAL)
            mainView = inflater.inflate(R.layout.boat_fields_scrollvert, mainContainer, false);
        else if (AppConfig.DISPLAY_MODE == InstView.SCROLL_HORIZONTAL)
            mainView = inflater.inflate(R.layout.boat_fields_scrollhor, mainContainer, false);
        else if (AppConfig.DISPLAY_MODE == InstView.TRANSITION) {
            mainView = inflater.inflate(R.layout.boat_fields_transition, mainContainer, false);
            // in addition, set the container and first page for the Transition view
            transContainer = mainView.findViewById(R.id.trans_frame);
            curPage = 0;
        }
        else {
            Log.i(TAG, "invalid display mode: " + AppConfig.DISPLAY_MODE + " - using default vertical scroll view");
            mainView = inflater.inflate(R.layout.boat_fields_defaultview, mainContainer, false);
        }
        // previous and next buttons and day/night button
        prevPage = mainView.findViewById(R.id.prev_button);
        nextPage = mainView.findViewById(R.id.next_button);
        switchDayNight = mainView.findViewById(R.id.bright_button);
        // action (setup) button
        setupButton = mainView.findViewById(R.id.action_button);
        // max values button
        maxButton = mainView.findViewById(R.id.max_button);
        // app status view
        imgStatus = mainView.findViewById(R.id.status);

        return mainView;
    }

    /**
     * inflate the page view into its container for scrolling views, or
     * set the scene for the page - transition display
     */
    public void setPageView(Page page, int pageLayoutId, int pageContainerId, Context context) {
        if (AppConfig.DISPLAY_MODE != InstView.TRANSITION) {
            // get the page container
            View pageContainer = mainView.findViewById(pageContainerId);
            // non-Transition - inflate the page into its container
            // for horizontal scroll view, set the width of the frame
            if (AppConfig.DISPLAY_MODE == InstView.SCROLL_HORIZONTAL)
                pageContainer.setMinimumWidth(dispWidth);
            inflater.inflate(pageLayoutId, (FrameLayout)pageContainer, true);
            for (Page p : pageList)
                if (Objects.equals(p, page)) {
                    // save the page container in the relevant page
                    p.setContainer(pageContainer);
                    p.getTextViews();
                    break;
                }
        }
        else {
            // Transition display - get a new scene for this layout
            // here we ignore the page container in the parameters and use the previously identified transContainer
            Scene scene = Scene.getSceneForLayout(transContainer, pageLayoutId, context);
            // set this scene and the id of the container into the relevant page in the list of pages
            for (Page p : pageList)
                if (Objects.equals(p, page)) {
                    // saver the scene and page container id in the relevant page
                    p.setScene(scene);
                    p.setContainerId(pageContainerId);
                    break;
                }
        }
    }

    /** set the page for the transition */
    public void setTransitionPage() {
            Page p = pageList.get(curPage);
            // transition to the new scene
            Scene scene = p.getScene();
            TransitionManager.go(scene);
            // get the view this page is displayed on
            View thisPageContainer = scene.getSceneRoot().findViewById(p.getContainerId());
            // and save it in the page so that the text views can be extracted
            p.setContainer(thisPageContainer);
            p.getTextViews();
    }

    /**
     * set the button listeners
     */
    public void setOnClickListener(ButtonClickListener onClickListener) {
        if (prevPage != null)
            prevPage.setOnClickListener(onClickListener);
        if (nextPage != null)
            nextPage.setOnClickListener(onClickListener);
        if (switchDayNight != null)
            switchDayNight.setOnClickListener(onClickListener);
        if (setupButton != null)
            setupButton.setOnClickListener(onClickListener);
        if (maxButton != null)
            maxButton.setOnClickListener(onClickListener);
    }

    /**
     * set current page
     */
    public void setCurPage(int pageNumber) {
        curPage = pageNumber;
    }

    /**
     * get current page
     */
    public int getCurPage() {
        return curPage;
    }

    /**
     * display this page - behaviour depends on display mode:
     * in transition mode displays the current page, in scrolling mode displays all the pages
     * that are part of the scroll view
     */
    public void thisPage(BoatData boatData) {
        if (AppConfig.DISPLAY_MODE == InstView.TRANSITION) {
            // check if this is the first time we display a page
            if (firstTimeDisplayed) {
                // in this case transition to the first page
                setTransitionPage();
                firstTimeDisplayed = false;
            }
            // in transition mode display the current page
            displayCurPage(boatData);
        }
        else {
            // in scrolling mode display all pages
            for (int i = 0; i < pageList.size(); ++i) {
                curPage = i;
                displayCurPage(boatData);
            }
        }
    }

    /**
     * display next page - only for Transition display
     */
    public void nextPage(BoatData boatData) {
        // set display flag
        displayFlag = DisplayFlag.CURRENT;
        // unfreeze display (just in case)
        freezeDisplay = false;
        if (curPage >= 0 && curPage < pageList.size() - 1) {
            ++curPage;
            // execute transition
            setTransitionPage();
            displayCurPage(boatData);
        }
    }

    /**
     * display previous page - only for Transition display
     */
    public void prevPage(BoatData boatData) {
        // set display flag
        displayFlag = DisplayFlag.CURRENT;
        // unfreeze display (just in case)
        freezeDisplay = false;
        if (curPage > 0) {
            --curPage;
            // execute transition
            setTransitionPage();
            displayCurPage(boatData);
        }
    }

    /**
     * display page indicated in curPage
     */
    private void displayCurPage(BoatData boatData) {
        // if display freeze flag is on, do nothing
        if (freezeDisplay)
            return;
        Page page = pageList.get(curPage);
        if (AppConfig.DISPLAY_MODE == InstView.TRANSITION) {
            Log.d(TAG, "transitioning to page #" + curPage);
            // update prev / next buttons
            updatePrevNextButtons();
        }
        // set the colours and text size on this page
        page.setColours();
        page.setTextSize(dispHeight);
        // display the instrument values
        page.setTextValues(boatData);
        // update the day/night button
        updateDayNightButton();
        // update colour if data out-of-date
        page.setOutOfDate();
    }

    /** update prev/next buttons */
    public void updatePrevNextButtons() {
        if (curPage == 0) {
            if (prevPage != null) {
                prevPage.setVisibility(View.GONE);
                prevPage.setEnabled(false);
            }
            if (nextPage != null) {
                nextPage.setVisibility(View.VISIBLE);
                nextPage.setEnabled(true);
            }
        }
        else if (curPage == pageList.size()-1) {
            if (prevPage != null) {
                prevPage.setVisibility(View.VISIBLE);
                prevPage.setEnabled(true);
            }
            if (nextPage != null) {
                nextPage.setVisibility(View.GONE);
                nextPage.setEnabled(false);
            }
        }
        else {
            if (prevPage != null) {
                prevPage.setVisibility(View.VISIBLE);
                prevPage.setEnabled(true);
            }
            if (nextPage != null) {
                nextPage.setVisibility(View.VISIBLE);
                nextPage.setEnabled(true);
            }
        }
    }

    /** update day/night button */
    public void updateDayNightButton() {
        if (! AppConfig.NIGHT_MODE_AUTO) {
            if (switchDayNight != null) {
                switchDayNight.setVisibility(View.VISIBLE);
                switchDayNight.setEnabled(true);
            }
        }
        else {
            if (switchDayNight != null) {
                switchDayNight.setVisibility(View.GONE);
                switchDayNight.setEnabled(false);
            }
        }
    }

    /** set disp colours */
    public void setDispColAndSizes() {
        if (AppConfig.DISPLAY_MODE == InstView.TRANSITION) {
            pageList.get(curPage).setColours();
            pageList.get(curPage).setTextSize(dispHeight);
            pageList.get(curPage).setOutOfDate();
        }
        else
            for (Page p: pageList) {
                p.setColours();
                p.setTextSize(dispHeight);
                p.setOutOfDate();
            }
    }

    /** toggle between day and night colours */
    public void toggleDayNight() {
        // if display freeze flag is on, do nothing
        if (freezeDisplay)
            return;
        AppConfig.NIGHT_MODE = !AppConfig.NIGHT_MODE;
        Log.d(TAG, "night mode:" + AppConfig.NIGHT_MODE);
        setDispColAndSizes();
        updateDayNightButton();
    }

    /** check for outdated data */
    public void setOutdatedColour() {
        // if display freeze flag is on, do nothing
        if (freezeDisplay)
            return;
        if (AppConfig.DISPLAY_MODE == InstView.TRANSITION)
            pageList.get(curPage).setOutOfDate();
        else
            for (Page p: pageList)
                p.setOutOfDate();
    }

    /** get the status image view */
    public ImageView getImageStatus() {
        return imgStatus;
    }

    /** switch between max values and current ones */
    public void toggleMaxValues(BoatData boatData) {
        // switch from current to max values and from max values to max timestamps
        // and then back to current
        switch (displayFlag) {
            case CURRENT:
                // freeze display when showing max values
                freezeDisplay = true;
                // display max values (based on DISPLAY_MODE)
                if (AppConfig.DISPLAY_MODE == InstView.TRANSITION)
                    pageList.get(curPage).setTextValuesMax(boatData.maxValues);
                else {
                    for (Page p: pageList)
                        p.setTextValuesMax(boatData.maxValues);
                }
                // and set the display mode
                displayFlag = DisplayFlag.MAX_VALUES;
                break;
            case MAX_VALUES:
                // keep the display frozen
                freezeDisplay = true;
                // display max values timestamps (based on DISPLAY_MODE)
                if (AppConfig.DISPLAY_MODE == InstView.TRANSITION)
                    pageList.get(curPage).setTextValuesMaxTime(boatData.maxValues);
                else {
                    for (Page p: pageList)
                        p.setTextValuesMaxTime(boatData.maxValues);
                }
                // and set the display mode
                displayFlag = DisplayFlag.MAX_VALUES_TIME;
                break;
            case MAX_VALUES_TIME:
                // return to normal display of current values
                // unfreeze display to return to current values
                freezeDisplay = false;
                // and display the current data
                thisPage(boatData);
                // and set the display mode
                displayFlag = DisplayFlag.CURRENT;
                break;
        }
    }
}
