# ProGuard rules for AI Auto Clicker

# Keep public classes and methods
-keep public class * {
    public protected *;
}

# Keep accessibility service
-keep class com.aiautoclicker.service.AutoClickerAccessibilityService {
    *;
}

# Keep Python bridge
-keep class com.aiautoclicker.python.PythonBridge {
    *;
}

# Keep task classes
-keep class com.aiautoclicker.tasks.** {
    *;
}

# Keep recognition classes
-keep class com.aiautoclicker.recognition.** {
    *;
}

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep OpenCV
-keep class org.opencv.** { *; }

# Keep Tesseract
-keep class com.googlecode.tesseract.android.** { *; }

# Keep Chaquopy
-keep class com.chaquo.python.** { *; }

# Keep AndroidX
-keep class androidx.** { *; }

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
