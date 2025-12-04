package com.zenologia.zhotbarlock;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ZHotbarLockConfig {

    private final Set<String> enabledWorlds;
    private final boolean allGamemodes;
    private final Set<GameMode> allowedGamemodes;
    private final boolean checkOnJoin;
    private final boolean checkOnRespawn;
    private final boolean checkOnWorldChange;
    private final Map<String, ZHotbarLockItem> items;
    private final NamespacedKey itemKey;

    public ZHotbarLockConfig(Set<String> enabledWorlds,
                             boolean allGamemodes,
                             Set<GameMode> allowedGamemodes,
                             boolean checkOnJoin,
                             boolean checkOnRespawn,
                             boolean checkOnWorldChange,
                             Map<String, ZHotbarLockItem> items,
                             NamespacedKey itemKey) {
        this.enabledWorlds = enabledWorlds;
        this.allGamemodes = allGamemodes;
        this.allowedGamemodes = allowedGamemodes;
        this.checkOnJoin = checkOnJoin;
        this.checkOnRespawn = checkOnRespawn;
        this.checkOnWorldChange = checkOnWorldChange;
        this.items = items;
        this.itemKey = itemKey;
    }

    public static ZHotbarLockConfig load(Plugin plugin, FileConfiguration cfg) {
        Set<String> enabledWorlds = new HashSet<>(cfg.getStringList("enabled-worlds"));

        boolean allGamemodes = cfg.getBoolean("all-gamemodes", true);
        Set<GameMode> allowedGamemodes = new HashSet<>();
        for (String gm : cfg.getStringList("allowed-gamemodes")) {
            try {
                allowedGamemodes.add(GameMode.valueOf(gm.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {}
        }

        ConfigurationSection eventsSec = cfg.getConfigurationSection("events");
        boolean checkOnJoin = eventsSec != null && eventsSec.getBoolean("check-on-join", true);
        boolean checkOnRespawn = eventsSec != null && eventsSec.getBoolean("check-on-respawn", true);
        boolean checkOnWorldChange = eventsSec != null && eventsSec.getBoolean("check-on-world-change", true);

        Map<String, ZHotbarLockItem> items = new LinkedHashMap<>();
        ConfigurationSection itemsSec = cfg.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection sec = itemsSec.getConfigurationSection(key);
                if (sec == null) continue;
                ZHotbarLockItem item = ZHotbarLockItem.fromConfig(key, sec);
                if (item != null && item.isEnabled()) {
                    items.put(key, item);
                }
            }
        }

        NamespacedKey itemKey = new NamespacedKey(plugin, "zhotbarlock-id");
        return new ZHotbarLockConfig(enabledWorlds, allGamemodes, allowedGamemodes,
                checkOnJoin, checkOnRespawn, checkOnWorldChange, items, itemKey);
    }

    public boolean isWorldEnabled(World world) {
        if (enabledWorlds.isEmpty()) return true;
        return enabledWorlds.contains(world.getName());
    }

    public boolean isGamemodeAllowed(GameMode gm) {
        if (allGamemodes) return true;
        return allowedGamemodes.contains(gm);
    }

    public boolean isCheckOnJoin() {
        return checkOnJoin;
    }

    public boolean isCheckOnRespawn() {
        return checkOnRespawn;
    }

    public boolean isCheckOnWorldChange() {
        return checkOnWorldChange;
    }

    public Map<String, ZHotbarLockItem> getItems() {
        return items;
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }

    public ZHotbarLockItem getItemByNbtId(String nbtId) {
        for (ZHotbarLockItem item : items.values()) {
            if (item.getNbtId().equals(nbtId)) {
                return item;
            }
        }
        return null;
    }
}
