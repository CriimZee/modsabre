package com.lightsaber.managers;

import com.hytale.api.entity.Player;
import com.hytale.api.item.ItemStack;
import com.hytale.api.item.ItemMeta;
import com.hytale.api.nbt.NBTCompound;
import com.hytale.api.sound.Sound;
import com.hytale.api.particle.Particle;
import com.hytale.api.world.Location;
import com.lightsaber.LightsaberPlugin;
import com.lightsaber.data.LightsaberColor;
import com.lightsaber.data.LightsaberData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all lightsaber-related functionality
 */
public class LightsaberManager {
    
    private final LightsaberPlugin plugin;
    private final Map<UUID, LightsaberData> activeLightsabers;
    private final Set<String> lightsaberItemIds;
    
    public LightsaberManager(LightsaberPlugin plugin) {
        this.plugin = plugin;
        this.activeLightsabers = new ConcurrentHashMap<>();
        this.lightsaberItemIds = new HashSet<>(Arrays.asList(
            "lightsaber_mod:lightsaber_blue",
            "lightsaber_mod:lightsaber_green",
            "lightsaber_mod:lightsaber_red",
            "lightsaber_mod:lightsaber_purple",
            "lightsaber_mod:lightsaber_yellow",
            "lightsaber_mod:lightsaber_white"
        ));
    }
    
    /**
     * Check if an item is a lightsaber
     */
    public boolean isLightsaber(ItemStack item) {
        if (item == null) return false;
        return lightsaberItemIds.contains(item.getType().getId());
    }
    
    /**
     * Toggle lightsaber on/off
     */
    public void toggleLightsaber(Player player, ItemStack lightsaber) {
        if (!isLightsaber(lightsaber)) return;
        
        UUID playerId = player.getUniqueId();
        LightsaberData data = activeLightsabers.get(playerId);
        
        if (data != null && data.isActive()) {
            // Deactivate
            deactivateLightsaber(player, lightsaber, data);
        } else {
            // Activate
            activateLightsaber(player, lightsaber);
        }
    }
    
    /**
     * Activate a lightsaber
     */
    public void activateLightsaber(Player player, ItemStack lightsaber) {
        UUID playerId = player.getUniqueId();
        LightsaberColor color = getColorFromItem(lightsaber);
        
        LightsaberData data = new LightsaberData(playerId, color, true);
        activeLightsabers.put(playerId, data);
        
        // Update item NBT
        NBTCompound nbt = lightsaber.getOrCreateNBT();
        nbt.setBoolean("lightsaber_active", true);
        lightsaber.setNBT(nbt);
        
        // Play activation sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.of("lightsaber_mod:lightsaber_on"),
            1.0f, 1.0f
        );
        
        // Play animation
        player.playAnimation("lightsaber_mod:lightsaber_activate");
        
        // Spawn ignite particles
        spawnIgniteParticles(player, color);
        
        // Register for dynamic lighting
        plugin.getLightingManager().registerLight(player, color);
        
        // Start hum sound loop
        startHumSound(player, color);
    }
    
    /**
     * Deactivate a lightsaber
     */
    public void deactivateLightsaber(Player player, ItemStack lightsaber, LightsaberData data) {
        UUID playerId = player.getUniqueId();
        
        // Update data
        data.setActive(false);
        activeLightsabers.remove(playerId);
        
        // Update item NBT
        NBTCompound nbt = lightsaber.getOrCreateNBT();
        nbt.setBoolean("lightsaber_active", false);
        lightsaber.setNBT(nbt);
        
        // Play deactivation sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.of("lightsaber_mod:lightsaber_off"),
            1.0f, 1.0f
        );
        
        // Play animation
        player.playAnimation("lightsaber_mod:lightsaber_deactivate");
        
        // Remove dynamic lighting
        plugin.getLightingManager().unregisterLight(player);
        
        // Stop hum sound
        stopHumSound(player);
    }
    
    /**
     * Handle lightsaber swing
     */
    public void onSwing(Player player, ItemStack lightsaber) {
        if (!isLightsaberActive(player)) return;
        
        LightsaberData data = activeLightsabers.get(player.getUniqueId());
        if (data == null) return;
        
        // Play swing sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.of("lightsaber_mod:lightsaber_swing"),
            0.8f,
            0.9f + (float)(Math.random() * 0.2)
        );
        
        // Spawn trail particles
        spawnSwingTrail(player, data.getColor());
    }
    
    /**
     * Handle lightsaber hit on entity
     */
    public void onHit(Player attacker, Object target, ItemStack lightsaber) {
        if (!isLightsaberActive(attacker)) return;
        
        LightsaberData data = activeLightsabers.get(attacker.getUniqueId());
        if (data == null) return;
        
        Location hitLocation = attacker.getLocation().add(
            attacker.getDirection().multiply(2)
        );
        
        // Play hit sound
        attacker.getWorld().playSound(
            hitLocation,
            Sound.of("lightsaber_mod:lightsaber_hit"),
            1.0f,
            0.85f + (float)(Math.random() * 0.3)
        );
        
        // Spawn hit sparks
        spawnHitSparks(hitLocation, data.getColor());
    }
    
    /**
     * Handle lightsaber vs lightsaber clash
     */
    public void onClash(Player player1, Player player2) {
        LightsaberData data1 = activeLightsabers.get(player1.getUniqueId());
        LightsaberData data2 = activeLightsabers.get(player2.getUniqueId());
        
        if (data1 == null || data2 == null) return;
        
        // Calculate clash point (midpoint between players)
        Location clashPoint = player1.getLocation()
            .add(player2.getLocation())
            .multiply(0.5)
            .add(0, 1.5, 0);
        
        // Play clash sound
        player1.getWorld().playSound(
            clashPoint,
            Sound.of("lightsaber_mod:lightsaber_clash"),
            1.2f,
            0.9f + (float)(Math.random() * 0.2)
        );
        
        // Spawn clash effects
        spawnClashEffects(clashPoint, data1.getColor(), data2.getColor());
        
        // Apply knockback
        applyClashKnockback(player1, player2, clashPoint);
    }
    
    /**
     * Check if player has an active lightsaber
     */
    public boolean isLightsaberActive(Player player) {
        LightsaberData data = activeLightsabers.get(player.getUniqueId());
        return data != null && data.isActive();
    }
    
    /**
     * Get lightsaber color from item
     */
    private LightsaberColor getColorFromItem(ItemStack item) {
        String itemId = item.getType().getId();
        
        return switch (itemId) {
            case "lightsaber_mod:lightsaber_blue" -> LightsaberColor.BLUE;
            case "lightsaber_mod:lightsaber_green" -> LightsaberColor.GREEN;
            case "lightsaber_mod:lightsaber_red" -> LightsaberColor.RED;
            case "lightsaber_mod:lightsaber_purple" -> LightsaberColor.PURPLE;
            case "lightsaber_mod:lightsaber_yellow" -> LightsaberColor.YELLOW;
            case "lightsaber_mod:lightsaber_white" -> LightsaberColor.WHITE;
            default -> LightsaberColor.BLUE;
        };
    }
    
    /**
     * Spawn ignition particles
     */
    private void spawnIgniteParticles(Player player, LightsaberColor color) {
        Location loc = player.getLocation().add(0, 1.2, 0)
            .add(player.getDirection().multiply(0.5));
        
        player.getWorld().spawnParticle(
            Particle.of("lightsaber_mod:lightsaber_ignite"),
            loc,
            15,
            0.1, 0.5, 0.1,
            0.2
        );
    }
    
    /**
     * Spawn swing trail particles
     */
    private void spawnSwingTrail(Player player, LightsaberColor color) {
        String particleName = "lightsaber_mod:lightsaber_trail_" + color.name().toLowerCase();
        
        Location start = player.getLocation().add(0, 1.5, 0);
        Location end = start.clone().add(player.getDirection().multiply(3));
        
        // Spawn particles along the swing arc
        for (double t = 0; t <= 1; t += 0.1) {
            Location point = start.clone().lerp(end, t);
            player.getWorld().spawnParticle(
                Particle.of(particleName),
                point,
                3,
                0.05, 0.05, 0.05,
                0.01
            );
        }
    }
    
    /**
     * Spawn hit spark particles
     */
    private void spawnHitSparks(Location location, LightsaberColor color) {
        location.getWorld().spawnParticle(
            Particle.of("lightsaber_mod:lightsaber_sparks"),
            location,
            20,
            0.2, 0.2, 0.2,
            0.3
        );
    }
    
    /**
     * Spawn clash effects
     */
    private void spawnClashEffects(Location location, LightsaberColor color1, LightsaberColor color2) {
        // Spawn sparks
        location.getWorld().spawnParticle(
            Particle.of("lightsaber_mod:lightsaber_clash_sparks"),
            location,
            50,
            0.1, 0.1, 0.1,
            0.5
        );
        
        // Spawn flash
        location.getWorld().spawnParticle(
            Particle.of("lightsaber_mod:lightsaber_clash_flash"),
            location,
            1,
            0, 0, 0,
            0
        );
    }
    
    /**
     * Apply knockback on clash
     */
    private void applyClashKnockback(Player player1, Player player2, Location clashPoint) {
        // Direction from clash to each player
        Location dir1 = player1.getLocation().subtract(clashPoint).normalize();
        Location dir2 = player2.getLocation().subtract(clashPoint).normalize();
        
        // Apply velocity
        player1.setVelocity(dir1.toVector().multiply(0.5).setY(0.2));
        player2.setVelocity(dir2.toVector().multiply(0.5).setY(0.2));
    }
    
    /**
     * Start the humming sound loop
     */
    private void startHumSound(Player player, LightsaberColor color) {
        String soundName = color == LightsaberColor.RED 
            ? "lightsaber_mod:lightsaber_hum_sith"
            : "lightsaber_mod:lightsaber_hum";
        
        // Note: In actual implementation, this would use Hytale's
        // continuous sound system for proper looping
        player.playSound(Sound.of(soundName), 0.4f, 1.0f);
    }
    
    /**
     * Stop the humming sound
     */
    private void stopHumSound(Player player) {
        player.stopSound(Sound.of("lightsaber_mod:lightsaber_hum"));
        player.stopSound(Sound.of("lightsaber_mod:lightsaber_hum_sith"));
    }
    
    /**
     * Give a lightsaber to a player
     */
    public void giveLightsaber(Player player, LightsaberColor color) {
        String itemId = "lightsaber_mod:lightsaber_" + color.name().toLowerCase();
        ItemStack lightsaber = ItemStack.of(itemId, 1);
        
        NBTCompound nbt = lightsaber.getOrCreateNBT();
        nbt.setBoolean("lightsaber_active", false);
        nbt.setString("blade_color", color.name());
        lightsaber.setNBT(nbt);
        
        player.getInventory().addItem(lightsaber);
    }
    
    /**
     * Get active lightsaber data for a player
     */
    public LightsaberData getActiveLightsaber(Player player) {
        return activeLightsabers.get(player.getUniqueId());
    }
    
    /**
     * Cleanup when player disconnects
     */
    public void onPlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        LightsaberData data = activeLightsabers.remove(playerId);
        
        if (data != null && data.isActive()) {
            plugin.getLightingManager().unregisterLight(player);
        }
    }
}
