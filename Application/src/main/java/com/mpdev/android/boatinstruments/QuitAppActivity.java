package com.mpdev.android.boatinstruments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.mpdev.android.logger.Log;

/** exits the application */
public class QuitAppActivity extends AppCompatActivity {
    private final String TAG = "QuitAppActivity";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.a(TAG,"onCreate called");
        super.onCreate(savedInstanceState);
        finishAndRemoveTask();
        Log.a(TAG,"exiting application");
        System.exit(0);
    }
}
