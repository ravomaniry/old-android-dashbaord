package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
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
    
    // Color scheme - Theme-based colors
    private int normalColor;
    private int warningColor;
    private int dangerColor;
    
    public enum GaugeType {
        TEMPERATURE,  // For coolant temperature
        FUEL,         // For fuel level
        GENERIC       // For other gauges
    }
    
    // Theme colors - Dynamic based on current theme
    private int primaryColor;
    private int secondaryColor;
    
    // Gauge style
    private ThemeManager.GaugeStyle gaugeStyle = ThemeManager.GaugeStyle.MINIMAL;
    
    // Font
    private Typeface font = Typeface.DEFAULT;
    private ThemeManager themeManager;
    private int backgroundColor;
    
    public GaugeView(Context context) {
        super(context);
        this.themeManager = new ThemeManager(context);
        init();
    }
    
    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.themeManager = new ThemeManager(context);
        init();
    }
    
    private void init() {
        // Initialize theme colors
        updateThemeColors();
        
        // Background paint for the gauge ring
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(themeManager.getInactiveColor());
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
        textPaint.setTypeface(themeManager.getBoldFont());
        
        // Label paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(themeManager.getTextSecondaryColor());
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(themeManager.getPrimaryFont());
        
        // Center circle paint
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(themeManager.getContainerColor());
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
        
        // Calculate progress and color
        float normalizedValue = (value - minValue) / (maxValue - minValue);
        int progressColor = getProgressColor(normalizedValue);
        
        // Draw gauge based on style
        switch (gaugeStyle) {
            case MINIMAL:
                // Draw background circle for minimal theme
                canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
                drawMinimalStyleGauge(canvas, normalizedValue, progressColor);
                break;
            case HTOP:
                // No background circle for Linux theme
                drawHtopStyleGauge(canvas, normalizedValue, progressColor);
                break;
        }
        
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
    
    public void setPrimaryColor(int color) {
        this.primaryColor = color;
        invalidate();
    }
    
    public void setSecondaryColor(int color) {
        this.secondaryColor = color;
        invalidate();
    }
    
    public void setThemeColors(int primaryColor, int secondaryColor) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        invalidate();
    }
    
    public void setGaugeStyle(ThemeManager.GaugeStyle style) {
        this.gaugeStyle = style;
        invalidate();
    }
    
    public void setFont(Typeface font) {
        this.font = font;
        if (textPaint != null) {
            textPaint.setTypeface(themeManager.getBoldFont());
        }
        if (labelPaint != null) {
            labelPaint.setTypeface(themeManager.getPrimaryFont());
        }
        invalidate();
    }
    
    public void updateThemeColors() {
        this.normalColor = themeManager.getSuccessColor();
        this.warningColor = themeManager.getWarningColor();
        this.dangerColor = themeManager.getDangerColor();
        this.primaryColor = themeManager.getPrimaryAccentColor();
        this.secondaryColor = themeManager.getSecondaryAccentColor();
        this.backgroundColor = themeManager.getBackgroundColor();
        
        if (backgroundPaint != null) {
            backgroundPaint.setColor(themeManager.getInactiveColor());
        }
        if (labelPaint != null) {
            labelPaint.setColor(themeManager.getTextSecondaryColor());
        }
        if (centerPaint != null) {
            centerPaint.setColor(themeManager.getContainerColor());
        }
        invalidate();
    }
    
    private void drawHtopStyleGauge(Canvas canvas, float normalizedValue, int progressColor) {
        // Linux terminal style with pentagon ticks (like htop)
        int numTicks = 25; // More ticks with reduced spacing
        int activeTicks = Math.round(normalizedValue * numTicks);
        
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.FILL);
        
        for (int i = 0; i < numTicks; i++) {
            float angle = (float) (-Math.PI * 1.25 + (i / (float) numTicks) * Math.PI * 1.5);
            
            // Only draw active ticks (skip inactive gray ticks)
            if (i < activeTicks) {
                tickPaint.setColor(progressColor);
                // Draw pentagon tick
                drawPentagonTick(canvas, centerX, centerY, angle, radius, tickPaint);
            }
        }
    }
    
    private void drawPentagonTick(Canvas canvas, float centerX, float centerY, float angle, float radius, Paint paint) {
        // Create rounded pentagon tick pointing inward (no sharp tip)
        float tickLength = radius * 0.4f; // Much longer ticks - almost touch inner circle
        float tickWidth = 6;   // Thinner ticks for pentagon shape
        float innerRadius = radius * 0.5f; // Inner circle radius (matches center circle)
        
        // Calculate the five points of the rounded pentagon (no sharp tip)
        // Inner edge (rounded, not sharp)
        float innerX = centerX + (float) (Math.cos(angle) * innerRadius);
        float innerY = centerY + (float) (Math.sin(angle) * innerRadius);
        
        // Base of pentagon (at outer edge)
        float baseX = centerX + (float) (Math.cos(angle) * radius);
        float baseY = centerY + (float) (Math.sin(angle) * radius);
        
        // Calculate perpendicular points for pentagon width at the base
        float perpAngle1 = angle + (float) (Math.PI / 2);
        float perpAngle2 = angle - (float) (Math.PI / 2);
        
        float width1X = baseX + (float) (Math.cos(perpAngle1) * (tickWidth / 2));
        float width1Y = baseY + (float) (Math.sin(perpAngle1) * (tickWidth / 2));
        float width2X = baseX + (float) (Math.cos(perpAngle2) * (tickWidth / 2));
        float width2Y = baseY + (float) (Math.sin(perpAngle2) * (tickWidth / 2));
        
        // Calculate intermediate points for rounded pentagon shape
        float midRadius = innerRadius + (radius - innerRadius) * 0.4f;
        float midX = centerX + (float) (Math.cos(angle) * midRadius);
        float midY = centerY + (float) (Math.sin(angle) * midRadius);
        
        float midWidth1X = midX + (float) (Math.cos(perpAngle1) * (tickWidth / 3));
        float midWidth1Y = midY + (float) (Math.sin(perpAngle1) * (tickWidth / 3));
        float midWidth2X = midX + (float) (Math.cos(perpAngle2) * (tickWidth / 3));
        float midWidth2Y = midY + (float) (Math.sin(perpAngle2) * (tickWidth / 3));
        
        // Calculate inner edge points (rounded, not sharp)
        float innerWidth1X = innerX + (float) (Math.cos(perpAngle1) * (tickWidth / 4));
        float innerWidth1Y = innerY + (float) (Math.sin(perpAngle1) * (tickWidth / 4));
        float innerWidth2X = innerX + (float) (Math.cos(perpAngle2) * (tickWidth / 4));
        float innerWidth2Y = innerY + (float) (Math.sin(perpAngle2) * (tickWidth / 4));
        
        // Create rounded pentagon path (no sharp tip)
        Path pentagonPath = new Path();
        pentagonPath.moveTo(innerWidth1X, innerWidth1Y); // Inner edge 1 (rounded)
        pentagonPath.lineTo(midWidth1X, midWidth1Y); // First intermediate point
        pentagonPath.lineTo(width1X, width1Y); // Base edge 1
        pentagonPath.lineTo(width2X, width2Y); // Base edge 2
        pentagonPath.lineTo(midWidth2X, midWidth2Y); // Second intermediate point
        pentagonPath.lineTo(innerWidth2X, innerWidth2Y); // Inner edge 2 (rounded)
        pentagonPath.close();
        
        canvas.drawPath(pentagonPath, paint);
    }
    
    private void drawMinimalStyleGauge(Canvas canvas, float normalizedValue, int progressColor) {
        // Minimal style with smooth arc
        float sweepAngle = normalizedValue * 270; // 270 degrees for 3/4 circle
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(progressRect, -135, sweepAngle, false, progressPaint);
    }
}
