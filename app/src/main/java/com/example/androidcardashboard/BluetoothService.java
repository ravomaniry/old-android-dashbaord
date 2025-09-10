package com.example.androidcardashboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private static final String TARGET_DEVICE_NAME = "RAVO_CAR_DASH";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private Handler mainHandler;
    private BluetoothDataListener dataListener;
    
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private String status = "Disconnected";
    
    public interface BluetoothDataListener {
        void onBluetoothDataUpdate(double coolantTemp, double fuelLevel, boolean oilWarning, 
                                 double batteryVoltage, boolean drlOn, boolean lowBeamOn, 
                                 boolean highBeamOn, boolean leftTurnSignal, boolean rightTurnSignal, 
                                 boolean hazardLights);
        void onBluetoothStatusChange(boolean connected, String status);
    }
    
    public BluetoothService(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
        initializeBluetooth();
    }
    
    public void setDataListener(BluetoothDataListener listener) {
        this.dataListener = listener;
    }
    
    private void initializeBluetooth() {
        if (bluetoothAdapter == null) {
            updateStatus(false, "Bluetooth not available");
            return;
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            updateStatus(false, "Bluetooth disabled");
            return;
        }
        
        updateStatus(false, "Bluetooth ready");
        connectToDevice();
    }
    
    public void connectToDevice() {
        if (isConnecting || isConnected) return;
        
        isConnecting = true;
        updateStatus(false, "Scanning for devices...");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get bonded devices
                    Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                    Log.d(TAG, "Found " + bondedDevices.size() + " bonded devices");
                    
                    BluetoothDevice targetDevice = null;
                    for (BluetoothDevice device : bondedDevices) {
                        if (TARGET_DEVICE_NAME.equals(device.getName())) {
                            targetDevice = device;
                            break;
                        }
                    }
                    
                    if (targetDevice == null) {
                        updateStatus(false, "Device not found. Please pair " + TARGET_DEVICE_NAME + " first.");
                        isConnecting = false;
                        return;
                    }
                    
                    updateStatus(false, "Connecting to " + targetDevice.getName() + "...");
                    
                    // Create socket and connect
                    bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                    bluetoothSocket.connect();
                    
                    isConnected = true;
                    isConnecting = false;
                    updateStatus(true, "Connected");
                    
                    // Start listening for data
                    listenForData();
                    
                } catch (IOException e) {
                    Log.e(TAG, "Connection failed", e);
                    updateStatus(false, "Connection failed: " + e.getMessage());
                    isConnecting = false;
                }
            }
        }).start();
    }
    
    private void listenForData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = bluetoothSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    
                    String line;
                    while (isConnected && (line = reader.readLine()) != null) {
                        parseIncomingData(line.trim());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading data", e);
                    if (isConnected) {
                        updateStatus(false, "Data reception error");
                    }
                }
            }
        }).start();
    }
    
    private void parseIncomingData(String data) {
        if (data.isEmpty()) return;
        
        try {
            JSONObject json = new JSONObject(data);
            
            double coolantTemp = json.optDouble("coolantTemp", 0.0);
            double fuelLevel = json.optDouble("fuelLevel", 0.0);
            boolean oilWarning = json.optBoolean("oilWarning", false);
            double batteryVoltage = json.optDouble("batteryVoltage", 0.0);
            boolean drlOn = json.optBoolean("drlOn", false);
            boolean lowBeamOn = json.optBoolean("lowBeamOn", false);
            boolean highBeamOn = json.optBoolean("highBeamOn", false);
            boolean leftTurnSignal = json.optBoolean("leftTurnSignal", false);
            boolean rightTurnSignal = json.optBoolean("rightTurnSignal", false);
            boolean hazardLights = json.optBoolean("hazardLights", false);
            
            Log.d(TAG, String.format("Received data: temp=%.1f°C, fuel=%.1f%%, battery=%.1fV", 
                coolantTemp, fuelLevel, batteryVoltage));
            
            // Update UI on main thread
            if (dataListener != null) {
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
                
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataListener.onBluetoothDataUpdate(finalCoolantTemp, finalFuelLevel, finalOilWarning, 
                            finalBatteryVoltage, finalDrlOn, finalLowBeamOn, finalHighBeamOn, 
                            finalLeftTurnSignal, finalRightTurnSignal, finalHazardLights);
                    }
                });
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON data: " + data, e);
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
                    dataListener.onBluetoothStatusChange(finalConnected, finalStatus);
                }
            });
        }
    }
    
    public void disconnect() {
        isConnected = false;
        isConnecting = false;
        
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket", e);
        }
        
        updateStatus(false, "Disconnected");
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
    
    public void cleanup() {
        disconnect();
    }
}
