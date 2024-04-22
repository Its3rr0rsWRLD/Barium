package org.its3rr0rswrld.barium.Classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.its3rr0rswrld.barium.Barium;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Movement implements Listener {
    private final HashMap<UUID, Long> lastMoveTime = new HashMap<>();
    public static final HashMap<UUID, HashMap<Long, Location>> playerLocations = new HashMap<>();
    double LoSBufferTime;
    private Barium barium;


    public Movement(Barium plugin) {
        this.barium = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        int blinkMaxDistance = plugin.getConfig().getInt("BlinkMovement.maxDistance", 10);
        LoSBufferTime = barium.getConfig().getDouble("BlockInteractions.LoSBufferTime", 250);
    }

    public boolean isPlayerMoving(Player player) {
        UUID playerId = player.getUniqueId();

        if (lastMoveTime.containsKey(playerId)) {
            long lastMove = lastMoveTime.get(playerId);
            return System.currentTimeMillis() - lastMove < 100;
        } else {
            lastMoveTime.put(playerId, System.currentTimeMillis());
        }
        return false;
    }

    public boolean isPlayerFalling(Player player) {
        return player.getVelocity().getY() < 0;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Location location = player.getLocation();

        HashMap<Long, Location> locationHistory = playerLocations.getOrDefault(player.getUniqueId(), new HashMap<>());
        locationHistory.put(System.currentTimeMillis(), location);
        locationHistory.entrySet().removeIf(entry -> entry.getKey() < System.currentTimeMillis() - 2 * LoSBufferTime);
        playerLocations.put(player.getUniqueId(), locationHistory);

        Location from = event.getFrom();
        Location to = event.getTo();
        assert to != null;

        lastMoveTime.put(playerId, System.currentTimeMillis());
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        Entity entity = event.getVehicle().getPassengers().isEmpty() ? null : event.getVehicle().getPassengers().get(0);
        if (entity instanceof Player player) {
            UUID playerId = player.getUniqueId();
            handleBoatFly(player, event);
        }
    }

    private void handleBoatFly(Player player, VehicleMoveEvent event) {
        if (!Objects.requireNonNull(player.getVehicle()).isEmpty() && player.getVehicle().getType().toString().equals("BOAT")) {
            double yVelocity = player.getVelocity().getY();
            if (yVelocity < 0.5) {
                Bukkit.getScheduler().runTaskLater(this.barium, () -> {
                    if (player.getVelocity().getY() < 0.5 && player.isOnline()) {
                        // If the velocity hasn't changed and player is still online, might be trying to cheat
                        this.barium.getLogger().info(player.getName() + " might be attempting to use boatfly.");
                        // Optionally, you can take further actions such as warning the player or kicking them out.
                    }
                }, 20L); // 20 ticks = 1 second delay
            }
        }
    }
}