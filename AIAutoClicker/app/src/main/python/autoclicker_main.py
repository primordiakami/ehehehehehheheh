"""
AI Auto Clicker - Main Python API Module
This module provides the main API for the Python console.
"""

import sys
import json
import time
import math
import random

# Import recognition module
from recognition_init import recognition, get_api, help as recognition_help

class AutoClickerAPI:
    """
    Main API class for AI Auto Clicker.
    Provides methods for automation, recognition, and control flow.
    """
    
    def __init__(self):
        self.variables = {}
        self.tasks = []
        self.recording = False
        self.recorded_actions = []
        
    # ==================== ACTION METHODS ====================
    
    def click(self, x, y):
        """
        Perform a click at the specified coordinates.
        
        Args:
            x: X coordinate
            y: Y coordinate
            
        Returns:
            dict with action details
        """
        result = {'action': 'click', 'x': x, 'y': y}
        self._log_action(result)
        return result
        
    def long_click(self, x, y, duration=1000):
        """
        Perform a long click at the specified coordinates.
        
        Args:
            x: X coordinate
            y: Y coordinate
            duration: press duration in milliseconds
            
        Returns:
            dict with action details
        """
        result = {'action': 'long_click', 'x': x, 'y': y, 'duration': duration}
        self._log_action(result)
        return result
        
    def swipe(self, x1, y1, x2, y2, duration=300):
        """
        Perform a swipe gesture.
        
        Args:
            x1, y1: start coordinates
            x2, y2: end coordinates
            duration: swipe duration in milliseconds
            
        Returns:
            dict with action details
        """
        result = {'action': 'swipe', 'x1': x1, 'y1': y1, 'x2': x2, 'y2': y2, 'duration': duration}
        self._log_action(result)
        return result
        
    def scroll(self, x, y, amount):
        """
        Perform a scroll action.
        
        Args:
            x, y: scroll position
            amount: scroll amount (positive = up, negative = down)
            
        Returns:
            dict with action details
        """
        result = {'action': 'scroll', 'x': x, 'y': y, 'amount': amount}
        self._log_action(result)
        return result
        
    def wait(self, milliseconds):
        """
        Wait for the specified duration.
        
        Args:
            milliseconds: wait duration
            
        Returns:
            dict with action details
        """
        time.sleep(milliseconds / 1000.0)
        result = {'action': 'wait', 'duration': milliseconds}
        self._log_action(result)
        return result
        
    def input_text(self, text):
        """
        Input text at the current focus.
        
        Args:
            text: text to input
            
        Returns:
            dict with action details
        """
        result = {'action': 'input_text', 'text': text}
        self._log_action(result)
        return result
        
    def press_key(self, keycode):
        """
        Press a key.
        
        Args:
            keycode: Android keycode
            
        Returns:
            dict with action details
        """
        result = {'action': 'press_key', 'keycode': keycode}
        self._log_action(result)
        return result
        
    # ==================== RECOGNITION METHODS ====================
    
    def find_color(self, color, tolerance=10, x=0, y=0, width=1080, height=1920):
        """
        Find a color on the screen.
        
        Args:
            color: RGB tuple or hex string
            tolerance: color matching tolerance
            x, y, width, height: search region
            
        Returns:
            (x, y) coordinates or None
        """
        return recognition.find_color(None, color, tolerance, x, y, width, height)
        
    def find_text(self, text, confidence=0.8):
        """
        Find text on the screen using OCR.
        
        Args:
            text: text to search for
            confidence: minimum confidence (0-1)
            
        Returns:
            (x, y, w, h, confidence) or None
        """
        return recognition.find_text(None, text, confidence)
        
    def find_image(self, image_path, confidence=0.8):
        """
        Find an image on the screen.
        
        Args:
            image_path: path to template image
            confidence: minimum confidence (0-1)
            
        Returns:
            (x, y, confidence) or None
        """
        return recognition.find_image(None, image_path, confidence)
        
    def wait_for_color(self, color, timeout=10000, tolerance=10, scan_interval=500):
        """
        Wait for a color to appear on screen.
        
        Args:
            color: color to wait for
            timeout: maximum wait time in milliseconds
            tolerance: color matching tolerance
            scan_interval: time between scans
            
        Returns:
            (x, y) coordinates or None if timeout
        """
        start = time.time()
        while (time.time() - start) * 1000 < timeout:
            result = self.find_color(color, tolerance)
            if result:
                return result
            time.sleep(scan_interval / 1000.0)
        return None
        
    def wait_for_text(self, text, timeout=10000, confidence=0.8, scan_interval=500):
        """
        Wait for text to appear on screen.
        
        Args:
            text: text to wait for
            timeout: maximum wait time
            confidence: minimum confidence
            scan_interval: time between scans
            
        Returns:
            (x, y, w, h, confidence) or None if timeout
        """
        start = time.time()
        while (time.time() - start) * 1000 < timeout:
            result = self.find_text(text, confidence)
            if result:
                return result
            time.sleep(scan_interval / 1000.0)
        return None
        
    # ==================== CONTROL FLOW ====================
    
    def loop(self, count, actions_func):
        """
        Execute actions in a loop.
        
        Args:
            count: number of iterations
            actions_func: function that returns list of actions
            
        Returns:
            list of all action results
        """
        results = []
        for i in range(count):
            actions = actions_func(i)
            if isinstance(actions, list):
                results.extend(actions)
            else:
                results.append(actions)
        return results
        
    def condition(self, condition, true_actions, false_actions=None):
        """
        Execute actions based on a condition.
        
        Args:
            condition: boolean condition
            true_actions: actions to execute if true
            false_actions: actions to execute if false (optional)
            
        Returns:
            result of executed actions
        """
        if condition:
            return true_actions
        elif false_actions:
            return false_actions
        return None
        
    def repeat_until(self, condition_func, actions_func, timeout=30000, interval=500):
        """
        Repeat actions until condition is met or timeout.
        
        Args:
            condition_func: function that returns boolean
            actions_func: function that executes actions
            timeout: maximum time in milliseconds
            interval: time between iterations
            
        Returns:
            True if condition met, False if timeout
        """
        start = time.time()
        while (time.time() - start) * 1000 < timeout:
            if condition_func():
                return True
            actions_func()
            time.sleep(interval / 1000.0)
        return False
        
    # ==================== VARIABLE METHODS ====================
    
    def set_var(self, name, value):
        """
        Set a variable.
        
        Args:
            name: variable name
            value: variable value
            
        Returns:
            the value
        """
        self.variables[name] = value
        return value
        
    def get_var(self, name, default=None):
        """
        Get a variable value.
        
        Args:
            name: variable name
            default: default value if not found
            
        Returns:
            variable value or default
        """
        return self.variables.get(name, default)
        
    def increment(self, name, amount=1):
        """Increment a numeric variable"""
        current = self.get_var(name, 0)
        self.set_var(name, current + amount)
        return current + amount
        
    def decrement(self, name, amount=1):
        """Decrement a numeric variable"""
        current = self.get_var(name, 0)
        self.set_var(name, current - amount)
        return current - amount
        
    # ==================== UTILITY METHODS ====================
    
    def random_click(self, x, y, width, height):
        """
        Click at a random position within an area.
        
        Args:
            x, y: top-left corner
            width, height: area dimensions
            
        Returns:
            click action result
        """
        rx = random.randint(x, x + width)
        ry = random.randint(y, y + height)
        return self.click(rx, ry)
        
    def random_delay(self, min_ms, max_ms):
        """Wait for a random duration"""
        delay = random.randint(min_ms, max_ms)
        return self.wait(delay)
        
    def log(self, message):
        """
        Log a message.
        
        Args:
            message: message to log
            
        Returns:
            the message
        """
        print(f"[LOG] {message}")
        return message
        
    def debug(self, obj):
        """Print debug info about an object"""
        print(f"[DEBUG] {type(obj)}: {obj}")
        return obj
        
    # ==================== RECORDING METHODS ====================
    
    def start_recording(self):
        """Start recording actions"""
        self.recording = True
        self.recorded_actions = []
        return True
        
    def stop_recording(self):
        """Stop recording actions"""
        self.recording = False
        return self.recorded_actions
        
    def get_recorded_actions(self):
        """Get recorded actions"""
        return self.recorded_actions
        
    def clear_recording(self):
        """Clear recorded actions"""
        self.recorded_actions = []
        return True
        
    def _log_action(self, action):
        """Internal method to log actions"""
        if self.recording:
            self.recorded_actions.append(action)
            
    # ==================== TASK METHODS ====================
    
    def create_task(self, name, steps):
        """Create a new task"""
        task = {
            'name': name,
            'steps': steps,
            'created_at': time.time()
        }
        self.tasks.append(task)
        return task
        
    def get_tasks(self):
        """Get all tasks"""
        return self.tasks
        
    def clear_tasks(self):
        """Clear all tasks"""
        self.tasks = []
        return True


# Create global API instance
ac = AutoClickerAPI()

# Convenience function for help
def help():
    """Show available commands"""
    return """
╔══════════════════════════════════════════════════════════════════╗
║           AI Auto Clicker - Python Console Help                  ║
╠══════════════════════════════════════════════════════════════════╣
║ ACTIONS                                                          ║
║   ac.click(x, y)                    - Click at coordinates       ║
║   ac.long_click(x, y, duration)     - Long press                 ║
║   ac.swipe(x1, y1, x2, y2, dur)     - Swipe gesture              ║
║   ac.scroll(x, y, amount)           - Scroll action              ║
║   ac.wait(milliseconds)             - Wait duration              ║
║   ac.input_text(text)               - Input text                 ║
║   ac.press_key(keycode)             - Press key                  ║
╠══════════════════════════════════════════════════════════════════╣
║ RECOGNITION                                                      ║
║   ac.find_color(color, tol, x, y, w, h)   - Find color           ║
║   ac.find_text(text, confidence)          - Find text (OCR)      ║
║   ac.find_image(path, confidence)         - Find image           ║
║   ac.wait_for_color(color, timeout)       - Wait for color       ║
║   ac.wait_for_text(text, timeout)         - Wait for text        ║
╠══════════════════════════════════════════════════════════════════╣
║ CONTROL FLOW                                                     ║
║   ac.loop(count, func)              - Loop actions               ║
║   ac.condition(cond, true, false)   - Conditional execution      ║
║   ac.repeat_until(cond, actions, t) - Repeat until condition     ║
╠══════════════════════════════════════════════════════════════════╣
║ VARIABLES                                                        ║
║   ac.set_var(name, value)           - Set variable               ║
║   ac.get_var(name, default)         - Get variable               ║
║   ac.increment(name, amount)        - Increment variable         ║
║   ac.decrement(name, amount)        - Decrement variable         ║
╠══════════════════════════════════════════════════════════════════╣
║ UTILITIES                                                        ║
║   ac.random_click(x, y, w, h)       - Random click in area       ║
║   ac.random_delay(min, max)         - Random wait                ║
║   ac.log(message)                   - Log message                ║
║   ac.debug(obj)                     - Debug object               ║
║   help()                            - Show this help             ║
╠══════════════════════════════════════════════════════════════════╣
║ RECOGNITION API (recognition object)                             ║
║   recognition_help()                - Recognition help           ║
╚══════════════════════════════════════════════════════════════════╝

Python Standard Library is also available!
"""

# Print welcome message on import
print("""
╔══════════════════════════════════════════════════════════════════╗
║           AI Auto Clicker Python Console v1.0                    ║
║           Type help() for available commands                     ║
╚══════════════════════════════════════════════════════════════════╝
""")
