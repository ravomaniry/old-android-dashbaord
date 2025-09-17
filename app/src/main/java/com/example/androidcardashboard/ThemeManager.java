package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class ThemeManager {
    public enum ThemeType {
        MINIMAL, LINUX, ANALOG
    }
    
    public enum GaugeStyle {
        MINIMAL, // Simple smooth arcs
        HTOP,    // Linux terminal style with ticks
        ANALOG   // Classic analog gauge with traditional styling
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
                currentTheme = ThemeType.ANALOG;
                break;
            case ANALOG:
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
            case ANALOG:
                return Color.parseColor("#2D1B0E"); // Dark brown/sepia background
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
            case ANALOG:
                return Color.parseColor("#3D2B1E"); // Medium brown container
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
            case ANALOG:
                return Color.parseColor("#D4AF37"); // Gold/brass accent
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
            case ANALOG:
                return Color.parseColor("#B8860B"); // Dark goldenrod
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
            case ANALOG:
                return Color.parseColor("#F5DEB3"); // Wheat/cream text
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
            case ANALOG:
                return Color.parseColor("#DEB887"); // Burlywood text
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
            case ANALOG:
                return Color.parseColor("#228B22"); // Forest green
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
            case ANALOG:
                return Color.parseColor("#CD853F"); // Peru/orange-brown
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
            case ANALOG:
                return Color.parseColor("#8B0000"); // Dark red
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
            case ANALOG:
                return Color.parseColor("#654321"); // Dark brown
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
            case ANALOG:
                return "Analog";
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
            case ANALOG:
                return GaugeStyle.ANALOG;
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
            case ANALOG:
                return Typeface.SERIF; // Use serif font for analog theme
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
            case ANALOG:
                return Typeface.create(Typeface.SERIF, Typeface.BOLD); // Use serif bold for analog theme
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
            case ANALOG:
                return Typeface.SERIF; // Use serif font for analog theme
            default:
                return Typeface.DEFAULT;
        }
    }
}
