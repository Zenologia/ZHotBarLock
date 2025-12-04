package com.zenologia.zhotbarlock;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ZHotbarLockCommand implements CommandExecutor, TabCompleter {

    private final ZHotbarLockPlugin plugin;

    public ZHotbarLockCommand(ZHotbarLockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("zhotbarlock.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload":
                plugin.reloadZHotbarLockConfig();
                sender.sendMessage("§aZHotbarLock configuration reloaded.");
                return true;

            case "clear":
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /" + label + " clear <player>");
                    return true;
                }
                return handleClear(sender, args[1]);

            case "reissue":
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /" + label + " reissue <player> [item-id]");
                    return true;
                }
                String playerName = args[1];
                String itemId = args.length >= 3 ? args[2] : null;
                return handleReissue(sender, playerName, itemId, label);

            default:
                sendUsage(sender, label);
                return true;
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("§eUsage:");
        sender.sendMessage("§e/" + label + " reload");
        sender.sendMessage("§e/" + label + " clear <player>");
        sender.sendMessage("§e/" + label + " reissue <player> [item-id]");
    }

    private boolean handleClear(CommandSender sender, String playerName) {
        Player targetClear = Bukkit.getPlayerExact(playerName);
        if (targetClear == null) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return true;
        }
        plugin.getHotbarManager().clearSelectorItems(targetClear);
        sender.sendMessage("§aCleared selector items for " + targetClear.getName() + ".");
        return true;
    }

    private boolean handleReissue(CommandSender sender, String playerName, String itemId, String label) {
        Player targetReissue = Bukkit.getPlayerExact(playerName);
        if (targetReissue == null) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return true;
        }

        if (itemId == null) {
            // Reissue all items (backwards compatible behavior)
            plugin.getHotbarManager().enforceForPlayer(targetReissue);
            sender.sendMessage("§aReissued all selector items for " + targetReissue.getName() + ".");
            return true;
        }

        // Reissue a single item by identifier
        Map<String, ZHotbarLockItem> items = plugin.getHotbarConfig().getItems();
        if (!items.containsKey(itemId)) {
            sender.sendMessage("§cUnknown item identifier: §f" + itemId);
            if (!items.isEmpty()) {
                sender.sendMessage("§eAvailable item identifiers:");
                for (String key : items.keySet()) {
                    sender.sendMessage("§7 - §f" + key);
                }
            }
            return true;
        }

        boolean ok = plugin.getHotbarManager().enforceSingleItemForPlayer(targetReissue, itemId);
        if (!ok) {
            sender.sendMessage("§cFailed to reissue item '" + itemId + "' for " + targetReissue.getName() + ".");
        } else {
            sender.sendMessage("§aReissued item '" + itemId + "' for " + targetReissue.getName() + ".");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> results = new ArrayList<>();
        if (!sender.hasPermission("zhotbarlock.admin")) {
            return results;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if ("reload".startsWith(prefix)) results.add("reload");
            if ("clear".startsWith(prefix)) results.add("clear");
            if ("reissue".startsWith(prefix)) results.add("reissue");
            return results;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("clear") || sub.equals("reissue")) {
                String prefix = args[1].toLowerCase(Locale.ROOT);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        results.add(p.getName());
                    }
                }
            }
            return results;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("reissue")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            Map<String, ZHotbarLockItem> items = plugin.getHotbarConfig().getItems();
            for (String key : items.keySet()) {
                if (key.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    results.add(key);
                }
            }
            return results;
        }

        return results;
    }
}
