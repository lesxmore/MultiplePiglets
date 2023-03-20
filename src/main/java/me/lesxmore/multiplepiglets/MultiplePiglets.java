package me.lesxmore.multiplepiglets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public final class MultiplePiglets extends JavaPlugin implements Listener {

    // Add a default value for pigletsPerLitter
    private int pigletsPerLitter = 4;

    // Define the configuration file and its path
    private File configFile = new File(getDataFolder(), "config.yml");
    private FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);


    @Override
    public void onEnable() {

        // Load the configuration file
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Load the config.yml file if it exists
        if (configFile.exists()) {
            pigletsPerLitter = config.getInt("pigletsPerLitter", pigletsPerLitter);
        } else {
            // Save the default config.yml file if it does not exist
            config.set("pigletsPerLitter", pigletsPerLitter);
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Register the plugin's event listener
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPigBreed(EntityBreedEvent event) {
        // Check if the bred entity is a pig
        if (event.getEntity().getType() == EntityType.PIG) {
            Pig parent = (Pig) event.getEntity();

            // Get the maximum pigletsPerLitter allowed for the player's permission level
            int maxPigletsPerLitter = 0;
            for (int i = 10; i > 0; i--) {
                String permission = "multiplepiglets.pigletsPerLitter." + i;
                if (Objects.requireNonNull(event.getBreeder()).hasPermission(permission)) {
                    maxPigletsPerLitter = i;
                    break;
                }
            }

            // If the player does not have any permission for pigletsPerLitter, cancel the event
            if (maxPigletsPerLitter == 0) {
                event.setCancelled(true);
                return;
            }

            // Set the number of piglets based on the maximum pigletsPerLitter allowed
            Random rand = new Random();
            int numPiglets = rand.nextInt(maxPigletsPerLitter - 1) + 2; // 2 to maxPigletsPerLitter piglets

            // Spawn the piglets
            for (int i = 0; i < numPiglets; i++) {
                Pig piglet = (Pig) parent.getWorld().spawnEntity(parent.getLocation(), EntityType.PIG);
                piglet.setBaby();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("multiplepigletsreload")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            // Reload the configuration file
            reloadConfig();
            pigletsPerLitter = getConfig().getInt("pigletsPerLitter", pigletsPerLitter);
            sender.sendMessage(ChatColor.GREEN + "Configuration file reloaded.");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Invalid command. Usage: /multiplepigletsreload");
        return true;
    }
}
