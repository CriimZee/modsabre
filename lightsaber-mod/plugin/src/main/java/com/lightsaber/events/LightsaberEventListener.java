package com.lightsaber.events;

import com.hytale.api.event.EventHandler;
import com.hytale.api.event.Listener;
import com.hytale.api.event.player.*;
import com.hytale.api.event.entity.*;
import com.hytale.api.entity.Player;
import com.hytale.api.entity.Entity;
import com.hytale.api.item.ItemStack;
import com.lightsaber.LightsaberPlugin;
import com.lightsaber.managers.LightsaberManager;
import com.lightsaber.managers.DuelManager;

/**
 * Handles all lightsaber-related events
 */
public class LightsaberEventListener implements Listener {
    
    private final LightsaberPlugin plugin;
    private final LightsaberManager lightsaberManager;
    private final DuelManager duelManager;
    
    public LightsaberEventListener(LightsaberPlugin plugin) {
        this.plugin = plugin;
        this.lightsaberManager = plugin.getLightsaberManager();
        this.duelManager = plugin.getDuelManager();
    }
    
    /**
     * Handle right-click to toggle lightsaber
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!lightsaberManager.isLightsaber(item)) return;
        
        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR ||
            event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            
            // Toggle lightsaber on/off
            lightsaberManager.toggleLightsaber(player, item);
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle lightsaber swing (left click air)
     */
    @EventHandler
    public void onPlayerSwing(PlayerArmSwingEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (lightsaberManager.isLightsaber(item) && lightsaberManager.isLightsaberActive(player)) {
            lightsaberManager.onSwing(player, item);
        }
    }
    
    /**
     * Handle lightsaber hitting an entity
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        
        ItemStack item = attacker.getInventory().getItemInMainHand();
        
        if (!lightsaberManager.isLightsaber(item)) return;
        if (!lightsaberManager.isLightsaberActive(attacker)) {
            // Lightsaber is off - reduced damage
            event.setDamage(event.getDamage() * 0.3);
            return;
        }
        
        Entity target = event.getEntity();
        
        // Check for lightsaber clash (both players have active lightsabers)
        if (target instanceof Player defender) {
            ItemStack defenderItem = defender.getInventory().getItemInMainHand();
            
            if (lightsaberManager.isLightsaber(defenderItem) && 
                lightsaberManager.isLightsaberActive(defender)) {
                
                // Lightsaber clash!
                lightsaberManager.onClash(attacker, defender);
                
                // Reduce damage on clash
                event.setDamage(event.getDamage() * 0.5);
                
                // Check for duel
                if (duelManager.isInDuel(attacker) && duelManager.isInDuel(defender)) {
                    // They're dueling - check if defender is low
                    if (defender.getHealth() - event.getDamage() <= 0) {
                        duelManager.endDuel(attacker, defender);
                    }
                }
            } else {
                // Normal hit on player without active lightsaber
                lightsaberManager.onHit(attacker, target, item);
            }
        } else {
            // Hit on non-player entity
            lightsaberManager.onHit(attacker, target, item);
        }
    }
    
    /**
     * Handle player switching items
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        // If switching away from an active lightsaber, deactivate it
        if (lightsaberManager.isLightsaber(previousItem) && 
            lightsaberManager.isLightsaberActive(player)) {
            
            var data = lightsaberManager.getActiveLightsaber(player);
            if (data != null) {
                lightsaberManager.deactivateLightsaber(player, previousItem, data);
            }
        }
    }
    
    /**
     * Handle player dropping a lightsaber
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        if (lightsaberManager.isLightsaber(item) && lightsaberManager.isLightsaberActive(player)) {
            var data = lightsaberManager.getActiveLightsaber(player);
            if (data != null) {
                lightsaberManager.deactivateLightsaber(player, item, data);
            }
        }
    }
    
    /**
     * Handle player death (end duel if in one)
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        
        // Clean up lightsaber
        lightsaberManager.onPlayerQuit(player);
        
        // Check if in duel
        if (duelManager.isInDuel(player)) {
            Player opponent = duelManager.getDuelOpponent(player);
            if (opponent != null) {
                duelManager.endDuel(opponent, player);
            }
        }
    }
    
    /**
     * Handle player disconnect
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        lightsaberManager.onPlayerQuit(player);
        duelManager.onPlayerQuit(player);
    }
    
    /**
     * Handle player respawn (cleanup any lingering effects)
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Make sure lighting is cleaned up
        plugin.getLightingManager().unregisterLight(player);
    }
}
