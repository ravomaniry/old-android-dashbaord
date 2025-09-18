package com.example.androidcardashboard;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static final String TAG = "EventManager";
    private static EventManager instance;
    
    private List<GpsEvent> latestGpsEvents;
    private List<TcpEvent> latestTcpEvents;
    private Handler mainHandler;
    
    private EventManager() {
        latestGpsEvents = new ArrayList<>();
        latestTcpEvents = new ArrayList<>();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }
    
    public void addGpsEvent(String message, String level) {
        GpsEvent event = new GpsEvent(message, level, System.currentTimeMillis());
        
        // Add to beginning of list
        latestGpsEvents.add(0, event);
        
        // Keep only last 10 events
        if (latestGpsEvents.size() > 10) {
            latestGpsEvents.remove(latestGpsEvents.size() - 1);
        }
        
        Log.d(TAG, "GPS Event [" + level + "]: " + message);
    }
    
    
    public void addTcpEvent(String message, String level) {
        TcpEvent event = new TcpEvent(message, level, System.currentTimeMillis());
        
        // Add to beginning of list
        latestTcpEvents.add(0, event);
        
        // Keep only last 10 events
        if (latestTcpEvents.size() > 10) {
            latestTcpEvents.remove(latestTcpEvents.size() - 1);
        }
        
        Log.d(TAG, "TCP Event [" + level + "]: " + message);
    }
    
    public List<GpsEvent> getLatestGpsEvents() {
        return new ArrayList<>(latestGpsEvents);
    }
    
    
    public List<TcpEvent> getLatestTcpEvents() {
        return new ArrayList<>(latestTcpEvents);
    }
    
    public static class GpsEvent {
        private String message;
        private String level;
        private long timestamp;
        
        public GpsEvent(String message, String level, long timestamp) {
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
        
        public String getMessage() { return message; }
        public String getLevel() { return level; }
        public long getTimestamp() { return timestamp; }
        
        public String getFormattedTime() {
            long seconds = timestamp / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            
            return String.format("%02d:%02d", hours % 24, minutes % 60);
        }
    }
    
    
    public static class TcpEvent {
        private String message;
        private String level;
        private long timestamp;
        
        public TcpEvent(String message, String level, long timestamp) {
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
        
        public String getMessage() { return message; }
        public String getLevel() { return level; }
        public long getTimestamp() { return timestamp; }
        
        public String getFormattedTime() {
            long seconds = timestamp / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            
            return String.format("%02d:%02d", hours % 24, minutes % 60);
        }
    }
}
