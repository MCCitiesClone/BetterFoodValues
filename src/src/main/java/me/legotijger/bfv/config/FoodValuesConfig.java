package me.legotijger.bfv.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Handles the food-values.yml configuration file
 */
public class FoodValuesConfig {
    
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    
    public FoodValuesConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        reloadConfig();
    }
    
    /**
     * Reloads the configuration from file
     * This method should be called asynchronously to avoid blocking the main thread
     */
    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "food-values.yml");
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load defaults from resources
        try (InputStream defaultStream = plugin.getResource("food-values.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                config.setDefaults(defaultConfig);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load default configuration", e);
        }
    }
    
    /**
     * Reloads the configuration asynchronously to avoid blocking the main thread
     */
    public void reloadConfigAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                reloadConfig();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info("Configuration reloaded successfully!");
                    // Clear any caches that might be affected by config changes
                    if (plugin instanceof me.legotijger.bfv.Main) {
                        ((me.legotijger.bfv.Main) plugin).clearCaches();
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Gets the configuration
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }
    
    /**
     * Saves the configuration to file
     */
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }
    
    /**
     * Saves the default configuration file if it doesn't exist
     */
    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "food-values.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("food-values.yml", false);
        }
    }
    
    /**
     * Gets the food value for a specific item
     */
    public double getFoodValue(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return 0;
        }
        double value = getConfig().getDouble(itemName + ".food", 0);
        return Math.max(0, Math.min(value, 20)); // Clamp between 0 and 20
    }
    
    /**
     * Gets the saturation value for a specific item
     */
    public double getSaturationValue(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return 0;
        }
        double value = getConfig().getDouble(itemName + ".saturation", 0);
        return Math.max(0, Math.min(value, 20)); // Clamp between 0 and 20
    }
    
    /**
     * Gets the damage value for a specific item
     */
    public double getDamageValue(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return 0;
        }
        double value = getConfig().getDouble(itemName + ".damage", 0);
        return Math.max(0, value); // Clamp to non-negative values
    }
    
    /**
     * Checks if an item has custom food values configured
     */
    public boolean hasCustomValues(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return false;
        }
        return getConfig().contains(itemName);
    }
}
