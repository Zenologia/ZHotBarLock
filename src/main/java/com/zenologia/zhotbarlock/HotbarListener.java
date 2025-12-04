package com.zenologia.zhotbarlock;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HotbarListener implements Listener {

    private final ZHotbarLockPlugin plugin;

    public HotbarListener(ZHotbarLockPlugin plugin) {
        this.plugin = plugin;
    }

    private HotbarManager manager() {
        return plugin.getHotbarManager();
    }

    private ZHotbarLockConfig config() {
        return plugin.getHotbarConfig();
    }

    private boolean hasBypass(Player player) {
        return player.hasPermission("zhotbarlock.bypass");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;
        if (!config().isCheckOnJoin()) return;
        manager().enforceForPlayer(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;
        if (!config().isCheckOnRespawn()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> manager().enforceForPlayer(player));
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;
        if (!config().isCheckOnWorldChange()) return;
        manager().enforceForPlayer(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (hasBypass(player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        ZHotbarLockItem currentItem = manager().getItemFromStack(current);
        ZHotbarLockItem cursorItem = manager().getItemFromStack(cursor);

        boolean cancel = false;

        if (currentItem != null) {
            cancel |= shouldCancelMovement(event, currentItem);
        }
        if (cursorItem != null) {
            cancel |= shouldCancelMovement(event, cursorItem);
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            ZHotbarLockItem hotbarLocked = manager().getItemFromStack(hotbarItem);
            if (hotbarLocked != null) {
                cancel |= shouldCancelMovement(event, hotbarLocked);
            }
        }

        if (cancel) {
            event.setCancelled(true);
        }
    }

    private boolean shouldCancelMovement(InventoryClickEvent event, ZHotbarLockItem item) {
        Inventory clicked = event.getClickedInventory();
        InventoryType clickedType = clicked != null ? clicked.getType() : null;

        boolean involvesPlayer = clickedType == InventoryType.PLAYER || clickedType == InventoryType.CRAFTING;
        boolean involvesContainer = clickedType != null && clickedType != InventoryType.PLAYER && clickedType != InventoryType.CRAFTING;

        InventoryAction action = event.getAction();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (clickedType == InventoryType.PLAYER || clickedType == InventoryType.CRAFTING) {
                involvesContainer = true;
            } else {
                involvesPlayer = true;
            }
        }

        if (involvesContainer && item.isBlockContainerMove()) {
            return true;
        }
        if (involvesPlayer && item.isLockInventoryMovement()) {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (hasBypass(player)) return;

        ItemStack cursor = event.getOldCursor();
        ZHotbarLockItem item = manager().getItemFromStack(cursor);
        if (item == null) return;

        for (int rawSlot : event.getRawSlots()) {
            Inventory inv = event.getView().getInventory(rawSlot);
            if (inv == null) continue;
            InventoryType type = inv.getType();
            boolean isPlayer = type == InventoryType.PLAYER || type == InventoryType.CRAFTING;
            boolean isContainer = !isPlayer;
            if (isContainer && item.isBlockContainerMove()) {
                event.setCancelled(true);
                return;
            }
            if (isPlayer && item.isLockInventoryMovement()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;

        ItemStack stack = event.getItemDrop().getItemStack();
        ZHotbarLockItem item = manager().getItemFromStack(stack);
        if (item != null && item.isBlockDrop()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;

        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();

        ZHotbarLockItem mainItem = manager().getItemFromStack(main);
        ZHotbarLockItem offItem = manager().getItemFromStack(off);

        if ((mainItem != null && mainItem.isBlockOffhandSwap()) ||
            (offItem != null && offItem.isBlockOffhandSwap())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (hasBypass(player)) return;

        // Suppress drops only for exact selector items (matching our NBT)
        event.getDrops().removeIf(stack -> manager().getItemFromStack(stack) != null);
    }
}
