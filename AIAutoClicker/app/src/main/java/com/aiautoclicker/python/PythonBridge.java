package com.aiautoclicker.python;

import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.HashMap;
import java.util.Map;

public class PythonBridge {
    private static final String TAG = "PythonBridge";
    
    private Python python;
    private PyObject mainModule;
    private Map<String, PyObject> cachedModules;
    
    public PythonBridge() {
        try {
            if (!Python.isStarted()) {
                Log.e(TAG, "Python not started");
                return;
            }
            
            python = Python.getInstance();
            cachedModules = new HashMap<>();
            
            // Initialize main module
            initializeMainModule();
            
            Log.d(TAG, "PythonBridge initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PythonBridge", e);
        }
    }
    
    private void initializeMainModule() {
        try {
            // Create or get the main autoclicker module
            String mainCode = 
                "import sys\n" +
                "import json\n" +
                "import time\n" +
                "import math\n" +
                "import random\n" +
                "\n" +
                "# AutoClicker API\n" +
                "class AutoClickerAPI:\n" +
                "    def __init__(self):\n" +
                "        self.variables = {}\n" +
                "        self.tasks = []\n" +
                "    \n" +
                "    def click(self, x, y):\n" +
                "        '''Perform a click at coordinates'''\n" +
                "        return {'action': 'click', 'x': x, 'y': y}\n" +
                "    \n" +
                "    def swipe(self, x1, y1, x2, y2, duration=300):\n" +
                "        '''Perform a swipe gesture'''\n" +
                "        return {'action': 'swipe', 'x1': x1, 'y1': y1, 'x2': x2, 'y2': y2, 'duration': duration}\n" +
                "    \n" +
                "    def wait(self, milliseconds):\n" +
                "        '''Wait for specified milliseconds'''\n" +
                "        time.sleep(milliseconds / 1000.0)\n" +
                "        return {'action': 'wait', 'duration': milliseconds}\n" +
                "    \n" +
                "    def find_color(self, color, tolerance=10, x=0, y=0, width=1080, height=1920):\n" +
                "        '''Find a color on screen'''\n" +
                "        return {'action': 'find_color', 'color': color, 'tolerance': tolerance, 'area': [x, y, width, height]}\n" +
                "    \n" +
                "    def find_text(self, text, confidence=0.8):\n" +
                "        '''Find text on screen using OCR'''\n" +
                "        return {'action': 'find_text', 'text': text, 'confidence': confidence}\n" +
                "    \n" +
                "    def set_var(self, name, value):\n" +
                "        '''Set a variable'''\n" +
                "        self.variables[name] = value\n" +
                "        return value\n" +
                "    \n" +
                "    def get_var(self, name, default=None):\n" +
                "        '''Get a variable value'''\n" +
                "        return self.variables.get(name, default)\n" +
                "    \n" +
                "    def loop(self, count, actions):\n" +
                "        '''Execute actions in a loop'''\n" +
                "        results = []\n" +
                "        for i in range(count):\n" +
                "            for action in actions:\n" +
                "                results.append(action)\n" +
                "        return results\n" +
                "    \n" +
                "    def condition(self, condition, true_actions, false_actions=None):\n" +
                "        '''Execute actions based on condition'''\n" +
                "        if condition:\n" +
                "            return true_actions\n" +
                "        elif false_actions:\n" +
                "            return false_actions\n" +
                "        return None\n" +
                "    \n" +
                "    def random_click(self, x, y, width, height):\n" +
                "        '''Click at random position within area'''\n" +
                "        rx = random.randint(x, x + width)\n" +
                "        ry = random.randint(y, y + height)\n" +
                "        return self.click(rx, ry)\n" +
                "    \n" +
                "    def log(self, message):\n" +
                "        '''Log a message'''\n" +
                "        print(f'[LOG] {message}')\n" +
                "        return message\n" +
                "\n" +
                "# Create global API instance\n" +
                "ac = AutoClickerAPI()\n" +
                "\n" +
                "def help():\n" +
                "    '''Show available commands'''\n" +
                "    return '''\n" +
                "AutoClicker Python Console - Available Commands:\n" +
                "\n" +
                "Actions:\n" +
                "  ac.click(x, y) - Click at coordinates\n" +
                "  ac.swipe(x1, y1, x2, y2, duration) - Swipe gesture\n" +
                "  ac.wait(ms) - Wait for milliseconds\n" +
                "\n" +
                "Recognition:\n" +
                "  ac.find_color(color, tolerance, x, y, w, h) - Find color\n" +
                "  ac.find_text(text, confidence) - Find text with OCR\n" +
                "\n" +
                "Control Flow:\n" +
                "  ac.loop(count, actions) - Loop actions\n" +
                "  ac.condition(cond, true, false) - Conditional execution\n" +
                "\n" +
                "Variables:\n" +
                "  ac.set_var(name, value) - Set variable\n" +
                "  ac.get_var(name, default) - Get variable\n" +
                "\n" +
                "Utilities:\n" +
                "  ac.random_click(x, y, w, h) - Random click in area\n" +
                "  ac.log(message) - Log message\n" +
                "  help() - Show this help\n" +
                "'''\n";
            
            mainModule = python.getModule("autoclicker_main");
            mainModule.put("__doc__", mainCode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing main module", e);
        }
    }
    
    public PyObject executeCode(String code) {
        try {
            if (python == null) {
                Log.e(TAG, "Python not available");
                return null;
            }
            
            // Execute code in the main module context
            PyObject result = python.getModule("builtins").callAttr("exec", code, 
                mainModule.getDict(), mainModule.getDict());
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing Python code", e);
            return null;
        }
    }
    
    public PyObject evaluateExpression(String expression) {
        try {
            if (python == null) {
                return null;
            }
            
            return python.getModule("builtins").callAttr("eval", expression,
                mainModule.getDict(), mainModule.getDict());
                
        } catch (Exception e) {
            Log.e(TAG, "Error evaluating expression", e);
            return null;
        }
    }
    
    public PyObject callFunction(String functionName, Object... args) {
        try {
            if (mainModule == null) return null;
            
            PyObject func = mainModule.get(functionName);
            if (func != null) {
                return func.call(args);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error calling function: " + functionName, e);
        }
        return null;
    }
    
    public PyObject getModule(String moduleName) {
        try {
            if (cachedModules.containsKey(moduleName)) {
                return cachedModules.get(moduleName);
            }
            
            PyObject module = python.getModule(moduleName);
            cachedModules.put(moduleName, module);
            return module;
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting module: " + moduleName, e);
            return null;
        }
    }
    
    public void setVariable(String name, Object value) {
        try {
            if (mainModule != null) {
                mainModule.put(name, value);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting variable: " + name, e);
        }
    }
    
    public PyObject getVariable(String name) {
        try {
            if (mainModule != null) {
                return mainModule.get(name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting variable: " + name, e);
        }
        return null;
    }
    
    public String getOutput() {
        try {
            // Capture stdout
            PyObject sys = python.getModule("sys");
            PyObject stdout = sys.get("stdout");
            if (stdout != null) {
                return stdout.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting output", e);
        }
        return "";
    }
    
    public void clearOutput() {
        try {
            PyObject sys = python.getModule("sys");
            sys.put("stdout", "");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing output", e);
        }
    }
}
