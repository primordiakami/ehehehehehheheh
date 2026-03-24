"""
Screenshot module for AI Auto Clicker
Provides screenshot functionality via Android APIs
"""

import io
import base64

try:
    from android.graphics import Bitmap
    from android.view import Surface
    ANDROID_AVAILABLE = True
except ImportError:
    ANDROID_AVAILABLE = False

try:
    import numpy as np
    import cv2
    CV2_AVAILABLE = True
except ImportError:
    CV2_AVAILABLE = False

try:
    from PIL import Image
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False


def take_screenshot():
    """
    Take a screenshot using Android APIs.
    Returns the screenshot as bytes or None if failed.
    """
    if not ANDROID_AVAILABLE:
        print("Android APIs not available")
        return None
        
    try:
        # This is a placeholder - actual implementation would use
        # MediaProjection API through Java bridge
        # For now, return None to indicate screenshot not available
        return None
    except Exception as e:
        print(f"Error taking screenshot: {e}")
        return None


def screenshot_to_numpy(screenshot_bytes):
    """Convert screenshot bytes to numpy array"""
    if not CV2_AVAILABLE:
        return None
        
    try:
        nparr = np.frombuffer(screenshot_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        return img
    except Exception as e:
        print(f"Error converting screenshot: {e}")
        return None


def screenshot_to_pil(screenshot_bytes):
    """Convert screenshot bytes to PIL Image"""
    if not PIL_AVAILABLE:
        return None
        
    try:
        return Image.open(io.BytesIO(screenshot_bytes))
    except Exception as e:
        print(f"Error converting screenshot: {e}")
        return None


def save_screenshot(screenshot_bytes, filepath):
    """Save screenshot to file"""
    try:
        with open(filepath, 'wb') as f:
            f.write(screenshot_bytes)
        return True
    except Exception as e:
        print(f"Error saving screenshot: {e}")
        return False


def encode_screenshot_base64(screenshot_bytes):
    """Encode screenshot as base64 string"""
    return base64.b64encode(screenshot_bytes).decode('utf-8')


def decode_screenshot_base64(base64_string):
    """Decode screenshot from base64 string"""
    return base64.b64decode(base64_string)
