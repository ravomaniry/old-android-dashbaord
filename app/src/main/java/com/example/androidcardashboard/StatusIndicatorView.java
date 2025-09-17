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
import android.view.View.OnClickListener;

public class StatusIndicatorView extends View implements OnClickListener {
    private Paint indicatorPaint;
    private Paint textPaint;
    private Paint backgroundPaint;
    
    private boolean isActive = false;
    private String label = "";
    private int activeColor = Color.parseColor("#00BCD4");
    private int inactiveColor = Color.parseColor("#404040");
    private int textColor = Color.parseColor("#B0B0B0");
    private boolean alwaysShowColor = false; // For indicators that should show color even when inactive
    
    private boolean isBlinking = false;
    private boolean blinkState = false;
    private Handler blinkHandler = new Handler();
    private Runnable blinkRunnable;
    
    private int centerX, centerY;
    private float indicatorRadius;
    
    private OnStatusClickListener statusClickListener;
    
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
        
        // Set click listener
        setOnClickListener(this);
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
            // For indicators that should always show color (like oil), use activeColor when inactive
            if (alwaysShowColor) {
                indicatorPaint.setColor(activeColor);
            } else {
                indicatorPaint.setColor(inactiveColor);
            }
        }
        
        // Draw icon based on label
        drawIcon(canvas);
        
        // Draw label closer to the indicator to save space
        if (!label.isEmpty()) {
            // Set text color to match the indicator color
            textPaint.setColor(indicatorPaint.getColor());
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
            case "WIFI":
                drawWifiIcon(canvas, iconX, iconY, iconSize);
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
        // Oil can icon - step 2: body + T-shaped cover
        Paint oilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oilPaint.setColor(indicatorPaint.getColor());
        oilPaint.setStyle(Paint.Style.FILL);
        
        // Main can body - simple rounded rectangle
        float canWidth = size * 0.6f;
        float canHeight = size * 0.6f; // Reduced from 0.8f to 0.6f
        float cornerRadius = size * 0.1f;
        
        android.graphics.RectF canRect = new android.graphics.RectF(
            x - canWidth * 0.5f, y - canHeight * 0.5f, 
            x + canWidth * 0.5f, y + canHeight * 0.5f);
        canvas.drawRoundRect(canRect, cornerRadius, cornerRadius, oilPaint);
        
        // T-shaped cover on top - smaller width than body
        float coverWidth = size * 0.4f; // Smaller than body width
        float coverHeight = size * 0.15f; // Short height for the cover
        float coverTop = y - canHeight * 0.5f - coverHeight * 0.5f - size * 0.05f; // Position above body, moved up
        
        // Horizontal part of T (top)
        android.graphics.RectF topRect = new android.graphics.RectF(
            x - coverWidth * 0.5f, coverTop - coverHeight * 0.3f,
            x + coverWidth * 0.5f, coverTop + coverHeight * 0.3f);
        canvas.drawRoundRect(topRect, cornerRadius * 0.5f, cornerRadius * 0.5f, oilPaint);
        
        // Vertical part of T (center stem)
        float stemWidth = size * 0.08f;
        android.graphics.RectF stemRect = new android.graphics.RectF(
            x - stemWidth * 0.5f, coverTop - coverHeight * 0.3f,
            x + stemWidth * 0.5f, coverTop + coverHeight * 0.3f);
        canvas.drawRoundRect(stemRect, cornerRadius * 0.3f, cornerRadius * 0.3f, oilPaint);
        
        // C-shaped handle from top left of body, going left, down, and back to center left
        Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(indicatorPaint.getColor());
        handlePaint.setStyle(Paint.Style.STROKE);
        handlePaint.setStrokeWidth(size * 0.08f);
        handlePaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Calculate handle positions
        float handleStartX = x - canWidth * 0.5f - size * 0.05f; // Left side of body - offset
        float handleTopY = y - canHeight * 0.3f; // Top left of body
        float handleBottomY = y + canHeight * 0.1f; // Center left of body
        float handleRadius = size * 0.12f; // Radius of the C-shape curve
        
        // Draw C-shaped handle as an arc (going left, down, and back)
        android.graphics.RectF handleRect = new android.graphics.RectF(
            handleStartX - handleRadius, handleTopY,
            handleStartX + handleRadius, handleBottomY + handleRadius);
        canvas.drawArc(handleRect, 90f, 180f, false, handlePaint);
        
        // Triangular neck/spout from right side of body
        Paint neckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        neckPaint.setColor(indicatorPaint.getColor());
        neckPaint.setStyle(Paint.Style.FILL);
        
        // Calculate neck positions
        float neckStartY = y - canHeight * 0.25f; // 25% from top of body
        float neckEndY = y + canHeight * 0.25f;  // 25% from bottom of body
        float neckBaseX = x + canWidth * 0.5f;   // Right side of body
        float neckTipX = neckBaseX + size * 0.35f; // Tip extends outward (longer)
        
        // Create triangular path for the neck
        Path neckPath = new Path();
        neckPath.moveTo(neckBaseX, neckStartY);      // Top-left corner (base against body)
        neckPath.lineTo(neckTipX, neckStartY);        // Tip horizontally aligned with highest point
        neckPath.lineTo(neckBaseX, neckEndY);       // Bottom-left corner (base against body)
        neckPath.close();                            // Close the triangle
        
        canvas.drawPath(neckPath, neckPaint);
        
        // Oil drop below the tip of the neck
        Paint dropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dropPaint.setColor(indicatorPaint.getColor());
        dropPaint.setStyle(Paint.Style.FILL);
        
        // Calculate drop positions
        float dropTopY = neckStartY + size * 0.08f; // Few pixels below the tip
        float dropBottomY = dropTopY + size * 0.15f; // Drop height
        float dropCenterX = neckTipX; // Centered below the tip
        float dropWidth = size * 0.12f; // Drop width
        
        // Create water drop path (pointy top, circular bottom)
        Path dropPath = new Path();
        dropPath.moveTo(dropCenterX, dropTopY); // Start at top point
        
        // Left side curve
        dropPath.quadTo(dropCenterX - dropWidth * 0.3f, dropTopY + size * 0.05f,
                        dropCenterX - dropWidth * 0.5f, dropBottomY - size * 0.03f);
        
        // Bottom curve (circular)
        dropPath.quadTo(dropCenterX, dropBottomY,
                        dropCenterX + dropWidth * 0.5f, dropBottomY - size * 0.03f);
        
        // Right side curve
        dropPath.quadTo(dropCenterX + dropWidth * 0.3f, dropTopY + size * 0.05f,
                        dropCenterX, dropTopY);
        
        dropPath.close();
        canvas.drawPath(dropPath, dropPaint);
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
    
    private void drawWifiIcon(Canvas canvas, float x, float y, float size) {
        // WiFi icon inspired by SVG design - clean signal arcs
        Paint wifiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wifiPaint.setColor(indicatorPaint.getColor());
        wifiPaint.setStyle(Paint.Style.STROKE);
        wifiPaint.setStrokeWidth(size * 0.08f);
        wifiPaint.setStrokeCap(Paint.Cap.ROUND);
        
        float centerX = x;
        float centerY = y;
        
        // Draw 4 concentric arcs representing WiFi signal strength
        // Each arc is progressively larger and positioned higher
        for (int i = 0; i < 4; i++) {
            float arcRadius = size * 0.2f + (i * size * 0.12f);
            float startAngle = 225f; // Start from bottom-left
            float sweepAngle = 90f;   // Quarter circle
            
            // Position each arc so they appear to emanate from the center point
            float arcCenterY = centerY + size * 0.1f;
            
            android.graphics.RectF rect = new android.graphics.RectF(
                centerX - arcRadius, arcCenterY - arcRadius, 
                centerX + arcRadius, arcCenterY + arcRadius);
            canvas.drawArc(rect, startAngle, sweepAngle, false, wifiPaint);
        }
        
        // Draw center dot (WiFi access point indicator)
        wifiPaint.setStyle(Paint.Style.FILL);
        float dotRadius = size * 0.06f;
        canvas.drawCircle(centerX, centerY + size * 0.1f, dotRadius, wifiPaint);
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
        // Low beam icon - lamp pointing to the right (same as high beam but with downward light rays)
        Paint lampPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lampPaint.setColor(indicatorPaint.getColor());
        lampPaint.setStyle(Paint.Style.FILL);
        
        // Calculate lamp dimensions - fill entire canvas height
        float lampWidth = size * 0.6f; // Width of the lamp
        float lampHeight = size; // Fill entire canvas height
        float leftEdge = x - size * 0.5f; // Left edge touches canvas edge
        float rightEdge = leftEdge + lampWidth; // Right edge
        float topEdge = y - lampHeight * 0.5f; // Top edge (full height)
        float bottomEdge = y + lampHeight * 0.5f; // Bottom edge (full height)
        
        // Draw the rectangular part (right side) with gap from circular part
        float bodyGap = size * 0.05f; // Small gap between circular and rectangular parts
        float rectLeft = leftEdge + size * 0.5f + bodyGap; // Start after the circular part + gap
        android.graphics.RectF rectPart = new android.graphics.RectF(
            rectLeft, topEdge, rightEdge, bottomEdge);
        canvas.drawRoundRect(rectPart, size * 0.05f, size * 0.05f, lampPaint);
        
        // Draw the perfect semicircle on the left - diameter same as canvas height
        float circleRadius = size * 0.5f; // Radius is half the canvas height (diameter = canvas height)
        float circleCenterX = leftEdge + circleRadius;
        float circleCenterY = y;
        
        // Create a path for the semicircle
        Path semicirclePath = new Path();
        android.graphics.RectF arcRect = new android.graphics.RectF(
            circleCenterX - circleRadius, circleCenterY - circleRadius,
            circleCenterX + circleRadius, circleCenterY + circleRadius);
        semicirclePath.addArc(arcRect, 90f, 180f); // 180 degrees starting from top
        canvas.drawPath(semicirclePath, lampPaint);
        
        // Draw 3 light rays pointing down and right (opposite of high beam)
        Paint rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rayPaint.setColor(indicatorPaint.getColor());
        rayPaint.setStyle(Paint.Style.STROKE);
        rayPaint.setStrokeWidth(size * 0.06f);
        rayPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Calculate ray positions
        float gap = size * 0.08f; // Small gap from the flat side
        float rayStartX = rightEdge + gap; // Start after the gap
        float rayLength = size * 0.3f; // Length of the rays
        
        // Draw 3 rays at different angles pointing downward
        for (int i = 0; i < 3; i++) {
            float rayY = y - size * 0.2f + (i * size * 0.2f); // Spread vertically
            float rayEndX = rayStartX + rayLength;
            float rayEndY = rayY + rayLength * 0.3f; // Angle down and right (opposite of high beam)
            
            canvas.drawLine(rayStartX, rayY, rayEndX, rayEndY, rayPaint);
        }
    }
    
    private void drawHighBeamIcon(Canvas canvas, float x, float y, float size) {
        // High beam icon - lamp pointing to the right (fills canvas height, perfectly circular left edge)
        Paint lampPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lampPaint.setColor(indicatorPaint.getColor());
        lampPaint.setStyle(Paint.Style.FILL);
        
        // Calculate lamp dimensions - fill entire canvas height
        float lampWidth = size * 0.6f; // Width of the lamp
        float lampHeight = size; // Fill entire canvas height
        float leftEdge = x - size * 0.5f; // Left edge touches canvas edge
        float rightEdge = leftEdge + lampWidth; // Right edge
        float topEdge = y - lampHeight * 0.5f; // Top edge (full height)
        float bottomEdge = y + lampHeight * 0.5f; // Bottom edge (full height)
        
        // Draw the rectangular part (right side) with gap from circular part
        float bodyGap = size * 0.05f; // Small gap between circular and rectangular parts
        float rectLeft = leftEdge + size * 0.5f + bodyGap; // Start after the circular part + gap
        android.graphics.RectF rectPart = new android.graphics.RectF(
            rectLeft, topEdge, rightEdge, bottomEdge);
        canvas.drawRoundRect(rectPart, size * 0.05f, size * 0.05f, lampPaint);
        
        // Draw the perfect semicircle on the left - diameter same as canvas height
        float circleRadius = size * 0.5f; // Radius is half the canvas height (diameter = canvas height)
        float circleCenterX = leftEdge + circleRadius;
        float circleCenterY = y;
        
        // Create a path for the semicircle
        Path semicirclePath = new Path();
        android.graphics.RectF arcRect = new android.graphics.RectF(
            circleCenterX - circleRadius, circleCenterY - circleRadius,
            circleCenterX + circleRadius, circleCenterY + circleRadius);
        semicirclePath.addArc(arcRect, 90f, 180f); // 180 degrees starting from top
        canvas.drawPath(semicirclePath, lampPaint);
        
        // Draw 3 light rays pointing up and right
        Paint rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rayPaint.setColor(indicatorPaint.getColor());
        rayPaint.setStyle(Paint.Style.STROKE);
        rayPaint.setStrokeWidth(size * 0.06f);
        rayPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Calculate ray positions
        float gap = size * 0.08f; // Small gap from the flat side
        float rayStartX = rightEdge + gap; // Start after the gap
        float rayLength = size * 0.3f; // Length of the rays
        
        // Draw 3 rays at different angles
        for (int i = 0; i < 3; i++) {
            float rayY = y - size * 0.2f + (i * size * 0.2f); // Spread vertically
            float rayEndX = rayStartX + rayLength;
            float rayEndY = rayY - rayLength * 0.3f; // Angle up and right
            
            canvas.drawLine(rayStartX, rayY, rayEndX, rayEndY, rayPaint);
        }
    }
    
    private void drawHazardIcon(Canvas canvas, float x, float y, float size) {
        // Hazard icon - warning triangle with exclamation mark (inspired by SVG)
        Paint hazardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hazardPaint.setColor(indicatorPaint.getColor());
        hazardPaint.setStyle(Paint.Style.STROKE);
        hazardPaint.setStrokeWidth(size * 0.08f);
        hazardPaint.setStrokeCap(Paint.Cap.ROUND);
        hazardPaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Draw the warning triangle outline
        Path trianglePath = new Path();
        float triangleHeight = size * 0.6f;
        float triangleWidth = size * 0.5f;
        
        // Triangle points: top, bottom-left, bottom-right
        trianglePath.moveTo(x, y - triangleHeight * 0.4f);
        trianglePath.lineTo(x - triangleWidth * 0.5f, y + triangleHeight * 0.3f);
        trianglePath.lineTo(x + triangleWidth * 0.5f, y + triangleHeight * 0.3f);
        trianglePath.close();
        
        canvas.drawPath(trianglePath, hazardPaint);
        
        // Draw the exclamation mark inside
        Paint exclamationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        exclamationPaint.setColor(indicatorPaint.getColor());
        exclamationPaint.setStyle(Paint.Style.FILL);
        
        // Vertical line of exclamation mark
        float lineWidth = size * 0.06f;
        float lineHeight = size * 0.25f;
        canvas.drawRect(x - lineWidth * 0.5f, y - lineHeight * 0.3f, 
                       x + lineWidth * 0.5f, y + lineHeight * 0.2f, exclamationPaint);
        
        // Dot at bottom of exclamation mark
        float dotRadius = size * 0.08f;
        canvas.drawCircle(x, y + lineHeight * 0.4f, dotRadius, exclamationPaint);
    }
    
    private void drawLeftTurnIcon(Canvas canvas, float x, float y, float size) {
        // Draw a clean left arrow using just the arrow head lines
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(indicatorPaint.getColor());
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(size * 0.15f);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Left arrow: < (just the arrow head)
        // Top diagonal line
        canvas.drawLine(x + size * 0.2f, y - size * 0.3f, x - size * 0.2f, y, arrowPaint);
        // Bottom diagonal line  
        canvas.drawLine(x + size * 0.2f, y + size * 0.3f, x - size * 0.2f, y, arrowPaint);
    }
    
    private void drawRightTurnIcon(Canvas canvas, float x, float y, float size) {
        // Draw a clean right arrow using just the arrow head lines
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(indicatorPaint.getColor());
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(size * 0.15f);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Right arrow: > (just the arrow head)
        // Top diagonal line
        canvas.drawLine(x - size * 0.2f, y - size * 0.3f, x + size * 0.2f, y, arrowPaint);
        // Bottom diagonal line
        canvas.drawLine(x - size * 0.2f, y + size * 0.3f, x + size * 0.2f, y, arrowPaint);
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
    
    public void setAlwaysShowColor(boolean alwaysShowColor) {
        this.alwaysShowColor = alwaysShowColor;
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
    
    public interface OnStatusClickListener {
        void onStatusClick(String statusType);
    }
    
    public void setOnStatusClickListener(OnStatusClickListener listener) {
        this.statusClickListener = listener;
    }
    
    @Override
    public void onClick(View v) {
        if (statusClickListener != null) {
            statusClickListener.onStatusClick(label);
        }
    }
}
