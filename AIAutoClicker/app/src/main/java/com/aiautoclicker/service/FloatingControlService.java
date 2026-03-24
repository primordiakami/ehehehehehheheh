package com.aiautoclicker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.core.app.NotificationCompat;

import com.aiautoclicker.MainActivity;
import com.aiautoclicker.R;

public class FloatingControlService extends Service {
    private static final String CHANNEL_ID = "floating_control_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private WindowManager windowManager;
    private View floatingView;
    
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
        showFloatingControls();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Floating Control Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Running floating controls");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Auto Clicker")
            .setContentText("Floating controls active")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    private void showFloatingControls() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_controls, null);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        
        setupControlButtons(floatingView);
        
        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupControlButtons(View view) {
        ImageButton btnPlay = view.findViewById(R.id.btn_play);
        ImageButton btnPause = view.findViewById(R.id.btn_pause);
        ImageButton btnStop = view.findViewById(R.id.btn_stop);
        ImageButton btnRecord = view.findViewById(R.id.btn_record);
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        
        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> {
                if (service != null) service.startAutomation();
            });
        }
        
        if (btnPause != null) {
            btnPause.setOnClickListener(v -> {
                if (service != null) service.pauseAutomation();
            });
        }
        
        if (btnStop != null) {
            btnStop.setOnClickListener(v -> {
                if (service != null) service.stopAutomation();
            });
        }
        
        if (btnRecord != null) {
            btnRecord.setOnClickListener(v -> {
                if (service != null) {
                    if (service.isRecording()) {
                        service.stopRecording();
                    } else {
                        service.startRecording();
                    }
                }
            });
        }
        
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> stopSelf());
        }
    }
}
