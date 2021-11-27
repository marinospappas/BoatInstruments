package com.mpdev.android.boatinstruments;

import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.mpdev.android.logger.Log;


/**
 * NmeaGw class
 * Interfaces to the NMEA Gateway and passes on the incoming messages to the application
 * Reads from the specified IP address and port
 * or if DEMO_MODE is set from text file
 *
 * Version 2
 * Does not maintain open connection to the server.
 * Every time read is called, it opens a new socket to the server and reads available lines
 */
class NmeaGwV2 {

    private static final String TAG = "NmeaGw";

    // net read status
    static final int READ_NO_STATUS = 0;
    static final int READ_STARTED = 1;

    int readStatus = READ_NO_STATUS;
    boolean networkReadDone = false;

    // network connected flag
    static boolean neverConected = true;

    // network read error count
    final int NET_ERR_COUNT = 3;
    int netErrorCount = 0;

    // input stream for demo mode
    BufferedReader fileInput = null;

    // nmea messages returned to caller
    List<String> listStr;

    // the status view
    ImageView imageStatus;

    /** Constructor */
    NmeaGwV2() {}

    /** read NMEA messages */
    public List<String> read(ImageView status) {
        List<String> nmeaMsgs;

        imageStatus = status;
        if (AppConfig.DEMO_MODE) {
            // set DEMO status on screen
            if (imageStatus != null)
                imageStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        imageStatus.setImageResource(R.drawable.demo);
                        imageStatus.setVisibility(View.VISIBLE);
                    }
                });
            // read from file
            nmeaMsgs = readFromFile();
        }
        else
            nmeaMsgs = readFromGateway();

        Log.i(TAG, "read returned: " + nmeaMsgs);
        return nmeaMsgs;
    }

    /** read messages form file for demo/testing */
    private List<String> readFromFile() {
        if (fileInput == null) {
            Log.a(TAG, "DEMO mode activated");
            Log.a(TAG, "ignoring NMEA checksum");
            AppConfig.NMEA_VALIDATE_CHKSUM = false;
            String fileName = MainActivity.appFilePath + AppConfig.DEMO_FILE;
            try {
                fileInput = new BufferedReader(new FileReader(fileName));
                Log.i(TAG, "reading NMEA messages from file: " + fileName);
            } catch (Exception e) {
                Log.e(TAG, "could not open demo file: " + e.getMessage());

            }
        }
        // read just one line from the demo file
        try {
            String line = fileInput.readLine();
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
    private List<String> readFromGateway() {

        // check the state and act accordingly
        switch (readStatus) {
            case READ_NO_STATUS:
                // initialise new read cycle
                listStr = new ArrayList<>();
                networkReadDone = false;
                // read from network in the background
                ( new Thread(new NetRead()) ).start();
                // set state
                readStatus = READ_STARTED;
                // and return nothing to the caller for now
                return null;
            case READ_STARTED:
                // check if network read has completed
                if (!networkReadDone)
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

    /** class for connecting to the server and reading the NMEA messages in a background thread */
    private class NetRead implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "background thread for network read started");
            Socket clientSocket = new Socket();
            BufferedReader netInput = null;
            try {
                // connect to server
                clientSocket.connect(new InetSocketAddress(AppConfig.NMEA_GW_IP, AppConfig.NMEA_GW_PORT),AppConfig.NMEA_GW_TIMEOUT);
                neverConected = false;
                netInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Log.i(TAG, "connected to " + AppConfig.NMEA_GW_IP + ":" + AppConfig.NMEA_GW_PORT);
                if (AppConfig.NMEA_GW_TIMEOUT > 0) {
                    clientSocket.setSoTimeout(AppConfig.NMEA_GW_TIMEOUT);
                    Log.d(TAG, "socket timeout set to " + AppConfig.NMEA_GW_TIMEOUT + "msec");
                }
                // set wifi status on
                if (imageStatus != null)
                    imageStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            imageStatus.setImageResource(R.drawable.wifi_ok);
                            imageStatus.setVisibility(View.VISIBLE);
                        }
                    });
                // read from the server
                try {
                    String line;
                    int count = 0;
                    // read as many lines as possible from the network socket
                    while ((line = netInput.readLine()) != null && count++ < 10 /*AppConfig.NMEA_MAX_LINES*/) {
                        listStr.add(line);
                    }
                    Log.i(TAG, "network read completed: max lines (" + listStr.size() + " lines)");
                } catch (SocketTimeoutException e) {
                    Log.i(TAG, "network read completed: " + e.getMessage() + " read "+ listStr.size() + " lines");
                } catch (Exception e) {
                    Log.e(TAG, "network read completed with error: " + e.getMessage() + " read "+ listStr.size() + " lines");
                }
                Log.d(TAG,"received nmea msgs: "+listStr.toString());
                // turn off wifi status on screen
                if (imageStatus != null)
                    imageStatus.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageStatus.setVisibility(View.INVISIBLE);
                        }
                    }, 50);
            } catch (Exception e) {
                // could not connect to server
                Log.e(TAG, "network error: " + e.getMessage());
                // set status to no wifi
                if (imageStatus != null)
                    imageStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            imageStatus.setImageResource(R.drawable.no_wifi);
                            imageStatus.setVisibility(View.VISIBLE);
                        }
                    });
                // increase the error count and check if max errors reached to switch to demo
                if (++netErrorCount >= NET_ERR_COUNT && neverConected) {
                    AppConfig.DEMO_MODE = true;
                    // and reset error count
                    netErrorCount = 0;
                }
            }
            networkReadDone = true;

            // close any open resources before closing this thread
            try {
                if (netInput != null)
                    netInput.close();
                if (clientSocket.isConnected())
                    clientSocket.close();
            }
            catch (Exception e) {
                Log.e(TAG,"could not close resource: "+e.getMessage());
            }
        }
    } // class NetRead

}
