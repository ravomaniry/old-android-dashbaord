package com.example.androidcardashboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Build;
import java.util.Random;
import java.util.List;

public class MainActivity extends Activity implements HttpService.HttpDataListener, StatusIndicatorView.OnStatusClickListener {
    // Dashboard data
    private double speed = 0.0;
    private double rpm = 0.0;
    private double coolantTemp = 82.0;
    private double fuelLevel = 65.0;
    private double tripDistance = 0.0;
    private double fuelUsage = 0.0;
    private double avgTemperature = 0.0;
    private double avgSpeed = 0.0;
    private double batteryVoltage = 12.6;
    
    // Status indicators
    private boolean oilWarning = false;
    private boolean drlOn = true;
    private boolean lowBeamOn = false;
    private boolean highBeamOn = false;
    private boolean leftTurnSignal = false;
    private boolean rightTurnSignal = false;
    private boolean hazardLights = false;
    private boolean reverseGear = false;
    private boolean httpConnected = true;
    
    // Custom Views
    private SpeedometerView speedometer;
    private GaugeView coolantGauge;
    private GaugeView fuelGauge;
    private StatusIndicatorView oilWarningIndicator;
    private StatusIndicatorView batteryIndicator;
    private StatusIndicatorView httpIndicator;
    private StatusIndicatorView drlIndicator;
    private StatusIndicatorView lowBeamIndicator;
    private StatusIndicatorView highBeamIndicator;
    private StatusIndicatorView hazardIndicator;
    private StatusIndicatorView leftTurnIndicator;
    private StatusIndicatorView rightTurnIndicator;
    private TripDetailView distanceDetail;
    private TripDetailView fuelUsageDetail;
    private TripDetailView avgTempDetail;
    private TripDetailView avgSpeedDetail;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private boolean demoMode = false;
    
    // Demo and theme button visibility
    private boolean showDemoButton = false;
    private boolean showThemeButton = false;
    private Handler buttonHideHandler = new Handler(Looper.getMainLooper());
    private Runnable hideButtonsRunnable;
    
    // Services
    private HttpService httpService;
    private ThemeManager themeManager;
    private PowerManager.WakeLock wakeLock;
    private TripCalculator tripCalculator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Log auto-startup
        android.util.Log.i("MainActivity", "Car Dashboard app started - onCreate");
        
        // Enable fullscreen mode
        setupFullscreen();
        
        setContentView(R.layout.activity_main_simple);
        
        initializeViews();
        setupTouchListeners();
        initializeServices();
        startDataSimulation();
        startFullscreenEnforcer();
    }
    
    private void initializeViews() {
        // Custom Views
        speedometer = (SpeedometerView) findViewById(R.id.speedometer);
        coolantGauge = (GaugeView) findViewById(R.id.coolant_gauge);
        fuelGauge = (GaugeView) findViewById(R.id.fuel_gauge);
        
        // Status indicators
        oilWarningIndicator = (StatusIndicatorView) findViewById(R.id.oil_warning_indicator);
        batteryIndicator = (StatusIndicatorView) findViewById(R.id.battery_indicator);
        httpIndicator = (StatusIndicatorView) findViewById(R.id.wifi_indicator);
        drlIndicator = (StatusIndicatorView) findViewById(R.id.drl_indicator);
        lowBeamIndicator = (StatusIndicatorView) findViewById(R.id.low_beam_indicator);
        highBeamIndicator = (StatusIndicatorView) findViewById(R.id.high_beam_indicator);
        hazardIndicator = (StatusIndicatorView) findViewById(R.id.hazard_indicator);
        leftTurnIndicator = (StatusIndicatorView) findViewById(R.id.left_turn_indicator);
        rightTurnIndicator = (StatusIndicatorView) findViewById(R.id.right_turn_indicator);
        
        // Trip details
        distanceDetail = (TripDetailView) findViewById(R.id.distance_detail);
        fuelUsageDetail = (TripDetailView) findViewById(R.id.fuel_usage_detail);
        avgTempDetail = (TripDetailView) findViewById(R.id.avg_temp_detail);
        avgSpeedDetail = (TripDetailView) findViewById(R.id.avg_speed_detail);
        
        // Initialize gauge ranges and labels
        coolantGauge.setRange(60, 120);
        coolantGauge.setUnit("°C");
        coolantGauge.setLabel("TEMP");
        coolantGauge.setGaugeType(GaugeView.GaugeType.TEMPERATURE);
        
        fuelGauge.setRange(0, 100);
        fuelGauge.setUnit("%");
        fuelGauge.setLabel("FUEL");
        fuelGauge.setGaugeType(GaugeView.GaugeType.FUEL);
        
        
        // Initialize status indicator labels
        oilWarningIndicator.setLabel(getString(R.string.oil_warning));
        oilWarningIndicator.setAlwaysShowColor(true); // Always show color for oil indicator
        batteryIndicator.setLabel(getString(R.string.battery));
        httpIndicator.setLabel(getString(R.string.wifi));
        drlIndicator.setLabel(getString(R.string.drl));
        lowBeamIndicator.setLabel(getString(R.string.low_beam));
        highBeamIndicator.setLabel(getString(R.string.high_beam));
        hazardIndicator.setLabel(getString(R.string.hazard));
        leftTurnIndicator.setLabel(getString(R.string.left_turn));
        rightTurnIndicator.setLabel(getString(R.string.right_turn));
        
        // Set up status click listeners
        httpIndicator.setOnStatusClickListener(this);
        
        // Force text size update for all indicators
        oilWarningIndicator.updateTextSize();
        batteryIndicator.updateTextSize();
        httpIndicator.updateTextSize();
        drlIndicator.updateTextSize();
        lowBeamIndicator.updateTextSize();
        highBeamIndicator.updateTextSize();
        hazardIndicator.updateTextSize();
        leftTurnIndicator.updateTextSize();
        rightTurnIndicator.updateTextSize();
        
        // Initialize trip detail labels
        distanceDetail.setLabel(getString(R.string.distance));
        fuelUsageDetail.setLabel(getString(R.string.fuel_use));
        avgTempDetail.setLabel(getString(R.string.avg_temp));
        avgSpeedDetail.setLabel(getString(R.string.avg_speed));
        
        // Set up speedometer button click listener
        speedometer.setButtonClickListener(new SpeedometerView.OnButtonClickListener() {
            @Override
            public void onDemoButtonClick() {
                MainActivity.this.onDemoButtonClick();
            }
            
            @Override
            public void onThemeButtonClick() {
                MainActivity.this.onThemeButtonClick();
            }
        });
    }
    
    private void initializeServices() {
        // Initialize theme manager
        themeManager = new ThemeManager(this);
        
        // Initialize trip calculator
        tripCalculator = new TripCalculator();
        
        // Initialize HTTP service
        httpService = new HttpService(this);
        httpService.setDataListener(this);
    }
    
    
    private void startDataSimulation() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateDashboardData();
                updateUI();
                handler.postDelayed(this, 1000); // Update every second
            }
        });
    }
    
    private void updateDashboardData() {
        if (demoMode) {
            // Simulate realistic car data only if no real data is available
            if (!httpConnected) {
                speed = Math.max(0, speed + (random.nextDouble() - 0.5) * 10);
                speed = Math.min(120, speed);
                
                // Simulate trip data for demo mode
                tripDistance += speed / 3600; // km per second
                avgSpeed = speed * 0.8 + random.nextDouble() * 10;
                
                // Simulate fuel usage and temperature
                if (speed > 0) {
                    fuelUsage = 5.5 + (speed / 20) + random.nextDouble() * 2;
                    avgTemperature = 20 + (speed / 15) + random.nextDouble() * 10;
                }
            }
            
            if (!httpService.isConnected()) {
                rpm = speed * 100 + random.nextDouble() * 500;
                rpm = Math.max(800, Math.min(6000, rpm));
                
                coolantTemp = 80 + random.nextDouble() * 20;
                coolantTemp = Math.max(75, Math.min(105, coolantTemp));
                
                fuelLevel = Math.max(0, fuelLevel - random.nextDouble() * 0.1);
                
                // Simulate status changes
                if (random.nextDouble() < 0.1) {
                    oilWarning = !oilWarning;
                }
                
                batteryVoltage = 12.0 + random.nextDouble() * 2.0;
                
                // Simulate turn signals
                if (random.nextDouble() < 0.05) {
                    leftTurnSignal = !leftTurnSignal;
                }
                if (random.nextDouble() < 0.05) {
                    rightTurnSignal = !rightTurnSignal;
                }
            }
            
            fuelUsage = 5.5 + random.nextDouble() * 2.0;
            avgTemperature = 20 + random.nextDouble() * 10;
        }
    }
    
    private void updateUI() {
        // Update speedometer
        speedometer.setSpeed((float) speed);
        speedometer.setRpm((float) rpm);
        speedometer.setReverseGear(reverseGear);
        
        // Update gauges
        coolantGauge.setValue((float) coolantTemp);
        fuelGauge.setValue((float) fuelLevel);
        
        // Update trip details
        distanceDetail.setValue(String.format("%.1f km", tripDistance));
        fuelUsageDetail.setValue(String.format("%.1f L/100km", fuelUsage));
        avgTempDetail.setValue(String.format("%.0f°C", avgTemperature));
        avgSpeedDetail.setValue(String.format("%.1f km/h", avgSpeed));
        
        // Update status indicators
        oilWarningIndicator.setActive(oilWarning);
        updateOilIndicatorColor(); // Set proper color based on oil state
        updateBatteryIndicator();
        httpIndicator.setActive(httpConnected);
        drlIndicator.setActive(drlOn);
        lowBeamIndicator.setActive(lowBeamOn);
        highBeamIndicator.setActive(highBeamOn);
        hazardIndicator.setActive(hazardLights);
        hazardIndicator.setBlinking(hazardLights);
        leftTurnIndicator.setActive(leftTurnSignal);
        leftTurnIndicator.setBlinking(leftTurnSignal);
        rightTurnIndicator.setActive(rightTurnSignal);
        rightTurnIndicator.setBlinking(rightTurnSignal);
    }
    
    private void updateBatteryIndicator() {
        if (batteryVoltage < 11.5) {
            batteryIndicator.setActiveColor(themeManager.getDangerColor()); // Red
        } else if (batteryVoltage < 12.2) {
            batteryIndicator.setActiveColor(themeManager.getWarningColor()); // Orange
        } else {
            batteryIndicator.setActiveColor(themeManager.getSuccessColor()); // Green
        }
        batteryIndicator.setActive(true);
    }
    
    private void updateOilIndicatorColor() {
        if (oilWarning) {
            // Critical state - Red
            oilWarningIndicator.setActiveColor(themeManager.getDangerColor());
        } else {
            // OK state - Green
            oilWarningIndicator.setActiveColor(themeManager.getSuccessColor());
        }
    }
    
    
    private void updateTheme() {
        // Update colors based on current theme
        int primaryColor = themeManager.getPrimaryAccentColor();
        int secondaryColor = themeManager.getSecondaryAccentColor();
        int backgroundColor = themeManager.getBackgroundColor();
        int containerColor = themeManager.getContainerColor();
        int textPrimaryColor = themeManager.getTextPrimaryColor();
        int textSecondaryColor = themeManager.getTextSecondaryColor();
        
        // Debug: Log theme change
        android.util.Log.d("ThemeManager", "Theme changed to: " + themeManager.getThemeName());
        
        // Update speedometer theme
        speedometer.updateThemeColors();
        speedometer.setGaugeStyle(themeManager.getGaugeStyle());
        speedometer.setFont(themeManager.getBoldFont());
        
        // Update gauge themes
        coolantGauge.updateThemeColors();
        coolantGauge.setGaugeStyle(themeManager.getGaugeStyle());
        coolantGauge.setFont(themeManager.getBoldFont());
        fuelGauge.updateThemeColors();
        fuelGauge.setGaugeStyle(themeManager.getGaugeStyle());
        fuelGauge.setFont(themeManager.getBoldFont());
        
        // Update trip detail themes
        distanceDetail.updateThemeColors();
        distanceDetail.setFont(themeManager.getBoldFont());
        fuelUsageDetail.updateThemeColors();
        fuelUsageDetail.setFont(themeManager.getBoldFont());
        avgTempDetail.updateThemeColors();
        avgTempDetail.setFont(themeManager.getBoldFont());
        avgSpeedDetail.updateThemeColors();
        avgSpeedDetail.setFont(themeManager.getBoldFont());
        
        // Update status indicator colors
        updateOilIndicatorColor();
        batteryIndicator.updateThemeColors();
        httpIndicator.updateThemeColors();
        
        drlIndicator.updateThemeColors();
        lowBeamIndicator.updateThemeColors();
        highBeamIndicator.updateThemeColors();
        hazardIndicator.updateThemeColors();
        leftTurnIndicator.updateThemeColors();
        rightTurnIndicator.updateThemeColors();
        
        // Update background colors
        findViewById(android.R.id.content).setBackgroundColor(backgroundColor);
        
        // Update status box styling based on theme
        updateStatusBoxStyling();
    }
    
    private void updateStatusBoxStyling() {
        // Find the status box containers
        LinearLayout systemStatusBox = (LinearLayout) findViewById(R.id.system_status_box);
        LinearLayout vehicleStatusBox = (LinearLayout) findViewById(R.id.vehicle_status_box);
        
        // Find the text labels
        TextView systemStatusLabel = (TextView) findViewById(R.id.system_status_label);
        TextView vehicleStatusLabel = (TextView) findViewById(R.id.vehicle_status_label);
        
        if (themeManager.getCurrentTheme() == ThemeManager.ThemeType.ANALOG) {
            // Analog theme: Remove backgrounds, borders, hide text labels, but keep icons visible
            if (systemStatusBox != null) {
                systemStatusBox.setBackground(null);
                systemStatusBox.setPadding(0, 0, 0, 0);
                systemStatusBox.setVisibility(View.VISIBLE); // Keep system status box visible
            }
            if (vehicleStatusBox != null) {
                vehicleStatusBox.setBackground(null);
                vehicleStatusBox.setPadding(0, 0, 0, 0);
                vehicleStatusBox.setVisibility(View.VISIBLE); // Keep vehicle status box visible
            }
            if (systemStatusLabel != null) {
                systemStatusLabel.setVisibility(View.GONE); // Hide text label only
            }
            if (vehicleStatusLabel != null) {
                vehicleStatusLabel.setVisibility(View.GONE); // Hide text label only
            }
        } else {
            // Other themes: Restore backgrounds, borders, show text labels, and show status indicators
            int paddingPx = (int) (12 * getResources().getDisplayMetrics().density); // Convert 12dp to pixels
            if (systemStatusBox != null) {
                systemStatusBox.setBackgroundResource(R.drawable.status_box_background);
                systemStatusBox.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                systemStatusBox.setVisibility(View.VISIBLE); // Show system status box
            }
            if (vehicleStatusBox != null) {
                vehicleStatusBox.setBackgroundResource(R.drawable.status_box_background);
                vehicleStatusBox.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                vehicleStatusBox.setVisibility(View.VISIBLE); // Show vehicle status box
            }
            if (systemStatusLabel != null) {
                systemStatusLabel.setVisibility(View.VISIBLE);
            }
            if (vehicleStatusLabel != null) {
                vehicleStatusLabel.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void setupFullscreen() {
        // Hide the action bar
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        
        // Set fullscreen flags - more comprehensive approach
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        );
        
        // For all Android versions, try to hide navigation
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );
        
        // For Android 4.4+ (API 19+), add immersive mode
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | 0x00001000  // SYSTEM_UI_FLAG_IMMERSIVE_STICKY for API 19+
            );
        }
        
        // Acquire wake lock to keep screen on
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "CarDashboard::WakeLock");
        wakeLock.acquire();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        
        if (hasFocus) {
            // Re-enable fullscreen when window gains focus - more aggressive approach
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
            );
            
            // For Android 4.4+ (API 19+), add immersive mode
            if (Build.VERSION.SDK_INT >= 19) {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | 0x00001000  // SYSTEM_UI_FLAG_IMMERSIVE_STICKY for API 19+
                );
            }
        }
    }
    
    private void startFullscreenEnforcer() {
        // Periodically enforce fullscreen mode to ensure navigation stays hidden
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Force fullscreen mode
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                );
                
                // For Android 4.4+ (API 19+), add immersive mode
                if (Build.VERSION.SDK_INT >= 19) {
                    getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | 0x00001000  // SYSTEM_UI_FLAG_IMMERSIVE_STICKY for API 19+
                    );
                }
                
                // Schedule next enforcement in 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }
    
    private void setupTouchListeners() {
        // Show demo and theme buttons when speedometer is tapped
        speedometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDemoAndThemeButtons();
            }
        });
    }
    
    private void showDemoAndThemeButtons() {
        // Cancel any existing hide timer
        if (hideButtonsRunnable != null) {
            buttonHideHandler.removeCallbacks(hideButtonsRunnable);
        }
        
        // Show buttons on speedometer
        speedometer.setShowDemoButton(true);
        speedometer.setShowThemeButton(true);
        
        // Hide buttons after 5 seconds
        hideButtonsRunnable = new Runnable() {
            @Override
            public void run() {
                speedometer.setShowDemoButton(false);
                speedometer.setShowThemeButton(false);
            }
        };
        buttonHideHandler.postDelayed(hideButtonsRunnable, 5000);
    }
    
    public void onDemoButtonClick() {
        if (demoMode) {
            // Stop demo mode
            demoMode = false;
            // Reset to real data
            speed = 0;
            tripDistance = 0;
            rpm = 0;
            coolantTemp = 82.0;
            fuelLevel = 65.0;
        } else {
            // Start demo mode with animated values
            demoMode = true;
            startDemoAnimation();
        }
        updateUI();
    }
    
    public void onThemeButtonClick() {
        themeManager.cycleTheme();
        updateTheme();
    }
    
    private void startDemoAnimation() {
        if (!demoMode) return;
        
        // Reset values to start from minimum
        speed = 0;
        rpm = 0;
        coolantTemp = 60;
        fuelLevel = 100;
        tripDistance = 0;
        fuelUsage = 0;
        avgTemperature = 20;
        avgSpeed = 0;
        oilWarning = false;
        batteryVoltage = 12.0;
        leftTurnSignal = false;
        rightTurnSignal = false;
        reverseGear = false;
        
        final long startTime = System.currentTimeMillis();
        final long cycleDuration = 10000; // 10 seconds for full cycle
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (demoMode) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    float cycleProgress = (elapsed % cycleDuration) / (float) cycleDuration;
                    
                    // Create smooth sine wave animation (0 to 1 and back to 0)
                    float animationProgress = (float) (Math.sin(cycleProgress * 2 * Math.PI - Math.PI/2) + 1) / 2;
                    
                    // Animate speed from 0 to 120 km/h and back
                    speed = animationProgress * 120;
                    
                    // Animate RPM from 0 to 6000 and back
                    rpm = animationProgress * 6000;
                    
                    // Animate coolant temp from 60 to 120°C and back
                    coolantTemp = 60 + animationProgress * 60;
                    
                    // Animate fuel level from 100 to 0 and back
                    fuelLevel = 100 - animationProgress * 100;
                    
                    // Animate trip distance (continuously increasing)
                    tripDistance = (elapsed / 1000.0) * 10; // 10 km per second
                    
                    // Animate other values
                    fuelUsage = animationProgress * 10; // 0 to 10 L/100km
                    avgTemperature = 20 + animationProgress * 20; // 20 to 40°C
                    avgSpeed = animationProgress * 100; // 0 to 100 km/h
                    
                    // Animate status indicators based on animation progress
                    oilWarning = animationProgress > 0.7; // Warning at high values
                    batteryVoltage = 12.0 + animationProgress * 2.0; // 12 to 14V
                    
                    // Animate all 6 light icons
                    drlOn = animationProgress > 0.1; // DRL on most of the time
                    lowBeamOn = animationProgress > 0.4 && animationProgress < 0.8; // Low beam in middle range
                    highBeamOn = animationProgress > 0.6 && animationProgress < 0.9; // High beam in upper range
                    hazardLights = animationProgress > 0.8; // Hazard lights at high values
                    
                    // Animate turn signals with different patterns
                    if (animationProgress > 0.3 && animationProgress < 0.5) {
                        leftTurnSignal = true;
                        rightTurnSignal = false;
                    } else if (animationProgress > 0.7 && animationProgress < 0.9) {
                        leftTurnSignal = false;
                        rightTurnSignal = true;
                    } else {
                        leftTurnSignal = false;
                        rightTurnSignal = false;
                    }
                    
                    // Animate reverse gear - engage when speed is low and animation is in certain range
                    reverseGear = (animationProgress > 0.1 && animationProgress < 0.2) || 
                                 (animationProgress > 0.8 && animationProgress < 0.9);
                    
                    updateUI();
                    
                    // Continue animation
                    handler.postDelayed(this, 50); // Update every 50ms for smoother animation
                }
            }
        });
    }
    
    
    // HTTP Service Callbacks
    @Override
    public void onHttpDataUpdate(double speed, double rpm, double coolantTemp, double fuelLevel, 
                                boolean oilWarning, double batteryVoltage, boolean drlOn, 
                                boolean lowBeamOn, boolean highBeamOn, boolean leftTurnSignal, 
                                boolean rightTurnSignal, boolean hazardLights, boolean reverseGear, String location) {
        this.speed = speed;
        this.rpm = rpm;
        this.coolantTemp = coolantTemp;
        this.fuelLevel = fuelLevel;
        this.oilWarning = oilWarning;
        this.batteryVoltage = batteryVoltage;
        this.drlOn = drlOn;
        this.lowBeamOn = lowBeamOn;
        this.highBeamOn = highBeamOn;
        this.leftTurnSignal = leftTurnSignal;
        this.rightTurnSignal = rightTurnSignal;
        this.hazardLights = hazardLights;
        this.reverseGear = reverseGear;
        
        // Update trip calculator with location and current data
        if (tripCalculator != null) {
            tripCalculator.updateLocation(location, speed, coolantTemp, fuelLevel);
            
            // Get calculated trip metrics
            this.tripDistance = tripCalculator.getTotalDistance();
            this.fuelUsage = tripCalculator.getFuelUsage();
            this.avgTemperature = tripCalculator.getAvgTemperature();
            this.avgSpeed = tripCalculator.getAvgSpeed();
        }
        
        updateUI();
    }
    
    @Override
    public void onHttpStatusChange(boolean connected, String status) {
        httpConnected = connected;
        updateUI();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.d("MainActivity", "App paused - but keeping it running for car use");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MainActivity", "App resumed");
        
        // Ensure fullscreen mode is maintained
        setupFullscreen();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button from closing the app in car mode
        android.util.Log.d("MainActivity", "Back button pressed - ignoring for car mode");
        // Don't call super.onBackPressed() to prevent app from closing
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.i("MainActivity", "Car Dashboard app destroyed");
        
        handler.removeCallbacksAndMessages(null);
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // Cleanup services
        if (httpService != null) {
            httpService.cleanup();
        }
        if (tripCalculator != null) {
            tripCalculator.resetTrip();
        }
        
    }
    
    public void resetTrip() {
        if (tripCalculator != null) {
            tripCalculator.resetTrip();
            tripDistance = 0.0;
            fuelUsage = 0.0;
            avgTemperature = 0.0;
            avgSpeed = 0.0;
            updateUI();
            android.util.Log.d("MainActivity", "Trip reset");
        }
    }
    
    // Status click handling
    @Override
    public void onStatusClick(String statusType) {
        if (getString(R.string.wifi).equals(statusType)) {
            showHttpDialog();
        }
    }
    
    private void showHttpDialog() {
        String status = httpService != null ? httpService.getStatus() : "HTTP Service not available";
        List<EventManager.HttpEvent> events = EventManager.getInstance().getLatestHttpEvents();
        
        // Generate current JSON data
        String currentJsonData = generateCurrentJsonData();
        
        ServiceStatusDialog dialog = new ServiceStatusDialog(
            this,
            "WiFi Service",
            android.R.drawable.ic_dialog_info,
            status,
            events,
            new ServiceStatusDialog.OnActionClickListener() {
                @Override
                public void onConnectClick() {
                    if (httpService != null) {
                        httpService.connectToServer();
                    }
                }
                
                @Override
                public void onDisconnectClick() {
                    if (httpService != null) {
                        httpService.disconnect();
                    }
                }
                
                @Override
                public void onRetryClick() {
                    if (httpService != null) {
                        httpService.connectToServer();
                    }
                }
                
            },
            currentJsonData
        );
        
        Dialog dialogInstance = dialog.createDialog();
        dialogInstance.show();
    }
    
    private String generateCurrentJsonData() {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
            json.append("  \"speed\": ").append(String.format("%.1f", speed)).append(",\n");
            json.append("  \"rpm\": ").append(String.format("%.0f", rpm)).append(",\n");
            json.append("  \"coolantTemp\": ").append(String.format("%.1f", coolantTemp)).append(",\n");
            json.append("  \"fuelLevel\": ").append(String.format("%.1f", fuelLevel)).append(",\n");
            json.append("  \"oilWarning\": ").append(oilWarning).append(",\n");
            json.append("  \"batteryVoltage\": ").append(String.format("%.1f", batteryVoltage)).append(",\n");
            json.append("  \"drlOn\": ").append(drlOn).append(",\n");
            json.append("  \"lowBeamOn\": ").append(lowBeamOn).append(",\n");
            json.append("  \"highBeamOn\": ").append(highBeamOn).append(",\n");
            json.append("  \"leftTurnSignal\": ").append(leftTurnSignal).append(",\n");
            json.append("  \"rightTurnSignal\": ").append(rightTurnSignal).append(",\n");
            json.append("  \"hazardLights\": ").append(hazardLights).append(",\n");
            json.append("  \"reverseGear\": ").append(reverseGear).append(",\n");
            json.append("  \"tripDistance\": ").append(String.format("%.1f", tripDistance)).append(",\n");
            json.append("  \"fuelUsage\": ").append(String.format("%.1f", fuelUsage)).append(",\n");
            json.append("  \"avgTemperature\": ").append(String.format("%.1f", avgTemperature)).append(",\n");
            json.append("  \"avgSpeed\": ").append(String.format("%.1f", avgSpeed)).append(",\n");
            json.append("  \"httpConnected\": ").append(httpConnected).append(",\n");
            json.append("  \"demoMode\": ").append(demoMode).append("\n");
            json.append("}");
            
            return json.toString();
        } catch (Exception e) {
            return "{\n  \"error\": \"Failed to format JSON\"\n}";
        }
    }
    
}
