package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class TripDetailView extends View {
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint labelPaint;
    private Paint valuePaint;
    
    private String label = "";
    private String value = "";
    private int textColor;
    private int valueColor;
    
    private int centerX, centerY;
    private RectF backgroundRect;
    
    // Font
    private Typeface font = Typeface.DEFAULT;
    private ThemeManager themeManager;
    
    public TripDetailView(Context context) {
        super(context);
        this.themeManager = new ThemeManager(context);
        init();
    }
    
    public TripDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.themeManager = new ThemeManager(context);
        init();
    }
    
    private void init() {
        // Initialize theme colors
        updateThemeColors();
        
        // Background paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(themeManager.getContainerColor());
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(themeManager.getInactiveColor());
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        
        
        // Label text paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(textColor);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(themeManager.getPrimaryFont());
        
        // Value text paint
        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(valueColor);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);
        valuePaint.setTypeface(themeManager.getBoldFont());
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2;
        centerY = h / 2;
        
        backgroundRect = new RectF(2, 2, w - 2, h - 2);
        
        // Set text sizes based on view dimensions
        labelPaint.setTextSize(h * 0.15f);
        valuePaint.setTextSize(h * 0.2f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawRoundRect(backgroundRect, 8, 8, backgroundPaint);
        canvas.drawRoundRect(backgroundRect, 8, 8, borderPaint);
        
        // Draw label (centered vertically)
        canvas.drawText(label, centerX, centerY - 10, labelPaint);
        
        // Draw value (below label)
        canvas.drawText(value, centerX, centerY + 20, valuePaint);
    }
    
    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }
    
    public void setValue(String value) {
        this.value = value;
        invalidate();
    }
    
    
    public void setValueColor(int color) {
        this.valueColor = color;
        valuePaint.setColor(color);
        invalidate();
    }
    
    public void setTextColor(int color) {
        this.textColor = color;
        labelPaint.setColor(color);
        invalidate();
    }
    
    public void setFont(Typeface font) {
        this.font = font;
        if (labelPaint != null) {
            labelPaint.setTypeface(themeManager.getPrimaryFont());
        }
        if (valuePaint != null) {
            valuePaint.setTypeface(themeManager.getBoldFont());
        }
        invalidate();
    }
    
    public void updateThemeColors() {
        this.textColor = themeManager.getTextSecondaryColor();
        this.valueColor = themeManager.getPrimaryAccentColor();
        
        if (backgroundPaint != null) {
            backgroundPaint.setColor(themeManager.getContainerColor());
        }
        if (borderPaint != null) {
            borderPaint.setColor(themeManager.getInactiveColor());
        }
        if (labelPaint != null) {
            labelPaint.setColor(textColor);
        }
        if (valuePaint != null) {
            valuePaint.setColor(valueColor);
        }
        invalidate();
    }
}
