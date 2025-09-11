#!/bin/bash

# Quick Build and Deploy Script for Android Car Dashboard
# Simple version for fast development cycles

set -e

echo "🚗 Building Android Car Dashboard..."

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected. Please connect your device and enable USB debugging."
    exit 1
fi

# Build the app
echo "🔨 Building app..."
./gradlew assembleDebug

# Install and launch
echo "📱 Installing and launching app..."
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.androidcardashboard/.MainActivity

echo "✅ Done! App is running on your device."
echo "📊 To view logs: adb logcat | grep com.example.androidcardashboard"
