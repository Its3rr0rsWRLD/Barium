package org.its3rr0rswrld.barium.Classes;

import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.RayTraceResult;
import org.its3rr0rswrld.barium.Barium;
import org.bukkit.event.Listener;

public class EntityInteraction implements Listener {
    public EntityInteraction(Barium barium) {
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        RayTraceResult rayTraceResult = Barium.rayTrace("Entity", player, 5, entity);

        if (rayTraceResult != null && rayTraceResult.getHitEntity() != null && rayTraceResult.getHitEntity().equals(entity)) {
            Barium.runDetection(player, "EntityInteraction", event);
        }
    }
}
