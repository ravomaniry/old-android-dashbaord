package com.example.androidcardashboard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpService {
    private static final String TAG = "HttpService";
    private static final String ESP32_IP = "192.168.4.1"; // Default ESP32 AP IP
    private static final int ESP32_PORT = 80;
    private static final String DATA_ENDPOINT = "/data";
    private static final String DATA_STRUCTURE_ENDPOINT = "/data-structure";
    private static final int REQUEST_TIMEOUT_MS = 10000; // 10 seconds timeout for regular GET requests
    private static final int RETRY_DELAY_MS = 1000; // 1 second delay before retry on error
    private static final int POLL_DELAY_MS = 500;
    
    private Context context;
    private Handler mainHandler;
    private ExecutorService executorService;
    private HttpDataListener dataListener;
    
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean shouldPoll = false;
    private String status = "Disconnected";
    private String serverUrl;
    private String dataStructureUrl;
    
    // Data structure for binary parsing
    private JSONObject dataStructure = null;
    private boolean dataStructureLoaded = false;
    
    public interface HttpDataListener {
        void onHttpDataUpdate(double speed, double rpm, double coolantTemp, double fuelLevel, 
                             boolean oilWarning, double batteryVoltage, boolean drlOn, 
                             boolean lowBeamOn, boolean highBeamOn, boolean leftTurnSignal, 
                             boolean rightTurnSignal, boolean hazardLights, boolean reverseGear, String location);
        void onHttpStatusChange(boolean connected, String status);
    }
    
    public HttpService(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        this.serverUrl = "http://" + ESP32_IP + ":" + ESP32_PORT + DATA_ENDPOINT;
        this.dataStructureUrl = "http://" + ESP32_IP + ":" + ESP32_PORT + DATA_STRUCTURE_ENDPOINT;
        initializeHttpService();
    }
    
    public void setDataListener(HttpDataListener listener) {
        this.dataListener = listener;
    }
    
    private void initializeHttpService() {
        updateStatus(false, "HTTP Service ready");
        EventManager.getInstance().addHttpEvent("Service initialized", "STATUS");
        loadDataStructure();
        connectToServer();
    }
    
    private void loadDataStructure() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(dataStructureUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    
                    int responseCode = connection.getResponseCode();
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        
                        reader.close();
                        inputStream.close();
                        
                        dataStructure = new JSONObject(response.toString());
                        dataStructureLoaded = true;
                        
                        EventManager.getInstance().addHttpEvent("Data structure loaded", "STATUS");
                        Log.d(TAG, "Data structure loaded successfully");
                    } else {
                        throw new IOException("HTTP response code: " + responseCode);
                    }
                    
                    connection.disconnect();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load data structure", e);
                    EventManager.getInstance().addHttpEvent("Data structure load failed", "ERROR");
                }
            }
        });
    }
    
    public void connectToServer() {
        if (isConnecting || isConnected) return;
        
        isConnecting = true;
        updateStatus(false, "Connecting to ESP32...");
        EventManager.getInstance().addHttpEvent("Connecting...", "INFO");
        
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Test connection with a simple GET request
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000); // 5 second connection timeout
                    connection.setReadTimeout(10000); // 10 second read timeout
                    
                    int responseCode = connection.getResponseCode();
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        isConnected = true;
                        isConnecting = false;
                        updateStatus(true, "Connected to ESP32");
                        EventManager.getInstance().addHttpEvent("Connected", "STATUS");
                        
                        // Start polling for data
                        startPolling();
                    } else {
                        throw new IOException("HTTP response code: " + responseCode);
                    }
                    
                    connection.disconnect();
                    
                } catch (IOException e) {
                    Log.e(TAG, "Connection failed", e);
                    updateStatus(false, "Connection failed: " + e.getMessage());
                    EventManager.getInstance().addHttpEvent("Connection failed", "ERROR");
                    isConnecting = false;
                }
            }
        });
    }
    
    private void startPolling() {
        shouldPoll = true;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (shouldPoll && isConnected) {
                    try {
                        pollForData();
                        // Add delay between requests since ESP32 no longer uses long polling
                        Thread.sleep(POLL_DELAY_MS);
                    } catch (Exception e) {
                        Log.e(TAG, "Error during polling", e);
                        if (isConnected) {
                            updateStatus(false, "Polling error");
                            EventManager.getInstance().addHttpEvent("Polling error", "ERROR");
                        }
                        // Add a small delay before retrying on error to prevent rapid retry loops
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Log.d(TAG, "Polling interrupted");
                            break;
                        }
                    }
                }
            }
        });
    }
    
    private void pollForData() throws IOException {
        if (!dataStructureLoaded) {
            Log.w(TAG, "Data structure not loaded, skipping data poll");
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(REQUEST_TIMEOUT_MS);
        
        try {
            int responseCode = connection.getResponseCode();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log each HTTP request with time, path, duration, and status
            String timeStr = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(new java.util.Date());
            String statusStr = (responseCode == HttpURLConnection.HTTP_OK) ? "OK" : "ERROR";
            String logMessage = String.format("%s | %s | %dms | %s", timeStr, "/data", duration, statusStr);
            
            EventManager.getInstance().addHttpEvent(logMessage, "INFO");
            Log.d(TAG, logMessage);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                byte[] binaryData = new byte[64]; // Max expected size
                int bytesRead = inputStream.read(binaryData);
                inputStream.close();
                
                if (bytesRead > 0) {
                    parseBinaryData(binaryData, bytesRead);
                }
                
            } else {
                throw new IOException("HTTP response code: " + responseCode);
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    private void parseBinaryData(byte[] data, int length) {
        try {
            // Parse data dynamically using the loaded data structure
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
            
            // Parse each field dynamically based on the data structure
            if (dataStructure != null) {
                speed = parseFieldByType(data, "speed");
                coolantTemp = parseFieldByType(data, "coolantTemp");
                fuelLevel = parseFieldByType(data, "fuelLevel");
                batteryVoltage = parseFieldByType(data, "batteryVoltage");
                oilWarning = parseFieldByType(data, "oilWarning");
                drlOn = parseFieldByType(data, "drlOn");
                lowBeamOn = parseFieldByType(data, "lowBeamOn");
                highBeamOn = parseFieldByType(data, "highBeamOn");
                leftTurnSignal = parseFieldByType(data, "leftTurnSignal");
                rightTurnSignal = parseFieldByType(data, "rightTurnSignal");
                hazardLights = parseFieldByType(data, "hazardLights");
                reverseGear = parseFieldByType(data, "reverseGear");
                location = parseFieldByType(data, "location");
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
                        dataListener.onHttpDataUpdate(finalSpeed, finalRpm, finalCoolantTemp, finalFuelLevel, 
                            finalOilWarning, finalBatteryVoltage, finalDrlOn, finalLowBeamOn, finalHighBeamOn, 
                            finalLeftTurnSignal, finalRightTurnSignal, finalHazardLights, finalReverseGear, finalLocation);
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse binary data", e);
            EventManager.getInstance().addHttpEvent("Binary data parse error", "ERROR");
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T parseFieldByType(byte[] data, String fieldName) {
        try {
            if (dataStructure == null || !dataStructure.has(fieldName)) {
                return (T) getDefaultValue(fieldName);
            }
            
            JSONObject fieldInfo = dataStructure.getJSONObject(fieldName);
            String type = fieldInfo.getString("type");
            int position = fieldInfo.getInt("position");
            int size = fieldInfo.getInt("size");
            int scale = fieldInfo.optInt("scale", 1); // Default scale is 1 if not specified
            
            if ("int".equals(type)) {
                int value = ByteBuffer.wrap(data, position, size).order(ByteOrder.LITTLE_ENDIAN).getInt();
                return (T) Double.valueOf((double) value / scale);
            } else if ("bool".equals(type)) {
                // For boolean fields, we need to check the bit position within the byte
                int byteIndex = position / 8;
                int bitPosition = position % 8;
                
                if (byteIndex < data.length) {
                    int boolFlags = data[byteIndex] & 0xFF;
                    boolean value = (boolFlags & (1 << bitPosition)) != 0;
                    return (T) Boolean.valueOf(value);
                }
            } else if ("string".equals(type)) {
                if (position + size <= data.length) {
                    String result = new String(data, position, size).trim();
                    if (result.endsWith("\0")) {
                        result = result.substring(0, result.indexOf("\0"));
                    }
                    return (T) result;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse field: " + fieldName, e);
        }
        
        return (T) getDefaultValue(fieldName);
    }
    
    private Object getDefaultValue(String fieldName) {
        // Return appropriate default values based on field name
        if ("location".equals(fieldName)) {
            return "";
        } else if (fieldName.contains("On") || fieldName.contains("Signal") || 
                   fieldName.contains("Lights") || fieldName.contains("Gear") || 
                   fieldName.contains("Warning")) {
            return false;
        } else {
            return 0.0;
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
                    dataListener.onHttpStatusChange(finalConnected, finalStatus);
                }
            });
        }
    }
    
    public void disconnect() {
        shouldPoll = false;
        isConnected = false;
        isConnecting = false;
        
        updateStatus(false, "Disconnected");
        EventManager.getInstance().addHttpEvent("Disconnected", "STATUS");
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
        this.serverUrl = "http://" + ip + ":" + port + DATA_ENDPOINT;
        this.dataStructureUrl = "http://" + ip + ":" + port + DATA_STRUCTURE_ENDPOINT;
        Log.d(TAG, "Server URL updated to: " + this.serverUrl);
        Log.d(TAG, "Data structure URL updated to: " + this.dataStructureUrl);
    }
    
    public void cleanup() {
        disconnect();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
