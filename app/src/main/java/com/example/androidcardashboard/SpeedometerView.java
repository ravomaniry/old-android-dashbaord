package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SpeedometerView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint centerPaint;
    private Paint rpmPaint;
    
    private float speed = 0;
    private float rpm = 0;
    private float maxSpeed = 120;
    private float maxRpm = 6000;
    private boolean reverseGear = false;
    
    private int centerX, centerY;
    private float radius;
    private RectF progressRect;
    
    // Theme colors - Dynamic based on current theme
    private int primaryColor;
    private int secondaryColor;
    private int backgroundColor;
    
    // Gauge style
    private ThemeManager.GaugeStyle gaugeStyle = ThemeManager.GaugeStyle.MINIMAL;
    
    // Font
    private Typeface font = Typeface.DEFAULT;
    private ThemeManager themeManager;
    
    // Speed-based colors - Dynamic based on current theme
    private int greenColor;
    private int orangeColor;
    private int redColor;
    
    // Button visibility and callbacks
    private boolean showDemoButton = false;
    private boolean showThemeButton = false;
    private OnButtonClickListener buttonClickListener;
    
    public interface OnButtonClickListener {
        void onDemoButtonClick();
        void onThemeButtonClick();
    }
    
    public SpeedometerView(Context context) {
        super(context);
        this.themeManager = new ThemeManager(context);
        init();
    }
    
    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.themeManager = new ThemeManager(context);
        init();
    }
    
    private void init() {
        // Initialize theme colors
        updateThemeColors();
        
        // Background paint for the speedometer ring
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(themeManager.getInactiveColor());
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20);
        
        // Progress paint for the speed indicator
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(primaryColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Text paint for speed value
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(primaryColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setTypeface(themeManager.getBoldFont());
        
        // Center circle paint
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(backgroundColor);
        centerPaint.setStyle(Paint.Style.FILL);
        
        // RPM text paint
        rpmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rpmPaint.setColor(secondaryColor);
        rpmPaint.setTextAlign(Paint.Align.CENTER);
        rpmPaint.setTypeface(themeManager.getPrimaryFont());
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2 - 40;
        
        progressRect = new RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        );
        
        // Set text sizes based on radius
        textPaint.setTextSize(radius * 0.4f);
        rpmPaint.setTextSize(radius * 0.15f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw gauge based on style
        switch (gaugeStyle) {
            case MINIMAL:
                // Draw background circle for minimal theme
                canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
                drawMinimalStyleGauge(canvas);
                break;
            case HTOP:
                // No background circle for Linux theme
                drawHtopStyleGauge(canvas);
                break;
            case ANALOG:
                // Analog style handles its own background and bezel
                drawAnalogStyleGauge(canvas);
                break;
        }
        
        // Draw center circle
        canvas.drawCircle(centerX, centerY, radius * 0.6f, centerPaint);
        
        // Get speed-based color
        int speedColor = getSpeedBasedColor();
        
        // Draw speed value or "R" for reverse
        if (reverseGear) {
            textPaint.setColor(primaryColor); // Use primary color for reverse
            canvas.drawText("R", centerX, centerY + radius * 0.1f, textPaint);
        } else {
            textPaint.setColor(speedColor); // Use speed-based color
            canvas.drawText(String.valueOf((int) speed), centerX, centerY + radius * 0.1f, textPaint);
        }
        
        // Draw "KM/H" label with speed-based color
        rpmPaint.setColor(speedColor);
        canvas.drawText("KM/H", centerX, centerY + radius * 0.3f, rpmPaint);
        
        // Draw RPM display at bottom (reset to original color)
        rpmPaint.setColor(secondaryColor);
        String rpmText = String.format("%.1fK RPM", rpm / 1000);
        canvas.drawText(rpmText, centerX, centerY + radius * 0.8f, rpmPaint);
        
        // Draw demo and theme buttons
        drawButtons(canvas);
    }
    
    private void drawButtons(Canvas canvas) {
        if (!showDemoButton && !showThemeButton) return;
        
        float buttonWidth = radius * 0.3f;
        float buttonHeight = radius * 0.15f;
        float buttonY = centerY + radius * 0.4f;
        
        // Demo button (left)
        if (showDemoButton) {
            float demoButtonX = centerX - radius * 0.4f;
            RectF demoButtonRect = new RectF(
                demoButtonX - buttonWidth/2,
                buttonY - buttonHeight/2,
                demoButtonX + buttonWidth/2,
                buttonY + buttonHeight/2
            );
            
            Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            buttonPaint.setColor(themeManager.getSuccessColor());
            buttonPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(demoButtonRect, 8, 8, buttonPaint);
            
            Paint buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            buttonTextPaint.setColor(Color.WHITE);
            buttonTextPaint.setTextAlign(Paint.Align.CENTER);
            buttonTextPaint.setTextSize(buttonHeight * 0.4f);
            canvas.drawText("DEMO", demoButtonX, buttonY + buttonHeight * 0.15f, buttonTextPaint);
        }
        
        // Theme button (right)
        if (showThemeButton) {
            float themeButtonX = centerX + radius * 0.4f;
            RectF themeButtonRect = new RectF(
                themeButtonX - buttonWidth/2,
                buttonY - buttonHeight/2,
                themeButtonX + buttonWidth/2,
                buttonY + buttonHeight/2
            );
            
            Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            buttonPaint.setColor(primaryColor);
            buttonPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(themeButtonRect, 8, 8, buttonPaint);
            
            Paint buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            buttonTextPaint.setColor(Color.WHITE);
            buttonTextPaint.setTextAlign(Paint.Align.CENTER);
            buttonTextPaint.setTextSize(buttonHeight * 0.4f);
            canvas.drawText("THEME", themeButtonX, buttonY + buttonHeight * 0.15f, buttonTextPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            
            float buttonWidth = radius * 0.3f;
            float buttonHeight = radius * 0.15f;
            float buttonY = centerY + radius * 0.4f;
            
            // Check demo button (left)
            if (showDemoButton) {
                float demoButtonX = centerX - radius * 0.4f;
                if (x >= demoButtonX - buttonWidth/2 && x <= demoButtonX + buttonWidth/2 &&
                    y >= buttonY - buttonHeight/2 && y <= buttonY + buttonHeight/2) {
                    if (buttonClickListener != null) {
                        buttonClickListener.onDemoButtonClick();
                    }
                    return true;
                }
            }
            
            // Check theme button (right)
            if (showThemeButton) {
                float themeButtonX = centerX + radius * 0.4f;
                if (x >= themeButtonX - buttonWidth/2 && x <= themeButtonX + buttonWidth/2 &&
                    y >= buttonY - buttonHeight/2 && y <= buttonY + buttonHeight/2) {
                    if (buttonClickListener != null) {
                        buttonClickListener.onThemeButtonClick();
                    }
                    return true;
                }
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    public void setSpeed(float speed) {
        this.speed = Math.max(0, Math.min(maxSpeed, speed));
        invalidate();
    }
    
    public void setRpm(float rpm) {
        this.rpm = Math.max(0, Math.min(maxRpm, rpm));
        invalidate();
    }
    
    public float getSpeed() {
        return speed;
    }
    
    private int getSpeedBasedColor() {
        if (speed <= 50) {
            return greenColor; // Green up to 50 kph
        } else if (speed <= 80) {
            return orangeColor; // Orange until 80 kph
        } else {
            return redColor; // Red above 80 kph
        }
    }
    
    public float getRpm() {
        return rpm;
    }
    
    public void setThemeColors(int primary, int secondary, int background) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.backgroundColor = background;

        // Update paint colors
        progressPaint.setColor(primaryColor);
        textPaint.setColor(primaryColor);
        centerPaint.setColor(backgroundColor);
        rpmPaint.setColor(secondaryColor);

        invalidate();
    }
    
    public void setShowDemoButton(boolean show) {
        this.showDemoButton = show;
        invalidate();
    }
    
    public void setShowThemeButton(boolean show) {
        this.showThemeButton = show;
        invalidate();
    }
    
    public void setButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
    }
    
    public void setReverseGear(boolean reverseGear) {
        this.reverseGear = reverseGear;
        invalidate();
    }
    
    public void setPrimaryColor(int color) {
        this.primaryColor = color;
        progressPaint.setColor(color);
        invalidate();
    }
    
    public void setSecondaryColor(int color) {
        this.secondaryColor = color;
        invalidate();
    }
    
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        centerPaint.setColor(color);
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
        if (rpmPaint != null) {
            rpmPaint.setTypeface(themeManager.getPrimaryFont());
        }
        invalidate();
    }
    
    public void updateThemeColors() {
        this.primaryColor = themeManager.getPrimaryAccentColor();
        this.secondaryColor = themeManager.getSecondaryAccentColor();
        this.backgroundColor = themeManager.getBackgroundColor();
        this.greenColor = themeManager.getSuccessColor();
        this.orangeColor = themeManager.getWarningColor();
        this.redColor = themeManager.getDangerColor();
        
        if (backgroundPaint != null) {
            backgroundPaint.setColor(themeManager.getInactiveColor());
        }
        if (progressPaint != null) {
            progressPaint.setColor(primaryColor);
        }
        if (centerPaint != null) {
            centerPaint.setColor(backgroundColor);
        }
        invalidate();
    }
    
    
    public boolean getReverseGear() {
        return reverseGear;
    }
    
    private void drawHtopStyleGauge(Canvas canvas) {
        // Linux terminal style with pentagon ticks (like htop)
        int numTicks = 50; // More ticks with reduced spacing
        float normalizedSpeed = Math.min(speed / maxSpeed, 1.0f);
        int activeTicks = Math.round(normalizedSpeed * numTicks);
        
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.FILL);
        
        for (int i = 0; i < numTicks; i++) {
            float angle = (float) (-Math.PI * 1.25 + (i / (float) numTicks) * Math.PI * 1.5);
            
            // Only draw active ticks (skip inactive gray ticks)
            if (i < activeTicks) {
                tickPaint.setColor(getSpeedBasedColor());
                // Draw pentagon tick
                drawPentagonTick(canvas, centerX, centerY, angle, radius, tickPaint);
            }
        }
    }
    
    private void drawPentagonTick(Canvas canvas, float centerX, float centerY, float angle, float radius, Paint paint) {
        // Create rounded pentagon tick pointing inward (no sharp tip)
        float tickLength = radius * 0.4f; // Much longer ticks - almost touch inner circle
        float tickWidth = 8;   // Thinner ticks for pentagon shape
        float innerRadius = radius * 0.6f; // Inner circle radius
        
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
    
    private void drawMinimalStyleGauge(Canvas canvas) {
        // Minimal style with smooth arc
        float sweepAngle = (speed / maxSpeed) * 270; // 270 degrees for 3/4 circle
        progressPaint.setColor(getSpeedBasedColor());
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(progressRect, -135, sweepAngle, false, progressPaint);
    }
    
    private void drawAnalogStyleGauge(Canvas canvas) {
        // Analog-style speedometer with traditional markings and needle
        
        // Draw gauge bezel (outer rim)
        Paint bezelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bezelPaint.setColor(themeManager.getSecondaryAccentColor()); // Gold bezel
        bezelPaint.setStyle(Paint.Style.STROKE);
        bezelPaint.setStrokeWidth(8);
        canvas.drawCircle(centerX, centerY, radius, bezelPaint);
        
        // Draw inner bezel ring
        Paint innerBezelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerBezelPaint.setColor(themeManager.getPrimaryAccentColor()); // Lighter gold
        innerBezelPaint.setStyle(Paint.Style.STROKE);
        innerBezelPaint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, radius - 6, innerBezelPaint);
        
        // Draw gauge face background
        canvas.drawCircle(centerX, centerY, radius - 12, backgroundPaint);
        
        // Draw tick marks (0-200 km/h scale)
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(themeManager.getTextSecondaryColor());
        tickPaint.setStrokeWidth(2);
        
        Paint majorTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        majorTickPaint.setColor(themeManager.getTextPrimaryColor());
        majorTickPaint.setStrokeWidth(3);
        
        Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setColor(themeManager.getTextPrimaryColor());
        numberPaint.setTextSize(radius * 0.08f);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(themeManager.getPrimaryFont());
        
        int numTicks = 24; // 24 major ticks for 0-120 km/h (every 5 km/h)
        for (int i = 0; i <= numTicks; i++) {
            float angle = (float) (-Math.PI * 1.25 + (i / (float) numTicks) * Math.PI * 1.5);
            float tickLength = (i % 2 == 0) ? radius * 0.12f : radius * 0.06f; // Major ticks every 10 km/h
            float tickStart = radius - 12 - tickLength;
            
            Paint currentTickPaint = (i % 2 == 0) ? majorTickPaint : tickPaint;
            
            float startX = centerX + (float) (Math.cos(angle) * tickStart);
            float startY = centerY + (float) (Math.sin(angle) * tickStart);
            float endX = centerX + (float) (Math.cos(angle) * (radius - 12));
            float endY = centerY + (float) (Math.sin(angle) * (radius - 12));
            
            canvas.drawLine(startX, startY, endX, endY, currentTickPaint);
            
            // Draw numbers for major ticks (every 10 km/h)
            if (i % 2 == 0) {
                float numberRadius = radius - 12 - tickLength - 15;
                float numberX = centerX + (float) (Math.cos(angle) * numberRadius);
                float numberY = centerY + (float) (Math.sin(angle) * numberRadius) + radius * 0.03f;
                canvas.drawText(String.valueOf(i * 5), numberX, numberY, numberPaint);
            }
        }
        
        // Draw needle with realistic design
        float normalizedSpeed = speed / maxSpeed;
        float needleAngle = (float) (-Math.PI * 1.25 + normalizedSpeed * Math.PI * 1.5);
        
        // Main needle (red/white) - longer and more visible
        Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(getSpeedBasedColor());
        needlePaint.setStrokeWidth(8);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
        
        float needleLength = radius * 0.75f; // Longer needle
        float needleEndX = centerX + (float) (Math.cos(needleAngle) * needleLength);
        float needleEndY = centerY + (float) (Math.sin(needleAngle) * needleLength);
        
        canvas.drawLine(centerX, centerY, needleEndX, needleEndY, needlePaint);
        
        // Needle counterweight (small circle at opposite end)
        Paint counterweightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        counterweightPaint.setColor(themeManager.getTextSecondaryColor());
        counterweightPaint.setStyle(Paint.Style.FILL);
        
        float counterweightLength = radius * 0.25f; // Longer counterweight
        float counterweightX = centerX + (float) (Math.cos(needleAngle + Math.PI) * counterweightLength);
        float counterweightY = centerY + (float) (Math.sin(needleAngle + Math.PI) * counterweightLength);
        canvas.drawCircle(counterweightX, counterweightY, 5, counterweightPaint);
        
        // Draw needle center hub
        Paint hubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hubPaint.setColor(themeManager.getSecondaryAccentColor()); // Gold hub
        hubPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 8, hubPaint);
        
        // Draw center dot
        Paint centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerDotPaint.setColor(themeManager.getBackgroundColor()); // Dark center
        centerDotPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 4, centerDotPaint);
    }
}
