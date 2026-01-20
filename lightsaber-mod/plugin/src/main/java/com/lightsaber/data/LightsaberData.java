package com.lightsaber.data;

import java.util.UUID;

/**
 * Stores data about an active lightsaber for a player
 */
public class LightsaberData {
    
    private final UUID playerId;
    private final LightsaberColor color;
    private boolean active;
    private long activationTime;
    private int swingCount;
    private int hitCount;
    
    public LightsaberData(UUID playerId, LightsaberColor color, boolean active) {
        this.playerId = playerId;
        this.color = color;
        this.active = active;
        this.activationTime = active ? System.currentTimeMillis() : 0;
        this.swingCount = 0;
        this.hitCount = 0;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public LightsaberColor getColor() {
        return color;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            this.activationTime = System.currentTimeMillis();
        }
    }
    
    public long getActivationTime() {
        return activationTime;
    }
    
    public long getActiveTime() {
        if (!active) return 0;
        return System.currentTimeMillis() - activationTime;
    }
    
    public int getSwingCount() {
        return swingCount;
    }
    
    public void incrementSwingCount() {
        this.swingCount++;
    }
    
    public int getHitCount() {
        return hitCount;
    }
    
    public void incrementHitCount() {
        this.hitCount++;
    }
    
    public void resetStats() {
        this.swingCount = 0;
        this.hitCount = 0;
    }
}
