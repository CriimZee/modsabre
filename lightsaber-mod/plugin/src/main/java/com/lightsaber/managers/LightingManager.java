package com.lightsaber.managers;

import com.hytale.api.entity.Player;
import com.hytale.api.world.Location;
import com.hytale.api.world.World;
import com.hytale.api.lighting.DynamicLight;
import com.hytale.api.lighting.LightColor;
import com.lightsaber.LightsaberPlugin;
import com.lightsaber.data.LightsaberColor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dynamic lighting effects for active lightsabers
 * This creates the iconic glow effect around lightsaber blades
 */
public class LightingManager {
    
    private final LightsaberPlugin plugin;
    private final Map<UUID, DynamicLightSource> activeLights;
    private final Map<LightsaberColor, LightColor> colorMap;
    
    // Light settings
    private static final int BASE_LIGHT_RADIUS = 8;
    private static final float BASE_INTENSITY = 1.2f;
    private static final float FLICKER_AMPLITUDE = 0.1f;
    private static final long FLICKER_PERIOD = 100; // ms
    
    public LightingManager(LightsaberPlugin plugin) {
        this.plugin = plugin;
        this.activeLights = new ConcurrentHashMap<>();
        this.colorMap = new EnumMap<>(LightsaberColor.class);
        
        initializeColorMap();
    }
    
    private void initializeColorMap() {
        colorMap.put(LightsaberColor.BLUE, new LightColor(0, 120, 255));
        colorMap.put(LightsaberColor.GREEN, new LightColor(0, 255, 100));
        colorMap.put(LightsaberColor.RED, new LightColor(255, 0, 0));
        colorMap.put(LightsaberColor.PURPLE, new LightColor(180, 0, 255));
        colorMap.put(LightsaberColor.YELLOW, new LightColor(255, 230, 0));
        colorMap.put(LightsaberColor.WHITE, new LightColor(255, 255, 255));
    }
    
    /**
     * Register a new dynamic light for a player's lightsaber
     */
    public void registerLight(Player player, LightsaberColor color) {
        UUID playerId = player.getUniqueId();
        
        // Remove existing light if any
        unregisterLight(player);
        
        LightColor lightColor = colorMap.get(color);
        if (lightColor == null) {
            lightColor = colorMap.get(LightsaberColor.BLUE);
        }
        
        // Determine if this color should flicker
        boolean shouldFlicker = (color == LightsaberColor.RED);
        
        DynamicLightSource source = new DynamicLightSource(
            player,
            lightColor,
            getRadiusForColor(color),
            getIntensityForColor(color),
            shouldFlicker
        );
        
        activeLights.put(playerId, source);
        
        // Create the actual dynamic light in the world
        source.create();
        
        plugin.getLogger().fine("Registered light for " + player.getName() + " with color " + color);
    }
    
    /**
     * Unregister a player's dynamic light
     */
    public void unregisterLight(Player player) {
        UUID playerId = player.getUniqueId();
        DynamicLightSource source = activeLights.remove(playerId);
        
        if (source != null) {
            source.destroy();
            plugin.getLogger().fine("Unregistered light for " + player.getName());
        }
    }
    
    /**
     * Update all active lights (called every tick)
     */
    public void updateAllLights() {
        long currentTime = System.currentTimeMillis();
        
        for (DynamicLightSource source : activeLights.values()) {
            source.update(currentTime);
        }
    }
    
    /**
     * Get light radius based on color
     */
    private int getRadiusForColor(LightsaberColor color) {
        return switch (color) {
            case WHITE -> 10;
            case PURPLE -> 9;
            case RED -> 8;
            case YELLOW -> 7;
            default -> BASE_LIGHT_RADIUS;
        };
    }
    
    /**
     * Get light intensity based on color
     */
    private float getIntensityForColor(LightsaberColor color) {
        return switch (color) {
            case WHITE -> 1.6f;
            case YELLOW -> 1.5f;
            case RED -> 1.4f;
            case PURPLE -> 1.3f;
            default -> BASE_INTENSITY;
        };
    }
    
    /**
     * Clean up all lights
     */
    public void cleanup() {
        for (DynamicLightSource source : activeLights.values()) {
            source.destroy();
        }
        activeLights.clear();
    }
    
    /**
     * Inner class representing a dynamic light source
     */
    private static class DynamicLightSource {
        private final Player player;
        private final LightColor baseColor;
        private final int radius;
        private final float baseIntensity;
        private final boolean flickers;
        
        private DynamicLight light;
        private float currentIntensity;
        
        public DynamicLightSource(Player player, LightColor color, int radius, 
                                   float intensity, boolean flickers) {
            this.player = player;
            this.baseColor = color;
            this.radius = radius;
            this.baseIntensity = intensity;
            this.flickers = flickers;
            this.currentIntensity = intensity;
        }
        
        /**
         * Create the dynamic light in the world
         */
        public void create() {
            World world = player.getWorld();
            Location loc = getLightLocation();
            
            light = world.createDynamicLight(loc, baseColor, radius, baseIntensity);
            light.setFollowEntity(player);
            light.setOffset(0, 1.5, 0); // Offset to blade height
        }
        
        /**
         * Update the light position and intensity
         */
        public void update(long currentTime) {
            if (light == null || !player.isOnline()) return;
            
            // Update position
            light.setLocation(getLightLocation());
            
            // Apply flicker effect if enabled
            if (flickers) {
                float flicker = (float) Math.sin(currentTime / (double) FLICKER_PERIOD) 
                    * FLICKER_AMPLITUDE;
                currentIntensity = baseIntensity + flicker;
                light.setIntensity(currentIntensity);
            }
            
            // Subtle pulsing for all lightsabers
            float pulse = (float) Math.sin(currentTime / 500.0) * 0.05f;
            int currentRadius = radius + Math.round(pulse * 2);
            light.setRadius(currentRadius);
        }
        
        /**
         * Get the current light location (at blade position)
         */
        private Location getLightLocation() {
            Location playerLoc = player.getLocation();
            // Offset forward and up to where the blade would be
            return playerLoc.add(
                player.getDirection().multiply(0.5)
            ).add(0, 1.5, 0);
        }
        
        /**
         * Destroy the light
         */
        public void destroy() {
            if (light != null) {
                light.remove();
                light = null;
            }
        }
    }
}
