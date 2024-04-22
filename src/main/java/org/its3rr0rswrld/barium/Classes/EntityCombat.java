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
    private final int maxReachDistance;
    private final int maxCombatDistance;

    public EntityCombat(Barium barium) {
        this.barium = barium;
        this.maxReachDistance = barium.getConfig().getInt("EntityReach.maxDistance", 5);
        this.maxCombatDistance = barium.getConfig().getInt("EntityCombat.maxDistance", 5);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Entity target = event.getEntity();

            Barium.debug(player, "EntityCombat: Player " + player.getName() + " hit entity " + target.getName() + "\n" +
                    "Distance: " + player.getLocation().distance(target.getLocation()));

            //region Reach
            if (player.getLocation().distance(target.getLocation()) > maxReachDistance) {
                Barium.debug(player, player.getName() + " hit entity " + target.getName() + " from too far away.");
                Barium.runDetection(player, "EntityReach", event);
            }
            //endregion

            //region No Line of Sight
            RayTraceResult rayTraceResult = Barium.rayTrace("Entity", player, maxCombatDistance, target);
            if (rayTraceResult == null || rayTraceResult.getHitEntity() == null || !rayTraceResult.getHitEntity().equals(target)) {
                if (player.getLocation().distance(target.getLocation()) > 1) {
                    Barium.debug(player, "EntityCombat: Ray trace did not confirm a hit on target entity.");
                    Barium.runDetection(player, "EntityCombat", event);
                }
            }
            //endregion
        }
    }
}