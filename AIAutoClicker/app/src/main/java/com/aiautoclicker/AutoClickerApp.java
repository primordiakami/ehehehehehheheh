package com.aiautoclicker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class AutoClickerApp extends Application {
    private static final String TAG = "AutoClickerApp";
    private static Context appContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        
        // Initialize Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
            Log.d(TAG, "Python initialized successfully");
        }
        
        // Initialize recognition modules
        initializeRecognitionModules();
    }
    
    private void initializeRecognitionModules() {
        try {
            Python py = Python.getInstance();
            py.getModule("recognition_init");
            Log.d(TAG, "Recognition modules initialized");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize recognition modules", e);
        }
    }
    
    public static Context getAppContext() {
        return appContext;
    }
}
