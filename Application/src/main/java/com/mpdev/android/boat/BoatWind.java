package com.mpdev.android.boat;

import android.annotation.SuppressLint;

import java.lang.Math;
import java.text.DecimalFormat;

public class BoatWind {

    static final double PI_CONST = 3.14159265359;

    /**
     * Wind data class
     */
    public static class WindData {
        double speed;    // used in apparent and true wind
        double angle;    // used in apparent and true wind
        String pSb;     // used in apparent and true wind
        String direction;   // used in ground wind
        String force;   // used in ground wind
        String flag;    // "B" = boat wind, "G" = Ground wind
        /**
         * Default Constructor
         */
        WindData(String flag) {
            speed = -1.0;
            angle = -1.0;
            pSb = "-";
            direction = "---";
            force = "F--";
            this.flag = flag;
        }
        /**
         * Constructor from speed, angle and portStarboard
         */
        WindData(double speed, double angle, String pSb) {
            this.speed = speed;
            this.angle = angle;
            this.pSb = pSb;
        }

        /**
         * set speed, angle and p/sb
         * used in true and apparent wind
         */
        void set(double speed, double angle, String pSb) {
            this.speed = speed;
            this.angle = angle;
            this.pSb = pSb;
        }

        /**
         * set force and direction
         * used in ground wind
         */
        void set(String force, String direction) {
            this.force = force;
            this.direction = direction;
        }

        /** wind speed formatted string */
        @SuppressLint("DefaultLocale")
        public String speedString() {
            if (speed < 0.0)
                return "-.-";
            else
                return String.format("%.1f", speed);
        }

        /** wind angle formatted string */
        @SuppressLint("DefaultLocale")
        public String angleString() {
            if (angle < 0.0)
                return "---" + "⁰";

            String s = String.format("%3d", (int)angle) + "⁰";
            if (pSb.equals("P"))
                return ">"+s;
            else
                return s+"<";
        }

        /**
         * convert winddata to string
         */
        @Override
        public String toString() {
            String s = "";
            DecimalFormat dfSpeed = new DecimalFormat("0.00");
            DecimalFormat dfAngle = new DecimalFormat("000.0");
            if (flag.equals("B")) {
                s = s + dfSpeed.format(speed) + "kn";
                s = s + " " + dfAngle.format(angle) + "⁰";
                s = s + " " + pSb;
            }
            else if (flag.equals("G")) {
                s = force + "  " + direction;
            }
            return s;
        }
    } // Class WindData

    /**
     * 	calculates true wind from apparent wind and boat speed
     * @param boatSpeed     the boat speed
     * @param appWind       the apparent wind object (speed, angle and p/sb)
     * @param trueWind      the return value of true wind speed, angle and pSb
     */
    static void calcTrueWind (double boatSpeed, WindData appWind, WindData trueWind) {

        double a, appX, appY, b, trueX, trueY;

        a = appWind.angle * 2*PI_CONST / 360.0;
        appX = appWind.speed * Math.sin(a);
        appY = appWind.speed * Math.cos(a);

        trueX = appX;
        trueY = appY - boatSpeed;

        if (trueY == 0.0) {
            trueWind.speed = trueX;
            trueWind.angle = 90;
        }
        else if (trueY > 0.0) {
            b = Math.atan (trueX / trueY);
            trueWind.speed = trueY / Math.cos(b);
            trueWind.angle = b * 360.0 / (2*PI_CONST);
        }
        else {
            b = Math.atan (trueX / (-trueY));
            trueWind.speed = (-trueY) / Math.cos(b);
            trueWind.angle = 180 - (b * 360.0 / (2*PI_CONST));
        }
        trueWind.pSb = appWind.pSb;
    }

    /**
     * 	calculates ground wind from true wind and boat heading
     */
    static void calcGndWind (double heading, WindData trueWind, WindData gndWind) {

        gndWind.speed = trueWind.speed;
        if (trueWind.pSb.equals("S")) {
            gndWind.angle = trueWind.angle + heading;
            if (gndWind.angle >= 360)
                gndWind.angle -= 360;
        }
        else {
            gndWind.angle = heading - trueWind.angle;
            if (gndWind.angle < 0)
                gndWind.angle += 360;
        }
        gndWind.set(getBeaufort(gndWind.speed), getGndWindDir(gndWind.angle));
    }

    /**
     * calculates beaufort force from a given wind speed
     * @param windSpeed     the wind speed
     * @return              the Beaufort force
     */
    static String getBeaufort (double windSpeed) {
        double[] beaufortScale = {
                1.0,   /* F0 */
                3.9,   /* F1 */
                6.9,   /* F2 */
                10.9,  /* F3 */
                16.9,  /* F4 */
                21.9,  /* F5 */
                27.9,  /* F6 */
                33.9,  /* F7 */
                40.9,  /* F8 */
                47.0,  /* F9 */
                55.0,  /* F10 */
                63.0   /* F11 */
             /* 63.0+     F12 */
        };
        int i;
        // scan the scale and return the F-number
        for (i = 0; i < 12; ++i) {
            if (windSpeed < beaufortScale[i])
                break;
        }
        return "F" + i;
    }

    /**
     * converts ground wind angle to ground wind direction (compass points)
     * @param gndWindAngle     the ground wind angle (must be >= 0 and < 360)
     * @return                 the ground wind direction string in compass points (N, SE, etc)
     */
    static String getGndWindDir (double gndWindAngle) {
        double step = 22.5;
                String[] compassPoint =
        { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N" };

        int indx = (int)((gndWindAngle + step/2) / step);
        return compassPoint[indx];
    }

}
