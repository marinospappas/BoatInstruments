package com.mpdev.android.boat;

public class NmeaFormatterString {

    // talkers supported
    static final String  MAGNETIC_COMPASS = "HC";
    static final String  INTEGRATED_INSTRUMENTATION = "II";
    static final String  WIND_INSTRUMENTS = "WI";
    static final String  ECHO_SOUNDER = "SD";
    static final String  GPS = "GP";
    static final String  CHART_SYSTEM = "EC";

    // formatter strings supported and corresponding numbers of fields
    static final String  HEADING = "HDG";  // Heading, Deviation, Variation
    static final int     HEADING_NUMFIELDS = 5;
    static final String  SPEED = "VHW";  // Water speed and Heading
    static final int     SPEED_NUMFIELDS = 8;
    static final String  WIND = "MWV";  // Wind speed and angle
    static final int     WIND_NUMFIELDS = 5;
    static final String  GNDWIND = "MWD";  // Wind speed and Direction
    static final int     GNDWIND_NUMFIELDS = 8;
    static final String  WATERTEMP = "MTW";  // Water temperature
    static final int     WATERTEMP_NUMFIELDS = 2;
    static final String  DEPTH = "DPT";  // Depth
    static final int     DEPTH_NUMFIELDS = 3;
    static final String  LOG = "VLW";  // Water distance
    static final int     LOG_NUMFIELDS = 8;
    static final String  POSITION = "GLL";  // Geographic position
    static final int     POSITION_NUMFIELDS = 7;
    static final String  POSSOGCOG = "RMC";  // Recommended min. GNSS data
    static final int     POSSOGCOG_NUMFIELDS = 12;
    static final String  NEXTWP = "BWC";  // Bearing and distance to W/P
    static final int     NEXTWP_NUMFIELDS = 13;
    static final String  GPSTIME = "ZDA";  // GPS Date and Time
    static final int     GPSTIME_NUMFIELDS = 6;
    static final String  SOGCOG = "VTG";  // Speed over ground and Course over ground
    static final int     SOGCOG_NUMFIELDS = 9;
    static final String  FIXDATA = "GNS";  // GPS Fix data
    static final int     FIXDATA_NUMFIELDS = 13;
    static final String  XTE = "XTE";  // Cross track error
    static final int     XTE_NUMFIELDS = 6;

    static final String[] msgFmt = {
            HEADING,
            SPEED,
            WIND,
            GNDWIND,
            WATERTEMP,
            DEPTH,
            LOG,
            POSITION,
            POSSOGCOG,
            NEXTWP,
            GPSTIME,
            SOGCOG,
            FIXDATA,
            XTE
    };
    static final int[] numFlds = {
            HEADING_NUMFIELDS,
            SPEED_NUMFIELDS,
            WIND_NUMFIELDS,
            GNDWIND_NUMFIELDS,
            WATERTEMP_NUMFIELDS,
            DEPTH_NUMFIELDS,
            LOG_NUMFIELDS,
            POSITION_NUMFIELDS,
            POSSOGCOG_NUMFIELDS,
            NEXTWP_NUMFIELDS,
            GPSTIME_NUMFIELDS,
            SOGCOG_NUMFIELDS,
            FIXDATA_NUMFIELDS,
            XTE_NUMFIELDS
    };
}
