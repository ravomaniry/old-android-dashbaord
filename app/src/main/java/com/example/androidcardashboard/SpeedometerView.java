package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    
    // Theme colors
    private int primaryColor = Color.parseColor("#00BCD4");
    private int secondaryColor = Color.parseColor("#4CAF50");
    private int backgroundColor = Color.parseColor("#0A0A0A");
    
    // Speed-based colors
    private int greenColor = Color.parseColor("#4CAF50"); // Green up to 50 kph
    private int orangeColor = Color.parseColor("#FF9800"); // Orange until 80 kph
    private int redColor = Color.parseColor("#F44336"); // Red above 80 kph
    
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
        init();
    }
    
    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Background paint for the speedometer ring
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#404040"));
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
        
        // Center circle paint
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(backgroundColor);
        centerPaint.setStyle(Paint.Style.FILL);
        
        // RPM text paint
        rpmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rpmPaint.setColor(secondaryColor);
        rpmPaint.setTextAlign(Paint.Align.CENTER);
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
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        
        // Draw progress arc with speed-based color
        float sweepAngle = (speed / maxSpeed) * 270; // 270 degrees for 3/4 circle
        progressPaint.setColor(getSpeedBasedColor()); // Use speed-based color for the arc
        canvas.drawArc(progressRect, -135, sweepAngle, false, progressPaint);
        
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
            buttonPaint.setColor(Color.parseColor("#4CAF50"));
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
    
    public boolean getReverseGear() {
        return reverseGear;
    }
}
