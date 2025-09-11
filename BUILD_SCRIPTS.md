# Android Car Dashboard - Build Scripts

This directory contains scripts to build, install, and run the Android Car Dashboard app on your device.

## Prerequisites

1. **Android SDK**: Make sure you have the Android SDK installed with platform-tools
2. **ADB**: Android Debug Bridge should be in your PATH
3. **Device**: Connect your Android device via USB and enable USB debugging
4. **Java**: Java 7 or higher (as specified in build.gradle)

## Scripts

### 1. `quick_build.sh` - Simple Build Script

The simplest way to build and deploy the app:

```bash
./quick_build.sh
```

This script will:

- Check if a device is connected
- Build the debug APK
- Install it on your device
- Launch the app

### 2. `build_and_deploy.sh` - Full-Featured Build Script

A comprehensive script with many options:

```bash
# Basic usage
./build_and_deploy.sh

# Clean build and deploy
./build_and_deploy.sh --clean

# Uninstall existing app first
./build_and_deploy.sh --uninstall

# Build release version
./build_and_deploy.sh --release

# Show logs after launching
./build_and_deploy.sh --logs

# Install only (don't launch)
./build_and_deploy.sh --no-launch

# Combine options
./build_and_deploy.sh --clean --uninstall --logs
```

#### Available Options

- `-h, --help`: Show help message
- `-c, --clean`: Clean build before building
- `-u, --uninstall`: Uninstall existing app before installing
- `-l, --logs`: Show app logs after launching
- `-r, --release`: Build release version instead of debug
- `-n, --no-launch`: Don't launch the app after installation

## Manual Commands

If you prefer to run commands manually:

```bash
# Build the app
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.example.androidcardashboard/.MainActivity

# View logs
adb logcat | grep com.example.androidcardashboard
```

## Troubleshooting

### No device found

- Make sure your device is connected via USB
- Enable USB debugging in Developer Options
- Check that ADB is installed and in your PATH

### Build fails

- Make sure you have the Android SDK installed
- Check that Java 7+ is installed
- Try running `./gradlew clean` first

### App doesn't install

- Make sure the device has enough storage
- Check that the device allows installation from unknown sources
- Try uninstalling the existing app first: `adb uninstall com.example.androidcardashboard`

### App doesn't launch

- Check the device logs: `adb logcat | grep com.example.androidcardashboard`
- Make sure the app has the required permissions
- Try launching manually from the device's app drawer

## Project Information

- **Package Name**: `com.example.androidcardashboard`
- **Main Activity**: `MainActivity`
- **Min SDK**: 16 (Android 4.1)
- **Target SDK**: 16
- **Build Tool**: Gradle 3.2.1

## Development Tips

1. **Fast Development**: Use `quick_build.sh` for rapid iteration
2. **Debugging**: Use `--logs` option to see real-time logs
3. **Clean Builds**: Use `--clean` if you encounter build issues
4. **Release Testing**: Use `--release` to test production builds
