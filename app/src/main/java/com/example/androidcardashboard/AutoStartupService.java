package com.example.androidcardashboard;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/**
 * AutoStartupService - Ensures the Car Dashboard app starts automatically
 */
public class AutoStartupService extends Service {
    private static final String TAG = "AutoStartupService";
    private Handler handler;
    private Runnable startupRunnable;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AutoStartupService created");
        handler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AutoStartupService started");
        
        // Schedule the app to start after a delay
        scheduleAppStartup();
        
        // Return START_STICKY to ensure the service restarts if killed
        return START_STICKY;
    }
    
    private void scheduleAppStartup() {
        if (startupRunnable != null) {
            handler.removeCallbacks(startupRunnable);
        }
        
        startupRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Attempting to start Car Dashboard app...");
                    
                    // Start the main activity
                    Intent intent = new Intent(AutoStartupService.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    
                    startActivity(intent);
                    
                    Log.i(TAG, "Car Dashboard app started successfully");
                    
                    // Stop the service after starting the app
                    stopSelf();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start Car Dashboard app", e);
                    
                    // Retry after a longer delay
                    handler.postDelayed(this, 10000); // 10 seconds
                }
            }
        };
        
        // Start the app after 5 seconds to ensure system is ready
        handler.postDelayed(startupRunnable, 5000);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "AutoStartupService destroyed");
        
        if (startupRunnable != null) {
            handler.removeCallbacks(startupRunnable);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This service doesn't support binding
    }
}
