package com.example.androidcardashboard;

import android.graphics.Color;

public class ThemeManager {
    public enum ThemeType {
        LINUX, CLASSIC, MODERN, TESLA
    }
    
    private ThemeType currentTheme = ThemeType.LINUX;
    
    public ThemeType getCurrentTheme() {
        return currentTheme;
    }
    
    public void cycleTheme() {
        switch (currentTheme) {
            case LINUX:
                currentTheme = ThemeType.CLASSIC;
                break;
            case CLASSIC:
                currentTheme = ThemeType.MODERN;
                break;
            case MODERN:
                currentTheme = ThemeType.TESLA;
                break;
            case TESLA:
                currentTheme = ThemeType.LINUX;
                break;
        }
    }
    
    // Color schemes for different themes
    public int getBackgroundColor() {
        switch (currentTheme) {
            case LINUX:
                return Color.parseColor("#0A0A0A");
            case CLASSIC:
                return Color.parseColor("#1A1A1A");
            case MODERN:
                return Color.parseColor("#0F0F0F");
            case TESLA:
                return Color.parseColor("#000000");
            default:
                return Color.parseColor("#0A0A0A");
        }
    }
    
    public int getContainerColor() {
        switch (currentTheme) {
            case LINUX:
                return Color.parseColor("#1A1A1A");
            case CLASSIC:
                return Color.parseColor("#2A2A2A");
            case MODERN:
                return Color.parseColor("#1F1F1F");
            case TESLA:
                return Color.parseColor("#111111");
            default:
                return Color.parseColor("#1A1A1A");
        }
    }
    
    public int getPrimaryAccentColor() {
        switch (currentTheme) {
            case LINUX:
                return Color.parseColor("#00BCD4"); // Cyan
            case CLASSIC:
                return Color.parseColor("#2196F3"); // Blue
            case MODERN:
                return Color.parseColor("#00E5FF"); // Light Cyan
            case TESLA:
                return Color.parseColor("#FFFFFF"); // White
            default:
                return Color.parseColor("#00BCD4");
        }
    }
    
    public int getSecondaryAccentColor() {
        switch (currentTheme) {
            case LINUX:
                return Color.parseColor("#4CAF50"); // Green
            case CLASSIC:
                return Color.parseColor("#FF9800"); // Orange
            case MODERN:
                return Color.parseColor("#00BCD4"); // Cyan
            case TESLA:
                return Color.parseColor("#CCCCCC"); // Light Gray
            default:
                return Color.parseColor("#4CAF50");
        }
    }
    
    public int getTextPrimaryColor() {
        switch (currentTheme) {
            case LINUX:
                return Color.parseColor("#FFFFFF");
            case CLASSIC:
                return Color.parseColor("#FFFFFF");
            case MODERN:
                return Color.parseColor("#FFFFFF");
            case TESLA:
                return Color.parseColor("#FFFFFF");
            default:
                return Color.parseColor("#FFFFFF");
        }
    }
    
    public int getTextSecondaryColor() {
        switch (currentTheme) {
            case LINUX:
                return Color.parseColor("#888888");
            case CLASSIC:
                return Color.parseColor("#AAAAAA");
            case MODERN:
                return Color.parseColor("#CCCCCC");
            case TESLA:
                return Color.parseColor("#999999");
            default:
                return Color.parseColor("#888888");
        }
    }
    
    public int getSuccessColor() {
        return Color.parseColor("#4CAF50");
    }
    
    public int getWarningColor() {
        return Color.parseColor("#FF9800");
    }
    
    public int getDangerColor() {
        return Color.parseColor("#F44336");
    }
    
    public int getInactiveColor() {
        return Color.parseColor("#333333");
    }
    
    public String getThemeName() {
        switch (currentTheme) {
            case LINUX:
                return "Linux";
            case CLASSIC:
                return "Classic";
            case MODERN:
                return "Modern";
            case TESLA:
                return "Tesla";
            default:
                return "Linux";
        }
    }
}
