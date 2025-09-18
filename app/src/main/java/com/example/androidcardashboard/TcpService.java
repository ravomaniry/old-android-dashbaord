package com.example.androidcardashboard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpService {
    private static final String TAG = "TcpService";
    private static final String ESP32_IP = "192.168.4.1"; // Default ESP32 AP IP
    private static final int ESP32_PORT = 8888;
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int RECONNECT_DELAY_MS = 2000;
    private static final int BUFFER_SIZE = 64;
    
    private Context context;
    private Handler mainHandler;
    private ExecutorService executorService;
    private TcpDataListener dataListener;
    
    private Socket socket;
    private InputStream inputStream;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean shouldReconnect = true;
    private String status = "Disconnected";
    
    public interface TcpDataListener {
        void onTcpDataUpdate(double speed, double rpm, double coolantTemp, double fuelLevel, 
                           boolean oilWarning, double batteryVoltage, boolean drlOn, 
                           boolean lowBeamOn, boolean highBeamOn, boolean leftTurnSignal, 
                           boolean rightTurnSignal, boolean hazardLights, boolean reverseGear, String location);
        void onTcpStatusChange(boolean connected, String status);
    }
    
    public TcpService(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        initializeTcpService();
    }
    
    public void setDataListener(TcpDataListener listener) {
        this.dataListener = listener;
    }
    
    private void initializeTcpService() {
        updateStatus(false, "TCP Service ready");
        EventManager.getInstance().addTcpEvent("Service initialized", "STATUS");
        connectToServer();
    }
    
    public void connectToServer() {
        if (isConnecting || isConnected) return;
        
        isConnecting = true;
        updateStatus(false, "Connecting to ESP32...");
        EventManager.getInstance().addTcpEvent("Connecting...", "INFO");
        
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create socket connection
                    socket = new Socket();
                    socket.connect(new java.net.InetSocketAddress(ESP32_IP, ESP32_PORT), CONNECTION_TIMEOUT_MS);
                    socket.setTcpNoDelay(true); // Disable Nagle's algorithm for lower latency
                    socket.setSoTimeout(10000); // 10 second read timeout
                    
                    inputStream = socket.getInputStream();
                    
                    isConnected = true;
                    isConnecting = false;
                    updateStatus(true, "Connected to ESP32");
                    EventManager.getInstance().addTcpEvent("Connected", "STATUS");
                    
                    // Start receiving data
                    startReceivingData();
                    
                } catch (IOException e) {
                    Log.e(TAG, "Connection failed", e);
                    updateStatus(false, "Connection failed: " + e.getMessage());
                    EventManager.getInstance().addTcpEvent("Connection failed", "ERROR");
                    isConnecting = false;
                    
                    // Schedule reconnection if enabled
                    if (shouldReconnect) {
                        scheduleReconnect();
                    }
                }
            }
        });
    }
    
    private void scheduleReconnect() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                    if (shouldReconnect && !isConnected) {
                        connectToServer();
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG, "Reconnect interrupted");
                }
            }
        });
    }
    
    private void startReceivingData() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[BUFFER_SIZE];
                
                while (isConnected && shouldReconnect) {
                    try {
                        int bytesRead = inputStream.read(buffer);
                        
                if (bytesRead > 0) {
                    // Log data reception with timestamp
                    String timeStr = java.text.DateFormat.getTimeInstance(java.text.DateFormat.MEDIUM).format(new java.util.Date());
                    String logMessage = String.format("%s | Data received | %d bytes", timeStr, bytesRead);
                    EventManager.getInstance().addTcpEvent(logMessage, "DATA");
                    
                    parseBinaryData(buffer, bytesRead);
                } else if (bytesRead == -1) {
                            // End of stream - connection closed
                            Log.d(TAG, "Connection closed by server");
                            break;
                        }
                        
                    } catch (IOException e) {
                        Log.e(TAG, "Error receiving data", e);
                        if (isConnected) {
                            updateStatus(false, "Data receive error");
                            EventManager.getInstance().addTcpEvent("Data receive error", "ERROR");
                        }
                        break;
                    }
                }
                
                // Connection lost, attempt to reconnect
                if (isConnected) {
                    isConnected = false;
                    updateStatus(false, "Connection lost");
                    EventManager.getInstance().addTcpEvent("Connection lost", "ERROR");
                    
                    if (shouldReconnect) {
                        scheduleReconnect();
                    }
                }
            }
        });
    }
    
    private void parseBinaryData(byte[] data, int length) {
        try {
            // Parse data using the same structure as HTTP service
            double speed = 0.0;
            double rpm = 0.0; // ESP32 doesn't send RPM
            double coolantTemp = 0.0;
            double fuelLevel = 0.0;
            boolean oilWarning = false;
            double batteryVoltage = 0.0;
            boolean drlOn = false;
            boolean lowBeamOn = false;
            boolean highBeamOn = false;
            boolean leftTurnSignal = false;
            boolean rightTurnSignal = false;
            boolean hazardLights = false;
            boolean reverseGear = false;
            String location = "";
            
            int offset = 0;
            
            // Parse boolean flags (first byte)
            if (offset < length) {
                int boolFlags = data[offset] & 0xFF;
                reverseGear = (boolFlags & (1 << 0)) != 0;
                hazardLights = (boolFlags & (1 << 1)) != 0;
                rightTurnSignal = (boolFlags & (1 << 2)) != 0;
                leftTurnSignal = (boolFlags & (1 << 3)) != 0;
                highBeamOn = (boolFlags & (1 << 4)) != 0;
                lowBeamOn = (boolFlags & (1 << 5)) != 0;
                drlOn = (boolFlags & (1 << 6)) != 0;
                oilWarning = (boolFlags & (1 << 7)) != 0;
                offset++;
            }
            
            // Parse speed (4 bytes, little endian)
            if (offset + 4 <= length) {
                int speedInt = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                speed = (double) speedInt / 10.0;
                offset += 4;
            }
            
            // Parse coolant temp (4 bytes, little endian)
            if (offset + 4 <= length) {
                int coolantTempInt = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                coolantTemp = (double) coolantTempInt / 10.0;
                offset += 4;
            }
            
            // Parse fuel level (4 bytes, little endian)
            if (offset + 4 <= length) {
                int fuelLevelInt = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                fuelLevel = (double) fuelLevelInt / 10.0;
                offset += 4;
            }
            
            // Parse battery voltage (4 bytes, little endian)
            if (offset + 4 <= length) {
                int batteryVoltageInt = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                batteryVoltage = (double) batteryVoltageInt / 10.0;
                offset += 4;
            }
            
            // Parse location string (32 bytes, null-terminated)
            if (offset + 32 <= length) {
                String locationStr = new String(data, offset, 32).trim();
                if (locationStr.endsWith("\0")) {
                    location = locationStr.substring(0, locationStr.indexOf("\0"));
                } else {
                    location = locationStr;
                }
            }
            
            // Update UI on main thread
            if (dataListener != null) {
                final double finalSpeed = speed;
                final double finalRpm = rpm;
                final double finalCoolantTemp = coolantTemp;
                final double finalFuelLevel = fuelLevel;
                final boolean finalOilWarning = oilWarning;
                final double finalBatteryVoltage = batteryVoltage;
                final boolean finalDrlOn = drlOn;
                final boolean finalLowBeamOn = lowBeamOn;
                final boolean finalHighBeamOn = highBeamOn;
                final boolean finalLeftTurnSignal = leftTurnSignal;
                final boolean finalRightTurnSignal = rightTurnSignal;
                final boolean finalHazardLights = hazardLights;
                final boolean finalReverseGear = reverseGear;
                final String finalLocation = location;
                
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataListener.onTcpDataUpdate(finalSpeed, finalRpm, finalCoolantTemp, finalFuelLevel, 
                            finalOilWarning, finalBatteryVoltage, finalDrlOn, finalLowBeamOn, finalHighBeamOn, 
                            finalLeftTurnSignal, finalRightTurnSignal, finalHazardLights, finalReverseGear, finalLocation);
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse binary data", e);
            EventManager.getInstance().addTcpEvent("Binary data parse error", "ERROR");
        }
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
                    dataListener.onTcpStatusChange(finalConnected, finalStatus);
                }
            });
        }
    }
    
    public void disconnect() {
        shouldReconnect = false;
        isConnected = false;
        isConnecting = false;
        
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection", e);
        }
        
        updateStatus(false, "Disconnected");
        EventManager.getInstance().addTcpEvent("Disconnected", "STATUS");
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isConnecting() {
        return isConnecting;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setServerUrl(String ip, int port) {
        // For TCP, we would need to reconnect with new IP/port
        Log.d(TAG, "Server URL updated to: " + ip + ":" + port);
    }
    
    public void cleanup() {
        disconnect();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
