package com.mpdev.android.boatinstruments;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.mpdev.android.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * AppConfig class
 * Reads the application config from Json file
 * The application may also write the config file with updated config from usr input
 */
public class AppConfig {

    public static final String TAG = "AppConfig";

    // 4 different display modes
    public enum InstView {
        SCROLL_VERTICAL,
        SCROLL_HORIZONTAL,
        TRANSITION,
        SWIPE_PAGE
    }

    //////////// application config variables /////////

    // config file name
    static String CONF_FILENAME = null;
    // default config file name
    final static String CONF_FILENAME_DEF = "boatInstrumentsConfig.json";

    // config version
    static final String KEY_CONFIG_VERSION = "configVersion";

    // Network
    static final String KEY_NETWORK = "network";
    static final String KEY_NMEA_GW_IP = "nmeaGwIp";
    static String NMEA_GW_IP = "192.168.1.111";
    static final String KEY_NMEA_GW_PORT = "nmeaGwPort";
    static int NMEA_GW_PORT = 8888;
    static final String KEY_SOCKET_TIMEOUT = "socketTimeout";
    static int NMEA_GW_TIMEOUT = 200;
    static final String KEY_NMEA_CHECKSUM = "validateChksum";
    static boolean NMEA_VALIDATE_CHKSUM = true;
    static final String KEY_DEMO_MODE = "demoMode";
    static boolean DEMO_MODE = true;
    static final String KEY_DEMO_FILE = "demoFile";
    static String DEMO_FILE = "boatInstrumentsDemo.txt";

    // Timers
    static final String KEY_TIMERS = "timers";
    static final String KEY_TIMEOUT_OUT_OF_DATE = "timeoutOutOfDate";
    public static int TIMEOUT_OUT_OF_DATE = 9000;

    // Wind
    static final String KEY_WIND = "wind";
    static final String KEY_CALC_TRUE_WIND = "calcTrueWind";
    public static boolean CALCULATE_TRUE_WIND = true;
    static final String KEY_CALC_GND_WIND = "calcGndWind";
    public static boolean CALCULATE_GND_WIND = true;
    static final String KEY_POLAR = "polar";
    public static String POLAR_FILE = "zephyrPolar.json";

    // Display
    static final String KEY_DISPLAY = "display";
    static final String KEY_DISPLAY_MODE = "displayMode";
    public static InstView DISPLAY_MODE = InstView.TRANSITION;
    static final String KEY_INSTR_COLOUR_DAY = "instrumentColourDay";
    public static String INSTR_COLOUR_DAY = "#000000";
    static final String KEY_INSTR_COLOUR_DAY_OUT = "instrumentColourDayOut";
    public static String INSTR_COLOUR_DAY_OUT = "#888888";
    static final String KEY_INSTR_COLOUR_MAX_DAY = "instrumentColourMaxDay";
    public static String INSTR_COLOUR_MAX_DAY = "#0000EE";
    static final String KEY_BGND_COLOUR_DAY = "backgroundColourDay";
    public static String BGND_COLOUR_DAY = "#EEEE00";
    static final String KEY_INSTR_COLOUR_NIGHT = "instrumentColourNight";
    public static String INSTR_COLOUR_NIGHT = "#FF4444";
    static final String KEY_INSTR_COLOUR_NIGHT_OUT = "instrumentColourNightOut";
    public static String INSTR_COLOUR_NIGHT_OUT = "#FF8888";
    static final String KEY_INSTR_COLOUR_MAX_NIGHT = "instrumentColourMaxNight";
    public static String INSTR_COLOUR_MAX_NIGHT = "#EEEE00";
    static final String KEY_BGND_COLOUR_NIGHT = "backgroundColourNight";
    public static String BGND_COLOUR_NIGHT = "#000000";
    static final String KEY_NIGHT_MODE = "nightMode";
    public static boolean NIGHT_MODE = false;
    static final String KEY_NIGHT_MODE_AUTO = "nightModeAuto";
    public static boolean NIGHT_MODE_AUTO = false;
    static final double NIGHT_THRESHOLD = 15.0;
    static final double DAY_THRESHOLD = 40.0;

    // maximum values
    static final String KEY_MAX_VALUES_FILENAME = "maxValuesFile";
    public static String MAX_VALUES_FILENAME = "boatMaxValues.json";
    // flag to trigger reset of max values
    public static boolean resetMaxValues = false;

    /** reads config from json file - default file used */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static String readConfig() {

        // check if the filename has been set by cmd line or env var
        // otherwise use default file name
        if (CONF_FILENAME == null)
            CONF_FILENAME = CONF_FILENAME_DEF;

        // here we build the config info that will be returned to the caller
        StringBuilder sb = new StringBuilder();
        sb.append("reading config from: ").append(CONF_FILENAME).append("\n");

        // read config file into String
        String fileName = MainActivity.appFilePath + CONF_FILENAME;
        String sConfig;
        try {
            sConfig = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (Exception e) {
            Log.e(TAG, "could not read config - using defaults; file: " + e.toString());
            sb.append("Could not read confing file: ").append(e.toString());
            return sb.toString();
        }

        // get the fields from the config json
        // JSON Exceptions are -ignored, i.e. if a setting is not found or is not properly formatted,
        // it is ignored and the default value is used
        try {
            JSONObject jConfig = new JSONObject(sConfig);

            ////// config version
            try {
                String configVersion = jConfig.getString(KEY_CONFIG_VERSION);
                sb.append(KEY_CONFIG_VERSION+": ").append(configVersion).append("\n");
            } catch (JSONException ignored) {}

            ////// network
            try {
                JSONObject jNet = jConfig.getJSONObject(KEY_NETWORK);

                // NMEA GW IP address
                try {
                    String configGwIp = jNet.getString(KEY_NMEA_GW_IP);
                    NMEA_GW_IP = configGwIp;
                    sb.append("NMEA_GW_IP: ").append(NMEA_GW_IP).append("\n");
                } catch (JSONException ignored) {}
                // NMEA GW port number
                try {
                    int configGwPort = jNet.getInt(KEY_NMEA_GW_PORT);
                    NMEA_GW_PORT = configGwPort;
                    sb.append("NMEA_GW_PORT: ").append(NMEA_GW_PORT).append("\n");
                } catch (JSONException ignored) {}
                // NMEA GW read timeout
                try {
                    int configReadTimeout = jNet.getInt(KEY_SOCKET_TIMEOUT);
                    NMEA_GW_TIMEOUT = configReadTimeout;
                    sb.append("NMEA_GW_TIMEOUT: ").append(NMEA_GW_TIMEOUT).append("\n");
                } catch (JSONException ignored) {}
                // validate nmea checksum
                try {
                    boolean configNmeaChecksum = jNet.getBoolean(KEY_NMEA_CHECKSUM);
                    NMEA_VALIDATE_CHKSUM = configNmeaChecksum;
                    sb.append("NMEA_VALIDATE_CHKSUM: ").append(NMEA_VALIDATE_CHKSUM).append("\n");
                } catch (JSONException ignored) {}
                // Demo mode
                try {
                    boolean demoMode = jNet.getBoolean(KEY_DEMO_MODE);
                    DEMO_MODE = demoMode;
                    sb.append("DEMO_MODE: ").append(DEMO_MODE).append("\n");
                } catch (JSONException ignored) {}
                // Demo file name
                try {
                    String demoFile = jNet.getString(KEY_DEMO_FILE);
                    DEMO_FILE = demoFile;
                    sb.append("DEMO_FILE: ").append(DEMO_FILE).append("\n");
                } catch (JSONException ignored) {}
            } catch (JSONException ignored) {}

            ////// display
            try {
                JSONObject jDisplay = jConfig.getJSONObject(KEY_DISPLAY);

                // display mode
                try {
                    String configDispMode = jDisplay.getString(KEY_DISPLAY_MODE);
                    DISPLAY_MODE = InstView.valueOf(configDispMode);
                    sb.append("DISPLAY_MODE: ").append(DISPLAY_MODE).append("\n");
                } catch (JSONException ignored) {}
                // instrument digits colour day
                try {
                    String configInstrColDay = jDisplay.getString(KEY_INSTR_COLOUR_DAY);
                    INSTR_COLOUR_DAY = configInstrColDay;
                    sb.append("INSTR_COLOUR_DAY: ").append(INSTR_COLOUR_DAY).append("\n");
                } catch (JSONException ignored) {}
                // instrument digits colour day - out of date
                try {
                    String configInstrColDayOut = jDisplay.getString(KEY_INSTR_COLOUR_DAY_OUT);
                    INSTR_COLOUR_DAY_OUT = configInstrColDayOut;
                    sb.append("INSTR_COLOUR_DAY_OUT: ").append(INSTR_COLOUR_DAY_OUT).append("\n");
                } catch (JSONException ignored) {}
                // instrument digits colour day - max values
                try {
                    String configInstrColMaxDay = jDisplay.getString(KEY_INSTR_COLOUR_MAX_DAY);
                    INSTR_COLOUR_MAX_DAY = configInstrColMaxDay;
                    sb.append("INSTR_COLOUR_MAX_DAY: ").append(INSTR_COLOUR_MAX_DAY).append("\n");
                } catch (JSONException ignored) {}
                // background display colour day
                try {
                    String configBgndColDay = jDisplay.getString(KEY_BGND_COLOUR_DAY);
                    BGND_COLOUR_DAY = configBgndColDay;
                    sb.append("BGND_COLOUR_DAY: ").append(BGND_COLOUR_DAY).append("\n");
                } catch (JSONException ignored) {}
                // instrument digits colour night
                try {
                    String configInstrColNight = jDisplay.getString(KEY_INSTR_COLOUR_NIGHT);
                    INSTR_COLOUR_NIGHT = configInstrColNight;
                    sb.append("INSTR_COLOUR_NIGHT: ").append(INSTR_COLOUR_NIGHT).append("\n");
                } catch (JSONException ignored) {}
                // instrument digits colour night - out of date
                try {
                    String configInstrColNightOut = jDisplay.getString(KEY_INSTR_COLOUR_NIGHT_OUT);
                    INSTR_COLOUR_NIGHT_OUT = configInstrColNightOut;
                    sb.append("INSTR_COLOUR_NIGHT_OUT: ").append(INSTR_COLOUR_NIGHT_OUT).append("\n");
                } catch (JSONException ignored) {}
                // instrument digits colour night - max values
                try {
                    String configInstrColMaxNight = jDisplay.getString(KEY_INSTR_COLOUR_MAX_NIGHT);
                    INSTR_COLOUR_MAX_NIGHT = configInstrColMaxNight;
                    sb.append("INSTR_COLOUR_MAX_NIGHT: ").append(INSTR_COLOUR_MAX_NIGHT).append("\n");
                } catch (JSONException ignored) {}
                // background display colour night
                try {
                    String configBgndColNight = jDisplay.getString(KEY_BGND_COLOUR_NIGHT);
                    BGND_COLOUR_NIGHT = configBgndColNight;
                    sb.append("BGND_COLOUR_NIGHT: ").append(BGND_COLOUR_NIGHT).append("\n");
                } catch (JSONException ignored) {}
                // night mode
                try {
                    boolean nightMode = jDisplay.getBoolean(KEY_NIGHT_MODE);
                    NIGHT_MODE = nightMode;
                    sb.append("NIGHT_MODE: ").append(NIGHT_MODE).append("\n");
                } catch (JSONException ignored) {}
                // auto night mode
                try {
                    boolean autoNightMode = jDisplay.getBoolean(KEY_NIGHT_MODE_AUTO);
                    NIGHT_MODE_AUTO = autoNightMode;
                    sb.append("NIGHT_MODE_AUTO: ").append(NIGHT_MODE_AUTO).append("\n");
                } catch (JSONException ignored) {}
                // max values file name
                try {
                    String maxValuesFile = jDisplay.getString(KEY_MAX_VALUES_FILENAME);
                    MAX_VALUES_FILENAME = maxValuesFile;
                    sb.append("MAX_VALUES_FILENAME: ").append(MAX_VALUES_FILENAME).append("\n");
                } catch (JSONException ignored) {}
            } catch (JSONException ignored) {}

            ////// timers
            try {
                JSONObject jTimers = jConfig.getJSONObject(KEY_TIMERS);

                // out of date timeout
                try {
                    int configOutOfDate = jTimers.getInt(KEY_TIMEOUT_OUT_OF_DATE);
                    TIMEOUT_OUT_OF_DATE = configOutOfDate;
                    sb.append("TIMEOUT_OUT_OF_DATE: ").append(TIMEOUT_OUT_OF_DATE).append("\n");
                } catch (JSONException ignored) {}
            } catch (JSONException ignored) {}

            ////// wind
            try {
                JSONObject jWind = jConfig.getJSONObject(KEY_WIND);

                // calculate true wind flag
                try {
                    boolean configCalcTrueWind = jWind.getBoolean(KEY_CALC_TRUE_WIND);
                    CALCULATE_TRUE_WIND = configCalcTrueWind;
                    sb.append("CALCULATE_TRUE_WIND: ").append(CALCULATE_TRUE_WIND).append("\n");
                } catch (JSONException ignored) {}
                // calculate ground wind flag
                try {
                    boolean configCalcGndWind = jWind.getBoolean(KEY_CALC_GND_WIND);
                    CALCULATE_GND_WIND = configCalcGndWind;
                    sb.append("CALCULATE_GND_WIND: ").append(CALCULATE_GND_WIND).append("\n");
                } catch (JSONException ignored) {}
                // polar file
                try {
                    String configPolarFile = jWind.getString(KEY_POLAR);
                    POLAR_FILE = configPolarFile;
                    sb.append("POLAR_FILE: ").append(POLAR_FILE).append("\n");
                } catch (JSONException ignored) {}
            } catch (JSONException ignored) {}

            sb.append("completed reading config");

        } catch (Exception e) {
            Log.e(TAG, "ERROR in reading the json config file (will use defaults): " + e.toString());
            sb.append("ERROR in reading the json config file (will use defaults): ").append(e.toString());
        }

        // return config info
        return sb.toString();
    }

    /** writes config to json file */
    static void writeConfig() {
        // check if the filename has been set by cmd line or env var
        // otherwise use default file name
        if (CONF_FILENAME == null)
            CONF_FILENAME = CONF_FILENAME_DEF;

        // write config file
        String fileName = MainActivity.appFilePath + CONF_FILENAME;
        try {
            PrintWriter conf = new PrintWriter(fileName);

            JSONObject jNetwork = new JSONObject()
                    .put(KEY_NMEA_GW_IP, NMEA_GW_IP)
                    .put(KEY_NMEA_GW_PORT, NMEA_GW_PORT)
                    .put(KEY_SOCKET_TIMEOUT, NMEA_GW_TIMEOUT)
                    .put(KEY_NMEA_CHECKSUM, NMEA_VALIDATE_CHKSUM)
                    .put(KEY_DEMO_MODE, DEMO_MODE)
                    .put(KEY_DEMO_FILE, DEMO_FILE);

            JSONObject jDisplay = new JSONObject()
                    .put(KEY_DISPLAY_MODE, DISPLAY_MODE.toString())
                    .put(KEY_INSTR_COLOUR_DAY, INSTR_COLOUR_DAY)
                    .put(KEY_INSTR_COLOUR_DAY_OUT, INSTR_COLOUR_DAY_OUT)
                    .put(KEY_INSTR_COLOUR_MAX_DAY, INSTR_COLOUR_MAX_DAY)
                    .put(KEY_BGND_COLOUR_DAY, BGND_COLOUR_DAY)
                    .put(KEY_INSTR_COLOUR_NIGHT, INSTR_COLOUR_NIGHT)
                    .put(KEY_INSTR_COLOUR_NIGHT_OUT, INSTR_COLOUR_NIGHT_OUT)
                    .put(KEY_INSTR_COLOUR_MAX_NIGHT, INSTR_COLOUR_MAX_NIGHT)
                    .put(KEY_BGND_COLOUR_NIGHT, BGND_COLOUR_NIGHT)
                    .put(KEY_NIGHT_MODE, NIGHT_MODE)
                    .put(KEY_NIGHT_MODE_AUTO, NIGHT_MODE_AUTO)
                    .put(KEY_MAX_VALUES_FILENAME, MAX_VALUES_FILENAME);

            JSONObject jTimers = new JSONObject()
                    .put(KEY_TIMEOUT_OUT_OF_DATE, TIMEOUT_OUT_OF_DATE);

            JSONObject jWind = new JSONObject()
                    .put(KEY_CALC_TRUE_WIND, CALCULATE_TRUE_WIND)
                    .put(KEY_CALC_GND_WIND, CALCULATE_GND_WIND)
                    .put(KEY_POLAR, POLAR_FILE);

            JSONObject jConf = new JSONObject()
                    .put(KEY_CONFIG_VERSION, (new Date()).toString())
                    .put(KEY_NETWORK, jNetwork)
                    .put(KEY_DISPLAY, jDisplay)
                    .put(KEY_TIMERS, jTimers)
                    .put(KEY_WIND, jWind);

            String configData = jConf.toString(2);
            conf.println(configData);
            conf.close();
            Log.i(TAG, "application configuration saved to file: "+fileName);
            Log.i(TAG, configData);
        } catch (Exception e) {
            Log.e(TAG, "could not write config; file: " + e.toString());
        }
    }
}
