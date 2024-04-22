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
// imports for url
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Barium extends JavaPlugin implements Listener {
    private static Barium instance;
    public static boolean cfgDebug;
    private final Map<String, Long> lastWebhookTime = new HashMap<>();
    private final Map<String, Integer> webhookCount = new HashMap<>();

    public static void updateDebugSetting() {
        cfgDebug = instance.getConfig().getBoolean("settings.Debug.enabled");
    }

    @Override
    public void onEnable() {
        instance = this;  // Set the static instance reference

        this.saveDefaultConfig();
        this.reloadConfig();

        updateDebugSetting();

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

        new Commands(this);
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
            case "sendWebhook":
                Webhook.sendWebhook(arg);
            case "sendMessage":
                if (instance.getConfig().getBoolean("settings.SendMessage.toOPs")) {
                    for (Player op : instance.getServer().getOnlinePlayers()) {
                        if (op.isOp()) {
                            op.sendMessage(arg);
                        }
                    }
                }

                if (instance.getConfig().getBoolean("settings.SendMessage.toOffender")) {
                    player.sendMessage(arg);
                }

                if (instance.getConfig().getString("settings.Webhook") != null) {
                    Webhook.sendWebhook(arg);
                }
            case "cancelEvent":
                if (event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true); // no parameter required
                }
                break;
            case "log":
                instance.getLogger().info(arg); // expects a message after "log:"
                break;
            default:
                debug(player, "Unknown action: " + action); // handle unknown actions
                break;
        }
    }

    public static void runDetection(Player player, String caller, Event event) {
        if (instance == null) {
            System.err.println("[Barium] Plugin is not fully initialized or has been disabled.");
            return;
        }

        List<String> actions = instance.getConfig().getStringList("actions." + caller);
        if (instance.getConfig().getBoolean(caller + ".enabled")) {
            for (String actionEntry : actions) {
                String[] parts = actionEntry.split(":", 2);
                debug(player, "Running action: " + actionEntry);
                debug(player, "Parts: " + parts.length);
                debug(player, "Parts[0]: " + parts[0]);
                debug(player, "Parts[1]: " + parts[1]);
                if (parts.length == 2) {
                    runAction(player, event, parts[0], parts[1]);
                } else if (parts.length == 1) {
                    runAction(player, event, parts[0], "");
                } else {
                    instance.getLogger().warning("Misconfigured action for caller: " + caller);
                }
            }
        }
    }

    public static void debug(Player player, String msg) {
        if (cfgDebug) {
            if (instance.getConfig().getBoolean("settings.Debug.toConsole")) {
                instance.getLogger().info("[Barium Debug] " + msg);
            }

            if (instance.getConfig().getBoolean("settings.Debug.SendWebhook")) {
                Webhook.sendWebhook("[Barium Debug] " + msg);
            }
        }
    }

    static class Webhook {
        public static void sendWebhook(String message) {
            String webhookUrl = Objects.requireNonNull(instance.getConfig().getString("settings.Webhook"));
            long currentTime = System.currentTimeMillis();
            Long lastTime = instance.lastWebhookTime.get(webhookUrl);

            if (lastTime != null && (currentTime - lastTime < 10000)) {
                instance.getLogger().info("Webhook not sent due to rate limit.");
                return;
            }

            instance.lastWebhookTime.put(webhookUrl, currentTime);

            try {
                debug(null, "Sending webhook: " + message);
                URL url = new URL(webhookUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                String jsonInputString = "{\"content\": \"" + message + "\"}";

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Webhook sent successfully: " + response);
                }
            } catch (Exception e) {
                instance.getLogger().warning("[Barium] Error sending webhook: " + e.getMessage());
            }
        }
    }

    public static Barium getInstance() {
        return instance;
    }
}