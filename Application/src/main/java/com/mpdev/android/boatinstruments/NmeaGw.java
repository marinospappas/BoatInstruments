package com.mpdev.android.boatinstruments;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import com.mpdev.android.logger.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * NmeaGw class
 * Interfaces to the NMEA Gateway and passes on the incoming messages to the application
 * Reads from the specified IP address and port
 * or if DEMO_MODE is set from text file
 *
 * Version 3
 * Written as a background thread
 * Can implement blocking read or can sleep if needed without causing issue to the main app
 * When data is available it calls processMessage in the UI thread to update and display the data
 * The background thread is terminated by means of the Atomic Boolean "running"
 */
public class NmeaGw implements Runnable {

    private static final String TAG = "NmeaGw";

    // the running flag for this thread
    final AtomicBoolean running = new AtomicBoolean(false);

    // network error count
    int networkError = 0;

    // input stream for demo mode
    BufferedReader fileInput = null;

    // input stream for reading from network
    InputStream netInput = null;

    // the activity that calls this class
    Activity activity;
    // the display fragment that manages the UI
    InstrumentsDisplayFragment displayFragment;
    // the demo/wifi status on the screen
    ImageView imageStatus;

    // the worker thread
    private Thread worker;

    /** Constructor */
    NmeaGw(Activity activity, InstrumentsDisplayFragment displayFragment) {
        this.activity = activity;
        this.displayFragment = displayFragment;

        this.imageStatus = displayFragment.display.getImageStatus();
    }

    /** start reading in a new thread */
    public void start() {
        worker = new Thread(this);
        Log.i(TAG, "background read thread starting");
        worker.start();
    }

    /** stop this thread */
    public void stop() {
        running.set(false);
    }

    /** read NMEA messages */
    @Override
    public void run() {
        Log.i(TAG, "new background read called");
        int res;
        running.set(true);
        // file (demo data)
        if (AppConfig.DEMO_MODE) {
            // set DEMO status on screen
            setImageStatus("demo", 0);
            // start the read loop from file
            fileRead();
        }
        // network (real data)
        else {
            // start the read loop from network
            res = netReadTCP();
            if (res < 0) {
                // if we cannot connect to network at all re-run this method in Demo mode
                AppConfig.DEMO_MODE = true;
                this.run();
            }
            if (res == 0) {
                // if we have network time out then reconnect
                netInput = null;
                this.run();
            }
        }
    }

    /** open the demo file and read the NMEA messages
     * returns -1: switch to demo from file, 0: reconnect to network, 1: abort
     */
    private void fileRead() {
        Log.d(TAG, "background thread for file read started");
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
                return;
            }
        }
        // endless loop in the background thread (controlled by the 'running' flag)
        while (running.get()) {
            // read just one line from the demo file
            try {
                String line = fileInput.readLine();
                if (line != null) {
                    // process the line read from file
                    final String finalLine = line;
                    Log.d(TAG, "received line from file: " + finalLine);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayFragment.processMessage(finalLine);
                        }
                    });
                } else
                    // if eof terminate background thread
                    return;
            } catch (Exception e) {
                Log.e(TAG, "could not read from input file: " + e.getMessage() + " - thread exiting");
                return;
            }
            // sleep for a short while until next line is read
            try {Thread.sleep(500);} catch (Exception ignored) {}
        }
    }

    /**
     * connect to the server and read the NMEA messages
     * TCP protocol
     * @return   -1 if it cannot connect to network (the caller will switch to demo)
     *           0  if it cannot read from network (the caller will reconnect)
     *           1  if too many network errors (the caller will abort)
     */
    private int netReadTCP() {
        Log.d(TAG, "background thread for network read started");
        Socket clientSocket = new Socket();
        // if the input stream is null then we need to connect to the server first
        if (netInput == null) {
            // first set the wifi image on
            setImageStatus("wifiok", 0);
            // but while trying to connect set temporarily no wifi
            setImageStatus("nowifi", 200);
            try {
                // set receive buffer size
                // clientSocket.setReceiveBufferSize(256);
                Log.i(TAG, "receive buffer size: " + clientSocket.getReceiveBufferSize());
                // connect to server
                clientSocket.connect(new InetSocketAddress(AppConfig.NMEA_GW_IP, AppConfig.NMEA_GW_PORT), 3000);
                clientSocket.setKeepAlive(true);
                // get the input stream
                netInput = clientSocket.getInputStream();
                Log.i(TAG, "connected to " + AppConfig.NMEA_GW_IP + ":" + AppConfig.NMEA_GW_PORT);
                if (AppConfig.NMEA_GW_TIMEOUT > 0) {
                    clientSocket.setSoTimeout(AppConfig.NMEA_GW_TIMEOUT);
                    Log.d(TAG, "socket timeout set to " + AppConfig.NMEA_GW_TIMEOUT + "msec");
                }
                // set wifi status on
                setImageStatus("wifiok", 50  );
            } catch (Exception e) {
                // could not connect to server
                Log.e(TAG, "could not connect to server: " + e.getMessage());
                // set status to no wifi
                setImageStatus("nowifi", 0);
                // switch to demo
                AppConfig.DEMO_MODE = true;
                // and stop this thread (return -1 for error)
                return -1;
            }
        }
        // read from the server - endless loop controlled by the 'running' flag
        while (running.get()) {
            try {
                // read a line from the socket stream
                String line = "";
                byte[] c = { 0 };
                do {
                    c[0] = (byte)netInput.read();
                    // turn wifi status on for a short while to show data was read
                    setImageStatus("wifiok", 0);
                    setImageStatus("none", 50);
                    if (c[0] == 0x000A)
                        break;
                    if (c[0] != 0x000D)
                        line = line + new String(c);
                    if (c[0] < 0) {
                        Log.i(TAG, "network read returned nothing");
                        // return 0 will result in disconnecting and reconnecting
                        try {clientSocket.close();}catch (Exception ignored){}
                        return 0;
                    }
                } while (c[0] > 0);
                final String finalLine = line;
                Log.d(TAG, "received line from network: " + finalLine);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayFragment.processMessage(finalLine);
                    }
                });
            } catch (SocketTimeoutException e) {
                setImageStatus("nowifi", 50);
                Log.i(TAG, "network read timeout: " + e.getMessage());
                if (++networkError > 200) {
                    Log.i(TAG, "too many network errors - abort thread");
                    return 1;
                }
                // return 0 will result in disconnecting and reconnecting
                try {clientSocket.close();}catch (Exception ignored){}
                return 0;
            } catch (Exception e) {
                setImageStatus("nowifi", 50);
                Log.e(TAG, "network read error: " + e.getMessage());
                return 1;
            }
        }

        // close any open resources before closing this thread
        try {
            if (netInput != null)
                netInput.close();
            if (clientSocket.isConnected())
                clientSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "could not close resource: " + e.getMessage());
        }
        Log.i(TAG, "network read thread terminating");
        return 0;
    }

    /** sets the status image on the screen with or without delay */
    private void setImageStatus(String status, int delay) {
        int imageResource = 0;
        switch (status) {
            case "demo": imageResource = R.drawable.demo; break;
            case "wifiok": imageResource = R.drawable.wifi_ok; break;
            case "nowifi": imageResource = R.drawable.no_wifi; break;
        }
        if (imageStatus != null) {
            final int finalImageResource = imageResource;
            if (delay == 0) {
                imageStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalImageResource == 0)
                            imageStatus.setVisibility(View.INVISIBLE);
                        else {
                            imageStatus.setImageResource(finalImageResource);
                            imageStatus.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            else {
                imageStatus.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (finalImageResource == 0)
                            imageStatus.setVisibility(View.INVISIBLE);
                        else {
                            imageStatus.setImageResource(finalImageResource);
                            imageStatus.setVisibility(View.VISIBLE);
                        }
                    }
                }, delay);
            }
        }
    }
}
