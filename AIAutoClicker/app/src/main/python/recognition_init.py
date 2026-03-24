"""
AI Auto Clicker - Recognition Module Initialization
This module initializes all recognition capabilities for the auto clicker.
"""

import sys
import json
import time
import math
import random
import numpy as np

# Try to import OpenCV
try:
    import cv2
    from cv2 import cv2 as cv
    OPENCV_AVAILABLE = True
except ImportError:
    OPENCV_AVAILABLE = False
    print("Warning: OpenCV not available")

# Try to import PIL
try:
    from PIL import Image, ImageDraw, ImageFont
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False
    print("Warning: PIL not available")

# Try to import Tesseract
try:
    import pytesseract
    TESSERACT_AVAILABLE = True
except ImportError:
    TESSERACT_AVAILABLE = False
    print("Warning: Tesseract not available")

# Try to import scikit-learn
try:
    from sklearn.cluster import KMeans
    from sklearn.metrics import pairwise_distances
    SKLEARN_AVAILABLE = True
except ImportError:
    SKLEARN_AVAILABLE = False
    print("Warning: scikit-learn not available")

# Try to import scipy
try:
    from scipy import ndimage
    from scipy.spatial import distance
    SCIPY_AVAILABLE = True
except ImportError:
    SCIPY_AVAILABLE = False
    print("Warning: scipy not available")


class RecognitionAPI:
    """Main API for all recognition capabilities"""
    
    def __init__(self):
        self.variables = {}
        self.patterns = {}
        self.last_screenshot = None
        
    # ==================== COLOR RECOGNITION ====================
    
    def find_color(self, image, target_color, tolerance=10, x=0, y=0, width=None, height=None):
        """
        Find a specific color in an image.
        
        Args:
            image: numpy array or image path
            target_color: RGB tuple (r, g, b) or hex string
            tolerance: color matching tolerance
            x, y, width, height: search region
            
        Returns:
            (x, y) coordinates of found color or None
        """
        if not OPENCV_AVAILABLE:
            return None
            
        # Load image if path provided
        if isinstance(image, str):
            img = cv.imread(image)
        else:
            img = image
            
        if img is None:
            return None
            
        # Convert hex to RGB if needed
        if isinstance(target_color, str):
            target_color = tuple(int(target_color[i:i+2], 16) for i in (1, 3, 5))
            
        # Extract region if specified
        if width is not None and height is not None:
            img = img[y:y+height, x:x+width]
            
        # Convert to RGB if BGR
        if len(img.shape) == 3:
            img_rgb = cv.cvtColor(img, cv.COLOR_BGR2RGB)
        else:
            img_rgb = img
            
        # Create color range
        lower = np.array([max(0, c - tolerance) for c in target_color])
        upper = np.array([min(255, c + tolerance) for c in target_color])
        
        # Create mask
        mask = cv.inRange(img_rgb, lower, upper)
        
        # Find contours
        contours, _ = cv.findContours(mask, cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
        
        if contours:
            # Get largest contour
            largest = max(contours, key=cv.contourArea)
            M = cv.moments(largest)
            if M["m00"] != 0:
                cx = int(M["m10"] / M["m00"]) + x
                cy = int(M["m01"] / M["m00"]) + y
                return (cx, cy)
                
        return None
        
    def find_color_hsv(self, image, hue, saturation, value, tolerance=10):
        """Find color using HSV color space"""
        if not OPENCV_AVAILABLE:
            return None
            
        if isinstance(image, str):
            img = cv.imread(image)
        else:
            img = image
            
        if img is None:
            return None
            
        hsv = cv.cvtColor(img, cv.COLOR_BGR2HSV)
        
        lower = np.array([max(0, hue - tolerance), 
                         max(0, saturation - tolerance), 
                         max(0, value - tolerance)])
        upper = np.array([min(179, hue + tolerance), 
                         min(255, saturation + tolerance), 
                         min(255, value + tolerance)])
        
        mask = cv.inRange(hsv, lower, upper)
        contours, _ = cv.findContours(mask, cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
        
        if contours:
            largest = max(contours, key=cv.contourArea)
            M = cv.moments(largest)
            if M["m00"] != 0:
                return (int(M["m10"] / M["m00"]), int(M["m01"] / M["m00"]))
                
        return None
        
    # ==================== TEXT RECOGNITION (OCR) ====================
    
    def find_text(self, image, target_text, confidence_threshold=0.8):
        """
        Find text in an image using OCR.
        
        Args:
            image: numpy array or image path
            target_text: text to search for
            confidence_threshold: minimum confidence (0-1)
            
        Returns:
            (x, y, width, height, confidence) of found text or None
        """
        if not TESSERACT_AVAILABLE:
            return None
            
        if isinstance(image, str):
            img = cv.imread(image)
        else:
            img = image
            
        if img is None:
            return None
            
        # Convert to PIL Image
        if OPENCV_AVAILABLE:
            img_rgb = cv.cvtColor(img, cv.COLOR_BGR2RGB)
            pil_img = Image.fromarray(img_rgb)
        else:
            pil_img = Image.open(image)
            
        # Get OCR data
        data = pytesseract.image_to_data(pil_img, output_type=pytesseract.Output.DICT)
        
        n_boxes = len(data['text'])
        for i in range(n_boxes):
            text = data['text'][i].strip()
            conf = int(data['conf'][i])
            
            if target_text.lower() in text.lower() and conf >= confidence_threshold * 100:
                x = data['left'][i]
                y = data['top'][i]
                w = data['width'][i]
                h = data['height'][i]
                return (x + w//2, y + h//2, w, h, conf / 100.0)
                
        return None
        
    def recognize_all_text(self, image):
        """Recognize all text in an image"""
        if not TESSERACT_AVAILABLE:
            return []
            
        if isinstance(image, str):
            img = cv.imread(image)
            img_rgb = cv.cvtColor(img, cv.COLOR_BGR2RGB)
            pil_img = Image.fromarray(img_rgb)
        else:
            pil_img = Image.fromarray(image)
            
        data = pytesseract.image_to_data(pil_img, output_type=pytesseract.Output.DICT)
        
        results = []
        n_boxes = len(data['text'])
        for i in range(n_boxes):
            text = data['text'][i].strip()
            conf = int(data['conf'][i])
            
            if text and conf > 0:
                results.append({
                    'text': text,
                    'confidence': conf / 100.0,
                    'x': data['left'][i],
                    'y': data['top'][i],
                    'width': data['width'][i],
                    'height': data['height'][i]
                })
                
        return results
        
    # ==================== IMAGE RECOGNITION ====================
    
    def find_image(self, source, template, confidence_threshold=0.8):
        """
        Find a template image within a source image.
        
        Args:
            source: source image (path or array)
            template: template image (path or array)
            confidence_threshold: minimum match confidence
            
        Returns:
            (x, y, confidence) of best match or None
        """
        if not OPENCV_AVAILABLE:
            return None
            
        # Load images
        if isinstance(source, str):
            src_img = cv.imread(source)
        else:
            src_img = source
            
        if isinstance(template, str):
            tmpl_img = cv.imread(template)
        else:
            tmpl_img = template
            
        if src_img is None or tmpl_img is None:
            return None
            
        # Template matching
        result = cv.matchTemplate(src_img, tmpl_img, cv.TM_CCOEFF_NORMED)
        min_val, max_val, min_loc, max_loc = cv.minMaxLoc(result)
        
        if max_val >= confidence_threshold:
            h, w = tmpl_img.shape[:2]
            return (max_loc[0] + w//2, max_loc[1] + h//2, max_val)
            
        return None
        
    def find_all_images(self, source, template, confidence_threshold=0.8):
        """Find all occurrences of a template image"""
        if not OPENCV_AVAILABLE:
            return []
            
        if isinstance(source, str):
            src_img = cv.imread(source)
        else:
            src_img = source
            
        if isinstance(template, str):
            tmpl_img = cv.imread(template)
        else:
            tmpl_img = template
            
        if src_img is None or tmpl_img is None:
            return []
            
        result = cv.matchTemplate(src_img, tmpl_img, cv.TM_CCOEFF_NORMED)
        
        # Find all locations above threshold
        locations = np.where(result >= confidence_threshold)
        
        matches = []
        h, w = tmpl_img.shape[:2]
        for pt in zip(*locations[::-1]):
            confidence = result[pt[1], pt[0]]
            matches.append((pt[0] + w//2, pt[1] + h//2, confidence))
            
        return matches
        
    # ==================== PATTERN RECOGNITION ====================
    
    def record_pattern(self, name, points):
        """Record a touch pattern"""
        self.patterns[name] = points
        return True
        
    def match_pattern(self, name, points, tolerance=50):
        """Match points against a recorded pattern"""
        if name not in self.patterns:
            return False
            
        recorded = self.patterns[name]
        if len(recorded) != len(points):
            return False
            
        for i, (r, p) in enumerate(zip(recorded, points)):
            dist = math.sqrt((r[0] - p[0])**2 + (r[1] - p[1])**2)
            if dist > tolerance:
                return False
                
        return True
        
    def find_similar_pattern(self, points, tolerance=50):
        """Find which pattern matches the given points"""
        for name, pattern in self.patterns.items():
            if self.match_pattern(name, points, tolerance):
                return name
        return None
        
    # ==================== UTILITY METHODS ====================
    
    def set_variable(self, name, value):
        """Set a variable"""
        self.variables[name] = value
        return value
        
    def get_variable(self, name, default=None):
        """Get a variable value"""
        return self.variables.get(name, default)
        
    def wait(self, milliseconds):
        """Wait for specified milliseconds"""
        time.sleep(milliseconds / 1000.0)
        
    def random_point(self, x, y, width, height):
        """Generate random point within area"""
        return (random.randint(x, x + width), random.randint(y, y + height))
        
    def distance(self, p1, p2):
        """Calculate distance between two points"""
        return math.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)
        
    def interpolate(self, p1, p2, steps):
        """Interpolate points between two positions"""
        points = []
        for i in range(steps):
            t = i / (steps - 1)
            x = p1[0] + (p2[0] - p1[0]) * t
            y = p1[1] + (p2[1] - p1[1]) * t
            points.append((x, y))
        return points


# Create global API instance
recognition = RecognitionAPI()

# Export for Java access
def get_api():
    return recognition
    
def help():
    return """
AI Auto Clicker Recognition API

Color Recognition:
  recognition.find_color(image, (r,g,b), tolerance, x, y, w, h)
  recognition.find_color_hsv(image, hue, sat, val, tolerance)

Text Recognition (OCR):
  recognition.find_text(image, "text", confidence)
  recognition.recognize_all_text(image)

Image Recognition:
  recognition.find_image(source, template, confidence)
  recognition.find_all_images(source, template, confidence)

Pattern Recognition:
  recognition.record_pattern(name, points)
  recognition.match_pattern(name, points, tolerance)
  recognition.find_similar_pattern(points, tolerance)

Utilities:
  recognition.set_variable(name, value)
  recognition.get_variable(name, default)
  recognition.wait(milliseconds)
  recognition.random_point(x, y, w, h)
  recognition.distance(p1, p2)
  recognition.interpolate(p1, p2, steps)
"""

if __name__ == "__main__":
    print(help())
