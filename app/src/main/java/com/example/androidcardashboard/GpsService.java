package com.example.androidcardashboard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GpsService implements LocationListener {
    private static final String TAG = "GpsService";
    private static final long MIN_TIME_MS = 1000; // 1 second
    private static final float MIN_DISTANCE_M = 1.0f; // 1 meter
    private static final double MIN_SPEED_KMH = 1.0; // Minimum speed for trip calculations
    
    private Context context;
    private LocationManager locationManager;
    private Handler mainHandler;
    private GpsDataListener dataListener;
    
    private boolean isTracking = false;
    private boolean hasPermission = false;
    private boolean isConnected = false;
    private String status = "Disconnected";
    private Location lastLocation;
    private long lastUpdateTime;
    
    // Trip data
    private double totalDistance = 0.0;
    private double currentSpeed = 0.0;
    private double maxSpeed = 0.0;
    private double tripAverageSpeed = 0.0;
    private long tripStartTime = 0;
    private long tripAverageTime = 0;
    private double tripAverageDistance = 0.0;
    
    public interface GpsDataListener {
        void onGpsDataUpdate(double speed, double distance, double avgSpeed);
        void onGpsStatusChange(boolean connected, String status);
    }
    
    public GpsService(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        initializeGps();
    }
    
    public void setDataListener(GpsDataListener listener) {
        this.dataListener = listener;
    }
    
    private void initializeGps() {
        // Check if location services are enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            updateStatus(false, "GPS disabled");
            return;
        }
        
        // Check permissions (API 16 compatible)
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            updateStatus(false, "No location permission");
            return;
        }
        
        hasPermission = true;
        updateStatus(true, "GPS ready");
        startTracking();
    }
    
    public void startTracking() {
        if (isTracking || !hasPermission) return;
        
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                this
            );
            
            isTracking = true;
            tripStartTime = System.currentTimeMillis();
            updateStatus(true, "GPS tracking active");
            Log.d(TAG, "GPS tracking started");
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception starting GPS", e);
            updateStatus(false, "GPS permission denied");
        }
    }
    
    public void stopTracking() {
        if (!isTracking) return;
        
        locationManager.removeUpdates(this);
        isTracking = false;
        updateStatus(false, "GPS stopped");
        Log.d(TAG, "GPS tracking stopped");
    }
    
    @Override
    public void onLocationChanged(Location location) {
        long currentTime = System.currentTimeMillis();
        
        // Calculate speed (m/s to km/h)
        double speed = 0.0;
        if (location.hasSpeed()) {
            speed = location.getSpeed() * 3.6; // Convert m/s to km/h
        } else if (lastLocation != null && lastUpdateTime > 0) {
            // Calculate speed from position changes
            long timeDiff = currentTime - lastUpdateTime;
            if (timeDiff > 0) {
                float distance = lastLocation.distanceTo(location);
                speed = (distance / timeDiff) * 3.6; // Convert m/s to km/h
            }
        }
        
        currentSpeed = Math.max(0, Math.min(300, speed)); // Cap at 300 km/h
        
        // Calculate distance if we have a previous location
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location) / 1000.0f; // Convert to km
            
            // Only add distance if we're moving and distance is reasonable
            if (currentSpeed > MIN_SPEED_KMH && distance < 0.1) {
                totalDistance += distance;
                
                // Track distance and time for trip average (only at meaningful speeds)
                if (currentSpeed >= 10.0) { // Minimum 10 km/h for trip average
                    tripAverageDistance += distance;
                    
                    if (lastUpdateTime > 0) {
                        long timeDiff = currentTime - lastUpdateTime;
                        tripAverageTime += timeDiff;
                    }
                }
            }
        }
        
        // Update max speed
        if (currentSpeed > maxSpeed) {
            maxSpeed = currentSpeed;
        }
        
        // Calculate trip average speed
        if (tripAverageTime > 0 && tripAverageDistance > 0) {
            tripAverageSpeed = tripAverageDistance / (tripAverageTime / 3600000.0); // km/h
        }
        
        // Update UI on main thread
        if (dataListener != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    dataListener.onGpsDataUpdate(currentSpeed, totalDistance, tripAverageSpeed);
                }
            });
        }
        
        lastLocation = location;
        lastUpdateTime = currentTime;
        
        Log.d(TAG, String.format("GPS: Speed=%.1f km/h, Distance=%.3f km, Avg=%.1f km/h", 
            currentSpeed, totalDistance, tripAverageSpeed));
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "GPS status changed: " + provider + " = " + status);
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "GPS provider enabled: " + provider);
        if (hasPermission && !isTracking) {
            startTracking();
        }
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "GPS provider disabled: " + provider);
        updateStatus(false, "GPS disabled");
    }
    
    private void updateStatus(boolean connected, String status) {
        this.isConnected = connected;
        this.status = status;
        
        if (dataListener != null) {
            final boolean finalConnected = connected;
            final String finalStatus = status;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    dataListener.onGpsStatusChange(finalConnected, finalStatus);
                }
            });
        }
    }
    
    public void resetTrip() {
        totalDistance = 0.0;
        maxSpeed = 0.0;
        tripAverageSpeed = 0.0;
        tripStartTime = System.currentTimeMillis();
        tripAverageTime = 0;
        tripAverageDistance = 0.0;
        lastLocation = null;
        lastUpdateTime = 0;
        
        if (dataListener != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    dataListener.onGpsDataUpdate(0, 0, 0);
                }
            });
        }
    }
    
    public boolean isTracking() {
        return isTracking;
    }
    
    public boolean hasPermission() {
        return hasPermission;
    }
    
    public void cleanup() {
        stopTracking();
    }
}
