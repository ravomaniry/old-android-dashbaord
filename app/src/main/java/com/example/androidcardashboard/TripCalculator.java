package com.example.androidcardashboard;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class TripCalculator {
    private static final String TAG = "TripCalculator";
    
    // Location data
    private List<LocationPoint> locationHistory = new ArrayList<>();
    private LocationPoint lastLocation = null;
    
    // Trip metrics
    private double totalDistance = 0.0;
    private double totalFuelUsed = 0.0;
    private double totalTemperature = 0.0;
    private double totalSpeed = 0.0;
    private int dataPoints = 0;
    private long tripStartTime = 0;
    
    // Current trip data
    private double currentFuelLevel = 100.0;
    private double initialFuelLevel = 100.0;
    
    public static class LocationPoint {
        public double latitude;
        public double longitude;
        public long timestamp;
        public double speed;
        public double temperature;
        public double fuelLevel;
        
        public LocationPoint(double lat, double lon, long time, double spd, double temp, double fuel) {
            this.latitude = lat;
            this.longitude = lon;
            this.timestamp = time;
            this.speed = spd;
            this.temperature = temp;
            this.fuelLevel = fuel;
        }
    }
    
    public static class TripMetrics {
        public double distance;
        public double fuelUsage;
        public double avgTemperature;
        public double avgSpeed;
        
        public TripMetrics(double dist, double fuel, double avgTemp, double avgSpd) {
            this.distance = dist;
            this.fuelUsage = fuel;
            this.avgTemperature = avgTemp;
            this.avgSpeed = avgSpd;
        }
    }
    
    public TripCalculator() {
        resetTrip();
    }
    
    public void resetTrip() {
        locationHistory.clear();
        lastLocation = null;
        totalDistance = 0.0;
        totalFuelUsed = 0.0;
        totalTemperature = 0.0;
        totalSpeed = 0.0;
        dataPoints = 0;
        tripStartTime = System.currentTimeMillis();
        currentFuelLevel = 100.0;
        initialFuelLevel = 100.0;
        Log.d(TAG, "Trip reset");
    }
    
    public void updateLocation(String locationString, double speed, double temperature, double fuelLevel) {
        if (locationString == null || locationString.isEmpty()) {
            return;
        }
        
        try {
            // Parse comma-separated location string (lat,lon)
            String[] parts = locationString.split(",");
            if (parts.length != 2) {
                Log.w(TAG, "Invalid location format: " + locationString);
                return;
            }
            
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            long timestamp = System.currentTimeMillis();
            
            LocationPoint newLocation = new LocationPoint(latitude, longitude, timestamp, speed, temperature, fuelLevel);
            
            // Calculate distance from last location
            if (lastLocation != null) {
                double distance = calculateDistance(lastLocation, newLocation);
                totalDistance += distance;
                
                Log.d(TAG, String.format("Distance: %.2f km, Total: %.2f km", distance, totalDistance));
            }
            
            // Update fuel usage
            if (fuelLevel < currentFuelLevel) {
                double fuelUsed = currentFuelLevel - fuelLevel;
                totalFuelUsed += fuelUsed;
                Log.d(TAG, String.format("Fuel used: %.2f%%, Total: %.2f%%", fuelUsed, totalFuelUsed));
            }
            currentFuelLevel = fuelLevel;
            
            // Update averages
            totalTemperature += temperature;
            totalSpeed += speed;
            dataPoints++;
            
            // Store location
            locationHistory.add(newLocation);
            lastLocation = newLocation;
            
            // Keep only last 1000 points to prevent memory issues
            if (locationHistory.size() > 1000) {
                locationHistory.remove(0);
            }
            
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse location: " + locationString, e);
        }
    }
    
    public TripMetrics getTripMetrics() {
        double fuelUsage = 0.0;
        if (totalDistance > 0 && totalFuelUsed > 0) {
            // Calculate fuel usage in L/100km
            fuelUsage = (totalFuelUsed / totalDistance) * 100.0;
        }
        
        double avgTemperature = dataPoints > 0 ? totalTemperature / dataPoints : 0.0;
        double avgSpeed = dataPoints > 0 ? totalSpeed / dataPoints : 0.0;
        
        return new TripMetrics(totalDistance, fuelUsage, avgTemperature, avgSpeed);
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public double getFuelUsage() {
        TripMetrics metrics = getTripMetrics();
        return metrics.fuelUsage;
    }
    
    public double getAvgTemperature() {
        TripMetrics metrics = getTripMetrics();
        return metrics.avgTemperature;
    }
    
    public double getAvgSpeed() {
        TripMetrics metrics = getTripMetrics();
        return metrics.avgSpeed;
    }
    
    private double calculateDistance(LocationPoint point1, LocationPoint point2) {
        // Haversine formula for calculating distance between two GPS points
        final int R = 6371; // Earth's radius in kilometers
        
        double lat1Rad = Math.toRadians(point1.latitude);
        double lat2Rad = Math.toRadians(point2.latitude);
        double deltaLatRad = Math.toRadians(point2.latitude - point1.latitude);
        double deltaLonRad = Math.toRadians(point2.longitude - point1.longitude);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in kilometers
    }
    
    public void setInitialFuelLevel(double fuelLevel) {
        this.initialFuelLevel = fuelLevel;
        this.currentFuelLevel = fuelLevel;
    }
}
