package com.aiautoclicker.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String TAG = "TaskManager";
    private static final String PREF_NAME = "task_manager_prefs";
    private static final String KEY_TASKS = "saved_tasks";
    private static final String TASKS_DIR = "tasks";
    
    private static TaskManager instance;
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private List<Task> tasks;
    
    private TaskManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.tasks = new ArrayList<>();
        loadTasks();
    }
    
    public static synchronized TaskManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskManager(context);
        }
        return instance;
    }
    
    // ==================== TASK CRUD OPERATIONS ====================
    
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }
    
    public void updateTask(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(task.getId())) {
                tasks.set(i, task);
                saveTasks();
                return;
            }
        }
    }
    
    public void deleteTask(String taskId) {
        tasks.removeIf(task -> task.getId().equals(taskId));
        saveTasks();
    }
    
    public Task getTask(String taskId) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }
    
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }
    
    public List<Task> getEnabledTasks() {
        List<Task> enabledTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isEnabled()) {
                enabledTasks.add(task);
            }
        }
        return enabledTasks;
    }
    
    // ==================== PERSISTENCE ====================
    
    private void saveTasks() {
        try {
            String json = gson.toJson(tasks);
            prefs.edit().putString(KEY_TASKS, json).apply();
            
            // Also save to file for external access
            saveTasksToFile();
        } catch (Exception e) {
            Log.e(TAG, "Error saving tasks", e);
        }
    }
    
    private void loadTasks() {
        try {
            // Try loading from SharedPreferences first
            String json = prefs.getString(KEY_TASKS, null);
            if (json != null) {
                Type type = new TypeToken<List<Task>>(){}.getType();
                List<Task> loadedTasks = gson.fromJson(json, type);
                if (loadedTasks != null) {
                    tasks.addAll(loadedTasks);
                }
            }
            
            // If no tasks loaded, create sample tasks
            if (tasks.isEmpty()) {
                createSampleTasks();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading tasks", e);
        }
    }
    
    private void saveTasksToFile() {
        try {
            File dir = new File(context.getExternalFilesDir(null), TASKS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, "tasks.json");
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(tasks));
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving tasks to file", e);
        }
    }
    
    private void loadTasksFromFile() {
        try {
            File dir = new File(context.getExternalFilesDir(null), TASKS_DIR);
            File file = new File(dir, "tasks.json");
            
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                Type type = new TypeToken<List<Task>>(){}.getType();
                List<Task> loadedTasks = gson.fromJson(reader, type);
                reader.close();
                
                if (loadedTasks != null) {
                    tasks.clear();
                    tasks.addAll(loadedTasks);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading tasks from file", e);
        }
    }
    
    // ==================== SAMPLE TASKS ====================
    
    private void createSampleTasks() {
        // Sample Task 1: Simple Click
        Task clickTask = new Task("Simple Click", "Performs a simple click at coordinates");
        Task.TaskStep clickStep = new Task.TaskStep(Task.StepType.CLICK);
        clickStep.setX(500);
        clickStep.setY(1000);
        clickStep.setName("Click Center");
        clickTask.addStep(clickStep);
        tasks.add(clickTask);
        
        // Sample Task 2: Swipe
        Task swipeTask = new Task("Swipe Up", "Performs a swipe gesture");
        Task.TaskStep swipeStep = new Task.TaskStep(Task.StepType.SWIPE);
        swipeStep.setX(500);
        swipeStep.setY(1500);
        swipeStep.setEndX(500);
        swipeStep.setEndY(500);
        swipeStep.setDuration(300);
        swipeStep.setName("Swipe Up");
        swipeTask.addStep(swipeStep);
        tasks.add(swipeTask);
        
        // Sample Task 3: Color Recognition Click
        Task colorTask = new Task("Color Recognition", "Clicks when specific color is found");
        Task.TaskStep colorStep = new Task.TaskStep(Task.StepType.COLOR_RECOGNITION);
        Task.RecognitionParams colorParams = new Task.RecognitionParams();
        colorParams.setTargetColor(0xFF00FF00); // Green
        colorParams.setColorTolerance(15);
        colorParams.setScanX(0);
        colorParams.setScanY(0);
        colorParams.setScanWidth(1080);
        colorParams.setScanHeight(1920);
        colorStep.setRecognitionParams(colorParams);
        colorStep.setName("Find Green Color");
        colorTask.addStep(colorStep);
        tasks.add(colorTask);
        
        // Sample Task 4: Text Recognition
        Task textTask = new Task("Text Recognition", "Clicks when text is found");
        Task.TaskStep textStep = new Task.TaskStep(Task.StepType.TEXT_RECOGNITION);
        Task.RecognitionParams textParams = new Task.RecognitionParams();
        textParams.setTargetText("OK");
        textParams.setUseOCR(true);
        textParams.setConfidenceThreshold(0.85);
        textStep.setRecognitionParams(textParams);
        textStep.setName("Find OK Button");
        textTask.addStep(textStep);
        tasks.add(textTask);
        
        // Sample Task 5: Loop with Condition
        Task loopTask = new Task("Loop Task", "Repeats actions with condition");
        loopTask.setRepeatCount(5);
        loopTask.setDelayBetweenRepeats(1000);
        
        Task.TaskStep waitStep = new Task.TaskStep(Task.StepType.WAIT);
        waitStep.setDuration(500);
        waitStep.setName("Wait 500ms");
        loopTask.addStep(waitStep);
        
        Task.TaskStep loopClickStep = new Task.TaskStep(Task.StepType.CLICK);
        loopClickStep.setX(500);
        loopClickStep.setY(500);
        loopClickStep.setName("Click in Loop");
        loopTask.addStep(loopClickStep);
        
        tasks.add(loopTask);
        
        saveTasks();
    }
    
    // ==================== IMPORT/EXPORT ====================
    
    public boolean exportTask(Task task, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(task));
            writer.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error exporting task", e);
            return false;
        }
    }
    
    public Task importTask(File file) {
        try {
            FileReader reader = new FileReader(file);
            Task task = gson.fromJson(reader, Task.class);
            reader.close();
            
            // Generate new ID to avoid conflicts
            task.setId(java.util.UUID.randomUUID().toString());
            task.setName(task.getName() + " (Imported)");
            
            addTask(task);
            return task;
        } catch (IOException e) {
            Log.e(TAG, "Error importing task", e);
            return null;
        }
    }
    
    // ==================== UTILITY ====================
    
    public void clearAllTasks() {
        tasks.clear();
        saveTasks();
    }
    
    public int getTaskCount() {
        return tasks.size();
    }
    
    public void duplicateTask(String taskId) {
        Task original = getTask(taskId);
        if (original != null) {
            String json = gson.toJson(original);
            Task duplicate = gson.fromJson(json, Task.class);
            duplicate.setId(java.util.UUID.randomUUID().toString());
            duplicate.setName(original.getName() + " (Copy)");
            addTask(duplicate);
        }
    }
}
