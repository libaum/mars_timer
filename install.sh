#!/bin/bash

# Build the debug APK
echo "Building APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

# Install the APK
echo "Installing APK..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -ne 0 ]; then
    echo "Install failed!"
    exit 1
fi

# Launch the app
echo "Launching App..."
adb shell monkey -p com.cloudcatcher.marstimer -c android.intent.category.LAUNCHER 1

echo "Done!"
