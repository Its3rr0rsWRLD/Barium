package org.its3rr0rswrld.barium.Classes;

import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.RayTraceResult;
import org.its3rr0rswrld.barium.Barium;

import static org.bukkit.Bukkit.getLogger;

public class EntityCombat implements Listener {
    private Barium barium;
    private int maxDistance;

    public EntityCombat(Barium barium) {
        this.barium = barium;
        this.maxDistance = barium.getConfig().getInt("configurable.EntityCombat.maxDistance", 5);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Entity target = event.getEntity();
            RayTraceResult rayTraceResult = Barium.rayTrace("Entity", player, maxDistance, target);
            // Check if the ray trace hit the entity that was originally hit in the event
            if (rayTraceResult == null || rayTraceResult.getHitEntity() == null || !rayTraceResult.getHitEntity().equals(target)) {
                if (player.getLocation().distance(target.getLocation()) > 1) {
                    getLogger().info("EntityCombat: Ray trace did not confirm a hit on target entity.");
                    Barium.runDetection(player, "EntityCombat", event);
                }
            }
        }
    }
}