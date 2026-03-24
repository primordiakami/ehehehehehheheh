# AI Auto Clicker

A powerful Android auto clicker application with AI-powered recognition capabilities and Python scripting support.

## Features

### Core Auto Clicker Features
- **Click Automation**: Single click, long click, double click
- **Swipe Gestures**: Customizable swipe with direction and duration
- **Scroll Actions**: Automated scrolling
- **Text Input**: Automated text entry
- **Key Presses**: Simulate key presses

### AI-Powered Recognition
- **Color Recognition**: Find and click on specific colors with configurable tolerance
- **Text Recognition (OCR)**: Find and click on text using Tesseract OCR
- **Image Recognition**: Template matching for finding images on screen
- **Pattern Recognition**: Record and replay touch patterns
- **Event Recognition**: Detect popups, notifications, and UI changes

### Python Console
- **Interactive Python Console**: Write and execute Python code in real-time
- **AutoClicker API**: Built-in API for automation tasks
- **Script Saving/Loading**: Save and load Python scripts
- **Command History**: Navigate through previous commands

### Task System
- **Visual Task Editor**: Create tasks with a user-friendly interface
- **Flexible Step Structure**: Multiple step types (click, swipe, wait, condition, loop)
- **Recognition Steps**: Include color/text/image recognition in tasks
- **Task Repeating**: Configure repeat count and delays
- **Import/Export**: Share tasks with others

### Recording & Playback
- **Pattern Recording**: Record touch patterns for later playback
- **Event Recording**: Record accessibility events
- **Resizable Recognition Areas**: Define custom scan regions

## Project Structure

```
AIAutoClicker/
├── app/
│   ├── src/main/
│   │   ├── java/com/aiautoclicker/
│   │   │   ├── AutoClickerApp.java          # Application class
│   │   │   ├── MainActivity.java            # Main UI
│   │   │   ├── PythonConsoleActivity.java   # Python console
│   │   │   ├── TaskEditorActivity.java      # Task editor
│   │   │   ├── PatternRecorderActivity.java # Pattern recorder
│   │   │   ├── service/
│   │   │   │   └── AutoClickerAccessibilityService.java  # Core service
│   │   │   ├── tasks/
│   │   │   │   ├── Task.java                # Task model
│   │   │   │   ├── TaskManager.java         # Task persistence
│   │   │   │   ├── TaskAdapter.java         # Task list adapter
│   │   │   │   └── TaskExecutor.java        # Task execution
│   │   │   ├── recognition/
│   │   │   │   └── RecognitionManager.java  # Recognition features
│   │   │   └── python/
│   │   │       └── PythonBridge.java        # Python integration
│   │   ├── python/                          # Python modules
│   │   │   ├── autoclicker_main.py          # Main API
│   │   │   ├── recognition_init.py          # Recognition API
│   │   │   └── screenshot.py                # Screenshot utility
│   │   └── res/                             # Android resources
│   └── build.gradle.kts                     # App build config
├── build.gradle.kts                         # Project build config
└── settings.gradle.kts                      # Project settings
```

## Requirements

- Android 7.0+ (API 24)
- Accessibility Service permission
- Overlay permission (for floating controls)
- Storage permission (for saving tasks)

## Building

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34
- Python 3.11 (for Chaquopy)

### Build Steps

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the project: `./gradlew assembleDebug`

### Build APK

```bash
./gradlew assembleRelease
```

The APK will be located at:
`app/build/outputs/apk/release/app-release-unsigned.apk`

## Usage

### Getting Started

1. Install the APK
2. Open the app
3. Grant required permissions:
   - Accessibility Service (required for auto-clicking)
   - Overlay permission (for floating controls)
   - Storage permission (for saving tasks)

### Creating a Task

1. Tap "Create Task" on the main screen
2. Enter task name and description
3. Add steps:
   - Click: Set X, Y coordinates
   - Swipe: Set start and end coordinates
   - Wait: Set duration in milliseconds
   - Recognition: Configure color/text/image recognition
4. Save the task
5. Tap the task to execute

### Using Python Console

1. Tap "Python Console" on the main screen
2. Type Python code in the input area
3. Tap "Run" to execute

Example:
```python
# Simple click
ac.click(500, 1000)

# Find and click on color
result = ac.find_color((255, 0, 0), tolerance=20)
if result:
    ac.click(result[0], result[1])

# Loop with delay
ac.loop(5, lambda i: [ac.click(500, 500), ac.wait(1000)])
```

### Recording Patterns

1. Tap "Record Pattern" on the main screen
2. Tap "Start Recording"
3. Perform touch gestures on screen
4. Tap "Stop Recording"
5. Save the pattern with a name

### Using Recognition

#### Color Recognition
```python
# Find red color with 15% tolerance
result = ac.find_color("#FF0000", tolerance=15)
if result:
    ac.click(result[0], result[1])
```

#### Text Recognition
```python
# Find "OK" button
result = ac.find_text("OK", confidence=0.8)
if result:
    ac.click(result[0], result[1])
```

#### Image Recognition
```python
# Find template image
result = ac.find_image("/path/to/template.png", confidence=0.85)
if result:
    ac.click(result[0], result[1])
```

## Python API Reference

### Actions
- `ac.click(x, y)` - Click at coordinates
- `ac.long_click(x, y, duration)` - Long press
- `ac.swipe(x1, y1, x2, y2, duration)` - Swipe gesture
- `ac.scroll(x, y, amount)` - Scroll action
- `ac.wait(milliseconds)` - Wait duration
- `ac.input_text(text)` - Input text
- `ac.press_key(keycode)` - Press key

### Recognition
- `ac.find_color(color, tolerance, x, y, width, height)` - Find color
- `ac.find_text(text, confidence)` - Find text (OCR)
- `ac.find_image(image_path, confidence)` - Find image
- `ac.wait_for_color(color, timeout, tolerance)` - Wait for color
- `ac.wait_for_text(text, timeout, confidence)` - Wait for text

### Control Flow
- `ac.loop(count, actions_func)` - Loop actions
- `ac.condition(condition, true_actions, false_actions)` - Conditional execution
- `ac.repeat_until(condition_func, actions_func, timeout, interval)` - Repeat until condition

### Variables
- `ac.set_var(name, value)` - Set variable
- `ac.get_var(name, default)` - Get variable
- `ac.increment(name, amount)` - Increment variable
- `ac.decrement(name, amount)` - Decrement variable

### Utilities
- `ac.random_click(x, y, width, height)` - Random click in area
- `ac.random_delay(min_ms, max_ms)` - Random wait
- `ac.log(message)` - Log message
- `ac.debug(obj)` - Debug object

## Troubleshooting

### Accessibility Service Not Working
1. Go to Settings > Accessibility
2. Find "AI Auto Clicker Service"
3. Enable it

### Overlay Permission Denied
1. Go to Settings > Apps > AI Auto Clicker
2. Tap "Display over other apps"
3. Enable permission

### Python Not Working
1. Ensure Python 3.11 is installed
2. Check Chaquopy configuration in build.gradle

## License

MIT License - See LICENSE file for details

## Credits

- OpenCV for image processing
- Tesseract OCR for text recognition
- Chaquopy for Python integration
- Android Accessibility API for automation
