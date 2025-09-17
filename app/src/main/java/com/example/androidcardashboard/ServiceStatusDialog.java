package com.example.androidcardashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public class ServiceStatusDialog {
    private Context context;
    private String title;
    private int iconResId;
    private String serviceStatus;
    private List<?> events;
    private OnActionClickListener actionClickListener;
    private String currentJsonData;
    
    public interface OnActionClickListener {
        void onConnectClick();
        void onDisconnectClick();
        void onRetryClick();
    }
    
    public ServiceStatusDialog(Context context, String title, int iconResId, 
                             String serviceStatus, List<?> events, 
                             OnActionClickListener actionClickListener) {
        this(context, title, iconResId, serviceStatus, events, actionClickListener, null);
    }
    
    public ServiceStatusDialog(Context context, String title, int iconResId, 
                             String serviceStatus, List<?> events, 
                             OnActionClickListener actionClickListener, String currentJsonData) {
        this.context = context;
        this.title = title;
        this.iconResId = iconResId;
        this.serviceStatus = serviceStatus;
        this.events = events;
        this.actionClickListener = actionClickListener;
        this.currentJsonData = currentJsonData;
    }
    
    public Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // Create custom layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_service_status, null);
        
        // Set title
        TextView titleView = (TextView) dialogView.findViewById(R.id.dialog_title);
        titleView.setText(title);
        
        // Set service status
        TextView statusView = (TextView) dialogView.findViewById(R.id.service_status);
        statusView.setText(serviceStatus);
        
        // Set up events list
        ListView eventsList = (ListView) dialogView.findViewById(R.id.events_list);
        EventAdapter adapter = new EventAdapter(events);
        eventsList.setAdapter(adapter);
        
        // Set up JSON data display
        TextView jsonDataView = (TextView) dialogView.findViewById(R.id.json_data_display);
        if (currentJsonData != null && !currentJsonData.isEmpty()) {
            jsonDataView.setText(currentJsonData);
        } else {
            jsonDataView.setText("{\n  \"status\": \"No data available\"\n}");
        }
        
        // Set up action buttons
        Button connectBtn = (Button) dialogView.findViewById(R.id.btn_connect);
        Button disconnectBtn = (Button) dialogView.findViewById(R.id.btn_disconnect);
        Button retryBtn = (Button) dialogView.findViewById(R.id.btn_retry);
        Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close);
        
        // Set up buttons for WiFi service
        connectBtn.setText(context.getString(R.string.connect));
        disconnectBtn.setText(context.getString(R.string.disconnect));
        retryBtn.setText(context.getString(R.string.retry));
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionClickListener != null) {
                    actionClickListener.onConnectClick();
                }
            }
        });
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionClickListener != null) {
                    actionClickListener.onDisconnectClick();
                }
            }
        });
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionClickListener != null) {
                    actionClickListener.onRetryClick();
                }
            }
        });
        
        closeBtn.setText(context.getString(R.string.close));
        
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dialog will be dismissed automatically
            }
        });
        
        builder.setView(dialogView);
        return builder.create();
    }
    
    private class EventAdapter extends BaseAdapter {
        private List<?> events;
        
        public EventAdapter(List<?> events) {
            this.events = events;
        }
        
        @Override
        public int getCount() {
            return events.size();
        }
        
        @Override
        public Object getItem(int position) {
            return events.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_event, null);
            }
            
            Object event = getItem(position);
            TextView levelView = (TextView) convertView.findViewById(R.id.event_level);
            TextView messageView = (TextView) convertView.findViewById(R.id.event_message);
            TextView timeView = (TextView) convertView.findViewById(R.id.event_time);
            
            String level = "";
            String message = "";
            String time = "";
            
            if (event instanceof EventManager.GpsEvent) {
                EventManager.GpsEvent gpsEvent = (EventManager.GpsEvent) event;
                level = gpsEvent.getLevel();
                message = gpsEvent.getMessage();
                time = gpsEvent.getFormattedTime();
            } else if (event instanceof EventManager.HttpEvent) {
                EventManager.HttpEvent httpEvent = (EventManager.HttpEvent) event;
                level = httpEvent.getLevel();
                message = httpEvent.getMessage();
                time = httpEvent.getFormattedTime();
            }
            
            levelView.setText(level);
            messageView.setText(message);
            timeView.setText(time);
            
            // Set level color
            int levelColor = getLevelColor(level);
            levelView.setTextColor(levelColor);
            levelView.setBackgroundColor(Color.argb(50, Color.red(levelColor), 
                                                   Color.green(levelColor), 
                                                   Color.blue(levelColor)));
            
            return convertView;
        }
        
        private int getLevelColor(String level) {
            switch (level) {
                case "INFO":
                    return Color.parseColor("#40C4FF"); // Minimal theme secondary accent
                case "STATUS":
                    return Color.parseColor("#00E676"); // Minimal theme success
                case "CONFIG":
                    return Color.parseColor("#FFB74D"); // Minimal theme warning
                case "TRIP":
                    return Color.parseColor("#00E5FF"); // Minimal theme primary accent
                case "DATA":
                    return Color.parseColor("#40C4FF"); // Minimal theme secondary accent
                case "ERROR":
                    return Color.parseColor("#FF5252"); // Minimal theme danger
                default:
                    return Color.parseColor("#CCCCCC"); // Minimal theme text secondary
            }
        }
    }
}
