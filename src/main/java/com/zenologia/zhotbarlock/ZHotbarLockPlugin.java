package com.zenologia.zhotbarlock;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ZHotbarLockPlugin extends JavaPlugin {

    private ZHotbarLockConfig hotbarConfig;
    private HotbarManager hotbarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadZHotbarLockConfig();

        this.hotbarManager = new HotbarManager(this, hotbarConfig);

        getServer().getPluginManager().registerEvents(new HotbarListener(this), this);

        ZHotbarLockCommand commandExecutor = new ZHotbarLockCommand(this);
        Objects.requireNonNull(getCommand("zhotbarlock")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("zhotbarlock")).setTabCompleter(commandExecutor);

        getLogger().info("ZHotbarLock enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ZHotbarLock disabled.");
    }

    public void reloadZHotbarLockConfig() {
        reloadConfig();
        FileConfiguration cfg = getConfig();
        this.hotbarConfig = ZHotbarLockConfig.load(this, cfg);
        if (this.hotbarManager != null) {
            this.hotbarManager.setConfig(this.hotbarConfig);
        }
    }

    public HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public ZHotbarLockConfig getHotbarConfig() {
        return hotbarConfig;
    }
}
