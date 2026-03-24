package com.aiautoclicker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.aiautoclicker.MainActivity;
import com.aiautoclicker.R;
import com.aiautoclicker.tasks.Task;
import com.aiautoclicker.tasks.TaskExecutor;

public class TaskExecutionService extends Service {
    private static final String CHANNEL_ID = "task_execution_channel";
    private static final int NOTIFICATION_ID = 3;
    
    private final IBinder binder = new LocalBinder();
    private TaskExecutor taskExecutor;
    
    public class LocalBinder extends Binder {
        public TaskExecutionService getService() {
            return TaskExecutionService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Task Execution Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Running task execution");
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
            .setContentText("Task execution service running")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    public void executeTask(Task task) {
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        if (service != null) {
            service.executeTask(task);
        }
    }
    
    public void stopExecution() {
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        if (service != null) {
            service.stopAutomation();
        }
    }
}
