package com.mpdev.android.logger;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * wrapper class around android.util.Log
 * keeps a list of the log entries for displaying within the app
 * and calls the standard android.utilLog methods
 */
public class Log {

    /** class that holds the local copy of the log entry */
    static class LogEntry {
        Date timeStamp;
        int severity;
        String sevStr;
        String tag;
        String message;
        /** Constructor */
        LogEntry(int severity, Date timeStamp, String sevStr, String tag, String message) {
            this.timeStamp = timeStamp;
            this.severity = severity;
            this.sevStr = sevStr;
            this.tag = tag;
            this.message = message;
        }
    }

    /** the log snapshot for this application run */
    static List<LogEntry> appLog = new ArrayList<>();

    static final String TIME_FORMAT = "HH:mm:ss.SSS";

    /** keep a copy of the log entry in our list */
    private static void keepLog(int severity, Date timestamp, String sevStr, String tag, String message) {
        appLog.add(new LogEntry(severity, timestamp, sevStr, tag, message));
    }

    /** return the selected log entries as one string (new-line separated) */
    public static String log2String(String[] params) {
        // check parameters
        int severity = 0;
        String includeTag = ".*", excludeTag = "";
        Date fromTime = new Date(0L);
        boolean reverseOrder = false;
        if (params != null) {
            if (params.length > 0)
                try { severity = Integer.parseInt(params[0]); } catch (Exception ignored){}
            if (params.length > 1)
                includeTag = params[1];
            if (params.length > 2)
                excludeTag = params[2];
            if (params.length > 3) {
                String time = params[3];
                if (time.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}")) {
                    try {
                        int hours = Integer.parseInt(time.substring(0,2));
                        int minutes = Integer.parseInt(time.substring(3,5));
                        int seconds = Integer.parseInt(time.substring(6,8));
                        // Timestamp From
                        Calendar calFrom = Calendar.getInstance();
                        calFrom.setTime(new Date());
                        calFrom.set(Calendar.HOUR_OF_DAY, hours);
                        calFrom.set(Calendar.MINUTE, minutes);
                        calFrom.set(Calendar.SECOND, seconds);
                        calFrom.set(Calendar.MILLISECOND, 0);
                        fromTime = calFrom.getTime();
                    } catch (Exception ignored) {}
                }
            }
            if (params.length > 4)
                reverseOrder = "yes".equals(params[4]);
        }
        // select the entries that match the parameters to return
        // this message is also included in the log extract !!
        String criteria = "sev="+severity + " incl=["+includeTag + "] excl=["+excludeTag +
                "] time>="+fromTime.toString() + " reverse="+reverseOrder;
        Log.i("Log", "logreader parameters: " + criteria);
        StringBuilder sb = new StringBuilder();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dF = new SimpleDateFormat(TIME_FORMAT);
        int start = 0, end = appLog.size(), incr = 1;
        if (reverseOrder) {
            start = appLog.size()-1;
            end = -1;
            incr = -1;
        }
        // process all log entries from the list
        int linesCount = 0;
        for (int i = start; (start<end)&&(i<end) || (start>end)&&(i>end); i += incr) {
            LogEntry l = appLog.get(i);
            if (l.severity >= severity
            // &&  l.tag.matches(includeTag)
            // && !l.tag.matches(excludeTag)
            &&  l.message.matches(includeTag)
            && !l.message.matches(excludeTag)
            &&  l.timeStamp.getTime() >= fromTime.getTime()) {
                ++linesCount;
                String time = dF.format(l.timeStamp);
                l.message = l.message.replace("\n", "\n    ");
                sb.append(time).append(" ");
                sb.append(l.sevStr).append("/").append(l.tag).append(" ");
                sb.append(l.message).append("\n");
            }
        }
        return linesCount + " lines matching the criteria\n" + criteria + "\n" + sb.toString();
    }

    /** verbose */
    public static void v(String tag, String message) {
        keepLog(android.util.Log.VERBOSE, new Date(), "V", tag, message);
        android.util.Log.v(tag, message);
    }

    /** debug */
    public static void d(String tag, String message) {
        keepLog(android.util.Log.DEBUG, new Date(), "D", tag, message);
        android.util.Log.d(tag, message);
    }

    /** info */
    public static void i(String tag, String message) {
        keepLog(android.util.Log.INFO, new Date(), "I", tag, message);
        android.util.Log.i(tag, message);
    }

    /** warning */
    public static void w(String tag, String message) {
        keepLog(android.util.Log.WARN, new Date(), "W", tag, message);
        android.util.Log.w(tag, message);
    }

    /** error */
    public static void e(String tag, String message) {
        keepLog(android.util.Log.ERROR, new Date(), "E", tag, message);
        android.util.Log.e(tag, message);
    }

    /** assert */
    public static void a(String tag, String message) {
        keepLog(android.util.Log.ASSERT, new Date(), "A", tag, message);
        android.util.Log.println(android.util.Log.ASSERT, tag, message);
    }

}
