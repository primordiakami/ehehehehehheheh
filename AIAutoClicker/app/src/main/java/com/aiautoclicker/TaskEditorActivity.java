package com.aiautoclicker;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.aiautoclicker.tasks.Task;
import com.aiautoclicker.tasks.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskEditorActivity extends AppCompatActivity {
    
    private EditText etTaskName;
    private EditText etTaskDescription;
    private EditText etRepeatCount;
    private EditText etDelayBetweenRepeats;
    private LinearLayout stepsContainer;
    private Button btnAddStep;
    private Button btnSaveTask;
    private Button btnCancel;
    
    private TaskManager taskManager;
    private Task currentTask;
    private List<Task.TaskStep> steps;
    private boolean isEditing = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);
        
        taskManager = TaskManager.getInstance(this);
        steps = new ArrayList<>();
        
        initializeViews();
        setupListeners();
        
        // Check if editing existing task
        String taskId = getIntent().getStringExtra("task_id");
        if (taskId != null) {
            currentTask = taskManager.getTask(taskId);
            if (currentTask != null) {
                isEditing = true;
                loadTaskData();
            }
        } else {
            currentTask = new Task();
        }
    }
    
    private void initializeViews() {
        etTaskName = findViewById(R.id.et_task_name);
        etTaskDescription = findViewById(R.id.et_task_description);
        etRepeatCount = findViewById(R.id.et_repeat_count);
        etDelayBetweenRepeats = findViewById(R.id.et_delay_between_repeats);
        stepsContainer = findViewById(R.id.steps_container);
        btnAddStep = findViewById(R.id.btn_add_step);
        btnSaveTask = findViewById(R.id.btn_save_task);
        btnCancel = findViewById(R.id.btn_cancel);
    }
    
    private void setupListeners() {
        btnAddStep.setOnClickListener(v -> showAddStepDialog());
        btnSaveTask.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void loadTaskData() {
        etTaskName.setText(currentTask.getName());
        etTaskDescription.setText(currentTask.getDescription());
        etRepeatCount.setText(String.valueOf(currentTask.getRepeatCount()));
        etDelayBetweenRepeats.setText(String.valueOf(currentTask.getDelayBetweenRepeats()));
        
        steps.clear();
        steps.addAll(currentTask.getSteps());
        refreshStepsList();
    }
    
    private void refreshStepsList() {
        stepsContainer.removeAllViews();
        
        for (int i = 0; i < steps.size(); i++) {
            Task.TaskStep step = steps.get(i);
            View stepView = createStepView(step, i);
            stepsContainer.addView(stepView);
        }
    }
    
    private View createStepView(Task.TaskStep step, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_step, stepsContainer, false);
        
        TextView tvStepNumber = view.findViewById(R.id.tv_step_number);
        TextView tvStepType = view.findViewById(R.id.tv_step_type);
        TextView tvStepDetails = view.findViewById(R.id.tv_step_details);
        View colorIndicator = view.findViewById(R.id.step_color_indicator);
        Button btnEdit = view.findViewById(R.id.btn_edit_step);
        Button btnDelete = view.findViewById(R.id.btn_delete_step);
        Button btnMoveUp = view.findViewById(R.id.btn_move_up);
        Button btnMoveDown = view.findViewById(R.id.btn_move_down);
        
        tvStepNumber.setText(String.valueOf(index + 1));
        tvStepType.setText(step.getType().toString());
        tvStepDetails.setText(getStepDetails(step));
        colorIndicator.setBackgroundColor(getStepColor(step.getType()));
        
        btnEdit.setOnClickListener(v -> showEditStepDialog(step, index));
        btnDelete.setOnClickListener(v -> deleteStep(index));
        btnMoveUp.setOnClickListener(v -> moveStepUp(index));
        btnMoveDown.setOnClickListener(v -> moveStepDown(index));
        
        // Disable move buttons at boundaries
        btnMoveUp.setEnabled(index > 0);
        btnMoveDown.setEnabled(index < steps.size() - 1);
        
        return view;
    }
    
    private String getStepDetails(Task.TaskStep step) {
        StringBuilder details = new StringBuilder();
        
        switch (step.getType()) {
            case CLICK:
            case LONG_CLICK:
                details.append("(").append(step.getX()).append(", ").append(step.getY()).append(")");
                break;
            case SWIPE:
                details.append("(").append(step.getX()).append(", ").append(step.getY()).append(") -> (")
                       .append(step.getEndX()).append(", ").append(step.getEndY()).append(")");
                break;
            case WAIT:
                details.append(step.getDuration()).append("ms");
                break;
            case TEXT_INPUT:
                details.append("\"").append(step.getText()).append("\"");
                break;
            case PYTHON_CODE:
                String code = step.getPythonCode();
                if (code != null && code.length() > 30) {
                    code = code.substring(0, 30) + "...";
                }
                details.append(code);
                break;
            case COLOR_RECOGNITION:
            case TEXT_RECOGNITION:
            case IMAGE_RECOGNITION:
                if (step.getRecognitionParams() != null) {
                    details.append("Recognition");
                }
                break;
            default:
                details.append("Step");
        }
        
        if (step.getDelay() > 0) {
            details.append(" | Delay: ").append(step.getDelay()).append("ms");
        }
        
        return details.toString();
    }
    
    private int getStepColor(Task.StepType type) {
        switch (type) {
            case CLICK:
                return getColor(R.color.task_click);
            case LONG_CLICK:
                return getColor(R.color.task_click);
            case SWIPE:
                return getColor(R.color.task_swipe);
            case SCROLL:
                return getColor(R.color.task_swipe);
            case WAIT:
                return getColor(R.color.task_wait);
            case CONDITION:
                return getColor(R.color.task_condition);
            case LOOP:
                return getColor(R.color.task_loop);
            case PYTHON_CODE:
                return getColor(R.color.task_python);
            case COLOR_RECOGNITION:
            case TEXT_RECOGNITION:
            case IMAGE_RECOGNITION:
            case PATTERN_RECOGNITION:
            case EVENT_RECOGNITION:
                return getColor(R.color.secondary);
            default:
                return getColor(R.color.primary);
        }
    }
    
    private void showAddStepDialog() {
        showStepDialog(null, -1);
    }
    
    private void showEditStepDialog(Task.TaskStep step, int index) {
        showStepDialog(step, index);
    }
    
    private void showStepDialog(Task.TaskStep existingStep, int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_step_editor, null);
        builder.setView(dialogView);
        
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_step_type);
        EditText etX = dialogView.findViewById(R.id.et_x);
        EditText etY = dialogView.findViewById(R.id.et_y);
        EditText etEndX = dialogView.findViewById(R.id.et_end_x);
        EditText etEndY = dialogView.findViewById(R.id.et_end_y);
        EditText etDuration = dialogView.findViewById(R.id.et_duration);
        EditText etDelay = dialogView.findViewById(R.id.et_delay);
        EditText etText = dialogView.findViewById(R.id.et_text);
        EditText etPythonCode = dialogView.findViewById(R.id.et_python_code);
        LinearLayout layoutCoordinates = dialogView.findViewById(R.id.layout_coordinates);
        LinearLayout layoutEndCoordinates = dialogView.findViewById(R.id.layout_end_coordinates);
        LinearLayout layoutText = dialogView.findViewById(R.id.layout_text);
        LinearLayout layoutPython = dialogView.findViewById(R.id.layout_python);
        
        // Setup spinner
        ArrayAdapter<Task.StepType> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, Task.StepType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        
        // Load existing data if editing
        if (existingStep != null) {
            spinnerType.setSelection(existingStep.getType().ordinal());
            etX.setText(String.valueOf(existingStep.getX()));
            etY.setText(String.valueOf(existingStep.getY()));
            etEndX.setText(String.valueOf(existingStep.getEndX()));
            etEndY.setText(String.valueOf(existingStep.getEndY()));
            etDuration.setText(String.valueOf(existingStep.getDuration()));
            etDelay.setText(String.valueOf(existingStep.getDelay()));
            etText.setText(existingStep.getText());
            etPythonCode.setText(existingStep.getPythonCode());
        }
        
        // Update visibility based on step type
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Task.StepType type = Task.StepType.values()[position];
                updateFieldVisibility(type, layoutCoordinates, layoutEndCoordinates, layoutText, layoutPython);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Trigger initial visibility update
        Task.StepType initialType = existingStep != null ? existingStep.getType() : Task.StepType.CLICK;
        updateFieldVisibility(initialType, layoutCoordinates, layoutEndCoordinates, layoutText, layoutPython);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            Task.TaskStep step = existingStep != null ? existingStep : new Task.TaskStep();
            
            step.setType(Task.StepType.values()[spinnerType.getSelectedItemPosition()]);
            
            try {
                step.setX(Integer.parseInt(etX.getText().toString()));
            } catch (NumberFormatException e) {
                step.setX(0);
            }
            
            try {
                step.setY(Integer.parseInt(etY.getText().toString()));
            } catch (NumberFormatException e) {
                step.setY(0);
            }
            
            try {
                step.setEndX(Integer.parseInt(etEndX.getText().toString()));
            } catch (NumberFormatException e) {
                step.setEndX(0);
            }
            
            try {
                step.setEndY(Integer.parseInt(etEndY.getText().toString()));
            } catch (NumberFormatException e) {
                step.setEndY(0);
            }
            
            try {
                step.setDuration(Long.parseLong(etDuration.getText().toString()));
            } catch (NumberFormatException e) {
                step.setDuration(100);
            }
            
            try {
                step.setDelay(Long.parseLong(etDelay.getText().toString()));
            } catch (NumberFormatException e) {
                step.setDelay(0);
            }
            
            step.setText(etText.getText().toString());
            step.setPythonCode(etPythonCode.getText().toString());
            
            if (existingStep == null) {
                steps.add(step);
            }
            
            refreshStepsList();
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.show();
    }
    
    private void updateFieldVisibility(Task.StepType type, LinearLayout layoutCoordinates, 
                                       LinearLayout layoutEndCoordinates, LinearLayout layoutText, 
                                       LinearLayout layoutPython) {
        layoutCoordinates.setVisibility(View.GONE);
        layoutEndCoordinates.setVisibility(View.GONE);
        layoutText.setVisibility(View.GONE);
        layoutPython.setVisibility(View.GONE);
        
        switch (type) {
            case CLICK:
            case LONG_CLICK:
            case SCROLL:
                layoutCoordinates.setVisibility(View.VISIBLE);
                break;
            case SWIPE:
                layoutCoordinates.setVisibility(View.VISIBLE);
                layoutEndCoordinates.setVisibility(View.VISIBLE);
                break;
            case TEXT_INPUT:
                layoutText.setVisibility(View.VISIBLE);
                break;
            case PYTHON_CODE:
                layoutPython.setVisibility(View.VISIBLE);
                break;
            case COLOR_RECOGNITION:
            case TEXT_RECOGNITION:
            case IMAGE_RECOGNITION:
                layoutCoordinates.setVisibility(View.VISIBLE);
                break;
        }
    }
    
    private void deleteStep(int index) {
        if (index >= 0 && index < steps.size()) {
            steps.remove(index);
            refreshStepsList();
        }
    }
    
    private void moveStepUp(int index) {
        if (index > 0) {
            Task.TaskStep step = steps.remove(index);
            steps.add(index - 1, step);
            refreshStepsList();
        }
    }
    
    private void moveStepDown(int index) {
        if (index < steps.size() - 1) {
            Task.TaskStep step = steps.remove(index);
            steps.add(index + 1, step);
            refreshStepsList();
        }
    }
    
    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        
        if (name.isEmpty()) {
            etTaskName.setError("Task name is required");
            return;
        }
        
        if (steps.isEmpty()) {
            Toast.makeText(this, "Please add at least one step", Toast.LENGTH_SHORT).show();
            return;
        }
        
        currentTask.setName(name);
        currentTask.setDescription(description);
        
        try {
            currentTask.setRepeatCount(Integer.parseInt(etRepeatCount.getText().toString()));
        } catch (NumberFormatException e) {
            currentTask.setRepeatCount(1);
        }
        
        try {
            currentTask.setDelayBetweenRepeats(Long.parseLong(etDelayBetweenRepeats.getText().toString()));
        } catch (NumberFormatException e) {
            currentTask.setDelayBetweenRepeats(0);
        }
        
        currentTask.setSteps(new ArrayList<>(steps));
        
        if (isEditing) {
            taskManager.updateTask(currentTask);
        } else {
            taskManager.addTask(currentTask);
        }
        
        Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
