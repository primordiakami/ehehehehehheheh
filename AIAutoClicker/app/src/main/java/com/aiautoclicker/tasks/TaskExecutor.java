package com.aiautoclicker.tasks;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import com.aiautoclicker.python.PythonBridge;
import com.aiautoclicker.recognition.RecognitionManager;
import com.aiautoclicker.service.AutoClickerAccessibilityService;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskExecutor {
    private static final String TAG = "TaskExecutor";
    
    private AutoClickerAccessibilityService service;
    private RecognitionManager recognitionManager;
    private PythonBridge pythonBridge;
    private AtomicBoolean isRunning;
    private boolean isPaused;
    
    public TaskExecutor(AutoClickerAccessibilityService service) {
        this.service = service;
        this.recognitionManager = new RecognitionManager(service);
        this.pythonBridge = new PythonBridge();
        this.isRunning = new AtomicBoolean(false);
        this.isPaused = false;
    }
    
    public void execute(Task task) {
        if (task == null || task.getSteps().isEmpty()) {
            Log.w(TAG, "Empty task, nothing to execute");
            return;
        }
        
        isRunning.set(true);
        isPaused = false;
        
        Log.d(TAG, "Executing task: " + task.getName());
        
        for (int repeat = 0; repeat < task.getRepeatCount() && isRunning.get(); repeat++) {
            if (repeat > 0 && task.getDelayBetweenRepeats() > 0) {
                sleep(task.getDelayBetweenRepeats());
            }
            
            for (Task.TaskStep step : task.getSteps()) {
                if (!isRunning.get()) break;
                
                // Check for pause
                while (isPaused && isRunning.get()) {
                    sleep(100);
                }
                
                executeStep(step);
            }
        }
        
        isRunning.set(false);
        Log.d(TAG, "Task execution completed: " + task.getName());
    }
    
    private void executeStep(Task.TaskStep step) {
        Log.d(TAG, "Executing step: " + step.getName() + " (" + step.getType() + ")");
        
        switch (step.getType()) {
            case CLICK:
                executeClick(step);
                break;
            case LONG_CLICK:
                executeLongClick(step);
                break;
            case SWIPE:
                executeSwipe(step);
                break;
            case SCROLL:
                executeScroll(step);
                break;
            case WAIT:
                executeWait(step);
                break;
            case TEXT_INPUT:
                executeTextInput(step);
                break;
            case PYTHON_CODE:
                executePythonCode(step);
                break;
            case CONDITION:
                executeCondition(step);
                break;
            case LOOP:
                executeLoop(step);
                break;
            case COLOR_RECOGNITION:
                executeColorRecognition(step);
                break;
            case TEXT_RECOGNITION:
                executeTextRecognition(step);
                break;
            case IMAGE_RECOGNITION:
                executeImageRecognition(step);
                break;
            case PATTERN_RECOGNITION:
                executePatternRecognition(step);
                break;
            case EVENT_RECOGNITION:
                executeEventRecognition(step);
                break;
            default:
                Log.w(TAG, "Unknown step type: " + step.getType());
        }
        
        // Execute delay after step
        if (step.getDelay() > 0) {
            sleep(step.getDelay());
        }
    }
    
    private void executeClick(Task.TaskStep step) {
        service.performClick(step.getX(), step.getY());
    }
    
    private void executeLongClick(Task.TaskStep step) {
        service.performLongClick(step.getX(), step.getY(), step.getDuration());
    }
    
    private void executeSwipe(Task.TaskStep step) {
        service.performSwipe(step.getX(), step.getY(), step.getEndX(), step.getEndY(), step.getDuration());
    }
    
    private void executeScroll(Task.TaskStep step) {
        service.performScroll(step.getX(), step.getY(), (int) step.getDuration());
    }
    
    private void executeWait(Task.TaskStep step) {
        sleep(step.getDuration());
    }
    
    private void executeTextInput(Task.TaskStep step) {
        // Use accessibility service to find text field and input text
        service.findAndClickByText(step.getText());
    }
    
    private void executePythonCode(Task.TaskStep step) {
        try {
            String code = step.getPythonCode();
            if (code != null && !code.isEmpty()) {
                pythonBridge.executeCode(code);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing Python code", e);
        }
    }
    
    private void executeCondition(Task.TaskStep step) {
        boolean conditionMet = false;
        Task.RecognitionParams params = step.getRecognitionParams();
        
        if (params == null) return;
        
        switch (step.getConditionType()) {
            case COLOR_FOUND:
                conditionMet = recognitionManager.findColor(
                    params.getTargetColor(), 
                    params.getColorTolerance(),
                    params.getScanX(), params.getScanY(),
                    params.getScanWidth(), params.getScanHeight()
                ) != null;
                break;
            case COLOR_NOT_FOUND:
                conditionMet = recognitionManager.findColor(
                    params.getTargetColor(),
                    params.getColorTolerance(),
                    params.getScanX(), params.getScanY(),
                    params.getScanWidth(), params.getScanHeight()
                ) == null;
                break;
            case TEXT_FOUND:
                conditionMet = recognitionManager.findText(
                    params.getTargetText(),
                    params.getConfidenceThreshold()
                ) != null;
                break;
            case TEXT_NOT_FOUND:
                conditionMet = recognitionManager.findText(
                    params.getTargetText(),
                    params.getConfidenceThreshold()
                ) == null;
                break;
            case CUSTOM_PYTHON:
                conditionMet = evaluatePythonCondition(params.getTargetText());
                break;
            default:
                Log.w(TAG, "Unknown condition type: " + step.getConditionType());
        }
        
        Log.d(TAG, "Condition result: " + conditionMet);
    }
    
    private void executeLoop(Task.TaskStep step) {
        List<Task.TaskStep> loopSteps = step.getLoopSteps();
        if (loopSteps == null || loopSteps.isEmpty()) return;
        
        for (int i = 0; i < step.getLoopCount() && isRunning.get(); i++) {
            for (Task.TaskStep loopStep : loopSteps) {
                if (!isRunning.get()) break;
                executeStep(loopStep);
            }
        }
    }
    
    private void executeColorRecognition(Task.TaskStep step) {
        Task.RecognitionParams params = step.getRecognitionParams();
        if (params == null) return;
        
        long startTime = System.currentTimeMillis();
        long timeout = params.getScanInterval() * 10; // 10 scan intervals timeout
        
        while (isRunning.get()) {
            int[] location = recognitionManager.findColor(
                params.getTargetColor(),
                params.getColorTolerance(),
                params.getScanX(), params.getScanY(),
                params.getScanWidth(), params.getScanHeight()
            );
            
            if (location != null) {
                Log.d(TAG, "Color found at: " + location[0] + ", " + location[1]);
                service.performClick(location[0], location[1]);
                break;
            }
            
            if (System.currentTimeMillis() - startTime > timeout) {
                Log.w(TAG, "Color recognition timeout");
                break;
            }
            
            sleep(params.getScanInterval());
        }
    }
    
    private void executeTextRecognition(Task.TaskStep step) {
        Task.RecognitionParams params = step.getRecognitionParams();
        if (params == null) return;
        
        long startTime = System.currentTimeMillis();
        long timeout = params.getScanInterval() * 20;
        
        while (isRunning.get()) {
            int[] location = recognitionManager.findText(
                params.getTargetText(),
                params.getConfidenceThreshold()
            );
            
            if (location != null) {
                Log.d(TAG, "Text found at: " + location[0] + ", " + location[1]);
                service.performClick(location[0], location[1]);
                break;
            }
            
            if (System.currentTimeMillis() - startTime > timeout) {
                Log.w(TAG, "Text recognition timeout");
                break;
            }
            
            sleep(params.getScanInterval());
        }
    }
    
    private void executeImageRecognition(Task.TaskStep step) {
        Task.RecognitionParams params = step.getRecognitionParams();
        if (params == null || params.getImagePath() == null) return;
        
        long startTime = System.currentTimeMillis();
        long timeout = params.getScanInterval() * 20;
        
        while (isRunning.get()) {
            int[] location = recognitionManager.findImage(
                params.getImagePath(),
                params.getConfidenceThreshold()
            );
            
            if (location != null) {
                Log.d(TAG, "Image found at: " + location[0] + ", " + location[1]);
                service.performClick(location[0], location[1]);
                break;
            }
            
            if (System.currentTimeMillis() - startTime > timeout) {
                Log.w(TAG, "Image recognition timeout");
                break;
            }
            
            sleep(params.getScanInterval());
        }
    }
    
    private void executePatternRecognition(Task.TaskStep step) {
        // Pattern recognition is handled by the PatternRecorder
        // This would compare recorded patterns with current screen
        Log.d(TAG, "Pattern recognition step");
    }
    
    private void executeEventRecognition(Task.TaskStep step) {
        Task.RecognitionParams params = step.getRecognitionParams();
        if (params == null) return;
        
        // Event recognition waits for specific accessibility events
        Log.d(TAG, "Event recognition: " + params.getEventType());
    }
    
    private boolean evaluatePythonCondition(String code) {
        try {
            Python py = Python.getInstance();
            PyObject result = py.getModule("builtins").callAttr("eval", code);
            return result.toBoolean();
        } catch (Exception e) {
            Log.e(TAG, "Error evaluating Python condition", e);
            return false;
        }
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void stop() {
        isRunning.set(false);
        isPaused = false;
    }
    
    public void pause() {
        isPaused = true;
    }
    
    public void resume() {
        isPaused = false;
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    public boolean isPaused() {
        return isPaused;
    }
}
