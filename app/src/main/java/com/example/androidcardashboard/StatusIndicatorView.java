package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class StatusIndicatorView extends View {
    private Paint indicatorPaint;
    private Paint textPaint;
    private Paint backgroundPaint;
    
    private boolean isActive = false;
    private String label = "";
    private int activeColor = Color.parseColor("#00BCD4");
    private int inactiveColor = Color.parseColor("#404040");
    private int textColor = Color.parseColor("#B0B0B0");
    
    private boolean isBlinking = false;
    private boolean blinkState = false;
    private Handler blinkHandler = new Handler();
    private Runnable blinkRunnable;
    
    private int centerX, centerY;
    private float indicatorRadius;
    
    public StatusIndicatorView(Context context) {
        super(context);
        init();
    }
    
    public StatusIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Indicator circle paint
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.FILL);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // Background paint for the view
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.TRANSPARENT);
        backgroundPaint.setStyle(Paint.Style.FILL);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2;
        centerY = h / 2;
        indicatorRadius = Math.min(w, h) / 4;
        
        // Set fixed text size for uniform appearance across all indicators
        textPaint.setTextSize(14);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        
        // Set indicator color
        if (isActive) {
            if (isBlinking && !blinkState) {
                indicatorPaint.setColor(inactiveColor);
            } else {
                indicatorPaint.setColor(activeColor);
            }
        } else {
            indicatorPaint.setColor(inactiveColor);
        }
        
        // Draw icon based on label
        drawIcon(canvas);
        
        // Draw label closer to the indicator to save space
        if (!label.isEmpty()) {
            canvas.drawText(label, centerX, centerY + indicatorRadius * 1.2f, textPaint);
        }
    }
    
    private void drawIcon(Canvas canvas) {
        float iconSize = indicatorRadius * 1.5f;
        float iconX = centerX;
        float iconY = centerY - indicatorRadius;
        
        switch (label.toUpperCase()) {
            case "OIL":
                drawOilIcon(canvas, iconX, iconY, iconSize);
                break;
            case "BATTERY":
                drawBatteryIcon(canvas, iconX, iconY, iconSize);
                break;
            case "BLUETOOTH":
                drawBluetoothIcon(canvas, iconX, iconY, iconSize);
                break;
            case "GPS":
                drawGpsIcon(canvas, iconX, iconY, iconSize);
                break;
            case "DRL":
                drawDrlIcon(canvas, iconX, iconY, iconSize);
                break;
            case "LOW BEAM":
                drawLowBeamIcon(canvas, iconX, iconY, iconSize);
                break;
            case "HIGH BEAM":
                drawHighBeamIcon(canvas, iconX, iconY, iconSize);
                break;
            case "HAZARD":
                drawHazardIcon(canvas, iconX, iconY, iconSize);
                break;
            case "LEFT TURN":
                drawLeftTurnIcon(canvas, iconX, iconY, iconSize);
                break;
            case "RIGHT TURN":
                drawRightTurnIcon(canvas, iconX, iconY, iconSize);
                break;
            default:
                // Fallback to circle for unknown labels
                canvas.drawCircle(iconX, iconY, indicatorRadius, indicatorPaint);
                break;
        }
    }
    
    private void drawOilIcon(Canvas canvas, float x, float y, float size) {
        // Oil drop icon (opacity icon style)
        Paint oilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oilPaint.setColor(indicatorPaint.getColor());
        oilPaint.setStyle(Paint.Style.FILL);
        
        // Main drop shape
        Path oilPath = new Path();
        oilPath.moveTo(x, y - size * 0.5f);
        oilPath.quadTo(x + size * 0.3f, y - size * 0.2f, x + size * 0.2f, y + size * 0.1f);
        oilPath.quadTo(x, y + size * 0.4f, x - size * 0.2f, y + size * 0.1f);
        oilPath.quadTo(x - size * 0.3f, y - size * 0.2f, x, y - size * 0.5f);
        canvas.drawPath(oilPath, oilPaint);
        
        // Highlight
        oilPaint.setColor(Color.WHITE);
        oilPaint.setAlpha(120);
        canvas.drawCircle(x, y - size * 0.1f, size * 0.15f, oilPaint);
    }
    
    private void drawBatteryIcon(Canvas canvas, float x, float y, float size) {
        // Battery icon (battery_full/battery_alert style)
        Paint batteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        batteryPaint.setColor(indicatorPaint.getColor());
        batteryPaint.setStyle(Paint.Style.FILL);
        
        // Battery body
        RectF batteryBody = new RectF(
            x - size * 0.4f, y - size * 0.3f, 
            x + size * 0.4f, y + size * 0.3f
        );
        canvas.drawRoundRect(batteryBody, size * 0.05f, size * 0.05f, batteryPaint);
        
        // Battery terminal
        RectF batteryTerminal = new RectF(
            x + size * 0.4f, y - size * 0.15f, 
            x + size * 0.5f, y + size * 0.15f
        );
        canvas.drawRoundRect(batteryTerminal, size * 0.02f, size * 0.02f, batteryPaint);
        
        // Battery level indicator (3 bars)
        batteryPaint.setColor(Color.WHITE);
        batteryPaint.setAlpha(180);
        float barWidth = size * 0.15f;
        float barHeight = size * 0.08f;
        float barSpacing = size * 0.05f;
        
        canvas.drawRect(x - size * 0.3f, y - size * 0.2f, x - size * 0.3f + barWidth, y - size * 0.2f + barHeight, batteryPaint);
        canvas.drawRect(x - size * 0.1f, y - size * 0.2f, x - size * 0.1f + barWidth, y - size * 0.2f + barHeight, batteryPaint);
        canvas.drawRect(x + size * 0.1f, y - size * 0.2f, x + size * 0.1f + barWidth, y - size * 0.2f + barHeight, batteryPaint);
    }
    
    private void drawBluetoothIcon(Canvas canvas, float x, float y, float size) {
        // Bluetooth icon (bluetooth style)
        Paint bluetoothPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluetoothPaint.setColor(indicatorPaint.getColor());
        bluetoothPaint.setStyle(Paint.Style.STROKE);
        bluetoothPaint.setStrokeWidth(size * 0.08f);
        
        // Bluetooth symbol
        Path bluetoothPath = new Path();
        bluetoothPath.moveTo(x, y - size * 0.4f);
        bluetoothPath.lineTo(x + size * 0.2f, y - size * 0.2f);
        bluetoothPath.lineTo(x, y);
        bluetoothPath.lineTo(x + size * 0.2f, y + size * 0.2f);
        bluetoothPath.lineTo(x, y + size * 0.4f);
        bluetoothPath.lineTo(x - size * 0.2f, y + size * 0.2f);
        bluetoothPath.lineTo(x, y);
        bluetoothPath.lineTo(x - size * 0.2f, y - size * 0.2f);
        bluetoothPath.close();
        
        canvas.drawPath(bluetoothPath, bluetoothPaint);
    }
    
    private void drawGpsIcon(Canvas canvas, float x, float y, float size) {
        // GPS icon (gps_fixed style)
        Paint gpsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gpsPaint.setColor(indicatorPaint.getColor());
        gpsPaint.setStyle(Paint.Style.STROKE);
        gpsPaint.setStrokeWidth(size * 0.08f);
        
        // GPS crosshair
        canvas.drawLine(x - size * 0.3f, y, x + size * 0.3f, y, gpsPaint);
        canvas.drawLine(x, y - size * 0.3f, x, y + size * 0.3f, gpsPaint);
        
        // Center circle
        gpsPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, size * 0.1f, gpsPaint);
        
        // Outer circle
        gpsPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, size * 0.25f, gpsPaint);
    }
    
    private void drawDrlIcon(Canvas canvas, float x, float y, float size) {
        // DRL icon (wb_sunny style)
        Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunPaint.setColor(indicatorPaint.getColor());
        sunPaint.setStyle(Paint.Style.FILL);
        
        // Sun center
        canvas.drawCircle(x, y, size * 0.2f, sunPaint);
        
        // Sun rays
        sunPaint.setStyle(Paint.Style.STROKE);
        sunPaint.setStrokeWidth(size * 0.06f);
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI / 4);
            float startX = x + (float) Math.cos(angle) * size * 0.3f;
            float startY = y + (float) Math.sin(angle) * size * 0.3f;
            float endX = x + (float) Math.cos(angle) * size * 0.45f;
            float endY = y + (float) Math.sin(angle) * size * 0.45f;
            canvas.drawLine(startX, startY, endX, endY, sunPaint);
        }
    }
    
    private void drawLowBeamIcon(Canvas canvas, float x, float y, float size) {
        // Low beam icon - downward light
        canvas.drawRect(x - size * 0.3f, y - size * 0.2f, x + size * 0.3f, y + size * 0.2f, indicatorPaint);
        canvas.drawRect(x - size * 0.1f, y + size * 0.2f, x + size * 0.1f, y + size * 0.4f, indicatorPaint);
    }
    
    private void drawHighBeamIcon(Canvas canvas, float x, float y, float size) {
        // High beam icon - upward light
        canvas.drawRect(x - size * 0.3f, y - size * 0.2f, x + size * 0.3f, y + size * 0.2f, indicatorPaint);
        canvas.drawRect(x - size * 0.1f, y - size * 0.4f, x + size * 0.1f, y - size * 0.2f, indicatorPaint);
    }
    
    private void drawHazardIcon(Canvas canvas, float x, float y, float size) {
        // Hazard icon - triangle with exclamation
        float[] triangle = {
            x, y - size * 0.4f,
            x - size * 0.3f, y + size * 0.3f,
            x + size * 0.3f, y + size * 0.3f
        };
        canvas.drawLines(triangle, indicatorPaint);
        canvas.drawRect(x - size * 0.05f, y - size * 0.1f, x + size * 0.05f, y + size * 0.1f, indicatorPaint);
        canvas.drawCircle(x, y + size * 0.2f, size * 0.05f, indicatorPaint);
    }
    
    private void drawLeftTurnIcon(Canvas canvas, float x, float y, float size) {
        // Left turn arrow
        float[] arrow = {
            x + size * 0.2f, y - size * 0.3f,
            x - size * 0.2f, y,
            x + size * 0.2f, y + size * 0.3f,
            x + size * 0.1f, y + size * 0.1f,
            x + size * 0.1f, y - size * 0.1f
        };
        canvas.drawLines(arrow, indicatorPaint);
    }
    
    private void drawRightTurnIcon(Canvas canvas, float x, float y, float size) {
        // Right turn arrow
        float[] arrow = {
            x - size * 0.2f, y - size * 0.3f,
            x + size * 0.2f, y,
            x - size * 0.2f, y + size * 0.3f,
            x - size * 0.1f, y + size * 0.1f,
            x - size * 0.1f, y - size * 0.1f
        };
        canvas.drawLines(arrow, indicatorPaint);
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        invalidate();
    }
    
    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }
    
    public void setActiveColor(int color) {
        this.activeColor = color;
        invalidate();
    }
    
    public void setInactiveColor(int color) {
        this.inactiveColor = color;
        invalidate();
    }
    
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }
    
    public void updateTextSize() {
        // Force text size update to fixed size
        textPaint.setTextSize(14);
        invalidate();
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setBlinking(boolean blinking) {
        this.isBlinking = blinking;
        if (blinking) {
            startBlinking();
        } else {
            stopBlinking();
        }
    }
    
    private void startBlinking() {
        if (blinkRunnable != null) {
            blinkHandler.removeCallbacks(blinkRunnable);
        }
        
        blinkRunnable = new Runnable() {
            @Override
            public void run() {
                blinkState = !blinkState;
                invalidate();
                if (isBlinking) {
                    blinkHandler.postDelayed(this, 500); // Blink every 500ms
                }
            }
        };
        blinkHandler.post(blinkRunnable);
    }
    
    private void stopBlinking() {
        if (blinkRunnable != null) {
            blinkHandler.removeCallbacks(blinkRunnable);
            blinkRunnable = null;
        }
        blinkState = false;
        invalidate();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBlinking();
    }
}
