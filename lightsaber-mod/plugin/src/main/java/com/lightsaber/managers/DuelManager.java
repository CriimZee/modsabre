package com.lightsaber.managers;

import com.hytale.api.entity.Player;
import com.hytale.api.world.Location;
import com.hytale.api.chat.ChatColor;
import com.hytale.api.title.Title;
import com.hytale.api.sound.Sound;
import com.lightsaber.LightsaberPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages lightsaber duels between players
 */
public class DuelManager {
    
    private final LightsaberPlugin plugin;
    private final Map<UUID, DuelRequest> pendingRequests;
    private final Map<UUID, ActiveDuel> activeDuels;
    
    private static final long REQUEST_TIMEOUT = 30000; // 30 seconds
    
    public DuelManager(LightsaberPlugin plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.activeDuels = new ConcurrentHashMap<>();
        startCleanupTask();
    }
    
    public boolean sendDuelRequest(Player challenger, Player target) {
        UUID challengerId = challenger.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        if (isInDuel(challenger) || isInDuel(target)) {
            challenger.sendMessage(ChatColor.RED + "One of you is already in a duel!");
            return false;
        }
        
        if (pendingRequests.containsKey(targetId)) {
            challenger.sendMessage(ChatColor.RED + target.getName() + " already has a pending duel request!");
            return false;
        }
        
        DuelRequest request = new DuelRequest(challengerId, targetId, System.currentTimeMillis());
        pendingRequests.put(targetId, request);
        
        challenger.sendMessage(ChatColor.GOLD + "Duel request sent to " + target.getName() + "!");
        target.sendMessage(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + challenger.getName() + 
            ChatColor.GOLD + " challenges you to a lightsaber duel!");
        target.sendMessage(ChatColor.GRAY + "Use /lightsaber duel accept or decline");
        target.playSound(Sound.of("hytale:ui_notification"), 1.0f, 1.2f);
        
        return true;
    }
    
    public boolean acceptDuel(Player accepter) {
        UUID accepterId = accepter.getUniqueId();
        DuelRequest request = pendingRequests.remove(accepterId);
        
        if (request == null) {
            accepter.sendMessage(ChatColor.RED + "No pending duel requests!");
            return false;
        }
        
        if (System.currentTimeMillis() - request.timestamp > REQUEST_TIMEOUT) {
            accepter.sendMessage(ChatColor.RED + "The duel request has expired!");
            return false;
        }
        
        Player challenger = plugin.getServer().getPlayer(request.challengerId);
        if (challenger == null || !challenger.isOnline()) {
            accepter.sendMessage(ChatColor.RED + "The challenger is no longer online!");
            return false;
        }
        
        startDuel(challenger, accepter);
        return true;
    }
    
    public boolean declineDuel(Player decliner) {
        UUID declinerId = decliner.getUniqueId();
        DuelRequest request = pendingRequests.remove(declinerId);
        
        if (request == null) {
            decliner.sendMessage(ChatColor.RED + "No pending duel requests!");
            return false;
        }
        
        decliner.sendMessage(ChatColor.GRAY + "You declined the duel request.");
        Player challenger = plugin.getServer().getPlayer(request.challengerId);
        if (challenger != null) {
            challenger.sendMessage(ChatColor.RED + decliner.getName() + " declined your duel.");
        }
        return true;
    }
    
    private void startDuel(Player player1, Player player2) {
        UUID id1 = player1.getUniqueId();
        UUID id2 = player2.getUniqueId();
        
        ActiveDuel duel = new ActiveDuel(id1, id2, System.currentTimeMillis());
        activeDuels.put(id1, duel);
        activeDuels.put(id2, duel);
        
        // Countdown sequence
        for (int i = 3; i >= 1; i--) {
            final int count = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                showCountdown(player1, player2, count);
            }, 20L * (4 - i));
        }
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            duel.started = true;
            Title startTitle = Title.builder()
                .title(ChatColor.RED + "⚔ FIGHT! ⚔")
                .subtitle(ChatColor.GRAY + "May the Force be with you")
                .fadeIn(5).stay(30).fadeOut(10).build();
            player1.showTitle(startTitle);
            player2.showTitle(startTitle);
            player1.playSound(Sound.of("hytale:combat_start"), 1.0f, 1.0f);
            player2.playSound(Sound.of("hytale:combat_start"), 1.0f, 1.0f);
        }, 80L);
    }
    
    private void showCountdown(Player p1, Player p2, int number) {
        String color = switch (number) {
            case 3 -> ChatColor.GREEN.toString();
            case 2 -> ChatColor.YELLOW.toString();
            case 1 -> ChatColor.RED.toString();
            default -> ChatColor.WHITE.toString();
        };
        
        Title countdownTitle = Title.builder()
            .title(color + number)
            .fadeIn(0).stay(20).fadeOut(5).build();
        
        p1.showTitle(countdownTitle);
        p2.showTitle(countdownTitle);
        p1.playSound(Sound.of("hytale:ui_countdown"), 0.8f, 1.0f);
        p2.playSound(Sound.of("hytale:ui_countdown"), 0.8f, 1.0f);
    }
    
    public void endDuel(Player winner, Player loser) {
        activeDuels.remove(winner.getUniqueId());
        activeDuels.remove(loser.getUniqueId());
        
        winner.showTitle(Title.builder()
            .title(ChatColor.GOLD + "⚔ VICTORY! ⚔")
            .subtitle(ChatColor.YELLOW + "You won the duel!")
            .fadeIn(10).stay(60).fadeOut(20).build());
        
        loser.showTitle(Title.builder()
            .title(ChatColor.RED + "DEFEAT")
            .subtitle(ChatColor.GRAY + winner.getName() + " won")
            .fadeIn(10).stay(60).fadeOut(20).build());
        
        winner.playSound(Sound.of("hytale:victory_fanfare"), 1.0f, 1.0f);
    }
    
    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }
    
    public Player getDuelOpponent(Player player) {
        ActiveDuel duel = activeDuels.get(player.getUniqueId());
        if (duel == null) return null;
        UUID opponentId = duel.player1Id.equals(player.getUniqueId()) ? duel.player2Id : duel.player1Id;
        return plugin.getServer().getPlayer(opponentId);
    }
    
    public void endAllDuels() {
        activeDuels.clear();
        pendingRequests.clear();
    }
    
    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            pendingRequests.entrySet().removeIf(e -> now - e.getValue().timestamp > REQUEST_TIMEOUT);
        }, 200L, 200L);
    }
    
    public void onPlayerQuit(Player player) {
        UUID id = player.getUniqueId();
        pendingRequests.remove(id);
        pendingRequests.values().removeIf(r -> r.challengerId.equals(id));
        
        ActiveDuel duel = activeDuels.remove(id);
        if (duel != null) {
            UUID oppId = duel.player1Id.equals(id) ? duel.player2Id : duel.player1Id;
            activeDuels.remove(oppId);
            Player opp = plugin.getServer().getPlayer(oppId);
            if (opp != null) opp.sendMessage(ChatColor.YELLOW + "Opponent disconnected. Duel ended.");
        }
    }
    
    private record DuelRequest(UUID challengerId, UUID targetId, long timestamp) {}
    
    private static class ActiveDuel {
        final UUID player1Id, player2Id;
        final long startTime;
        boolean started = false;
        ActiveDuel(UUID p1, UUID p2, long time) { player1Id = p1; player2Id = p2; startTime = time; }
    }
}
