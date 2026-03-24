package com.aiautoclicker.python;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class PythonBridgeService extends Service {
    private static final String TAG = "PythonBridgeService";
    
    private final IBinder binder = new LocalBinder();
    private PythonBridge pythonBridge;
    
    public class LocalBinder extends Binder {
        public PythonBridgeService getService() {
            return PythonBridgeService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        pythonBridge = new PythonBridge();
        Log.d(TAG, "PythonBridgeService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PythonBridgeService destroyed");
    }
    
    public PyObject executeCode(String code) {
        if (pythonBridge != null) {
            return pythonBridge.executeCode(code);
        }
        return null;
    }
    
    public PyObject evaluateExpression(String expression) {
        if (pythonBridge != null) {
            return pythonBridge.evaluateExpression(expression);
        }
        return null;
    }
    
    public void setVariable(String name, Object value) {
        if (pythonBridge != null) {
            pythonBridge.setVariable(name, value);
        }
    }
    
    public PyObject getVariable(String name) {
        if (pythonBridge != null) {
            return pythonBridge.getVariable(name);
        }
        return null;
    }
}
