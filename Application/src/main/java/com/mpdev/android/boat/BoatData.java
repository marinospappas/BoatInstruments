package com.mpdev.android.boat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.mpdev.android.boat.BoatWind.WindData;
import com.mpdev.android.boat.BoatPosition.PositionData;
import com.mpdev.android.boat.BoatPosition.WayPoint;
import com.mpdev.android.boatinstruments.AppConfig;

import static com.mpdev.android.boat.NmeaFormatterString.*;
import static com.mpdev.android.boat.NmeaMessage.MsgStatus.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class BoatData
 * Holds all the boat data that is updated via the NMEA messages received
 * This data will be displayed on the screen as part of the main application functionality
 *
 * @author marinos pappas 15 June 2020
 */
public class BoatData {

    ////////// Boat data fields and timestamp for each field
    double boatSpeed; public Date boatSpeed_t;
    double heading; public Date heading_t;
    double depth; public Date depth_t;
    public WindData trueWind; public Date trueWind_t;
    public WindData appWind; public Date appWind_t;
    public WindData gndWind; public Date gndWind_t;
    double polarSpeed; public Date polarSpeed_t;
    double polarEff; public Date polarEff_t;
    double waterTemp; public Date waterTemp_t;
    double log; public Date log_t;
    double trip; public Date trip_t;
    public PositionData position; public Date position_t;
    double sog; public Date sog_t;
    double cog; public Date cog_t;
    public WayPoint nextWaypoint; public Date nextWaypoint_t;
    double xte; public Date xte_t;
    Date gpsTime; public Date gpsTime_t;
    // max values here
    public BoatDataMax maxValues;

    /**
     * Default constructor
     * initialise all variables
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public BoatData() {
        // init data
        boatSpeed = -1.0;
        heading = -1.0;
        depth = -1.0;
        trueWind = new WindData("B");
        appWind = new WindData("B");
        gndWind = new WindData("G");
        waterTemp = -1.0;
        polarSpeed = -1.0;
        polarEff = -1.0;
        log = -1.0;
        trip = -1.0;
        position = new PositionData();
        sog = -1.0;
        cog = -1.0;
        nextWaypoint = new WayPoint();
        gpsTime = new Date(0L);
        xte = -1.0;
        // init timestamps
        boatSpeed_t = heading_t = depth_t = trueWind_t = appWind_t = gndWind_t = waterTemp_t = polarSpeed_t = polarEff_t =
                log_t = trip_t = position_t = sog_t = cog_t = nextWaypoint_t = gpsTime_t = xte_t = new Date(0L);
        // init max values class as well
        maxValues = new BoatDataMax();
    }

    /**
     * Main method that updates boat data from an incoming NMEA message
     *
     * @param msg   the incoming NMEA msg
     * @return      true when a field has been updated
     */
    public boolean updateBoatData(NmeaMessage msg) {

        // check validity of the NMEA message
        if (msg.status != NMEA_MSG_OK)
            return false;

        // compass heading
        // $HCHDG,338.9, , ,0.7,W*3D
        if (msg.talker.equals(MAGNETIC_COMPASS)
        &&  msg.formatter.equals(HEADING))
            return updateHeading(msg.fields);
        else
        // boat speed
        // $IIVHW,,T,338.9,M,0,N,0,K*7A
        if (msg.talker.equals(INTEGRATED_INSTRUMENTATION)
        &&  msg.formatter.equals(SPEED))
            return updateBoatSpeed(msg.fields);
        else
        // apparent / true wind
        // $WIMWV,x.x,T,x.x,M,x.x,N,x.x,M*hh
        if (msg.talker.equals(WIND_INSTRUMENTS)
        &&  msg.formatter.equals(WIND))
            return updateWindSpeed(msg.fields);
        else
        // ground wind
        // $--MWD,x.x,T,x.x,M,x.x,N,x.x,M*hh
        if (// talker = any
            msg.formatter.equals(GNDWIND))
            return updateGndWind(msg.fields);
        else
        // water Temperature
        // $IIMTW,xx.x,C*hh
        if (msg.talker.equals(INTEGRATED_INSTRUMENTATION)
        &&  msg.formatter.equals(WATERTEMP))
            return updateWaterTemp(msg.fields);
        else
        // depth
        // $SDDPT,xx.x,x.x,x*hh
        if (msg.talker.equals(ECHO_SOUNDER)
        &&  msg.formatter.equals(DEPTH))
            return updateDepth(msg.fields);
        else
        // log - trip
        // $IIVLW,x.x,A,x.x,A,x.x,A,x.x,A*hh
        if (msg.talker.equals(INTEGRATED_INSTRUMENTATION)
        &&  msg.formatter.equals(LOG))
            return updateLog(msg.fields);
        else
        // position
        // $GPGLL,LLLL.LLL,A,lllll.lll,a,hhmmss,A,A*hh
        if (msg.talker.equals(GPS)
        &&  msg.formatter.equals(POSITION))
            return updatePosition(msg.fields);
        else
        // position - SOG - COG
        // $GPRMC,hhmmss,A,LLLL.LLL,A,lllll.lll,a,X.x,X,ddmmyy,X.x,A,A*hh
        if (msg.talker.equals(GPS)
        &&  msg.formatter.equals(POSSOGCOG))
            return updatePosnTimeSogCog(msg.fields);
        else
        // next waypoint
        // $--BWC,hhmmss.mm,LLLL.LLL,A,lllll.lll,a,X.x,T,X.x,M,X.x,N,sssss,A*hh
        if ((msg.talker.equals(CHART_SYSTEM) || msg.talker.equals(GPS))
        &&  msg.formatter.equals(NEXTWP))
            return updateNextWp(msg.fields);
        else
        // GPS date and time
        // $GPZDA,160853,03,04,2021,,*47
        if (msg.talker.equals(GPS)
        &&  msg.formatter.equals(GPSTIME))
            return updateGPSTime(msg.fields);
        else
        // Speed over ground and course over ground
        // $GPVTG,0.73,T,,M,0.16,N,0.29,K,A*35
        if (msg.talker.equals(GPS)
        &&  msg.formatter.equals(SOGCOG))
            return updateSogCog(msg.fields);
        else
        // GPS Fix data
        // $GPSGNS,134107,3609.3621,N,00521.4806,W,A,10,1,43.5,47.3,,,V*6B
        if (msg.talker.equals(GPS)
        &&  msg.formatter.equals(FIXDATA))
            return updatePositionFixData(msg.fields);
        else
        // XTE
        // $GPXTE,A,A,0,L,N,A*2D
        if (msg.talker.equals(GPS)
        &&  msg.formatter.equals(XTE))
            return updateXTE(msg.fields);

        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////// update relevant boat fields from NMEA message ///////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * updates boat heading in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    private boolean updateHeading(String[] fields) {
        // message fields
        //         0     1 2 3   4
        //	$HCHDG,338.9, , ,0.7,W
        double magnHeading;
        double variation;
        try {
            magnHeading = Double.parseDouble(fields[0]);
            variation = Double.parseDouble(fields[3]);
        } catch (Exception e) {
            return false;
        }
        String eastWest = fields[4];
        // variation West - compass Best (i.e. subtract from compass to get true heading)
        if (eastWest.equals("W"))
            heading = magnHeading - variation;
        // variation East - compass Least (i.e. add to compass to get true heading)
        else if (eastWest.equals("E"))
            heading = magnHeading + variation;
        else
            return false;

        heading_t = new Date();
        return true;
    }

    /**
     * updates boat speed in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    private boolean updateBoatSpeed(String[] fields) {
        // message fields
        //         0 1 2     3 4   5 6 7
        //	$IIVHW, ,T,338.9,M,6.8,N,0,K
        double speed;
        String knots;
        try {
            speed = Double.parseDouble(fields[4]);
        } catch (Exception e) {
            return false;
        }
        knots = fields[5];
        if (!knots.equals("N"))
            return false;
        boatSpeed = speed;

        // also update polar speed and efficiency
        if (BoatPolar.polarLoaded) {
            polarSpeed = BoatPolar.getPolarSpeed(trueWind);
            polarEff = (polarSpeed == 0.0) ? 0 : (boatSpeed * 100 / polarSpeed);
            polarSpeed_t = polarEff_t = new Date();
        }
        boatSpeed_t = new Date();
        return true;
    }

    /**
     * updates apparent and true speed in boat data
     * (also sets ground wind depending on app config option)
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    private boolean updateWindSpeed(String[] fields) {
        // message fields
        //         0      1 2    3 4
        //	$WIMWV,175.46,R,2.29,N,A
        // 	$WIMWV,175.46,T,2.29,N,A
        double speed;
        double angle;
        String valid;
        String knots;
        String trueRel;
        String portStarboard;
        try {
            speed = Double.parseDouble(fields[2]);
            angle = Double.parseDouble(fields[0]);
        } catch (Exception e) {
            return false;
        }
        trueRel = fields[1];
        knots = fields[3];
        valid = fields[4];
        if (angle > 360.0)
            return false;
        if (angle <= 180.0) {
            portStarboard = "S";
        }
        else {
            angle = 360 - angle;
            portStarboard = "P";
        }
        if (valid.equals("A") && knots.equals("N")) {
            if (trueRel.equals("R")) {
                // apparent wind
                appWind.set(speed, angle, portStarboard);
                appWind_t = new Date();
                // if necessary also calculate true wind
                if (AppConfig.CALCULATE_TRUE_WIND) {
                    BoatWind.calcTrueWind(boatSpeed, appWind, trueWind);
                    trueWind_t = new Date();
                    // also update polar speed and efficiency
                    if (BoatPolar.polarLoaded) {
                        polarSpeed = BoatPolar.getPolarSpeed(trueWind);
                        polarEff = (polarSpeed == 0.0) ? 0 : (boatSpeed * 100 / polarSpeed);
                        polarSpeed_t = polarEff_t = new Date();
                    }
                    if (AppConfig.CALCULATE_GND_WIND) {
                        // if necessary also calculate ground wind
                        BoatWind.calcGndWind(heading, trueWind, gndWind);
                        gndWind_t = new Date();
                    }
                }
            }
            else
            if (trueRel.equals("T")) {
                // true wind
                trueWind.set(speed, angle, portStarboard);
                trueWind_t = new Date();
                // also update polar speed and efficiency
                if (BoatPolar.polarLoaded) {
                    polarSpeed = BoatPolar.getPolarSpeed(trueWind);
                    polarEff = (polarSpeed == 0.0) ? 0 : (boatSpeed * 100 / polarSpeed);
                    polarSpeed_t = polarEff_t = new Date();
                }
                if (AppConfig.CALCULATE_GND_WIND) {
                    // if necessary also calculate ground wind
                    BoatWind.calcGndWind(heading, trueWind, gndWind);
                    gndWind_t = new Date();
                }
            }
            else
                return false;
        }
        else
            return false;

        return true;
    }

    /**
     * updates ground wind speed in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    private boolean updateGndWind (String[] fields) {
        // message fields
        //         0   1 2   3 4   5 6   7
        //	$--MWD,x.x,T,x.x,M,x.x,N,x.x,M
        double angle;
        double speed;
        try {
            angle = Double.parseDouble(fields[0]);
            speed = Double.parseDouble(fields[4]);
        }
        catch (Exception e) {
            return false;
        }

        String trueDir = fields[1];
        String knots = fields[5];

        if (trueDir.equals("T") && knots.equals("N") && angle < 360.0)
            gndWind.set(BoatWind.getBeaufort(speed), BoatWind.getGndWindDir(angle));
        else
            return false;

        gndWind_t = new Date();
        return true;
    }

    /**
     * updates water temperature in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updateWaterTemp(String[] fields) {
        // message fields
        //         0    1
        //  $IIMTW,xx.x,C
        double temp;
        try {
            temp = Double.parseDouble(fields[0]);
        }
        catch (Exception e) {
            return false;
        }
        String celcius = fields[1];
        if (!celcius.equals("C"))
            return false;
        waterTemp = temp;

        waterTemp_t = new Date();
        return true;
    }

    /**
     * updates depth in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updateDepth(String[] fields) {
        // message fields
        //         0    1   2
        //  $SDDPT,7.36,0.3, *4A
        double depthData, offset;
        try {
            depthData = Double.parseDouble(fields[0]);
            offset = Double.parseDouble(fields[1]);
        }
        catch (Exception e) {
            return false;
        }
        depth = depthData + offset;

        depth_t = new Date();
        return true;
    }

    /**
     * updates log in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updateLog(String[] fields) {
        // message fields
        //         0    1 2    3 4 5 6 7
        //  $IIVLW,2310,N,45.1,N, ,N, ,N
        double totalLog;
        double tripLog;
        try {
            totalLog = Double.parseDouble(fields[0]);
            tripLog = Double.parseDouble(fields[2]);
        }
        catch (Exception e) {
            return false;
        }
        String nmiles1 = fields[1];
        String nmiles2 = fields[3];
        if (nmiles1.equals("N") && nmiles2.equals("N")) {
            log = totalLog;
            trip = tripLog;
        }
        else
            return false;

        log_t = trip_t = new Date();
        return true;
    }

    /**
     * updates position in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updatePosition(String[] fields) {
        // message fields
        //         0         1 2          3 4      5 6
        //  $GPGLL,5047.7438,N,00107.0246,W,112402,A,A
        int latDeg;
        double latMin;
        int longDeg;
        double longMin;
        try {
            latDeg = Integer.parseInt(fields[0].substring(0,2));
            latMin = Double.parseDouble(fields[0].substring(2));
            longDeg = Integer.parseInt(fields[2].substring(0,3));
            longMin = Double.parseDouble(fields[2].substring(3));
        }
        catch (Exception e) {
            return false;
        }
        String nS = fields[1];
        String eW = fields[3];
        String valid = fields[6];
        if (!valid.equals("A"))
            return false;
        if (latDeg >= 90 || latMin >= 60.0
        ||  longDeg >= 180 || longMin >= 60.0)
            return false;
        if (!nS.equals("N") && !nS.equals("S"))
            return false;
        if (!eW.equals("W") && !eW.equals("E"))
            return false;
        position.set(latDeg, latMin, nS);
        position.set(longDeg, longMin, eW);

        position_t = new Date();
        return true;
    }

    /**
     * updates position, sog and cog in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updatePosnTimeSogCog(String[] fields) {
        // message fields
        //         0      1 2         3 4          5 6   7 8      9    10 11
        //  $GPRMC,112402,A,5047.7438,N,00107.0246,W,0.1,0,170218,0.73,W, A
        int latDeg;
        double latMin;
        int longDeg;
        double longMin;
        double speedOg;
        double courseOg;
        try {
            latDeg = Integer.parseInt(fields[2].substring(0, 2));
            latMin = Double.parseDouble(fields[2].substring(2));
            longDeg = Integer.parseInt(fields[4].substring(0,3));
            longMin = Double.parseDouble(fields[4].substring(3));
            speedOg = Double.parseDouble(fields[6]);
            courseOg = Double.parseDouble(fields[7]);
        }
        catch (Exception e) {
            return false;
        }
        String nS = fields[3];
        String eW = fields[5];
        String valid = fields[1];
        if (!valid.equals("A"))
            return false;
        if (latDeg >= 90 || latMin >= 60.0
        ||  longDeg >= 180 || longMin >= 60.0)
            return false;
        if (!nS.equals("N") && !nS.equals("S"))
            return false;
        if (!eW.equals("W") && !eW.equals("E"))
            return false;
        position.set(latDeg, latMin, nS);
        position.set(longDeg, longMin, eW);
        sog = speedOg;
        cog = courseOg;

        try {
            int hours = Integer.parseInt(fields[0].substring(0,2));
            int minutes = Integer.parseInt(fields[0].substring(2,4));
            int seconds = Integer.parseInt(fields[0].substring(4,6));
            Calendar cal = Calendar.getInstance();
            cal.setTime(gpsTime);
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.HOUR_OF_DAY, hours);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, seconds);
            cal.set(Calendar.MILLISECOND, 0);
            gpsTime = cal.getTime();
        } catch (Exception e) {
            return false;
        }

        position_t = sog_t = cog_t = new Date();

        return true;
    }

    /**
     * updates next waypoint name, coordinates, bearing and distance in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updateNextWp(String[] fields) {
        // message fields
        //         0         1        2 3         4 5     6 7     8 9    10 11          12
        //  $GPBWC,112356.00,5045.645,N,00107.027,W,180.0,T,181.0,M,2.10,N, GOTO CURSOR,A
        double nextWpBrng;
        double nextWpDist;

        // waypoint bearing, distance and name
        try {
            nextWpBrng = Double.parseDouble(fields[5]);
            nextWpDist = Double.parseDouble(fields[9]);
        }
        catch (Exception e) {
            return false;
        }
        String trueBrng = fields[6];
        String milesDist = fields[10];
        String nextWpName = fields[11];
        // waypoint coordinates
        int latDeg;
        double latMin;
        int longDeg;
        double longMin;
        try {
            latDeg = Integer.parseInt(fields[1].substring(0,2));
            latMin = Double.parseDouble(fields[1].substring(2));
            longDeg = Integer.parseInt(fields[3].substring(0,3));
            longMin = Double.parseDouble(fields[3].substring(3));
        }
        catch (Exception e) {
            return false;
        }
        String nS = fields[2];
        String eW = fields[4];
        if (latDeg >= 90 || latMin >= 60.0 || longDeg >= 180 || longMin >= 60.0)
            return false;
        if (!nS.equals("N") && !nS.equals("S"))
            return false;
        if (!eW.equals("W") && !eW.equals("E"))
            return false;
        PositionData wpCoord = new PositionData();
        wpCoord.set(latDeg, latMin, nS);
        wpCoord.set(longDeg, longMin, eW);

        String auton = fields[12];

        if (auton.equals("A") && trueBrng.equals("T") && milesDist.equals("N"))
            nextWaypoint.set(nextWpName, nextWpBrng, nextWpDist, wpCoord);
        else
            return false;

        nextWaypoint_t = new Date();
        return true;
    }

    /**
     * updates gps date/time in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updateGPSTime(String[] fields) {
        // message fields
        //         0      1  2  3    4 5
        //  $GPZDA,160853,03,04,2021, , *
        try {
            int hours = Integer.parseInt(fields[0].substring(0,2));
            int minutes = Integer.parseInt(fields[0].substring(2,4));
            int seconds = Integer.parseInt(fields[0].substring(4,6));
            int day = Integer.parseInt(fields[1]);
            int month = Integer.parseInt(fields[2]);
            int year = Integer.parseInt(fields[3]);
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.HOUR_OF_DAY, hours);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, seconds);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.MONTH, month-1);
            cal.set(Calendar.YEAR, year);
            gpsTime = cal.getTime();
            gpsTime_t = new Date();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * updates sog and cog in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updateSogCog(String[] fields) {
        // message fields
        //         0    1 2 3 4    5 6    7 8
        //  $GPVTG,0.73,T, ,M,0.16,N,0.29,K,A*35
        double speedOg;
        double courseOg;
        try {
            speedOg = Double.parseDouble(fields[4]);
            courseOg = Double.parseDouble(fields[0]);
        }
        catch (Exception e) {
            return false;
        }
        String valid = fields[8];
        if (!valid.equals("A"))
            return false;
        sog = speedOg;
        cog = courseOg;

        sog_t = cog_t = new Date();

        return true;
    }

    /**
     * updates position from fix data in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    boolean updatePositionFixData(String[] fields) {
        // message fields
        //         0      1         2 3          4 5 6  7 8    9    10 11 12
        // $GPSGNS,134107,3609.3621,N,00521.4806,W,A,10,1,43.5,47.3,  ,  ,V *6B
        int latDeg;
        double latMin;
        int longDeg;
        double longMin;
        try {
            latDeg = Integer.parseInt(fields[1].substring(0,2));
            latMin = Double.parseDouble(fields[1].substring(2));
            longDeg = Integer.parseInt(fields[3].substring(0,3));
            longMin = Double.parseDouble(fields[3].substring(3));
        }
        catch (Exception e) {
            return false;
        }
        String nS = fields[2];
        String eW = fields[4];
        String valid = fields[5];
        if (!valid.equals("A"))
            return false;
        if (latDeg >= 90 || latMin >= 60.0
                ||  longDeg >= 180 || longMin >= 60.0)
            return false;
        if (!nS.equals("N") && !nS.equals("S"))
            return false;
        if (!eW.equals("W") && !eW.equals("E"))
            return false;
        position.set(latDeg, latMin, nS);
        position.set(longDeg, longMin, eW);

        position_t = new Date();
        return true;
    }

    /**
     * updates xte in boat data
     * @param fields    the NMEA fields
     * @return          true if successful
     */
    private boolean updateXTE(String[] fields) {
        // message fields
        //         0 1 2   3 4 5
        //  $GPXTE,A,A,0.0,L,N,A*2D
        double xTrackError;
        String knots;
        try {
            xTrackError = Double.parseDouble(fields[2]);
        } catch (Exception e) {
            return false;
        }
        knots = fields[4];
        String valid = fields[5];
        if (!knots.equals("N") || !valid.equals("A"))
            return false;
        xte = xTrackError;
        xte_t = new Date();
        return true;
    }

    /////////////////////////////////////////////////////////////////////////
    // return boat data fields as formatted strings to be displayed on screen
    /////////////////////////////////////////////////////////////////////////

    /** boat speed formatted string */
    @SuppressLint("DefaultLocale")
    public String boatSpeedString() {
        if (boatSpeed < 0.0)
            return "-.-";
        else
            return String.format("%.1f", boatSpeed);
    }

    /** heading formatted string */
    @SuppressLint("DefaultLocale")
    public String headingString() {
        if (heading < 0.0)
            return "---" + "⁰";
        else
            return String.format("%3d", (int)heading)+"⁰";
    }

    /** log formatted string */
    @SuppressLint("DefaultLocale")
    public String logString() {
        if (log < 0.0)
            return "--.-";
        else
            return String.format("%.1f", log);
    }

    /** trip formatted string */
    @SuppressLint("DefaultLocale")
    public String tripString() {
        if (trip < 0.0)
            return "-.-";
        else
            return String.format("%.1f", trip);
    }

    /** depth formatted string */
    @SuppressLint("DefaultLocale")
    public String depthString() {
        if (depth < 0.0)
            return "-.-";
        else
            return String.format("%.1f", depth);
    }

    /** water temperature formatted string */
    @SuppressLint("DefaultLocale")
    public String waterTempString() {
        if (waterTemp < 0.0)
            return "--.-";
        else
            return String.format("%.1f", waterTemp);
    }

    /** polar speed formatted string */
    @SuppressLint("DefaultLocale")
    public String polarSpeedString() {
        if (polarSpeed < 0.0001)
            return "-.-";
        else
            return String.format("%.1f", polarSpeed);
    }

    /** polar efficiency formatted string */
    @SuppressLint("DefaultLocale")
    public String polarEffString() {
        if (polarEff < 0.0001)
            return "--%";
        else
            return String.format("%d%%", (int)polarEff);
    }

    /** sog formatted string */
    @SuppressLint("DefaultLocale")
    public String sogString() {
        if (sog < 0.0)
            return "-.-";
        else
            return String.format("%.1f", sog);
    }

    /** cog formatted string */
    @SuppressLint("DefaultLocale")
    public String cogString() {
        if (cog < 0.0)
            return "---" + "⁰";
        else
            return String.format("%3d", (int)cog)+"⁰";
    }

    /** gps date */
    public String gpsDate() {
        if (gpsTime.getTime() <= 60L * 60L * 24L * 1000L)
            return ("-- -- ----");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(gpsTime);
    }

    /** gps time */
    public String gpsTime() {
        if (gpsTime.getTime() == 0L)
            return ("--:--:-- GMT");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(gpsTime);
    }

    /** xte formatted string */
    @SuppressLint("DefaultLocale")
    public String xteString() {
        if (xte < 0.0)
            return "-.-";
        else
            return String.format("%.1f", xte);
    }

    /**
     * prints all the boat data for debugging purposes
     */
    void printBoatData() {

        DecimalFormat dfInt = new DecimalFormat("#0");
        DecimalFormat dfInt3 = new DecimalFormat("000");
        DecimalFormat df1dec = new DecimalFormat("#0.0");

        System.out.println("=================================================");
        System.out.println("Boat Speed, Heading: "+df1dec.format(boatSpeed)+"kn "+dfInt3.format(heading)+"⁰");
        System.out.println("Depth, Water Temp:   "+df1dec.format(depth)+"m "+df1dec.format(waterTemp)+"⁰C");
        System.out.println("Apparent Wind:       "+appWind.toString());
        System.out.println("True Wind:           "+trueWind.toString());
        System.out.println("Ground Wind:         "+gndWind.toString());
        System.out.println("Log, Trip:           "+df1dec.format(log)+"nM "+df1dec.format(trip)+"nM");
        System.out.println("Position, SOG, COG:  "+position.toString()+" "+df1dec.format(sog)+"kn "+dfInt3.format(cog)+"⁰");
        System.out.println("Next Waypoint:       "+nextWaypoint.toString());
        System.out.println("XTE:                 "+df1dec.format(xte)+"nM");
        System.out.println("GPS Date/Time:       "+gpsTime.toString());
    }

} /////////////// Class BoatData
