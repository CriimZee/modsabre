package com.lightsaber.commands;

import com.hytale.api.command.Command;
import com.hytale.api.command.CommandExecutor;
import com.hytale.api.command.CommandSender;
import com.hytale.api.command.TabCompleter;
import com.hytale.api.entity.Player;
import com.hytale.api.chat.ChatColor;
import com.lightsaber.LightsaberPlugin;
import com.lightsaber.data.LightsaberColor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all lightsaber mod commands
 */
public class LightsaberCommand implements CommandExecutor, TabCompleter {
    
    private final LightsaberPlugin plugin;
    
    public LightsaberCommand(LightsaberPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        return switch (subCommand) {
            case "give" -> handleGive(sender, args);
            case "color", "colors" -> handleColors(sender);
            case "duel" -> handleDuel(sender, args);
            case "help" -> { sendHelp(sender); yield true; }
            default -> { 
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /lightsaber help"); 
                yield false; 
            }
        };
    }
    
    /**
     * Handle /lightsaber give [color] [player]
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lightsaber.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return false;
        }
        
        // Default color
        LightsaberColor color = LightsaberColor.BLUE;
        Player target = null;
        
        // Parse arguments
        if (args.length >= 2) {
            color = LightsaberColor.fromString(args[1]);
        }
        
        if (args.length >= 3) {
            target = plugin.getServer().getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                return false;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Please specify a player!");
            return false;
        }
        
        // Give the lightsaber
        plugin.getLightsaberManager().giveLightsaber(target, color);
        
        sender.sendMessage(ChatColor.GREEN + "Gave " + color.getDisplayName() + 
            " lightsaber to " + target.getName() + "!");
        
        if (target != sender) {
            target.sendMessage(ChatColor.GOLD + "You received a " + 
                color.getDisplayName() + " lightsaber!");
        }
        
        return true;
    }
    
    /**
     * Handle /lightsaber colors - List available colors
     */
    private boolean handleColors(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "=== Available Lightsaber Colors ===");
        
        for (LightsaberColor color : LightsaberColor.values()) {
            String colorCode = getColorCode(color);
            sender.sendMessage(colorCode + "● " + color.getDisplayName() + 
                ChatColor.GRAY + " - " + color.getLore());
        }
        
        sender.sendMessage("");
        return true;
    }
    
    /**
     * Handle /lightsaber duel <player|accept|decline>
     */
    private boolean handleDuel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can duel!");
            return false;
        }
        
        if (!player.hasPermission("lightsaber.duel")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to duel!");
            return false;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /lightsaber duel <player|accept|decline>");
            return false;
        }
        
        String duelArg = args[1].toLowerCase();
        
        return switch (duelArg) {
            case "accept" -> plugin.getDuelManager().acceptDuel(player);
            case "decline" -> plugin.getDuelManager().declineDuel(player);
            default -> {
                // Try to find target player
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                    yield false;
                }
                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "You can't duel yourself!");
                    yield false;
                }
                yield plugin.getDuelManager().sendDuelRequest(player, target);
            }
        };
    }
    
    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "=== Lightsaber Mod Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/lightsaber give [color] [player]" + 
            ChatColor.GRAY + " - Give a lightsaber");
        sender.sendMessage(ChatColor.YELLOW + "/lightsaber colors" + 
            ChatColor.GRAY + " - List available colors");
        sender.sendMessage(ChatColor.YELLOW + "/lightsaber duel <player>" + 
            ChatColor.GRAY + " - Challenge to a duel");
        sender.sendMessage(ChatColor.YELLOW + "/lightsaber duel accept" + 
            ChatColor.GRAY + " - Accept a duel");
        sender.sendMessage(ChatColor.YELLOW + "/lightsaber duel decline" + 
            ChatColor.GRAY + " - Decline a duel");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Right-click with lightsaber to toggle on/off");
        sender.sendMessage("");
    }
    
    /**
     * Get chat color code for lightsaber color
     */
    private String getColorCode(LightsaberColor color) {
        return switch (color) {
            case BLUE -> ChatColor.BLUE.toString();
            case GREEN -> ChatColor.GREEN.toString();
            case RED -> ChatColor.RED.toString();
            case PURPLE -> ChatColor.LIGHT_PURPLE.toString();
            case YELLOW -> ChatColor.YELLOW.toString();
            case WHITE -> ChatColor.WHITE.toString();
        };
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(Arrays.asList("give", "colors", "duel", "help"), args[0]);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                return filterStartsWith(
                    Arrays.stream(LightsaberColor.values())
                        .map(c -> c.name().toLowerCase())
                        .collect(Collectors.toList()),
                    args[1]
                );
            }
            if (args[0].equalsIgnoreCase("duel")) {
                List<String> options = new ArrayList<>(Arrays.asList("accept", "decline"));
                plugin.getServer().getOnlinePlayers().forEach(p -> options.add(p.getName()));
                return filterStartsWith(options, args[1]);
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filterStartsWith(
                plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()),
                args[2]
            );
        }
        
        return Collections.emptyList();
    }
    
    private List<String> filterStartsWith(List<String> options, String prefix) {
        return options.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
