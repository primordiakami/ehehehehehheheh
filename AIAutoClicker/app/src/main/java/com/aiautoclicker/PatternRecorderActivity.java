package com.aiautoclicker;

import android.app.AlertDialog;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aiautoclicker.recognition.RecognitionManager;
import com.aiautoclicker.service.AutoClickerAccessibilityService;

import java.util.ArrayList;
import java.util.List;

public class PatternRecorderActivity extends AppCompatActivity {
    private static final String TAG = "PatternRecorder";
    
    private WindowManager windowManager;
    private FrameLayout overlayView;
    private View recordingPanel;
    
    private boolean isRecording = false;
    private List<RecognitionManager.TouchPoint> recordedPoints;
    private long recordingStartTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        recordedPoints = new ArrayList<>();
        
        createOverlay();
        showRecordingPanel();
    }
    
    private void createOverlay() {
        overlayView = new FrameLayout(this);
        overlayView.setBackgroundColor(0x44000000); // Semi-transparent black
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        
        // Set up touch listener for recording
        overlayView.setOnTouchListener((v, event) -> {
            if (isRecording) {
                handleTouchEvent(event);
            }
            return true;
        });
        
        try {
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            Log.e(TAG, "Failed to add overlay", e);
            finish();
        }
    }
    
    private void showRecordingPanel() {
        recordingPanel = LayoutInflater.from(this).inflate(R.layout.panel_recording, null);
        
        WindowManager.LayoutParams panelParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        panelParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        panelParams.y = 100;
        
        Button btnStartStop = recordingPanel.findViewById(R.id.btn_start_stop);
        Button btnSave = recordingPanel.findViewById(R.id.btn_save_pattern);
        Button btnClear = recordingPanel.findViewById(R.id.btn_clear_pattern);
        Button btnClose = recordingPanel.findViewById(R.id.btn_close);
        
        btnStartStop.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
                btnStartStop.setText("Start Recording");
            } else {
                startRecording();
                btnStartStop.setText("Stop Recording");
            }
        });
        
        btnSave.setOnClickListener(v -> showSaveDialog());
        
        btnClear.setOnClickListener(v -> clearRecording());
        
        btnClose.setOnClickListener(v -> finish());
        
        try {
            windowManager.addView(recordingPanel, panelParams);
        } catch (Exception e) {
            Log.e(TAG, "Failed to add recording panel", e);
        }
    }
    
    private void handleTouchEvent(MotionEvent event) {
        if (!isRecording) return;
        
        long timestamp = SystemClock.elapsedRealtime() - recordingStartTime;
        int action = event.getActionMasked();
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
 case MotionEvent.ACTION_UP:
                RecognitionManager.TouchPoint point = new RecognitionManager.TouchPoint(
                    event.getX(),
                    event.getY(),
                    timestamp,
                    action
                );
                recordedPoints.add(point);
                Log.d(TAG, "Recorded point: (" + point.x + ", " + point.y + ") at " + timestamp + "ms");
                break;
        }
    }
    
    private void startRecording() {
        isRecording = true;
        recordedPoints.clear();
        recordingStartTime = SystemClock.elapsedRealtime();
        Toast.makeText(this, "Recording started - Touch the screen", Toast.LENGTH_SHORT).show();
    }
    
    private void stopRecording() {
        isRecording = false;
        Toast.makeText(this, "Recording stopped - " + recordedPoints.size() + " points captured", Toast.LENGTH_SHORT).show();
    }
    
    private void clearRecording() {
        recordedPoints.clear();
        Toast.makeText(this, "Recording cleared", Toast.LENGTH_SHORT).show();
    }
    
    private void showSaveDialog() {
        if (recordedPoints.isEmpty()) {
            Toast.makeText(this, "No points recorded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        EditText input = new EditText(this);
        input.setHint("Pattern name");
        
        new AlertDialog.Builder(this)
            .setTitle("Save Pattern")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    savePattern(name);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void savePattern(String name) {
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        if (service != null && service.getRecognitionManager() != null) {
            service.getRecognitionManager().recordPattern(name, recordedPoints);
            Toast.makeText(this, "Pattern saved: " + name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                Log.e(TAG, "Error removing overlay", e);
            }
        }
        
        if (recordingPanel != null && windowManager != null) {
            try {
                windowManager.removeView(recordingPanel);
            } catch (Exception e) {
                Log.e(TAG, "Error removing recording panel", e);
            }
        }
    }
}
