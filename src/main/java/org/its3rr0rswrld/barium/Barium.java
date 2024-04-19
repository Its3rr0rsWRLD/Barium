package org.its3rr0rswrld.barium;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.its3rr0rswrld.barium.Classes.*;

import java.util.List;

public final class Barium extends JavaPlugin implements Listener {
    private static Barium instance;

    @Override
    public void onEnable() {
        instance = this;  // Set the static instance reference
        getLogger().info("Plugin enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        BlockInteractions blockInteractions = new BlockInteractions(this);
        getServer().getPluginManager().registerEvents(blockInteractions, this);

        Movement movement = new Movement(this);
        getServer().getPluginManager().registerEvents(movement, this);

        getServer().getPluginManager().registerEvents(new Inventory(this), this);

        EntityInteraction entityInteraction = new EntityInteraction(this);
        getServer().getPluginManager().registerEvents(entityInteraction, this);

        EntityCombat entityCombat = new EntityCombat(this);
        getServer().getPluginManager().registerEvents(entityCombat, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("[Barium] Plugin disabled!");
        instance = null;
    }

    public static RayTraceResult rayTrace(String type, Player player, int maxDistance) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        return switch (type) {
            case "Block" -> player.getWorld().rayTraceBlocks(
                    eyeLocation,
                    direction,
                    maxDistance,
                    FluidCollisionMode.NEVER,
                    false
            );
            default -> null;
        };
    }

    public static RayTraceResult rayTrace(String type, Player player, int maxDistance, Entity targetEntity) {
        if (!type.equals("Entity")) {
            return null; // Ensure only "Entity" type is handled here
        }
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        return player.getWorld().rayTraceEntities(
                eyeLocation,
                direction,
                maxDistance,
                0, // This can be adjusted for hitbox size if necessary
                entity -> entity.equals(targetEntity) // Predicate to check if the entity is the target
        );
    }

    public static void runAction(Player player, Event event, String action, String arg) {
        if (arg.contains("%player%")) {
            arg = arg.replace("%player%", player.getName());
        }

        switch (action) {
            case "sendMessage":
                player.sendMessage(arg); // expects a message after "sendMessage:"
                break;
            case "cancelEvent":
                if (event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true); // no parameter required
                }
                break;
            case "log":
                instance.getLogger().info(arg); // expects a message after "log:"
                break;
            default:
                player.sendMessage("Unknown action: " + action); // handle unknown actions
                break;
        }
    }

    public static void runDetection(Player player, String caller, Event event) {
        if (instance == null) {
            System.err.println("[Barium] Plugin is not fully initialized or has been disabled.");
            return;
        }
        List<String> actions = instance.getConfig().getStringList("actions." + caller);
        for (String actionEntry : actions) {
            String[] parts = actionEntry.split(":", 2);
            if (parts.length == 2) {
                runAction(player, event, parts[0], parts[1]);
            } else if (parts.length == 1) {
                runAction(player, event, parts[0], "");
            } else {
                instance.getLogger().warning("Misconfigured action for caller: " + caller);
            }
        }
    }

    public static Barium getInstance() {
        return instance;
    }
}