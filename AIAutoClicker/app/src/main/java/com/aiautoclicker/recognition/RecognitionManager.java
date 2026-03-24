package com.aiautoclicker.recognition;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.aiautoclicker.service.AutoClickerAccessibilityService;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecognitionManager {
    private static final String TAG = "RecognitionManager";
    private static final String TESSDATA_PATH = "tessdata";
    private static final String DEFAULT_LANGUAGE = "eng";
    
    private AutoClickerAccessibilityService service;
    private TessBaseAPI tessBaseAPI;
    private Handler mainHandler;
    private boolean isInitialized = false;
    
    // Pattern storage
    private Map<String, List<AccessibilityEvent>> recordedPatterns;
    private Map<String, List<TouchPoint>> recordedTouchPatterns;
    
    // Event listeners
    private List<RecognitionListener> listeners;
    
    // Screen capture
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    
    public RecognitionManager(AutoClickerAccessibilityService service) {
        this.service = service;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.recordedPatterns = new HashMap<>();
        this.recordedTouchPatterns = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        initializeTesseract();
        initializeOpenCV();
    }
    
    // ==================== INITIALIZATION ====================
    
    private void initializeTesseract() {
        try {
            tessBaseAPI = new TessBaseAPI();
            
            // Initialize with English language
            File tessdataDir = new File(service.getExternalFilesDir(null), TESSDATA_PATH);
            if (!tessdataDir.exists()) {
                tessdataDir.mkdirs();
            }
            
            // Copy trained data if needed
            copyTrainedDataIfNeeded(tessdataDir);
            
            boolean initSuccess = tessBaseAPI.init(tessdataDir.getParent(), DEFAULT_LANGUAGE);
            if (initSuccess) {
                Log.d(TAG, "Tesseract initialized successfully");
                isInitialized = true;
            } else {
                Log.e(TAG, "Failed to initialize Tesseract");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Tesseract", e);
        }
    }
    
    private void initializeOpenCV() {
        // OpenCV is initialized via the library
        Log.d(TAG, "OpenCV initialized");
    }
    
    private void copyTrainedDataIfNeeded(File tessdataDir) {
        // In a real app, you would copy the trained data from assets
        // For now, we'll assume it's already present
    }
    
    // ==================== COLOR RECOGNITION ====================
    
    public int[] findColor(int targetColor, int tolerance, int x, int y, int width, int height) {
        try {
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) {
                Log.w(TAG, "Could not take screenshot for color recognition");
                return null;
            }
            
            // Convert to OpenCV Mat
            Mat mat = new Mat();
            Utils.bitmapToMat(screenshot, mat);
            
            // Extract RGB components
            int targetR = Color.red(targetColor);
            int targetG = Color.green(targetColor);
            int targetB = Color.blue(targetColor);
            
            // Define color range with tolerance
            Scalar lowerBound = new Scalar(
                Math.max(0, targetB - tolerance),
                Math.max(0, targetG - tolerance),
                Math.max(0, targetR - tolerance)
            );
            Scalar upperBound = new Scalar(
                Math.min(255, targetB + tolerance),
                Math.min(255, targetG + tolerance),
                Math.min(255, targetR + tolerance)
            );
            
            // Create mask for color range
            Mat mask = new Mat();
            Core.inRange(mat, lowerBound, upperBound, mask);
            
            // Find contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
            // Find largest contour
            double maxArea = 0;
            Point center = null;
            
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > maxArea) {
                    maxArea = area;
                    org.opencv.core.Rect boundingRect = Imgproc.boundingRect(contour);
                    center = new Point(
                        boundingRect.x + boundingRect.width / 2.0,
                        boundingRect.y + boundingRect.height / 2.0
                    );
                }
            }
            
            // Cleanup
            mat.release();
            mask.release();
            hierarchy.release();
            
            if (center != null) {
                return new int[]{(int) center.x, (int) center.y};
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in color recognition", e);
        }
        
        return null;
    }
    
    public int[] findColorHSV(int hue, int saturation, int value, int tolerance) {
        try {
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) return null;
            
            Mat mat = new Mat();
            Utils.bitmapToMat(screenshot, mat);
            
            // Convert to HSV
            Mat hsvMat = new Mat();
            Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV);
            
            // Define HSV range
            Scalar lowerBound = new Scalar(
                Math.max(0, hue - tolerance),
                Math.max(0, saturation - tolerance),
                Math.max(0, value - tolerance)
            );
            Scalar upperBound = new Scalar(
                Math.min(179, hue + tolerance),
                Math.min(255, saturation + tolerance),
                Math.min(255, value + tolerance)
            );
            
            Mat mask = new Mat();
            Core.inRange(hsvMat, lowerBound, upperBound, mask);
            
            // Find contours and center (similar to RGB method)
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
            double maxArea = 0;
            Point center = null;
            
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > maxArea) {
                    maxArea = area;
                    org.opencv.core.Rect boundingRect = Imgproc.boundingRect(contour);
                    center = new Point(
                        boundingRect.x + boundingRect.width / 2.0,
                        boundingRect.y + boundingRect.height / 2.0
                    );
                }
            }
            
            // Cleanup
            mat.release();
            hsvMat.release();
            mask.release();
            hierarchy.release();
            
            if (center != null) {
                return new int[]{(int) center.x, (int) center.y};
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in HSV color recognition", e);
        }
        
        return null;
    }
    
    // ==================== TEXT RECOGNITION (OCR) ====================
    
    public int[] findText(String targetText, double confidenceThreshold) {
        try {
            if (tessBaseAPI == null || !isInitialized) {
                Log.w(TAG, "Tesseract not initialized");
                return null;
            }
            
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) return null;
            
            // Set image for OCR
            tessBaseAPI.setImage(screenshot);
            
            // Get recognized text with bounding boxes
            tessBaseAPI.setVariable("save_blob_choices", "T");
            
            // Get iterator for text with bounding boxes
            TessBaseAPI.ResultIterator iterator = tessBaseAPI.getResultIterator();
            int[] boundingBox = new int[4];
            
            iterator.begin();
            do {
                String text = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                float confidence = iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                
                if (text != null && text.toLowerCase().contains(targetText.toLowerCase())) {
                    if (confidence >= confidenceThreshold * 100) {
                        iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD, boundingBox);
                        int centerX = (boundingBox[0] + boundingBox[2]) / 2;
                        int centerY = (boundingBox[1] + boundingBox[3]) / 2;
                        
                        Log.d(TAG, "Text found: " + text + " at (" + centerX + ", " + centerY + ")");
                        return new int[]{centerX, centerY};
                    }
                }
            } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            
            iterator.delete();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in text recognition", e);
        }
        
        return null;
    }
    
    public String recognizeText(int x, int y, int width, int height) {
        try {
            if (tessBaseAPI == null || !isInitialized) {
                return "";
            }
            
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) return "";
            
            // Crop to region
            Bitmap cropped = Bitmap.createBitmap(screenshot, x, y, width, height);
            
            tessBaseAPI.setImage(cropped);
            String recognizedText = tessBaseAPI.getUTF8Text();
            
            return recognizedText != null ? recognizedText.trim() : "";
            
        } catch (Exception e) {
            Log.e(TAG, "Error recognizing text", e);
            return "";
        }
    }
    
    public List<TextRecognitionResult> recognizeAllText() {
        List<TextRecognitionResult> results = new ArrayList<>();
        
        try {
            if (tessBaseAPI == null || !isInitialized) {
                return results;
            }
            
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) return results;
            
            tessBaseAPI.setImage(screenshot);
            
            TessBaseAPI.ResultIterator iterator = tessBaseAPI.getResultIterator();
            int[] boundingBox = new int[4];
            
            iterator.begin();
            do {
                String text = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                float confidence = iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                
                if (text != null && !text.trim().isEmpty()) {
                    iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD, boundingBox);
                    
                    results.add(new TextRecognitionResult(
                        text.trim(),
                        confidence,
                        boundingBox[0],
                        boundingBox[1],
                        boundingBox[2] - boundingBox[0],
                        boundingBox[3] - boundingBox[1]
                    ));
                }
            } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            
            iterator.delete();
            
        } catch (Exception e) {
            Log.e(TAG, "Error recognizing all text", e);
        }
        
        return results;
    }
    
    // ==================== IMAGE RECOGNITION ====================
    
    public int[] findImage(String imagePath, double confidenceThreshold) {
        try {
            // Load template image
            Mat template = Imgcodecs.imread(imagePath);
            if (template.empty()) {
                Log.w(TAG, "Could not load template image: " + imagePath);
                return null;
            }
            
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) return null;
            
            Mat source = new Mat();
            Utils.bitmapToMat(screenshot, source);
            
            // Template matching
            Mat result = new Mat();
            Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);
            
            // Find best match
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            double maxVal = mmr.maxVal;
            Point maxLoc = mmr.maxLoc;
            
            // Cleanup
            source.release();
            template.release();
            result.release();
            
            if (maxVal >= confidenceThreshold) {
                int centerX = (int) (maxLoc.x + template.cols() / 2.0);
                int centerY = (int) (maxLoc.y + template.rows() / 2.0);
                return new int[]{centerX, centerY};
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in image recognition", e);
        }
        
        return null;
    }
    
    public List<int[]> findAllImages(String imagePath, double confidenceThreshold) {
        List<int[]> matches = new ArrayList<>();
        
        try {
            Mat template = Imgcodecs.imread(imagePath);
            if (template.empty()) return matches;
            
            Bitmap screenshot = takeScreenshot();
            if (screenshot == null) return matches;
            
            Mat source = new Mat();
            Utils.bitmapToMat(screenshot, source);
            
            Mat result = new Mat();
            Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);
            
            // Find all matches above threshold
            for (int y = 0; y < result.rows(); y++) {
                for (int x = 0; x < result.cols(); x++) {
                    double matchValue = result.get(y, x)[0];
                    if (matchValue >= confidenceThreshold) {
                        int centerX = x + template.cols() / 2;
                        int centerY = y + template.rows() / 2;
                        matches.add(new int[]{centerX, centerY, (int)(matchValue * 100)});
                    }
                }
            }
            
            source.release();
            template.release();
            result.release();
            
        } catch (Exception e) {
            Log.e(TAG, "Error finding all images", e);
        }
        
        return matches;
    }
    
    // ==================== PATTERN RECOGNITION ====================
    
    public void recordPattern(String patternName, List<TouchPoint> points) {
        recordedTouchPatterns.put(patternName, new ArrayList<>(points));
        Log.d(TAG, "Pattern recorded: " + patternName + " with " + points.size() + " points");
    }
    
    public boolean matchPattern(String patternName, List<TouchPoint> currentPoints, double tolerance) {
        List<TouchPoint> recordedPattern = recordedTouchPatterns.get(patternName);
        if (recordedPattern == null || currentPoints == null) {
            return false;
        }
        
        if (recordedPattern.size() != currentPoints.size()) {
            return false;
        }
        
        // Compare each point
        for (int i = 0; i < recordedPattern.size(); i++) {
            TouchPoint recorded = recordedPattern.get(i);
            TouchPoint current = currentPoints.get(i);
            
            double distance = Math.sqrt(
                Math.pow(recorded.x - current.x, 2) +
                Math.pow(recorded.y - current.y, 2)
            );
            
            if (distance > tolerance) {
                return false;
            }
        }
        
        return true;
    }
    
    public String findMatchingPattern(List<TouchPoint> currentPoints, double tolerance) {
        for (Map.Entry<String, List<TouchPoint>> entry : recordedTouchPatterns.entrySet()) {
            if (matchPattern(entry.getKey(), currentPoints, tolerance)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // ==================== EVENT RECOGNITION ====================
    
    public void processEvent(AccessibilityEvent event) {
        // Process accessibility events for event recognition
        for (RecognitionListener listener : listeners) {
            listener.onEventDetected(event);
        }
        
        // Check for popup detection
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            detectPopup(event);
        }
        
        // Check for text changes
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            detectTextChange(event);
        }
    }
    
    private void detectPopup(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode != null) {
            // Analyze window structure to detect popups
            List<AccessibilityNodeInfo> dialogs = findDialogs(rootNode);
            for (AccessibilityNodeInfo dialog : dialogs) {
                for (RecognitionListener listener : listeners) {
                    listener.onPopupDetected(dialog);
                }
            }
            rootNode.recycle();
        }
    }
    
    private void detectTextChange(AccessibilityEvent event) {
        CharSequence text = event.getText() != null && !event.getText().isEmpty() 
            ? event.getText().get(0) 
            : null;
        
        if (text != null) {
            for (RecognitionListener listener : listeners) {
                listener.onTextChanged(text.toString());
            }
        }
    }
    
    private List<AccessibilityNodeInfo> findDialogs(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> dialogs = new ArrayList<>();
        // Look for dialog-like structures
        // This is a simplified implementation
        return dialogs;
    }
    
    // ==================== SCREENSHOT ====================
    
    private Bitmap takeScreenshot() {
        try {
            // Use Python to take screenshot via Android APIs
            Python py = Python.getInstance();
            PyObject screenshotModule = py.getModule("screenshot");
            PyObject result = screenshotModule.callAttr("take_screenshot");
            
            if (result != null) {
                byte[] bytes = result.toJava(byte[].class);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error taking screenshot", e);
        }
        return null;
    }
    
    // ==================== LISTENERS ====================
    
    public void addListener(RecognitionListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(RecognitionListener listener) {
        listeners.remove(listener);
    }
    
    public interface RecognitionListener {
        void onColorFound(int x, int y, int color);
        void onTextFound(String text, int x, int y);
        void onImageFound(String imagePath, int x, int y);
        void onPatternMatched(String patternName);
        void onEventDetected(AccessibilityEvent event);
        void onPopupDetected(AccessibilityNodeInfo popup);
        void onTextChanged(String newText);
    }
    
    // ==================== DATA CLASSES ====================
    
    public static class TouchPoint {
        public float x;
        public float y;
        public long timestamp;
        public int action;
        
        public TouchPoint(float x, float y, long timestamp, int action) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
            this.action = action;
        }
    }
    
    public static class TextRecognitionResult {
        public String text;
        public float confidence;
        public int x;
        public int y;
        public int width;
        public int height;
        
        public TextRecognitionResult(String text, float confidence, int x, int y, int width, int height) {
            this.text = text;
            this.confidence = confidence;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    // ==================== CLEANUP ====================
    
    public void release() {
        if (tessBaseAPI != null) {
            tessBaseAPI.end();
            tessBaseAPI = null;
        }
        
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }
}
