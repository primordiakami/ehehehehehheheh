package com.aiautoclicker.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiautoclicker.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    
    private List<Task> tasks;
    private OnTaskClickListener listener;
    
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
    }
    
    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }
    
    public void updateTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, listener);
    }
    
    @Override
    public int getItemCount() {
        return tasks.size();
    }
    
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskName;
        private TextView tvTaskDescription;
        private TextView tvStepCount;
        private View colorIndicator;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        private ImageButton btnRun;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvStepCount = itemView.findViewById(R.id.tv_step_count);
            colorIndicator = itemView.findViewById(R.id.color_indicator);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnRun = itemView.findViewById(R.id.btn_run);
        }
        
        public void bind(Task task, OnTaskClickListener listener) {
            tvTaskName.setText(task.getName());
            tvTaskDescription.setText(task.getDescription());
            tvStepCount.setText(task.getSteps().size() + " steps");
            
            // Set color indicator based on first step type
            if (!task.getSteps().isEmpty()) {
                int colorRes = getColorForStepType(task.getSteps().get(0).getType());
                colorIndicator.setBackgroundResource(colorRes);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
            
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskEdit(task);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskDelete(task);
                }
            });
            
            btnRun.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }
        
        private int getColorForStepType(Task.StepType type) {
            switch (type) {
                case CLICK:
                case LONG_CLICK:
                    return R.color.task_click;
                case SWIPE:
                case SCROLL:
                    return R.color.task_swipe;
                case WAIT:
                    return R.color.task_wait;
                case CONDITION:
                    return R.color.task_condition;
                case LOOP:
                    return R.color.task_loop;
                case PYTHON_CODE:
                    return R.color.task_python;
                default:
                    return R.color.primary;
            }
        }
    }
}
