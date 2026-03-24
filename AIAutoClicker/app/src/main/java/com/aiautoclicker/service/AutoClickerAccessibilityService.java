package com.aiautoclicker.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.aiautoclicker.R;
import com.aiautoclicker.tasks.Task;
import com.aiautoclicker.tasks.TaskExecutor;
import com.aiautoclicker.tasks.TaskManager;
import com.aiautoclicker.recognition.RecognitionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoClickerAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoClickerService";
    private static final long DEFAULT_GESTURE_DURATION = 100;
    
    private static AutoClickerAccessibilityService instance;
    private Handler mainHandler;
    private ExecutorService executorService;
    private TaskExecutor taskExecutor;
    private RecognitionManager recognitionManager;
    private WindowManager windowManager;
    private View floatingControls;
    
    private boolean isRunning = false;
    private boolean isRecording = false;
    private List<AccessibilityEvent> recordedEvents = new ArrayList<>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
        taskExecutor = new TaskExecutor(this);
        recognitionManager = new RecognitionManager(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        Log.d(TAG, "Accessibility Service created");
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service connected");
        
        // Configure service info
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                         AccessibilityEvent.TYPE_VIEW_LONG_CLICKED |
                         AccessibilityEvent.TYPE_VIEW_SELECTED |
                         AccessibilityEvent.TYPE_VIEW_FOCUSED |
                         AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                         AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED |
                         AccessibilityEvent.TYPE_VIEW_SCROLLED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.notificationTimeout = 0;
        setServiceInfo(info);
        
        showFloatingControls();
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (isRecording) {
            recordedEvents.add(event);
        }
        
        // Process events for recognition
        if (recognitionManager != null) {
            recognitionManager.processEvent(event);
        }
        
        // Broadcast event to listeners
        Intent intent = new Intent("com.aiautoclicker.ACCESSIBILITY_EVENT");
        intent.putExtra("event_type", event.getEventType());
        sendBroadcast(intent);
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted");
        stopAutomation();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        stopAutomation();
        hideFloatingControls();
        if (executorService != null) {
            executorService.shutdown();
        }
        Log.d(TAG, "Accessibility Service destroyed");
    }
    
    // ==================== CLICK AND GESTURE METHODS ====================
    
    public void performClick(int x, int y) {
        if (!isRunning) return;
        
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, DEFAULT_GESTURE_DURATION));
        
        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Click completed at (" + x + ", " + y + ")");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.w(TAG, "Click cancelled at (" + x + ", " + y + ")");
            }
        }, null);
    }
    
    public void performLongClick(int x, int y, long duration) {
        if (!isRunning) return;
        
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        
        dispatchGesture(builder.build(), null, null);
    }
    
    public void performSwipe(int startX, int startY, int endX, int endY, long duration) {
        if (!isRunning) return;
        
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        
        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Swipe completed from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")");
            }
        }, null);
    }
    
    public void performMultiTouch(List<int[]> points, long duration) {
        if (!isRunning || points == null || points.isEmpty()) return;
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        
        for (int[] point : points) {
            if (point.length >= 2) {
                Path path = new Path();
                path.moveTo(point[0], point[1]);
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
            }
        }
        
        dispatchGesture(builder.build(), null, null);
    }
    
    public void performScroll(int x, int y, int scrollAmount) {
        if (!isRunning) return;
        
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y - scrollAmount);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        
        dispatchGesture(builder.build(), null, null);
    }
    
    // ==================== NODE INTERACTION ====================
    
    public void clickOnNode(AccessibilityNodeInfo node) {
        if (node == null) return;
        
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        
        performClick(centerX, centerY);
    }
    
    public void findAndClickByText(String text) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (!nodes.isEmpty()) {
            clickOnNode(nodes.get(0));
            for (AccessibilityNodeInfo node : nodes) {
                node.recycle();
            }
        }
        rootNode.recycle();
    }
    
    public void findAndClickByViewId(String viewId) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (!nodes.isEmpty()) {
            clickOnNode(nodes.get(0));
            for (AccessibilityNodeInfo node : nodes) {
                node.recycle();
            }
        }
        rootNode.recycle();
    }
    
    // ==================== TASK EXECUTION ====================
    
    public void startAutomation() {
        isRunning = true;
        Toast.makeText(this, "Auto Clicker Started", Toast.LENGTH_SHORT).show();
    }
    
    public void stopAutomation() {
        isRunning = false;
        if (taskExecutor != null) {
            taskExecutor.stop();
        }
        Toast.makeText(this, "Auto Clicker Stopped", Toast.LENGTH_SHORT).show();
    }
    
    public void pauseAutomation() {
        isRunning = false;
        Toast.makeText(this, "Auto Clicker Paused", Toast.LENGTH_SHORT).show();
    }
    
    public void executeTask(Task task) {
        if (task == null) return;
        
        if (!isRunning) {
            startAutomation();
        }
        
        executorService.execute(() -> {
            taskExecutor.execute(task);
        });
    }
    
    // ==================== RECORDING ====================
    
    public void startRecording() {
        isRecording = true;
        recordedEvents.clear();
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
    }
    
    public void stopRecording() {
        isRecording = false;
        Toast.makeText(this, "Recording stopped - " + recordedEvents.size() + " events captured", Toast.LENGTH_SHORT).show();
    }
    
    public List<AccessibilityEvent> getRecordedEvents() {
        return new ArrayList<>(recordedEvents);
    }
    
    public void clearRecording() {
        recordedEvents.clear();
    }
    
    // ==================== FLOATING CONTROLS ====================
    
    private void showFloatingControls() {
        if (floatingControls != null) return;
        
        floatingControls = LayoutInflater.from(this).inflate(R.layout.floating_controls, null);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        
        setupFloatingControls(floatingControls);
        
        try {
            windowManager.addView(floatingControls, params);
        } catch (Exception e) {
            Log.e(TAG, "Failed to add floating controls", e);
        }
    }
    
    private void hideFloatingControls() {
        if (floatingControls != null && windowManager != null) {
            try {
                windowManager.removeView(floatingControls);
            } catch (Exception e) {
                Log.e(TAG, "Failed to remove floating controls", e);
            }
            floatingControls = null;
        }
    }
    
    private void setupFloatingControls(View view) {
        View playButton = view.findViewById(R.id.btn_play);
        View pauseButton = view.findViewById(R.id.btn_pause);
        View stopButton = view.findViewById(R.id.btn_stop);
        View recordButton = view.findViewById(R.id.btn_record);
        View closeButton = view.findViewById(R.id.btn_close);
        
        if (playButton != null) {
            playButton.setOnClickListener(v -> startAutomation());
        }
        
        if (pauseButton != null) {
            pauseButton.setOnClickListener(v -> pauseAutomation());
        }
        
        if (stopButton != null) {
            stopButton.setOnClickListener(v -> stopAutomation());
        }
        
        if (recordButton != null) {
            recordButton.setOnClickListener(v -> {
                if (isRecording) {
                    stopRecording();
                    recordButton.setBackgroundResource(R.drawable.ic_record);
                } else {
                    startRecording();
                    recordButton.setBackgroundResource(R.drawable.ic_stop);
                }
            });
        }
        
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> hideFloatingControls());
        }
    }
    
    // ==================== GETTERS ====================
    
    public static AutoClickerAccessibilityService getInstance() {
        return instance;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public RecognitionManager getRecognitionManager() {
        return recognitionManager;
    }
    
    public Handler getMainHandler() {
        return mainHandler;
    }
}
