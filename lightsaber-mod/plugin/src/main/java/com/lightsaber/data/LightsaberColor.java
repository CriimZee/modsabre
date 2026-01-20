package com.lightsaber.data;

/**
 * Enumeration of available lightsaber blade colors
 */
public enum LightsaberColor {
    BLUE(0, 120, 255, "Blue", "Jedi Guardian"),
    GREEN(0, 255, 100, "Green", "Jedi Consular"),
    RED(255, 0, 0, "Red", "Sith"),
    PURPLE(180, 0, 255, "Purple", "Rare"),
    YELLOW(255, 230, 0, "Yellow", "Jedi Sentinel"),
    WHITE(255, 255, 255, "White", "Purified");
    
    private final int red;
    private final int green;
    private final int blue;
    private final String displayName;
    private final String lore;
    
    LightsaberColor(int r, int g, int b, String name, String lore) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.displayName = name;
        this.lore = lore;
    }
    
    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }
    public String getDisplayName() { return displayName; }
    public String getLore() { return lore; }
    
    public int[] toRGB() {
        return new int[] { red, green, blue };
    }
    
    public String toHex() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }
    
    public static LightsaberColor fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BLUE;
        }
    }
}
