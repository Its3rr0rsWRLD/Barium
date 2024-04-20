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
import java.util.Objects;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class BlockInteractions implements Listener {
    private final double breakThreshold = 0.2;
    private Barium barium;

    public BlockInteractions(Barium barium) {
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        /* This is the block that was being broken */

        //region LoS check
        RayTraceResult result = Barium.rayTrace("Block", player, 10);
        if (result != null && !Objects.equals(result.getHitBlock(), block)) {
            /* getHitBlock is NOT the block that was being broken */
            Barium.debug(player, "Calling runDetection from noLoS");
            Barium.runDetection(player, "noLoS", event);
        } else if (result == null && block.getType() != Material.AIR) {
            /*
               result returns null if it doesn't hit a block (air)
               if the block broken is not air (never should be), then
               they are (probably) cheating
            */
            Barium.debug(player, "Calling (potential) runDetection from noLoS [RayHitBlock=false]");
            Barium.runDetection(player, "noLoS", event);
        }

        //endregion
    }

    //region Reach check
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            double maxReach = 5.75; /* 5 has a lot of false positives, 6 is too high */
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