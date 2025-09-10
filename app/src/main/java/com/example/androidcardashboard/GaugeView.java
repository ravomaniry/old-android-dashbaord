package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint centerPaint;
    
    private float value = 0;
    private float minValue = 0;
    private float maxValue = 100;
    private String unit = "";
    private String label = "";
    private GaugeType gaugeType = GaugeType.GENERIC;
    
    private int centerX, centerY;
    private float radius;
    private RectF progressRect;
    
    // Color scheme
    private int normalColor = Color.parseColor("#4CAF50");
    private int warningColor = Color.parseColor("#FF9800");
    private int dangerColor = Color.parseColor("#F44336");
    
    public enum GaugeType {
        TEMPERATURE,  // For coolant temperature
        FUEL,         // For fuel level
        GENERIC       // For other gauges
    }
    
    // Theme colors
    private int primaryColor = Color.parseColor("#00BCD4");
    private int secondaryColor = Color.parseColor("#4CAF50");
    private int backgroundColor = Color.parseColor("#0A0A0A");
    
    public GaugeView(Context context) {
        super(context);
        init();
    }
    
    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Background paint for the gauge ring
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#404040"));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(15);
        
        // Progress paint for the value indicator
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(15);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Text paint for value
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        // Label paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#B0B0B0"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        // Center circle paint
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.parseColor("#1A1A1A"));
        centerPaint.setStyle(Paint.Style.FILL);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2 - 30;
        
        progressRect = new RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        );
        
        // Set text sizes based on radius
        textPaint.setTextSize(radius * 0.3f);
        labelPaint.setTextSize(radius * 0.15f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        
        // Calculate progress and color
        float normalizedValue = (value - minValue) / (maxValue - minValue);
        float sweepAngle = normalizedValue * 270; // 270 degrees for 3/4 circle
        
        // Set progress color based on value
        int progressColor = getProgressColor(normalizedValue);
        progressPaint.setColor(progressColor);
        
        // Draw progress arc
        canvas.drawArc(progressRect, -135, sweepAngle, false, progressPaint);
        
        // Draw center circle
        canvas.drawCircle(centerX, centerY, radius * 0.5f, centerPaint);
        
        // Draw value
        textPaint.setColor(progressColor);
        canvas.drawText(String.valueOf((int) value), centerX, centerY + radius * 0.1f, textPaint);
        
        // Draw unit
        canvas.drawText(unit, centerX, centerY + radius * 0.3f, labelPaint);
    }
    
    private int getProgressColor(float normalizedValue) {
        switch (gaugeType) {
            case TEMPERATURE:
                // Temperature gauge: Green for optimal (80-100°C), Orange for low/high, Red for extreme
                if (value >= 80 && value <= 100) {
                    return normalColor; // Green for optimal temperature
                } else if (value >= 70 && value < 80) {
                    return warningColor; // Orange for slightly low
                } else if (value > 100 && value <= 110) {
                    return warningColor; // Orange for slightly high
                } else {
                    return dangerColor; // Red for too low (<70) or too high (>110)
                }
                
            case FUEL:
                // Fuel gauge: Green for >40%, Orange for 5-40%, Red for <5%
                if (value > 40) {
                    return normalColor; // Green for good fuel level
                } else if (value >= 5) {
                    return warningColor; // Orange for low fuel
                } else {
                    return dangerColor; // Red for very low fuel
                }
                
            case GENERIC:
            default:
                // Generic gauge: Green for low, Orange for medium, Red for high
                if (normalizedValue < 0.3f) {
                    return normalColor;
                } else if (normalizedValue < 0.7f) {
                    return warningColor;
                } else {
                    return dangerColor;
                }
        }
    }
    
    public void setValue(float value) {
        this.value = Math.max(minValue, Math.min(maxValue, value));
        invalidate();
    }
    
    public void setRange(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        invalidate();
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }
    
    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }
    
    public void setGaugeType(GaugeType type) {
        this.gaugeType = type;
        invalidate();
    }
    
    public float getValue() {
        return value;
    }
}
