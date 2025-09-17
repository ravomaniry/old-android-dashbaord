package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class StatusBoxView extends View {
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint textPaint;
    
    private String title = "";
    private int backgroundColor;
    private int borderColor;
    private int textColor;
    
    private int centerX, centerY;
    private RectF backgroundRect;
    private ThemeManager themeManager;
    
    public StatusBoxView(Context context) {
        super(context);
        this.themeManager = ThemeManager.getInstance(context);
        init();
    }
    
    public StatusBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.themeManager = ThemeManager.getInstance(context);
        init();
    }
    
    private void init() {
        // Initialize theme colors
        updateThemeColors();
        
        // Background paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(24);
        textPaint.setTypeface(themeManager.getPrimaryFont());
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2;
        centerY = h / 2;
        
        backgroundRect = new RectF(2, 2, w - 2, h - 2);
        
        // Set text size based on view height
        textPaint.setTextSize(h * 0.08f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background with rounded corners
        canvas.drawRoundRect(backgroundRect, 8, 8, backgroundPaint);
        canvas.drawRoundRect(backgroundRect, 8, 8, borderPaint);
        
        // Draw title
        canvas.drawText(title, centerX, centerY - 20, textPaint);
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }
    
    public void setBorderColor(int color) {
        this.borderColor = color;
        borderPaint.setColor(color);
        invalidate();
    }
    
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }
    
    public void updateThemeColors() {
        this.backgroundColor = themeManager.getContainerColor();
        this.borderColor = themeManager.getInactiveColor();
        this.textColor = themeManager.getPrimaryAccentColor();
        
        if (backgroundPaint != null) {
            backgroundPaint.setColor(backgroundColor);
        }
        if (borderPaint != null) {
            borderPaint.setColor(borderColor);
        }
        if (textPaint != null) {
            textPaint.setColor(textColor);
        }
        invalidate();
    }
}
