package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class ThemeManager {
    public enum ThemeType {
        MINIMAL, LINUX
    }
    
    public enum GaugeStyle {
        MINIMAL, // Simple smooth arcs
        HTOP     // Linux terminal style with ticks
    }
    
    private ThemeType currentTheme = ThemeType.MINIMAL; // Start with Minimal as default
    private FontManager fontManager;
    
    public ThemeManager(Context context) {
        this.fontManager = FontManager.getInstance(context);
    }
    
    public ThemeType getCurrentTheme() {
        return currentTheme;
    }
    
    public void cycleTheme() {
        switch (currentTheme) {
            case MINIMAL:
                currentTheme = ThemeType.LINUX;
                break;
            case LINUX:
                currentTheme = ThemeType.MINIMAL;
                break;
        }
    }
    
    // Color schemes for different themes
    public int getBackgroundColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#0F0F0F");
            case LINUX:
                return Color.parseColor("#0A0A0A");
            default:
                return Color.parseColor("#0F0F0F");
        }
    }
    
    public int getContainerColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#1F1F1F");
            case LINUX:
                return Color.parseColor("#1A1A1A");
            default:
                return Color.parseColor("#1F1F1F");
        }
    }
    
    public int getPrimaryAccentColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#00E5FF"); // Light Cyan
            case LINUX:
                return Color.parseColor("#00FF41"); // Terminal Green
            default:
                return Color.parseColor("#00E5FF");
        }
    }
    
    public int getSecondaryAccentColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#40C4FF"); // Light Blue
            case LINUX:
                return Color.parseColor("#00D9FF"); // Terminal Cyan
            default:
                return Color.parseColor("#40C4FF");
        }
    }
    
    public int getTextPrimaryColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#E0E0E0");
            case LINUX:
                return Color.parseColor("#00D9FF"); // Terminal cyan text
            default:
                return Color.parseColor("#E0E0E0");
        }
    }
    
    public int getTextSecondaryColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#CCCCCC");
            case LINUX:
                return Color.parseColor("#888888");
            default:
                return Color.parseColor("#CCCCCC");
        }
    }
    
    public int getSuccessColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#00E676"); // Light Green
            case LINUX:
                return Color.parseColor("#00FF41"); // Terminal Green
            default:
                return Color.parseColor("#00E676");
        }
    }
    
    public int getWarningColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#FFB74D"); // Light Orange
            case LINUX:
                return Color.parseColor("#FF9800"); // Orange
            default:
                return Color.parseColor("#FFB74D");
        }
    }
    
    public int getDangerColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#FF5252"); // Light Red
            case LINUX:
                return Color.parseColor("#FF5722"); // Terminal Red
            default:
                return Color.parseColor("#FF5252");
        }
    }
    
    public int getInactiveColor() {
        switch (currentTheme) {
            case MINIMAL:
                return Color.parseColor("#424242");
            case LINUX:
                return Color.parseColor("#444444");
            default:
                return Color.parseColor("#424242");
        }
    }
    
    public String getThemeName() {
        switch (currentTheme) {
            case MINIMAL:
                return "Minimal";
            case LINUX:
                return "Linux";
            default:
                return "Minimal";
        }
    }
    
    public GaugeStyle getGaugeStyle() {
        switch (currentTheme) {
            case MINIMAL:
                return GaugeStyle.MINIMAL;
            case LINUX:
                return GaugeStyle.HTOP;
            default:
                return GaugeStyle.MINIMAL;
        }
    }
    
    /**
     * Get the primary font for the current theme
     */
    public Typeface getPrimaryFont() {
        switch (currentTheme) {
            case MINIMAL:
                return Typeface.DEFAULT; // Use system default for minimal theme
            case LINUX:
                return fontManager.getFiraCodeRegular(); // Use Fira Code for Linux theme
            default:
                return Typeface.DEFAULT;
        }
    }
    
    /**
     * Get the bold font for the current theme
     */
    public Typeface getBoldFont() {
        switch (currentTheme) {
            case MINIMAL:
                return Typeface.DEFAULT_BOLD; // Use system default bold for minimal theme
            case LINUX:
                return fontManager.getFiraCodeBold(); // Use Fira Code Bold for Linux theme
            default:
                return Typeface.DEFAULT_BOLD;
        }
    }
    
    /**
     * Get the medium font for the current theme
     */
    public Typeface getMediumFont() {
        switch (currentTheme) {
            case MINIMAL:
                return Typeface.DEFAULT; // Use system default for minimal theme
            case LINUX:
                return fontManager.getFiraCodeMedium(); // Use Fira Code Medium for Linux theme
            default:
                return Typeface.DEFAULT;
        }
    }
}
