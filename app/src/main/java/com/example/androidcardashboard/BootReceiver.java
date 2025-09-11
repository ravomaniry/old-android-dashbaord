package com.example.androidcardashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BootReceiver - Automatically starts the Car Dashboard app when the device boots
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "BootReceiver received action: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            
            Log.i(TAG, "Device boot completed - starting Car Dashboard app");
            
            // Start the main activity directly
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            try {
                context.startActivity(activityIntent);
                Log.i(TAG, "Car Dashboard app started successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start Car Dashboard app", e);
                
                // Fallback: Start the service
                Intent serviceIntent = new Intent(context, AutoStartupService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
