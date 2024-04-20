package org.its3rr0rswrld.barium.Classes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.its3rr0rswrld.barium.Barium;

import java.util.Objects;

public class Commands implements CommandExecutor {

    private final Barium plugin;

    public Commands(Barium plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("bdebug")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bdebug")) {
            if (!sender.hasPermission("barium.debug")) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                /* Toggle the debug mode */
                boolean currentDebug = plugin.getConfig().getBoolean("settings.debug", false);
                plugin.getConfig().set("settings.debug", !currentDebug);
                plugin.saveConfig();
                plugin.reloadConfig();
                Barium.updateDebugSetting();
                sender.sendMessage("Debug mode toggled to: " + !currentDebug);
            } else {
                sender.sendMessage("Current debug mode: " + Barium.cfgDebug);
            }
            return true;
        }
        return false;
    }
}
