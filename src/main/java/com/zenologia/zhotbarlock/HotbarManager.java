package com.zenologia.zhotbarlock;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class HotbarManager {

    private final ZHotbarLockPlugin plugin;
    private ZHotbarLockConfig config;

    public HotbarManager(ZHotbarLockPlugin plugin, ZHotbarLockConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void setConfig(ZHotbarLockConfig config) {
        this.config = config;
    }

    public void enforceForPlayer(Player player) {
        if (!config.isWorldEnabled(player.getWorld())) return;
        if (!config.isGamemodeAllowed(player.getGameMode())) return;

        PlayerInventory inv = player.getInventory();
        for (Map.Entry<String, ZHotbarLockItem> entry : config.getItems().entrySet()) {
            ZHotbarLockItem item = entry.getValue();
            ensureItemPresent(player, inv, item);
        }
    }

    public boolean enforceSingleItemForPlayer(Player player, String itemId) {
        ZHotbarLockItem item = config.getItems().get(itemId);
        if (item == null || !item.isEnabled()) {
            return false;
        }
        if (!config.isWorldEnabled(player.getWorld())) return true; // silently succeed logically
        if (!config.isGamemodeAllowed(player.getGameMode())) return true;
        ensureItemPresent(player, player.getInventory(), item);
        return true;
    }

    private void ensureItemPresent(Player player, PlayerInventory inv, ZHotbarLockItem item) {
        if (hasItem(inv, item.getNbtId())) {
            return;
        }

        ItemStack stack = item.createStack(config);
        boolean placed = false;

        // 1. Force offhand (primary choice)
        if (item.isForceOffhand()) {
            ItemStack off = inv.getItemInOffHand();
            if (off == null || off.getType().isAir()) {
                inv.setItemInOffHand(stack);
                placed = true;
            }
        }

        // 2. Preferred slot (only if empty)
        if (!placed) {
            int preferred = item.getPreferredSlot();
            if (preferred >= 0 && preferred <= 8) {
                ItemStack existing = inv.getItem(preferred);
                if (existing == null || existing.getType().isAir()) {
                    inv.setItem(preferred, stack);
                    placed = true;
                }
            }
        }

        // 3. Scan other hotbar slots if configured
        if (!placed && item.isUseNextFreeHotbarSlot()) {
            int preferred = item.getPreferredSlot();
            for (int slot = 0; slot <= 8; slot++) {
                if (slot == preferred) continue;
                ItemStack existing = inv.getItem(slot);
                if (existing == null || existing.getType().isAir()) {
                    inv.setItem(slot, stack);
                    placed = true;
                    break;
                }
            }
        }

        // 4. Offhand fallback (only if not force-offhand)
        if (!placed && item.isAllowOffhandFallback() && !item.isForceOffhand()) {
            ItemStack offHand = inv.getItemInOffHand();
            if (offHand == null || offHand.getType().isAir()) {
                inv.setItemInOffHand(stack);
                placed = true;
            }
        }

        // 5. Log failure if still not placed
        if (!placed && item.isLogOnPlacementFailure()) {
            plugin.getLogger().warning("Failed to place selector item '" + item.getNbtId() + "' for player " + player.getName());
        }
    }

    private boolean hasItem(PlayerInventory inv, String nbtId) {
        NamespacedKey key = config.getItemKey();
        for (ItemStack stack : inv.getContents()) {
            if (stack == null || stack.getType().isAir()) continue;
            if (!stack.hasItemMeta()) continue;
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            String val = pdc.get(key, PersistentDataType.STRING);
            if (nbtId.equals(val)) {
                return true;
            }
        }
        ItemStack off = inv.getItemInOffHand();
        if (off != null && off.hasItemMeta()) {
            String val = off.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (nbtId.equals(val)) {
                return true;
            }
        }
        return false;
    }

    public ZHotbarLockItem getItemFromStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return null;
        if (!stack.hasItemMeta()) return null;
        NamespacedKey key = config.getItemKey();
        String value = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (value == null) return null;
        return config.getItemByNbtId(value);
    }

    public boolean isAnyLockedItem(ItemStack stack) {
        return getItemFromStack(stack) != null;
    }

    public void clearSelectorItems(Player player) {
        PlayerInventory inv = player.getInventory();
        NamespacedKey key = config.getItemKey();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.getType().isAir()) continue;
            if (!stack.hasItemMeta()) continue;
            String val = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (val != null && config.getItemByNbtId(val) != null) {
                inv.setItem(i, null);
            }
        }
        ItemStack off = inv.getItemInOffHand();
        if (off != null && off.hasItemMeta()) {
            String val = off.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (val != null && config.getItemByNbtId(val) != null) {
                inv.setItemInOffHand(null);
            }
        }
    }

    public ZHotbarLockConfig getConfig() {
        return config;
    }

    public ZHotbarLockPlugin getPlugin() {
        return plugin;
    }
}
