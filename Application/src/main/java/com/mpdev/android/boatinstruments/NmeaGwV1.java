package com.mpdev.android.boatinstruments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * NmeaGw class
 * Interfaces to the NMEA Gateway and passes on the incoming messages to the application
 * Reads from the specified IP address and port
 * or if DEMO_MODE is set from text file
 *
 * Version 1
 * Opens the socket at initialisation and keeps it open
 * Read calls execute readln on the open socket
 */
class NmeaGwV1 {

    private static final String TAG = "NmeaGw";

    // network commands results
    static final int RES_INIT_SUCCESS = 0;
    static final int RES_BAD_ARG = 1;
    static final int RES_INIT_FAIL = 2;
    static final int RES_READ_SUCCESS = 10;
    static final int RES_READ_FAIL = 11;
    static final int RES_READ_SOC_TIMEOUT = 12;

    // net read status
    static final int READ_NO_STATUS = 0;
    static final int READ_STARTED = 1;
    static int readStatus = READ_NO_STATUS;
    static boolean networkReadReady;

    // initialised flag
    static boolean isInitialised = false;
    static boolean neverConected = true;

    // network read error count
    static final int NET_ERR_COUNT = 5;
    static int netErrorCount = 0;

    // the network socket
    static Socket clientSocket = null;

    // input stream
    static BufferedReader input = null;
    // nmea messages returned to caller
    static List<String> listStr;

    /** initialisation */
    public static void initialise() {

        // if already initialised, ignore
        if (isInitialised)
            return;

        // first close any open resources from previous call
        NmeaGwV1.close();
        netErrorCount = 0;

        // initialise the interface to the NMEA Gateway
        if (AppConfig.DEMO_MODE) {
            // if DEMO_MODE set then initialise read from file
            Log.i(TAG, "DEMO mode activated");
            String fileName = MainActivity.appFilePath + AppConfig.DEMO_FILE;
            try {
                input = new BufferedReader(new FileReader(fileName));
                isInitialised = true;
                Log.i(TAG, "reading NMEA messages from file: " + fileName);
            } catch (Exception e) {
                Log.e(TAG, "could not open demo file: " + e.getMessage());
            }
        } else {
            // else initialise network
            (new NetInit()).execute(AppConfig.NMEA_GW_IP, String.valueOf(AppConfig.NMEA_GW_PORT));
        }
    }

    /** read NMEA messages */
    public static List<String> read() {
        if (!isInitialised) {
            Log.e(TAG, "input stream not initialised");
            return null;
        }
        if (AppConfig.DEMO_MODE)
            return readFromFile();
        else
            return readFromGateway();
    }

    /** read messages form file for demo/testing */
    static private List<String> readFromFile() {
        // read just one line from the demo file
        try {
            String line = input.readLine();
            if (line == null)
                // if eof return null
                return null;
            else {
                // build the return list (just one line)
                listStr = new ArrayList<>();
                listStr.add(line);
                return listStr;
            }
        } catch (Exception e) {
            Log.e(TAG, "could not read from input file: "+e.getMessage());
            return null;
        }
    }

    /** read messages from the NMEA gateway */
    static private List<String> readFromGateway() {

        // check the state and act accordingly
        switch (readStatus) {
            case READ_NO_STATUS:
                // initialise new read cycle
                listStr = new ArrayList<>();
                networkReadReady = false;
                // read from network in the background
                (new NetRead()).execute();
                // set state
                readStatus = READ_STARTED;
                // and return nothing to the caller for now
                return null;
            case READ_STARTED:
                // check if network read has completed
                if (!networkReadReady)
                    return null;
                else {
                    // done - reset the status
                    readStatus = READ_NO_STATUS;
                    return listStr;
                }
            default:
                return null;
        }
    }

    /** class for the android background task to initialise the network */
    private static class NetInit extends AsyncTask<String, Void, Integer> {
        // execute task in background
        // parameters: (IP Address, String(Port))
        @SuppressLint("DefaultLocale")
        protected Integer doInBackground(String... cmdArgs) {
            Log.i(TAG, "background network initialisation started");
            // check for valid arguments
            if (cmdArgs.length < 2)
                return RES_BAD_ARG;
            try {
                clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(cmdArgs[0], Integer.parseInt(cmdArgs[1])), 2000);
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Log.i(TAG, "connected to " + cmdArgs[0] + ":" + Integer.parseInt(cmdArgs[1]));
                if (AppConfig.NMEA_GW_TIMEOUT > 0) {
                    clientSocket.setSoTimeout(AppConfig.NMEA_GW_TIMEOUT);
                    System.out.println("socket timeout set to " + AppConfig.NMEA_GW_TIMEOUT + "msec");
                }
                return RES_INIT_SUCCESS;
            }
            catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
                return RES_INIT_FAIL;
            }
        } // doInBackground

        /** background command completed */
        protected void onPostExecute(Integer result) {
            switch (result) {
                case RES_INIT_SUCCESS:
                    Log.i(TAG, "network initialisation completed successfully");
                    isInitialised = true;
                    neverConected = false;
                    break;
                case RES_INIT_FAIL:
                    Log.e(TAG, "network initialisation failed");
                    isInitialised = false;
                    // in addition if never connected, switch to demo mode
                    if (neverConected) {
                        AppConfig.DEMO_MODE = true;
                        Log.i(TAG, "switching to demo mode");
                        // invoke init again for demo
                        NmeaGwV1.initialise();
                    }
                    break;
            }
        } // onPostExecute
    } // class NetInit

    /** class for the background task to read from network */
    private static class NetRead extends AsyncTask<Void, Void, Integer> {
        // execute task in background
        // parameters: (none)
        @SuppressLint("DefaultLocale")
        protected Integer doInBackground(Void... cmdArgs) {
            Log.i(TAG, "background network read started");
            try {
                String line;
                int count = 0;
                // read as many lines as possible from the network socket
                while ((line = input.readLine()) != null && count++ < 10 /*AppConfig.NMEA_MAX_LINES*/) {
                    Log.i(TAG, "netRead - received line: " + line);
                    listStr.add(line);
                }
                return RES_READ_SUCCESS;
            } catch (SocketTimeoutException e) {
                Log.i(TAG, "socket timeout: " + e.getMessage());
                // indicate network read is done
                return RES_READ_SOC_TIMEOUT;
            } catch (Exception e) {
                Log.e(TAG, "could not read from network port: " + e.getMessage());
                networkReadReady = true;
                // increase the error count
                ++netErrorCount;
                return RES_READ_FAIL;
            }
        } // doInBackground

        /** background command completed */
        protected void onPostExecute(Integer result) {
            Log.i(TAG, "netRead - list: "+listStr.toString());
            switch (result) {
                case RES_READ_SUCCESS:
                    networkReadReady = true;
                    Log.i(TAG, "network read completed - read "+listStr.size()+" lines");
                    break;
                case RES_READ_SOC_TIMEOUT:
                    networkReadReady = true;
                    Log.i(TAG, "network read completed (timeout) - read "+listStr.size()+" lines");
                    break;
                case RES_READ_FAIL:
                    networkReadReady = true;
                    Log.i(TAG, "network read failed - read "+listStr.size()+" lines");
                    // check the error count
                    if (netErrorCount >= NET_ERR_COUNT)
                        isInitialised = false;
                    break;
            }
        } // onPostExecute
    } // class NetRead

    /** closes open resources */
    static void close() {
        try {
            if (clientSocket != null)
                clientSocket.close();
            if (input != null)
                input.close();
        }
        catch (Exception e) {
            Log.e(TAG, "could not close resource "+e.getMessage());
        }
    }
}
