package com.mpdev.android.boat;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;

public class BoatPosition {

    /**
     * position data class
     */
    public static class PositionData {
        int latDeg;
        double latMin;
        String latNs;
        int longDeg;
        double longMin;
        String longEw;

        /**
         * Default Constructor
         */
        PositionData() {
            latDeg = -1;
            latMin = 0.0;
            latNs = "-";
            longDeg = -1;
            longMin = 0.0;
            longEw = "-";
        }

        /**
         * set lat or long
         */
        void set(int degrees, double minutes, String nSeW) {
            if (nSeW.equals("N") || nSeW.equals("S")) {
                latNs = nSeW;
                latDeg = degrees;
                latMin = minutes;
            }
            else
            if (nSeW.equals("E") || nSeW.equals("W")) {
                longEw = nSeW;
                longDeg = degrees;
                longMin = minutes;
            }
        }

        /**
         * convert position data to string
         */
        @Override
        public String toString() {
            return toString("lat") + " " + toString("long");
        }

        /**
         * convert lattitude or longitude to string
         * @param latLong       if "lat" then return the latitude as string otherwise the longitude
         * @return              the latitude or longitude as string in ⁰ '
         */
        public String toString(String latLong) {
            if (latLong.equals("lat") && latDeg < 0)
                return "--" + "⁰" + "--.---" + "'";
            if (latLong.equals("long") && longDeg < 0)
                return "---" + "⁰" + "--.---" + "'";

            String s = "";
            DecimalFormat dfDegLat = new DecimalFormat("00");
            DecimalFormat dfDegLong = new DecimalFormat("000");
            DecimalFormat dfMin = new DecimalFormat("00.000");
            if (latLong.equals("lat")) {
                s = dfDegLat.format(latDeg)+"⁰"+dfMin.format(latMin)+"'";
                s = s + latNs;
            }
            else
            if (latLong.equals("long")) {
                s = dfDegLong.format(longDeg)+"⁰"+dfMin.format(longMin)+"'";
                s = s + longEw;
            }
            return s;
        }
    } // Class PositionData

    /**
     * Class WayPoint
     */
    public static class WayPoint {
        public String name;
        double bearing;
        double distance;
        public PositionData wpCoord;

        /**
         * Default Constructor
         */
        WayPoint() {
            name = "";
            bearing = -1.0;
            distance = -1.0;
            wpCoord = new PositionData();
        }
        /**
         * set the values
         */
        void set(String name, double bearing, double distance, PositionData wpCoord) {
            this.name = name;
            this.bearing = bearing;
            this.distance = distance;
            this.wpCoord = wpCoord;
        }
        /** waypoint distance formatted to string */
        @SuppressLint("DefaultLocale")
        public String distString() {
            if (distance < 0.0)
                return "-.-";

            if (distance > 10.0)
                return String.format("%.1f", distance);
            else
                return String.format("%.2f", distance);
        }

        /** bearing formatted string */
        @SuppressLint("DefaultLocale")
        public String bearingString() {
            if (bearing < 0.0)
                return "---" + "⁰";
            else
                return String.format("%03d", (int)bearing) + "⁰";
        }

        /**
         * convert waypoint to string
         */
        @Override
        public String toString() {
            String s = name;
            DecimalFormat dfBrng = new DecimalFormat("000");
            DecimalFormat dfDist = new DecimalFormat("#.00");
            s = s + " " + wpCoord.toString();
            s = s + " " + dfBrng.format(bearing) + "⁰";
            s = s + " " + dfDist.format(distance) + "nM";
            return s;
        }
    } // Class WayPoint

}
