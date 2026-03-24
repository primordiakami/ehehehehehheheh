package com.aiautoclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiautoclicker.service.AutoClickerAccessibilityService;
import com.aiautoclicker.tasks.Task;
import com.aiautoclicker.tasks.TaskAdapter;
import com.aiautoclicker.tasks.TaskManager;
import com.google.android.material.card.MaterialCardView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int REQUEST_OVERLAY_PERMISSION = 1001;
    private static final int REQUEST_ACCESSIBILITY = 1002;
    
    private TextView tvStatus;
    private View statusIndicator;
    private RecyclerView recyclerTasks;
    private TaskAdapter taskAdapter;
    private TaskManager taskManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        taskManager = TaskManager.getInstance(this);
        
        initializeViews();
        setupClickListeners();
        checkPermissions();
        updateStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        refreshTaskList();
    }
    
    private void initializeViews() {
        tvStatus = findViewById(R.id.tv_status);
        statusIndicator = findViewById(R.id.status_indicator);
        recyclerTasks = findViewById(R.id.recycler_tasks);
        
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskManager.getTasks(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                executeTask(task);
            }
            
            @Override
            public void onTaskEdit(Task task) {
                editTask(task);
            }
            
            @Override
            public void onTaskDelete(Task task) {
                deleteTask(task);
            }
        });
        recyclerTasks.setAdapter(taskAdapter);
    }
    
    private void setupClickListeners() {
        MaterialCardView cardStartService = findViewById(R.id.card_start_service);
        MaterialCardView cardPythonConsole = findViewById(R.id.card_python_console);
        MaterialCardView cardCreateTask = findViewById(R.id.card_create_task);
        MaterialCardView cardRecordPattern = findViewById(R.id.card_record_pattern);
        
        cardStartService.setOnClickListener(v -> {
            if (checkAccessibilityService()) {
                startAutoClickerService();
            }
        });
        
        cardPythonConsole.setOnClickListener(v -> {
            openPythonConsole();
        });
        
        cardCreateTask.setOnClickListener(v -> {
            createNewTask();
        });
        
        cardRecordPattern.setOnClickListener(v -> {
            startPatternRecording();
        });
    }
    
    private void checkPermissions() {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        }
        
        // Check storage permission
        TedPermission.create()
            .setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    // Permissions granted
                }
                
                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    Toast.makeText(MainActivity.this, 
                        "Storage permission required for saving tasks", Toast.LENGTH_LONG).show();
                }
            })
            .setDeniedMessage("Storage permission is required to save and load tasks")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .check();
    }
    
    private void requestOverlayPermission() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.overlay_permission_message)
            .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            })
            .setNegativeButton(R.string.dismiss, null)
            .show();
    }
    
    private boolean checkAccessibilityService() {
        // Check if accessibility service is enabled
        String serviceName = getPackageName() + "/" + AutoClickerAccessibilityService.class.getName();
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        
        if (accessibilityEnabled == 0) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.accessibility_permission_message)
                .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, REQUEST_ACCESSIBILITY);
                })
                .setNegativeButton(R.string.dismiss, null)
                .show();
            return false;
        }
        return true;
    }
    
    private void startAutoClickerService() {
        Intent serviceIntent = new Intent(this, AutoClickerAccessibilityService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Auto Clicker Service Started", Toast.LENGTH_SHORT).show();
        updateStatus();
    }
    
    private void openPythonConsole() {
        Intent intent = new Intent(this, PythonConsoleActivity.class);
        startActivity(intent);
    }
    
    private void createNewTask() {
        Intent intent = new Intent(this, TaskEditorActivity.class);
        startActivity(intent);
    }
    
    private void editTask(Task task) {
        Intent intent = new Intent(this, TaskEditorActivity.class);
        intent.putExtra("task_id", task.getId());
        startActivity(intent);
    }
    
    private void deleteTask(Task task) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '" + task.getName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                taskManager.deleteTask(task.getId());
                refreshTaskList();
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void executeTask(Task task) {
        if (!checkAccessibilityService()) {
            return;
        }
        
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        if (service != null) {
            service.executeTask(task);
            Toast.makeText(this, "Executing: " + task.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service not running. Please start the service first.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void startPatternRecording() {
        if (!checkAccessibilityService()) {
            return;
        }
        
        Intent intent = new Intent(this, PatternRecorderActivity.class);
        startActivity(intent);
    }
    
    private void updateStatus() {
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        boolean isRunning = service != null && service.isRunning();
        
        if (tvStatus != null) {
            tvStatus.setText(isRunning ? "Service: Running" : "Service: Stopped");
        }
        
        if (statusIndicator != null) {
            statusIndicator.setBackgroundResource(isRunning ? 
                R.drawable.bg_status_active : R.drawable.bg_status_stopped);
        }
    }
    
    private void refreshTaskList() {
        if (taskAdapter != null) {
            taskAdapter.updateTasks(taskManager.getTasks());
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCESSIBILITY) {
            checkAccessibilityService();
        }
    }
}
