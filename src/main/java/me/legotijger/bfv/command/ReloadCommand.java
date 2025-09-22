package me.legotijger.bfv.command;

import me.legotijger.bfv.config.FoodValuesConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles the reload command for the plugin
 */
public class ReloadCommand implements CommandExecutor {
    
    private final JavaPlugin plugin;
    private final FoodValuesConfig foodConfig;
    
    public ReloadCommand(JavaPlugin plugin, FoodValuesConfig foodConfig) {
        this.plugin = plugin;
        this.foodConfig = foodConfig;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("bfv.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Send immediate feedback
        sender.sendMessage(ChatColor.YELLOW + "Reloading BetterFoodValues configuration...");
        plugin.getLogger().info("Configuration reload requested by " + sender.getName());
        
        // Reload configuration asynchronously to avoid blocking main thread
        foodConfig.reloadConfigAsync();
        
        return true;
    }
}
