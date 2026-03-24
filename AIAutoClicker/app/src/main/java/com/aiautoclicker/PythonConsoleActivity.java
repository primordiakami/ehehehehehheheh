package com.aiautoclicker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aiautoclicker.python.PythonBridge;
import com.chaquo.python.PyObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PythonConsoleActivity extends AppCompatActivity {
    
    private TextView tvConsoleOutput;
    private EditText etCodeInput;
    private ScrollView scrollConsole;
    private Button btnRun;
    private Button btnClear;
    private Button btnSave;
    private Button btnLoad;
    private Button btnHelp;
    
    private PythonBridge pythonBridge;
    private List<String> commandHistory;
    private int historyIndex;
    private StringBuilder consoleBuffer;
    
    private static final String SCRIPTS_DIR = "python_scripts";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python_console);
        
        pythonBridge = new PythonBridge();
        commandHistory = new ArrayList<>();
        historyIndex = -1;
        consoleBuffer = new StringBuilder();
        
        initializeViews();
        setupListeners();
        
        // Print welcome message
        appendToConsole(getString(R.string.console_welcome), getColor(R.color.console_info));
    }
    
    private void initializeViews() {
        tvConsoleOutput = findViewById(R.id.tv_console_output);
        etCodeInput = findViewById(R.id.et_code_input);
        scrollConsole = findViewById(R.id.scroll_console);
        btnRun = findViewById(R.id.btn_run);
        btnClear = findViewById(R.id.btn_clear);
        btnSave = findViewById(R.id.btn_save);
        btnLoad = findViewById(R.id.btn_load);
        btnHelp = findViewById(R.id.btn_help);
    }
    
    private void setupListeners() {
        btnRun.setOnClickListener(v -> executeCode());
        
        btnClear.setOnClickListener(v -> clearConsole());
        
        btnSave.setOnClickListener(v -> showSaveDialog());
        
        btnLoad.setOnClickListener(v -> showLoadDialog());
        
        btnHelp.setOnClickListener(v -> showHelp());
        
        etCodeInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                executeCode();
                return true;
            }
            return false;
        });
        
        // Handle up/down arrows for command history
        etCodeInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    navigateHistory(-1);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    navigateHistory(1);
                    return true;
                }
            }
            return false;
        });
    }
    
    private void executeCode() {
        String code = etCodeInput.getText().toString().trim();
        if (code.isEmpty()) return;
        
        // Add to history
        commandHistory.add(code);
        historyIndex = commandHistory.size();
        
        // Show input in console
        appendToConsole(">>> " + code + "\n", getColor(R.color.console_text));
        
        // Clear input
        etCodeInput.setText("");
        
        // Execute in background
        new Thread(() -> {
            try {
                PyObject result = pythonBridge.executeCode(code);
                
                runOnUiThread(() -> {
                    if (result != null && !result.isNone()) {
                        String output = result.toString();
                        if (!output.isEmpty() && !output.equals("None")) {
                            appendToConsole(output + "\n", getColor(R.color.console_text));
                        }
                    }
                    
                    // Also check for any print output
                    String printOutput = pythonBridge.getOutput();
                    if (!printOutput.isEmpty()) {
                        appendToConsole(printOutput + "\n", getColor(R.color.console_text));
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToConsole("Error: " + e.getMessage() + "\n", getColor(R.color.console_error));
                });
            }
        }).start();
    }
    
    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) return;
        
        historyIndex += direction;
        
        if (historyIndex < 0) {
            historyIndex = 0;
        } else if (historyIndex >= commandHistory.size()) {
            historyIndex = commandHistory.size();
            etCodeInput.setText("");
            return;
        }
        
        etCodeInput.setText(commandHistory.get(historyIndex));
        etCodeInput.setSelection(etCodeInput.getText().length());
    }
    
    private void appendToConsole(String text, int color) {
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        tvConsoleOutput.append(spannable);
        consoleBuffer.append(text);
        
        // Auto-scroll to bottom
        scrollConsole.post(() -> scrollConsole.fullScroll(View.FOCUS_DOWN));
    }
    
    private void clearConsole() {
        tvConsoleOutput.setText("");
        consoleBuffer.setLength(0);
        appendToConsole(getString(R.string.console_welcome), getColor(R.color.console_info));
    }
    
    private void showSaveDialog() {
        EditText input = new EditText(this);
        input.setHint("Script name");
        
        new AlertDialog.Builder(this)
            .setTitle("Save Script")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    saveScript(name);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void saveScript(String name) {
        try {
            File dir = new File(getExternalFilesDir(null), SCRIPTS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, name + ".py");
            FileWriter writer = new FileWriter(file);
            
            // Save current console buffer as the script
            writer.write(consoleBuffer.toString());
            writer.close();
            
            Toast.makeText(this, "Script saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Error saving script: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLoadDialog() {
        File dir = new File(getExternalFilesDir(null), SCRIPTS_DIR);
        if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
            Toast.makeText(this, "No saved scripts", Toast.LENGTH_SHORT).show();
            return;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".py"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "No saved scripts", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName().replace(".py", "");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Load Script")
            .setItems(fileNames, (dialog, which) -> {
                loadScript(files[which]);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void loadScript(File file) {
        try {
            FileReader reader = new FileReader(file);
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
            reader.close();
            
            etCodeInput.setText(content.toString());
            Toast.makeText(this, "Script loaded: " + file.getName(), Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Error loading script: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showHelp() {
        String helpText = 
            "AutoClicker Python Console - Available Commands:\n\n" +
            "Actions:\n" +
            "  ac.click(x, y) - Click at coordinates\n" +
            "  ac.swipe(x1, y1, x2, y2, duration) - Swipe gesture\n" +
            "  ac.wait(ms) - Wait for milliseconds\n\n" +
            "Recognition:\n" +
            "  ac.find_color(color, tolerance, x, y, w, h) - Find color\n" +
            "  ac.find_text(text, confidence) - Find text with OCR\n\n" +
            "Control Flow:\n" +
            "  ac.loop(count, actions) - Loop actions\n" +
            "  ac.condition(cond, true, false) - Conditional execution\n\n" +
            "Variables:\n" +
            "  ac.set_var(name, value) - Set variable\n" +
            "  ac.get_var(name, default) - Get variable\n\n" +
            "Utilities:\n" +
            "  ac.random_click(x, y, w, h) - Random click in area\n" +
            "  ac.log(message) - Log message\n" +
            "  help() - Show this help\n\n" +
            "Python Standard Library is also available!\n";
        
        new AlertDialog.Builder(this)
            .setTitle("Python Console Help")
            .setMessage(helpText)
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
