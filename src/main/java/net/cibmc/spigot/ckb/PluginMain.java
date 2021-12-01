package net.cibmc.spigot.ckb;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin
{
    ConcurrentHashMap<Player, Boolean> editModeAdminMap = new ConcurrentHashMap<Player, Boolean>();

    @Override
    public void onEnable() {
        this.getCommand("ckbadmin").setExecutor(new CommandExe(this));
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        this.getLogger().info("[CKB] Plugin initialize finished.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("[CKB] Plugin has been disabled.");
    }
}
