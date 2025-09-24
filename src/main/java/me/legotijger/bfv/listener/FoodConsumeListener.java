package me.legotijger.bfv.listener;

import me.legotijger.bfv.config.FoodValuesConfig;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles food consumption events to apply custom food values
 */
public class FoodConsumeListener implements Listener {
    
    private final JavaPlugin plugin;
    private final FoodValuesConfig foodConfig;
    
    // Cache for material names to avoid repeated string operations
    private final ConcurrentHashMap<Material, String> materialNameCache = new ConcurrentHashMap<>();
    
    public FoodConsumeListener(JavaPlugin plugin, FoodValuesConfig foodConfig) {
        this.plugin = plugin;
        this.foodConfig = foodConfig;
    }
    
    /**
     * Clears the material name cache
     * Should be called when configuration is reloaded
     */
    public void clearCache() {
        materialNameCache.clear();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Validate inputs
        if (player == null || item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Skip potions and other non-food items
        if (item.getType() == Material.POTION || 
            item.getType() == Material.SPLASH_POTION || 
            item.getType() == Material.LINGERING_POTION) {
            return;
        }
        
        // Get cached material name to avoid repeated string operations
        String itemName = materialNameCache.computeIfAbsent(item.getType(), 
            material -> material.name().toLowerCase());
        
        // Check if this item has custom food values configured
        if (!foodConfig.hasCustomValues(itemName)) {
            return;
        }
        if (Objects.requireNonNull(item.getItemMeta()).getAsComponentString().contains("nexo:id")) {
            return;
        }
        
        // Let the default consumption happen (including burp sound and item consumption)
        // We'll modify the food/saturation values after the default behavior
        
        // Schedule the food value modification for the next tick to ensure
        // the default consumption has completed
        int currentFood = player.getFoodLevel();
        float currentSaturation = player.getSaturation();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Apply custom food values
            double foodValue = foodConfig.getFoodValue(itemName);
            double saturationValue = foodConfig.getSaturationValue(itemName);
            double damageValue = foodConfig.getDamageValue(itemName);
            
            // Set food level (capped at 20)
            int newFood = (int) Math.min(currentFood + foodValue, 20);
            player.setFoodLevel(newFood);
            
            // Set saturation level (capped at current food level)
            float newSaturation = (float) Math.min(currentSaturation + saturationValue, newFood);
            player.setSaturation(newSaturation);
            
            // Apply damage if specified
            if (damageValue > 0) {
                player.damage(damageValue);
            }
            
            if (plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
                plugin.getLogger().fine(String.format(
                    "Player %s consumed %s: +%.1f food, +%.1f saturation, %.1f damage",
                    player.getName(), itemName, foodValue, saturationValue, damageValue
                ));
            }
        });
    }
}
