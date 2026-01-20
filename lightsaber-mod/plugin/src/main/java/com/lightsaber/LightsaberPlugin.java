package com.lightsaber;

import com.hytale.api.HytalePlugin;
import com.hytale.api.event.EventHandler;
import com.hytale.api.event.Listener;
import com.hytale.api.command.CommandExecutor;
import com.hytale.api.entity.Player;
import com.hytale.api.item.ItemStack;
import com.hytale.api.world.World;
import com.hytale.api.scheduler.Scheduler;
import com.lightsaber.commands.LightsaberCommand;
import com.lightsaber.events.LightsaberEventListener;
import com.lightsaber.managers.LightsaberManager;
import com.lightsaber.managers.DuelManager;
import com.lightsaber.managers.LightingManager;

import java.util.logging.Logger;

/**
 * Main plugin class for the Lightsaber Mod
 * Handles initialization, events, and core functionality
 */
public class LightsaberPlugin extends HytalePlugin {
    
    private static LightsaberPlugin instance;
    private Logger logger;
    
    private LightsaberManager lightsaberManager;
    private DuelManager duelManager;
    private LightingManager lightingManager;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        logger.info("=================================");
        logger.info("  Lightsaber Mod v1.0.0");
        logger.info("  May the Force be with you!");
        logger.info("=================================");
        
        // Initialize managers
        initializeManagers();
        
        // Register events
        registerEvents();
        
        // Register commands
        registerCommands();
        
        // Start lighting update task
        startLightingTask();
        
        logger.info("Lightsaber Mod enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        // Clean up
        if (lightingManager != null) {
            lightingManager.cleanup();
        }
        if (duelManager != null) {
            duelManager.endAllDuels();
        }
        
        logger.info("Lightsaber Mod disabled.");
    }
    
    private void initializeManagers() {
        lightsaberManager = new LightsaberManager(this);
        duelManager = new DuelManager(this);
        lightingManager = new LightingManager(this);
        
        logger.info("Managers initialized.");
    }
    
    private void registerEvents() {
        LightsaberEventListener eventListener = new LightsaberEventListener(this);
        getServer().getEventManager().registerEvents(eventListener, this);
        
        logger.info("Events registered.");
    }
    
    private void registerCommands() {
        LightsaberCommand command = new LightsaberCommand(this);
        getServer().getCommandManager().registerCommand("lightsaber", command);
        
        logger.info("Commands registered.");
    }
    
    private void startLightingTask() {
        // Update dynamic lighting every tick (50ms)
        getServer().getScheduler().runTaskTimer(this, () -> {
            lightingManager.updateAllLights();
        }, 0L, 1L);
        
        logger.info("Lighting update task started.");
    }
    
    // Getters
    public static LightsaberPlugin getInstance() {
        return instance;
    }
    
    public LightsaberManager getLightsaberManager() {
        return lightsaberManager;
    }
    
    public DuelManager getDuelManager() {
        return duelManager;
    }
    
    public LightingManager getLightingManager() {
        return lightingManager;
    }
}
