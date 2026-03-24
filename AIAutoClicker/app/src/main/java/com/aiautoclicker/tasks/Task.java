package com.aiautoclicker.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {
    
    public enum StepType {
        CLICK,
        LONG_CLICK,
        SWIPE,
        WAIT,
        CONDITION,
        LOOP,
        PYTHON_CODE,
        TEXT_INPUT,
        KEY_EVENT,
        SCROLL,
        MULTI_TOUCH,
        COLOR_RECOGNITION,
        TEXT_RECOGNITION,
        IMAGE_RECOGNITION,
        PATTERN_RECOGNITION,
        EVENT_RECOGNITION
    }
    
    public enum ConditionType {
        COLOR_FOUND,
        COLOR_NOT_FOUND,
        TEXT_FOUND,
        TEXT_NOT_FOUND,
        IMAGE_FOUND,
        IMAGE_NOT_FOUND,
        EVENT_OCCURRED,
        TIME_ELAPSED,
        CUSTOM_PYTHON
    }
    
    private String id;
    private String name;
    private String description;
    private List<TaskStep> steps;
    private long createdAt;
    private long lastModified;
    private boolean isEnabled;
    private int repeatCount;
    private long delayBetweenRepeats;
    
    public Task() {
        this.id = UUID.randomUUID().toString();
        this.steps = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.isEnabled = true;
        this.repeatCount = 1;
        this.delayBetweenRepeats = 0;
    }
    
    public Task(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.lastModified = System.currentTimeMillis();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.lastModified = System.currentTimeMillis();
    }
    
    public List<TaskStep> getSteps() {
        return steps;
    }
    
    public void setSteps(List<TaskStep> steps) {
        this.steps = steps;
        this.lastModified = System.currentTimeMillis();
    }
    
    public void addStep(TaskStep step) {
        steps.add(step);
        this.lastModified = System.currentTimeMillis();
    }
    
    public void removeStep(int index) {
        if (index >= 0 && index < steps.size()) {
            steps.remove(index);
            this.lastModified = System.currentTimeMillis();
        }
    }
    
    public void moveStepUp(int index) {
        if (index > 0 && index < steps.size()) {
            TaskStep step = steps.remove(index);
            steps.add(index - 1, step);
            this.lastModified = System.currentTimeMillis();
        }
    }
    
    public void moveStepDown(int index) {
        if (index >= 0 && index < steps.size() - 1) {
            TaskStep step = steps.remove(index);
            steps.add(index + 1, step);
            this.lastModified = System.currentTimeMillis();
        }
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    public int getRepeatCount() {
        return repeatCount;
    }
    
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = Math.max(1, repeatCount);
    }
    
    public long getDelayBetweenRepeats() {
        return delayBetweenRepeats;
    }
    
    public void setDelayBetweenRepeats(long delayBetweenRepeats) {
        this.delayBetweenRepeats = Math.max(0, delayBetweenRepeats);
    }
    
    // ==================== TASK STEP CLASS ====================
    
    public static class TaskStep {
        private StepType type;
        private String name;
        private int x;
        private int y;
        private int endX;
        private int endY;
        private long duration;
        private long delay;
        private String text;
        private String pythonCode;
        private ConditionType conditionType;
        private String conditionValue;
        private int loopCount;
        private List<TaskStep> loopSteps;
        private RecognitionParams recognitionParams;
        
        public TaskStep() {
            this.loopSteps = new ArrayList<>();
            this.duration = 100;
            this.delay = 0;
            this.loopCount = 1;
        }
        
        public TaskStep(StepType type) {
            this();
            this.type = type;
        }
        
        // Getters and Setters
        
        public StepType getType() {
            return type;
        }
        
        public void setType(StepType type) {
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getEndX() {
            return endX;
        }
        
        public void setEndX(int endX) {
            this.endX = endX;
        }
        
        public int getEndY() {
            return endY;
        }
        
        public void setEndY(int endY) {
            this.endY = endY;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public void setDuration(long duration) {
            this.duration = duration;
        }
        
        public long getDelay() {
            return delay;
        }
        
        public void setDelay(long delay) {
            this.delay = delay;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getPythonCode() {
            return pythonCode;
        }
        
        public void setPythonCode(String pythonCode) {
            this.pythonCode = pythonCode;
        }
        
        public ConditionType getConditionType() {
            return conditionType;
        }
        
        public void setConditionType(ConditionType conditionType) {
            this.conditionType = conditionType;
        }
        
        public String getConditionValue() {
            return conditionValue;
        }
        
        public void setConditionValue(String conditionValue) {
            this.conditionValue = conditionValue;
        }
        
        public int getLoopCount() {
            return loopCount;
        }
        
        public void setLoopCount(int loopCount) {
            this.loopCount = loopCount;
        }
        
        public List<TaskStep> getLoopSteps() {
            return loopSteps;
        }
        
        public void setLoopSteps(List<TaskStep> loopSteps) {
            this.loopSteps = loopSteps;
        }
        
        public RecognitionParams getRecognitionParams() {
            return recognitionParams;
        }
        
        public void setRecognitionParams(RecognitionParams recognitionParams) {
            this.recognitionParams = recognitionParams;
        }
    }
    
    // ==================== RECOGNITION PARAMS CLASS ====================
    
    public static class RecognitionParams {
        private int targetColor;
        private int colorTolerance;
        private String targetText;
        private String imagePath;
        private int scanX;
        private int scanY;
        private int scanWidth;
        private int scanHeight;
        private long scanInterval;
        private double confidenceThreshold;
        private boolean useOCR;
        private String eventType;
        
        public RecognitionParams() {
            this.colorTolerance = 10;
            this.scanInterval = 500;
            this.confidenceThreshold = 0.8;
            this.scanWidth = 100;
            this.scanHeight = 100;
        }
        
        // Getters and Setters
        
        public int getTargetColor() {
            return targetColor;
        }
        
        public void setTargetColor(int targetColor) {
            this.targetColor = targetColor;
        }
        
        public int getColorTolerance() {
            return colorTolerance;
        }
        
        public void setColorTolerance(int colorTolerance) {
            this.colorTolerance = colorTolerance;
        }
        
        public String getTargetText() {
            return targetText;
        }
        
        public void setTargetText(String targetText) {
            this.targetText = targetText;
        }
        
        public String getImagePath() {
            return imagePath;
        }
        
        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }
        
        public int getScanX() {
            return scanX;
        }
        
        public void setScanX(int scanX) {
            this.scanX = scanX;
        }
        
        public int getScanY() {
            return scanY;
        }
        
        public void setScanY(int scanY) {
            this.scanY = scanY;
        }
        
        public int getScanWidth() {
            return scanWidth;
        }
        
        public void setScanWidth(int scanWidth) {
            this.scanWidth = scanWidth;
        }
        
        public int getScanHeight() {
            return scanHeight;
        }
        
        public void setScanHeight(int scanHeight) {
            this.scanHeight = scanHeight;
        }
        
        public long getScanInterval() {
            return scanInterval;
        }
        
        public void setScanInterval(long scanInterval) {
            this.scanInterval = scanInterval;
        }
        
        public double getConfidenceThreshold() {
            return confidenceThreshold;
        }
        
        public void setConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
        }
        
        public boolean isUseOCR() {
            return useOCR;
        }
        
        public void setUseOCR(boolean useOCR) {
            this.useOCR = useOCR;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
    }
}
