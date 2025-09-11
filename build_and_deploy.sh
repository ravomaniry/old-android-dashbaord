#!/bin/bash

# Android Car Dashboard - Build and Deploy Script
# This script builds the app, installs it on a connected device, and launches it

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_PACKAGE="com.example.androidcardashboard"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
BUILD_TYPE="debug"  # Change to "release" for production builds

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if device is connected
check_device() {
    print_status "Checking for connected Android devices..."
    
    if ! command -v adb &> /dev/null; then
        print_error "ADB (Android Debug Bridge) is not installed or not in PATH"
        print_error "Please install Android SDK platform-tools"
        exit 1
    fi
    
    # Get list of connected devices
    devices=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
    
    if [ "$devices" -eq 0 ]; then
        print_error "No Android devices connected"
        print_error "Please connect your device and enable USB debugging"
        exit 1
    elif [ "$devices" -gt 1 ]; then
        print_warning "Multiple devices connected. Using the first one."
    fi
    
    # Get device info
    device_id=$(adb devices | grep -v "List of devices" | grep -v "^$" | head -n1 | cut -f1)
    print_success "Found device: $device_id"
}

# Function to clean previous build
clean_build() {
    print_status "Cleaning previous build..."
    ./gradlew clean
    print_success "Build cleaned"
}

# Function to build the app
build_app() {
    print_status "Building Android app ($BUILD_TYPE)..."
    
    if [ "$BUILD_TYPE" = "debug" ]; then
        ./gradlew assembleDebug
    else
        ./gradlew assembleRelease
    fi
    
    if [ ! -f "$APK_PATH" ]; then
        print_error "APK file not found at $APK_PATH"
        print_error "Build failed!"
        exit 1
    fi
    
    print_success "App built successfully"
    print_status "APK location: $APK_PATH"
}

# Function to uninstall existing app (optional)
uninstall_existing() {
    print_status "Checking for existing installation..."
    
    if adb shell pm list packages | grep -q "$APP_PACKAGE"; then
        print_warning "App already installed. Uninstalling previous version..."
        adb uninstall "$APP_PACKAGE" || print_warning "Failed to uninstall (app might not be installed)"
    else
        print_status "No existing installation found"
    fi
}

# Function to install the app
install_app() {
    print_status "Installing app on device..."
    
    adb install "$APK_PATH"
    
    if [ $? -eq 0 ]; then
        print_success "App installed successfully"
    else
        print_error "Failed to install app"
        exit 1
    fi
}

# Function to launch the app
launch_app() {
    print_status "Launching app..."
    
    # Start the main activity
    adb shell am start -n "$APP_PACKAGE/.MainActivity"
    
    if [ $? -eq 0 ]; then
        print_success "App launched successfully"
    else
        print_error "Failed to launch app"
        exit 1
    fi
}

# Function to show app logs (optional)
show_logs() {
    print_status "Showing app logs (press Ctrl+C to stop)..."
    print_status "Filtering logs for package: $APP_PACKAGE"
    echo ""
    adb logcat | grep --line-buffered "$APP_PACKAGE"
}

# Function to display help
show_help() {
    echo "Android Car Dashboard - Build and Deploy Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help          Show this help message"
    echo "  -c, --clean         Clean build before building"
    echo "  -u, --uninstall     Uninstall existing app before installing"
    echo "  -l, --logs          Show app logs after launching"
    echo "  -r, --release       Build release version instead of debug"
    echo "  -n, --no-launch     Don't launch the app after installation"
    echo ""
    echo "Examples:"
    echo "  $0                  # Build debug, install and launch"
    echo "  $0 -c -u            # Clean build, uninstall existing, install and launch"
    echo "  $0 -r -l            # Build release, install, launch and show logs"
    echo "  $0 -n               # Build and install only (don't launch)"
}

# Parse command line arguments
CLEAN_BUILD=false
UNINSTALL_EXISTING=false
SHOW_LOGS=false
NO_LAUNCH=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -c|--clean)
            CLEAN_BUILD=true
            shift
            ;;
        -u|--uninstall)
            UNINSTALL_EXISTING=true
            shift
            ;;
        -l|--logs)
            SHOW_LOGS=true
            shift
            ;;
        -r|--release)
            BUILD_TYPE="release"
            APK_PATH="app/build/outputs/apk/release/app-release.apk"
            shift
            ;;
        -n|--no-launch)
            NO_LAUNCH=true
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main execution
main() {
    echo "=========================================="
    echo "Android Car Dashboard - Build & Deploy"
    echo "=========================================="
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "gradlew" ]; then
        print_error "gradlew not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Make gradlew executable
    chmod +x gradlew
    
    # Check for connected device
    check_device
    
    # Clean build if requested
    if [ "$CLEAN_BUILD" = true ]; then
        clean_build
    fi
    
    # Build the app
    build_app
    
    # Uninstall existing app if requested
    if [ "$UNINSTALL_EXISTING" = true ]; then
        uninstall_existing
    fi
    
    # Install the app
    install_app
    
    # Launch the app (unless --no-launch is specified)
    if [ "$NO_LAUNCH" = false ]; then
        launch_app
    fi
    
    # Show logs if requested
    if [ "$SHOW_LOGS" = true ]; then
        echo ""
        show_logs
    else
        echo ""
        print_success "Deployment completed successfully!"
        print_status "To view logs, run: adb logcat | grep $APP_PACKAGE"
    fi
}

# Run main function
main "$@"
