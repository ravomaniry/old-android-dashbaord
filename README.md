# Android Car Dashboard

A modern Android car dashboard application that receives real-time vehicle data via Bluetooth and displays it on a tablet interface. The app calculates trip metrics on-device using GPS location data.

## Features

### 🚗 **Real-time Vehicle Data**

- **Speed & RPM:** Current speed in km/h and engine RPM
- **Reverse Gear:** Displays "R" in speedometer when car is in reverse
- **Engine Temperature:** Coolant temperature monitoring
- **Fuel Level:** Real-time fuel percentage
- **Battery Voltage:** Electrical system monitoring
- **Oil Warning:** Engine oil pressure alerts

### 💡 **Lighting Status**

- **DRL (Daytime Running Lights)**
- **Low Beam Headlights**
- **High Beam Headlights**
- **Hazard Lights**
- **Turn Signals (Left/Right)**

### 🔄 **Gear Status**

- **Reverse Gear:** When engaged, the speedometer displays "R" instead of speed value
- **Visual Feedback:** Clear indication when vehicle is in reverse mode
- **Real-time Updates:** Instant display changes when gear status changes

### 📊 **Trip Calculations**

- **Distance:** Calculated using GPS coordinates
- **Fuel Usage:** L/100km based on fuel level changes
- **Average Temperature:** Running average of coolant temperature
- **Average Speed:** Running average of speed readings

### 🔧 **System Status**

- **Bluetooth Connection:** Real-time connection status
- **Battery Status:** Electrical system health
- **Oil Warning:** Engine oil pressure alerts

## Bluetooth Data Format

The app expects JSON data via Bluetooth with the following structure. All fields are **optional** - missing keys will use default values.

### Required JSON Structure

```json
{
  "speed": 65.5,
  "rpm": 2500,
  "coolantTemp": 82.0,
  "fuelLevel": 45.2,
  "oilWarning": false,
  "batteryVoltage": 12.6,
  "drlOn": true,
  "lowBeamOn": false,
  "highBeamOn": false,
  "leftTurnSignal": false,
  "rightTurnSignal": false,
  "hazardLights": false,
  "location": "40.7128,-74.0060"
}
```

### Field Descriptions

#### Vehicle Performance

| Field            | Type    | Range      | Description               | Default |
| ---------------- | ------- | ---------- | ------------------------- | ------- |
| `speed`          | number  | 0-300      | Vehicle speed in km/h     | 0.0     |
| `rpm`            | number  | 0-8000     | Engine RPM                | 0.0     |
| `coolantTemp`    | number  | 70-110     | Coolant temperature in °C | 0.0     |
| `fuelLevel`      | number  | 0-100      | Fuel level percentage     | 0.0     |
| `oilWarning`     | boolean | true/false | Oil pressure warning      | false   |
| `batteryVoltage` | number  | 10-15      | Battery voltage in volts  | 0.0     |

#### Lighting Status

| Field             | Type    | Description                   | Default |
| ----------------- | ------- | ----------------------------- | ------- |
| `drlOn`           | boolean | Daytime running lights status | false   |
| `lowBeamOn`       | boolean | Low beam headlights status    | false   |
| `highBeamOn`      | boolean | High beam headlights status   | false   |
| `leftTurnSignal`  | boolean | Left turn signal status       | false   |
| `rightTurnSignal` | boolean | Right turn signal status      | false   |
| `hazardLights`    | boolean | Hazard lights status          | false   |
| `reverseGear`     | boolean | Reverse gear engagement       | false   |

#### Location Data

| Field      | Type   | Format    | Description                       | Default |
| ---------- | ------ | --------- | --------------------------------- | ------- |
| `location` | string | "lat,lon" | GPS coordinates (comma-separated) | ""      |

### Example Data Packets

#### Minimal Data (Speed Only)

```json
{
  "speed": 45.0
}
```

#### Location Update Only

```json
{
  "location": "40.7128,-74.0060"
}
```

#### Reverse Gear Example

```json
{
  "speed": 0.0,
  "reverseGear": true
}
```

When `reverseGear` is `true`, the speedometer will display "R" instead of the speed value, providing clear visual feedback that the vehicle is in reverse mode.

#### Full Status Update

```json
{
  "speed": 65.5,
  "rpm": 2500,
  "coolantTemp": 82.0,
  "fuelLevel": 45.2,
  "oilWarning": false,
  "batteryVoltage": 12.6,
  "drlOn": true,
  "lowBeamOn": false,
  "highBeamOn": false,
  "leftTurnSignal": false,
  "rightTurnSignal": false,
  "hazardLights": false,
  "reverseGear": false,
  "location": "40.7128,-74.0060"
}
```

## Trip Calculation Details

### Distance Calculation

- Uses **Haversine formula** for accurate GPS distance calculation
- Calculates cumulative distance between consecutive GPS points
- Only updates when valid location data is received

### Fuel Usage Calculation

- Tracks fuel level changes over time
- Calculates L/100km based on distance traveled and fuel consumed
- Only counts fuel level decreases (fuel consumption)

### Running Averages

- **Average Temperature:** Running average of all coolant temperature readings
- **Average Speed:** Running average of all speed readings
- Both reset when trip is reset

## Bluetooth Connection

### Target Device

- **Device Name:** `RAVO_CAR_DASH`
- **Protocol:** SPP (Serial Port Profile)
- **UUID:** `00001101-0000-1000-8000-00805F9B34FB`

### Connection Process

1. App scans for paired devices
2. Connects to `RAVO_CAR_DASH` if found
3. Listens for JSON data on the connection
4. Parses and displays data in real-time

## Build and Deployment

### Quick Build

```bash
./quick_build.sh
```

### Full Build with Options

```bash
./build_and_deploy.sh [OPTIONS]
```

#### Available Options

- `-h, --help` - Show help message
- `-c, --clean` - Clean build before building
- `-u, --uninstall` - Uninstall existing app before installing
- `-l, --logs` - Show app logs after launching
- `-r, --release` - Build release version instead of debug
- `-n, --no-launch` - Don't launch the app after installation

### Examples

```bash
# Basic build and deploy
./build_and_deploy.sh

# Clean build with uninstall
./build_and_deploy.sh --clean --uninstall

# Build release version with logs
./build_and_deploy.sh --release --logs
```

## Requirements

### Android Device

- **Android Version:** 4.1+ (API 16+)
- **Bluetooth:** SPP support required
- **Screen:** Landscape orientation recommended
- **Permissions:** Bluetooth, Wake Lock

### Car System

- **Bluetooth:** Must support SPP profile
- **GPS:** Must provide location data as comma-separated lat,lon
- **Data Format:** JSON over Bluetooth serial connection

## Troubleshooting

### Connection Issues

- Ensure car system is paired as `RAVO_CAR_DASH`
- Check Bluetooth is enabled on both devices
- Verify SPP profile is supported

### Data Issues

- Check JSON format is valid
- Ensure location format is "lat,lon" (comma-separated)
- Missing fields will use default values

### Performance

- App automatically manages memory (keeps last 1000 location points)
- Trip calculations are performed in real-time
- No GPS permissions required (uses received location data)

## Technical Details

### Architecture

- **MainActivity:** UI and data coordination
- **BluetoothService:** Bluetooth communication and JSON parsing
- **TripCalculator:** Location-based trip metric calculations
- **Custom Views:** Speedometer (with reverse gear display), gauges, and status indicators

### Data Flow

```
Car System → Bluetooth → JSON Data → TripCalculator → UI Display
```

### Memory Management

- Location history limited to 1000 points
- Automatic cleanup of old data
- Efficient calculation algorithms

## License

This project is part of the RAVO Car Dashboard system.
