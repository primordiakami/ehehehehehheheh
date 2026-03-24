#!/bin/bash

# AI Auto Clicker Build Script
# This script builds the APK for the AI Auto Clicker application

echo "=================================="
echo "AI Auto Clicker Build Script"
echo "=================================="
echo ""

# Check if Android SDK is set
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "Warning: ANDROID_SDK_ROOT or ANDROID_HOME not set"
    echo "Using default: /opt/android-sdk"
    export ANDROID_SDK_ROOT=/opt/android-sdk
fi

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo ""
echo "Building Debug APK..."
./gradlew assembleDebug

# Check if build succeeded
if [ $? -eq 0 ]; then
    echo ""
    echo "=================================="
    echo "Build Successful!"
    echo "=================================="
    echo ""
    echo "Debug APK location:"
    echo "  app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Build release APK
    echo "Building Release APK..."
    ./gradlew assembleRelease
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "Release APK location:"
        echo "  app/build/outputs/apk/release/app-release-unsigned.apk"
        echo ""
        echo "To sign the release APK, use:"
        echo "  apksigner sign --ks my-key.jks app-release-unsigned.apk"
    else
        echo "Release build failed!"
    fi
else
    echo ""
    echo "Build Failed!"
    echo "Check the error messages above."
    exit 1
fi

echo ""
echo "Done!"
