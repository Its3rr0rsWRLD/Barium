package org.its3rr0rswrld.barium.Classes;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.its3rr0rswrld.barium.Barium;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class BlockInteractions implements Listener {
    private final double breakThreshold = 0.2;
    private Barium barium;
    private final double maxReachBuffer, LoSBuffer, LoSBufferTime;

    public BlockInteractions(Barium barium) {
        this.barium = barium;
        maxReachBuffer = barium.getConfig().getDouble("Reach.maxBuffer", 1.1);
        LoSBuffer = barium.getConfig().getDouble("BlockInteractions.LoSBuffer", 1);
        LoSBufferTime = barium.getConfig().getDouble("BlockInteractions.LoSBufferTime", 250);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        RayTraceResult result = Barium.rayTrace("Block", player, 15);
        HashMap<UUID, HashMap<Long, Location>> playerLocations = Movement.playerLocations;

        /* TODO: Check if the block has break time faster than the buffer, if so, ignore */ 
        if (result != null && !Objects.equals(result.getHitBlock(), block)) {
            if (result.getHitBlock() != null) {
                HashMap<Long, Location> locationHistory = playerLocations.get(player.getUniqueId());

                if (locationHistory != null) {
                    long desiredTimestamp = (long) (System.currentTimeMillis() - LoSBufferTime);
                    Location pastLocation = null;
                    long closestTimeDifference = Long.MAX_VALUE;

                    for (HashMap.Entry<Long, Location> entry : locationHistory.entrySet()) {
                        long timeDifference = Math.abs(desiredTimestamp - entry.getKey());

                        if (timeDifference < closestTimeDifference) {
                            closestTimeDifference = timeDifference;
                            pastLocation = entry.getValue();
                        }
                    }

                    assert pastLocation != null;

                    double distance = pastLocation.distance(block.getLocation());

                    double dPLX = Math.round(pastLocation.getX() * 100.0) / 100.0,
                            dPLY = Math.round(pastLocation.getY() * 100.0) / 100.0,
                            dPLZ = Math.round(pastLocation.getZ() * 100.0) / 100.0;
                    Barium.debug(player, String.format("Past Location: X: %s, Y: %s, Z: %s\nDistance: %s", dPLX, dPLY, dPLZ, distance));

                    if (distance > LoSBuffer) {
                        Barium.debug(player, "Calling runDetection from noLoS");
                        Barium.runDetection(player, "noLoS", event);
                    }
                }
            }
        }
    }

    //region Reach check
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            double maxReach = 5 + maxReachBuffer;
            /* 5 has a lot of false positives, 7 is too high */
            double distance = player.getLocation().distance(block.getLocation());
            Barium.debug(player, "Distance from block: " + distance);

            if (distance > maxReach) {
                Barium.debug(player, " Calling runDetection from Reach");
                Barium.runDetection(player, "Reach", event);
            }
        }
    }
    //endregion
}