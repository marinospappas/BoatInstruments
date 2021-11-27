package com.mpdev.android.boatinstruments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.mpdev.android.logger.Log;

/**
 * Diplays the application log on the screen
 * the log is fetched from the local list maintained in com.mpdev.android.logger.Log
 * that keeps a copy of all log entires that are written tt he system log from this application run
 */
public class LogviewActivity extends AppCompatActivity {

    private final String TAG = "LogviewActivity";

    // display fields //
    TextView logContainer;
    ////////////////////

    Context context;

    @Override
    protected  void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate - started");

        context = getApplicationContext();

        // get the view and the container for the fields
        setContentView(R.layout.logview_activity);
        logContainer = findViewById(R.id.log_container);

        // "preparing log" message
        logContainer.setText(R.string.prep_log);

        // get the parameters from the caller activity
        Intent intent = getIntent();
        String params[] = intent.getStringArrayExtra(SetupActivity.LOG_PARAMS);

        // kick of a background thread to get the log and print it on the screen when ready
        // get the log and print it on the screen
        (new Thread(new GetLog(params))).start();
        Log.d(TAG,"onCreate - completed");
    }

    /** class that prepares the log in background */
    class GetLog implements Runnable {
        String logText;
        String[] params;
        GetLog(String[] params) {this.params = params;}
        @Override
        public void run() {
            Log.d(TAG, "get log started in new thread");
            try {Thread.sleep(500);} catch (Exception ignored){}
            logText = Log.log2String(params);
            // display the result on the screen
            logContainer.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    int numLines = logText.length() - logText.replace("\n", "").length();
                    Log.i(TAG, "log entries posted to the view - " + numLines + " lines");
                    logContainer.setText(logText);
                    logContainer.setTypeface(Typeface.MONOSPACE);
                }
            });
        }
    }
}