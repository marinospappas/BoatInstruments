package com.mpdev.android.boat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.mpdev.android.boatinstruments.AppConfig;
import com.mpdev.android.boatinstruments.MainActivity;
import com.mpdev.android.logger.Log;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class BoatDataMax
 * Holds maximum values for these instruments:
 * - app wind speed
 * - true wind speed
 * - ground wind
 * - boat speed
 * - sog
 * - polar efficiency
 * - water temp
 * Maximum values are retained until reset
 */
public class BoatDataMax {

    public static final String TAG = "BoatDataMax";

    ////////// Boat data maximum fields and timestamp for each field
    double boatSpeedMax; public Date boatSpeedMax_t;
    public double trueWindMax; public Date trueWindMax_t;
    public double appWindMax; public Date appWindMax_t;
    public String gndWindMax; public Date gndWindMax_t;
    double polarEffMax; public Date polarEffMax_t;
    double waterTempMax; public Date waterTempMax_t;
    double sogMax; public Date sogMax_t;

    /**
     * Default constructor
     * initialise all variables
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public BoatDataMax() {
        // init data
        boatSpeedMax = -1.0;
        trueWindMax = -1.0;
        appWindMax = -1.0;
        gndWindMax = "F-";
        waterTempMax = -1.0;
        polarEffMax = -1.0;
        sogMax = -1.0;
        // init timestamps
        boatSpeedMax_t = trueWindMax_t = appWindMax_t = gndWindMax_t = waterTempMax_t =
                polarEffMax_t = sogMax_t = new Date(0L);
    }

    /**
     * Updates new maximum values from boat data if a new max is observed
     * also updates timestamp of new maximum
     * @param boatData      the current boat data
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateMaxBoatData(BoatData boatData) {
        // first check if a request to reset max values has been raised
        if (AppConfig.resetMaxValues) {
            resetMax();
            AppConfig.resetMaxValues = false;
        }
        boolean hasNewMax = false;
        // check all values for a new maximum and update accordingly
        if (boatData.boatSpeed > boatSpeedMax + 0.001) {
            boatSpeedMax = boatData.boatSpeed;
            boatSpeedMax_t = boatData.boatSpeed_t;
            hasNewMax = true;
        }
        if (boatData.appWind.speed > appWindMax + 0.001) {
            appWindMax = boatData.appWind.speed;
            appWindMax_t = boatData.appWind_t;
            hasNewMax = true;
        }
        if (boatData.trueWind.speed > trueWindMax + 0.001) {
            trueWindMax = boatData.trueWind.speed;
            trueWindMax_t = boatData.trueWind_t;
            // if we have a new true wind max then calculate gnd wind max
            gndWindMax = BoatWind.getBeaufort(trueWindMax);
            gndWindMax_t = trueWindMax_t;
            hasNewMax = true;
        }
        if (boatData.waterTemp > waterTempMax + 0.001) {
            waterTempMax = boatData.waterTemp;
            waterTempMax_t = boatData.waterTemp_t;
            hasNewMax = true;
        }
        if (boatData.polarEff > polarEffMax + 0.001) {
            polarEffMax = boatData.polarEff;
            polarEffMax_t = boatData.polarEff_t;
            hasNewMax = true;
        }
        if (boatData.sog > sogMax + 0.001) {
            sogMax = boatData.sog;
            sogMax_t = boatData.sog_t;
            hasNewMax = true;
        }
        // if a new max has been observed then save to file
        if (hasNewMax)
            saveMaxBoatData();
    }

    // json file keys for the max values and timestamps
    static final String KEY_MAX_BOAT_SPEED = "maxBoatSpeed";
    static final String KEY_MAX_BOAT_SPEED_T = "maxBoatSpeedTime";
    static final String KEY_MAX_APP_WIND = "maxAppWind";
    static final String KEY_MAX_APP_WIND_T = "maxAppWindTime";
    static final String KEY_MAX_TRUE_WIND = "maxTrueWind";
    static final String KEY_MAX_TRUE_WIND_T = "maxTrueWindTime";
    static final String KEY_MAX_GND_WIND = "maxGndWind";
    static final String KEY_MAX_GND_WIND_T = "maxGndWindTime";
    static final String KEY_MAX_WATER_TEMP = "maxWaterTemp";
    static final String KEY_MAX_WATER_TEMP_T = "maxWaterTempTime";
    static final String KEY_MAX_POLAR_EFF = "maxPolarEff";
    static final String KEY_MAX_POLAR_EFF_T = "maxPolarEffTime";
    static final String KEY_MAX_SOG = "maxSog";
    static final String KEY_MAX_SOG_T = "maxSogTime";

    /**
     * loads the maximum values from file (from previous run)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void loadMaxBoatData() {
        String fileName = MainActivity.appFilePath + AppConfig.MAX_VALUES_FILENAME;
        StringBuilder sb = new StringBuilder();
        sb.append("reading max values from: ").append(AppConfig.MAX_VALUES_FILENAME).append("\n");
        String sMax;
        try {
            sMax = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject jMax = new JSONObject(sMax);

            boatSpeedMax = jMax.getDouble(KEY_MAX_BOAT_SPEED);
            sb.append("max boat speed: ").append(boatSpeedMax).append(" ");
            boatSpeedMax_t = new Date (jMax.getLong(KEY_MAX_BOAT_SPEED_T));
            sb.append(boatSpeedMax_t.toString()).append("\n");

            appWindMax = jMax.getDouble(KEY_MAX_APP_WIND);
            sb.append("max app wind speed: ").append(appWindMax).append(" ");
            appWindMax_t = new Date (jMax.getLong(KEY_MAX_APP_WIND_T));
            sb.append(appWindMax_t.toString()).append("\n");

            trueWindMax = jMax.getDouble(KEY_MAX_TRUE_WIND);
            sb.append("max true wind speed: ").append(trueWindMax).append(" ");
            trueWindMax_t = new Date (jMax.getLong(KEY_MAX_TRUE_WIND_T));
            sb.append(trueWindMax_t.toString()).append("\n");

            gndWindMax = jMax.getString(KEY_MAX_GND_WIND);
            sb.append("max gnd wind force: ").append(gndWindMax).append(" ");
            gndWindMax_t = new Date (jMax.getLong(KEY_MAX_GND_WIND_T));
            sb.append(gndWindMax_t.toString()).append("\n");

            waterTempMax = jMax.getDouble(KEY_MAX_WATER_TEMP);
            sb.append("max water temperature: ").append(waterTempMax).append(" ");
            waterTempMax_t = new Date (jMax.getLong(KEY_MAX_WATER_TEMP_T));
            sb.append(waterTempMax_t.toString()).append("\n");

            polarEffMax = jMax.getDouble(KEY_MAX_POLAR_EFF);
            sb.append("max polar efficiency: ").append(polarEffMax).append(" ");
            polarEffMax_t = new Date (jMax.getLong(KEY_MAX_POLAR_EFF_T));
            sb.append(polarEffMax_t.toString()).append("\n");

            sogMax = jMax.getDouble(KEY_MAX_SOG);
            sb.append("max sog: ").append(sogMax).append(" ");
            sogMax_t = new Date (jMax.getLong(KEY_MAX_SOG_T));
            sb.append(sogMax_t.toString()).append("\n");

            Log.i(TAG, sb.toString());

        } catch (Exception e) {
            Log.i(TAG, "could not load maximum data values; file: "+fileName);
        }
    }

    /**
     * saves the maximum values together with the timestamp in a file
     * so that they can be retrieved after a restart
     */
    public void saveMaxBoatData() {

        String fileName = MainActivity.appFilePath + AppConfig.MAX_VALUES_FILENAME;
        try {
            PrintWriter conf = new PrintWriter(fileName);

            JSONObject jMax = new JSONObject()
                    .put(KEY_MAX_BOAT_SPEED, boatSpeedMax)
                    .put(KEY_MAX_BOAT_SPEED_T, boatSpeedMax_t.getTime())
                    .put(KEY_MAX_APP_WIND, appWindMax)
                    .put(KEY_MAX_APP_WIND_T, appWindMax_t.getTime())
                    .put(KEY_MAX_TRUE_WIND, trueWindMax)
                    .put(KEY_MAX_TRUE_WIND_T, trueWindMax_t.getTime())
                    .put(KEY_MAX_GND_WIND, gndWindMax)
                    .put(KEY_MAX_GND_WIND_T, gndWindMax_t.getTime())
                    .put(KEY_MAX_WATER_TEMP, waterTempMax)
                    .put(KEY_MAX_WATER_TEMP_T, waterTempMax_t.getTime())
                    .put(KEY_MAX_POLAR_EFF, polarEffMax)
                    .put(KEY_MAX_POLAR_EFF_T, polarEffMax_t.getTime())
                    .put(KEY_MAX_SOG, sogMax)
                    .put(KEY_MAX_SOG_T, sogMax_t.getTime());

            String maxData = jMax.toString(2);
            conf.println(maxData);
            conf.close();
            Log.i(TAG, "maximum data values saved to file: "+fileName);
            Log.i(TAG, maxData);
        } catch (Exception e) {
            Log.e(TAG, "could not write max data; file: " + e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void resetMax() {
        Log.d(TAG, "entering resetMax");
        // remove max values file
        String fileName = MainActivity.appFilePath + AppConfig.MAX_VALUES_FILENAME;
        try {
            Files.delete(Paths.get(fileName));
        } catch (Exception e) {
            Log.e(TAG, "could not delete max data; file: " + e.toString());
        }
        // init data in memory
        boatSpeedMax = -1.0;
        trueWindMax = -1.0;
        appWindMax = -1.0;
        gndWindMax = "F-";
        waterTempMax = -1.0;
        polarEffMax = -1.0;
        sogMax = -1.0;
        // init timestamps
        boatSpeedMax_t = trueWindMax_t = appWindMax_t = gndWindMax_t = waterTempMax_t =
                polarEffMax_t = sogMax_t = new Date(0L);
    }

    /////////////////////////////////////////////////////////////////////////
    // return max data values as formatted strings to be displayed on screen
    /////////////////////////////////////////////////////////////////////////

    /** max boat speed formatted string */
    @SuppressLint("DefaultLocale")
    public String boatSpeedMaxString() {
        if (boatSpeedMax < 0.0)
            return "-.-";
        else
            return String.format("%.1f", boatSpeedMax);
    }

    /** max app wind speed formatted string */
    @SuppressLint("DefaultLocale")
    public String appWindSpeedMaxString() {
        if (appWindMax < 0.0)
            return "-.-";
        else
            return String.format("%.1f", appWindMax);
    }

    /** max ground wind formatted string */
    @SuppressLint("DefaultLocale")
    public String gndWindForceMaxString() {
        return gndWindMax;
    }

    /** max water temperature formatted string */
    @SuppressLint("DefaultLocale")
    public String waterTempMaxString() {
        if (waterTempMax < 0.0)
            return "-.-";
        else
            return String.format("%.1f", waterTempMax);
    }

    /** max true wind speed formatted string */
    @SuppressLint("DefaultLocale")
    public String trueWindSpeedMaxString() {
        if (trueWindMax < 0.0)
            return "-.-";
        else
            return String.format("%.1f", trueWindMax);
    }

    /** max polar efficiency formatted string */
    @SuppressLint("DefaultLocale")
    public String polarEffMaxString() {
        if (polarEffMax < 0.0)
            return "-.-";
        else
            return String.format("%.1f", polarEffMax);
    }

    /** max sog formatted string */
    @SuppressLint("DefaultLocale")
    public String sogMaxString() {
        if (sogMax < 0.0)
            return "-.-";
        else
            return String.format("%.1f", sogMax);
    }

    /** max value timestamp formatted string */
    public String maxValueTimeString(Date timeStamp) {
        final String TIME_FORMAT = "yyyyMMdd\nHH:mm:ss ";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dF = new SimpleDateFormat(TIME_FORMAT);
        return dF.format(timeStamp);
    }
}
