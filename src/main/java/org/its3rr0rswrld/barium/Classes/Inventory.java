package org.its3rr0rswrld.barium.Classes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.its3rr0rswrld.barium.Barium;

import static org.bukkit.Bukkit.getLogger;

public class Inventory implements Listener {
    private final Movement movement;

    public Inventory(Barium barium) {
        this.movement = new Movement(barium);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (movement.isPlayerMoving(player)) {
            Barium.debug(player, " Calling runDetection from Inventory");
            Barium.runDetection(player, "InventoryMovement", event);
        }
    }
}