package com.mpdev.android.boat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.mpdev.android.boat.BoatWind.WindData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class Polar Data
 * holds the data from the boat's polars
 * and calculates the maximum achievable speed for specific wind speed/angle
 *
 */
public class BoatPolar {

    public static boolean polarLoaded = false;

    // table dimensions
    static int maxX;
    static int maxY;

    // polar data
    static double[] windAnglePolar;
    static double[] windSpeedPolar;
    static double[][] polarSpeed;

    /**
     * Constructor
     */
    BoatPolar() { }

    /**
     * calculates ideal boat speed from polar curve data
     * @param trueWind       true wind object
     * @return               ideal boat speed for this wind
     */
    static double getPolarSpeed(WindData trueWind) {

        int x, y;
        double s, s1_1, s1_2, s2_1, s2_2, s1, s2;

        if (trueWind.angle <= 0.0001 || trueWind.speed == 0.0001)
            return 0.0;

        for (x=0; x < maxX && trueWind.angle > windAnglePolar[x]; ++x);
        for (y=0; y < maxY && trueWind.speed > windSpeedPolar[y]; ++y);

        s1_1 = polarSpeed[y-1][x-1];
        s1_2 = polarSpeed[y][x-1];

        s2_1 = polarSpeed[y-1][x];
        s2_2 = polarSpeed[y][x];

        s1 = s1_1 + (s1_2 - s1_1) * (trueWind.angle - windAnglePolar[x-1]) / (windAnglePolar[x] - windAnglePolar[x-1]);
        s2 = s2_1 + (s2_2 - s2_1) * (trueWind.angle - windAnglePolar[x-1]) / (windAnglePolar[x] - windAnglePolar[x-1]);

        s = s1 + (s2 - s1) * (trueWind.speed - windSpeedPolar[y-1]) / (windSpeedPolar[y] - windSpeedPolar[y-1]);

        return s;
    }

    /**
     * loads the polar curve from the input file
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String loadPolarTable(String fileName) {

        StringBuilder sb = new StringBuilder();

        sb.append("reading polar from: ").append(fileName).append("\n");

        // read polar file into String
        String sPolar;
        try {
            sPolar = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (Exception e) {
            sb.append("Could not read polar file: ").append(e.toString());
            return sb.toString();
        }

        // get the fields from the polar json
        try {
            JSONObject jPolar = new JSONObject(sPolar);

            // get the wind angle array
            JSONArray wAngle = jPolar.getJSONArray("windAngle");
            maxX = wAngle.length();

            // get the wind - boat speeds array
            JSONArray wbSpeed = jPolar.getJSONArray("speed");
            maxY = wbSpeed.length();

            // setup our arrays
            windAnglePolar = new double[maxX];
            windSpeedPolar = new double[maxY];
            polarSpeed = new double[maxY][maxX];

            // populate the arrays
            for (int i=0; i < maxX; ++i)
                windAnglePolar[i] = wAngle.getDouble(i);

            for (int j=0; j < maxY; ++j) {
                JSONObject jSpeed = wbSpeed.getJSONObject(j);
                windSpeedPolar[j] = jSpeed.getDouble("wind");
                JSONArray jbSpeed = jSpeed.getJSONArray("boat");
                for (int i=0; i < maxX; ++i)
                    polarSpeed[j][i] = jbSpeed.getDouble(i);
            }
        } catch (Exception e) {
            sb.append("ERROR in reading the json polar file: ").append(e.toString());
        }

        // set the flag for the polar curve to success
        polarLoaded = true;

        sb.append("completed reading polar");
        return sb.toString();
    }

    /** dumps the polar curve data to string */
    @SuppressLint("DefaultLocale")
    public static String polarToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("     ");
        for (int i=0; i < maxX; ++i)
            sb.append(String.format("%03.0f  ", windAnglePolar[i]));
        sb.append("\n");
        for (int j=0; j < maxY; ++j) {
            sb.append(String.format("%2.0f  ", windSpeedPolar[j]));
            for (int i=0; i < maxX; ++i)
                sb.append(String.format("%4.1f ", polarSpeed[j][i]));
            sb.append("\n");
        }
        return sb.toString();
    }
}
