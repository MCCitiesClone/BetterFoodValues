package me.legotijger.bfv;

import me.legotijger.bfv.command.ReloadCommand;
import me.legotijger.bfv.config.FoodValuesConfig;
import me.legotijger.bfv.listener.FoodConsumeListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for BetterFoodValues
 * 
 * A simple plugin that allows customization of food values through YAML configuration
 */
public class Main extends JavaPlugin {
    
    private FoodValuesConfig foodConfig;
    private FoodConsumeListener foodListener;
    
    @Override
    public void onEnable() {
        try {
            // Initialize configuration
            foodConfig = new FoodValuesConfig(this);
            
            // Register event listener
            foodListener = new FoodConsumeListener(this, foodConfig);
            getServer().getPluginManager().registerEvents(foodListener, this);
            
            // Register reload command
            if (getCommand("bfvreload") != null) {
                getCommand("bfvreload").setExecutor(new ReloadCommand(this, foodConfig));
            } else {
                getLogger().severe("Could not register reload command - command not found in plugin.yml");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // Log successful startup
            getLogger().info("BetterFoodValues has been enabled successfully!");
            getLogger().info("Food values can be customized in food-values.yml");
            getLogger().info("Use /bfvreload to reload configuration changes");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable BetterFoodValues: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("BetterFoodValues has been disabled.");
    }
    
    /**
     * Clears all caches when configuration is reloaded
     */
    public void clearCaches() {
        if (foodListener != null) {
            foodListener.clearCache();
        }
    }
}
