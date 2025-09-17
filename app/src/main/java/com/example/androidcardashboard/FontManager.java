package com.example.androidcardashboard;

import android.content.Context;
import android.graphics.Typeface;
import java.util.HashMap;
import java.util.Map;

/**
 * Font manager for loading and caching custom fonts
 */
public class FontManager {
    private static FontManager instance;
    private Context context;
    private Map<String, Typeface> fontCache;
    
    private FontManager(Context context) {
        this.context = context.getApplicationContext();
        this.fontCache = new HashMap<>();
    }
    
    public static FontManager getInstance(Context context) {
        if (instance == null) {
            instance = new FontManager(context);
        }
        return instance;
    }
    
    /**
     * Load a font from assets/fonts directory
     * @param fontName The name of the font file (e.g., "FiraCode-Regular.ttf")
     * @return Typeface object or null if font not found
     */
    public Typeface getFont(String fontName) {
        if (fontCache.containsKey(fontName)) {
            return fontCache.get(fontName);
        }
        
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
            fontCache.put(fontName, typeface);
            return typeface;
        } catch (Exception e) {
            // Return default typeface if font loading fails
            return Typeface.DEFAULT;
        }
    }
    
    /**
     * Get Fira Code Regular font
     */
    public Typeface getFiraCodeRegular() {
        return getFont("FiraCode-Regular.ttf");
    }
    
    /**
     * Get Fira Code Bold font
     */
    public Typeface getFiraCodeBold() {
        return getFont("FiraCode-Bold.ttf");
    }
    
    /**
     * Get Fira Code Medium font
     */
    public Typeface getFiraCodeMedium() {
        return getFont("FiraCode-Medium.ttf");
    }
    
    /**
     * Get Fira Code Light font
     */
    public Typeface getFiraCodeLight() {
        return getFont("FiraCode-Light.ttf");
    }
    
    /**
     * Get Fira Code SemiBold font
     */
    public Typeface getFiraCodeSemiBold() {
        return getFont("FiraCode-SemiBold.ttf");
    }
    
    /**
     * Clear font cache (useful for memory management)
     */
    public void clearCache() {
        fontCache.clear();
    }
}
