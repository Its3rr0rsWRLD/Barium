package org.its3rr0rswrld.barium.Classes;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
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
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class BlockInteractions implements Listener {
    private final HashMap<UUID, Long> lastBreakTimes = new HashMap<>();
    private final double breakThreshold = 0.2; // Adjust as needed, in seconds
    private Barium barium;

    public BlockInteractions(Barium barium) {
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.AIR) return;

        //#region LOS check
        RayTraceResult result = Barium.rayTrace("Block", player, 6);
        if (result != null && result.getHitBlock() != null && !result.getHitBlock().equals(block)) {
            getLogger().info("[Barium] Calling runDetection from noLOS");
            Barium.runDetection(player, "noLOS", event);
        }
        //#endregion

        //#region Fast Break check
        long lastBreakTime = lastBreakTimes.getOrDefault(player.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis();

        double timeDifference = (currentTime - lastBreakTime) / 1000.0;
    }

    //#region Reach check
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            double maxReach = 6;
            double distance = player.getLocation().distance(block.getLocation());

            if (distance > maxReach) {
                getLogger().info("[Barium] Calling runDetection from Reach");
                Barium.runDetection(player, "Reach", event);
            }
        }
    }
    //#endregion
}