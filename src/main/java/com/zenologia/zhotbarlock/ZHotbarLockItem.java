package com.zenologia.zhotbarlock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ZHotbarLockItem {

    private final String id;
    private final boolean enabled;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final boolean glow;
    private final String nbtId;
    private final int preferredSlot;
    private final boolean useNextFreeHotbarSlot;
    private final boolean allowOffhandFallback;
    private final boolean forceOffhand;
    private final boolean logOnPlacementFailure;
    private final boolean lockInventoryMovement;
    private final boolean blockContainerMove;
    private final boolean blockDrop;
    private final boolean blockOffhandSwap;

    public ZHotbarLockItem(String id,
                           boolean enabled,
                           Material material,
                           String name,
                           List<String> lore,
                           boolean glow,
                           String nbtId,
                           int preferredSlot,
                           boolean useNextFreeHotbarSlot,
                           boolean allowOffhandFallback,
                           boolean forceOffhand,
                           boolean logOnPlacementFailure,
                           boolean lockInventoryMovement,
                           boolean blockContainerMove,
                           boolean blockDrop,
                           boolean blockOffhandSwap) {
        this.id = id;
        this.enabled = enabled;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.glow = glow;
        this.nbtId = nbtId;
        this.preferredSlot = preferredSlot;
        this.useNextFreeHotbarSlot = useNextFreeHotbarSlot;
        this.allowOffhandFallback = allowOffhandFallback;
        this.forceOffhand = forceOffhand;
        this.logOnPlacementFailure = logOnPlacementFailure;
        this.lockInventoryMovement = lockInventoryMovement;
        this.blockContainerMove = blockContainerMove;
        this.blockDrop = blockDrop;
        this.blockOffhandSwap = blockOffhandSwap;
    }

    public static ZHotbarLockItem fromConfig(String id, ConfigurationSection sec) {
        boolean enabled = sec.getBoolean("enabled", true);
        String matName = sec.getString("material", "BARRIER");
        Material material = Material.matchMaterial(matName);
        if (material == null) {
            return null;
        }
        String name = sec.getString("name", "");
        List<String> lore = sec.getStringList("lore");
        boolean glow = sec.getBoolean("glow", false);
        String nbtId = sec.getString("nbt-id", id);
        int preferredSlot = sec.getInt("preferred-slot", 0);
        boolean useNextFree = sec.getBoolean("use-next-free-hotbar-slot", true);
        boolean allowOffhandFallback = sec.getBoolean("allow-offhand-fallback", true);
        boolean forceOffhand = sec.getBoolean("force-offhand", false);
        boolean logOnPlacementFailure = sec.getBoolean("log-on-placement-failure", true);
        boolean lockInventoryMovement = sec.getBoolean("lock-inventory-movement", true);
        boolean blockContainerMove = sec.getBoolean("block-container-move", true);
        boolean blockDrop = sec.getBoolean("block-drop", true);
        boolean blockOffhandSwap = sec.getBoolean("block-offhand-swap", true);

        return new ZHotbarLockItem(
                id, enabled, material, name, lore,
                glow, nbtId,
                preferredSlot, useNextFree, allowOffhandFallback, forceOffhand,
                logOnPlacementFailure, lockInventoryMovement,
                blockContainerMove, blockDrop, blockOffhandSwap
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPreferredSlot() {
        return preferredSlot;
    }

    public boolean isUseNextFreeHotbarSlot() {
        return useNextFreeHotbarSlot;
    }

    public boolean isAllowOffhandFallback() {
        return allowOffhandFallback;
    }

    public boolean isForceOffhand() {
        return forceOffhand;
    }

    public boolean isLogOnPlacementFailure() {
        return logOnPlacementFailure;
    }

    public boolean isLockInventoryMovement() {
        return lockInventoryMovement;
    }

    public boolean isBlockContainerMove() {
        return blockContainerMove;
    }

    public boolean isBlockDrop() {
        return blockDrop;
    }

    public boolean isBlockOffhandSwap() {
        return blockOffhandSwap;
    }

    public String getNbtId() {
        return nbtId;
    }

    public ItemStack createStack(ZHotbarLockConfig config) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(color(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(color(line));
                }
                meta.setLore(coloredLore);
            }
            if (glow) {
                // Use any valid enchant as fake glow; type does not matter
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.getPersistentDataContainer().set(config.getItemKey(), PersistentDataType.STRING, nbtId);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
