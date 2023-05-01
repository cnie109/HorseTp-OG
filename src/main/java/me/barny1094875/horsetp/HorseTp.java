package me.barny1094875.horsetp;

import me.barny1094875.horsetp.Listeners.onEntityDismount;
import org.bukkit.plugin.java.JavaPlugin;

public final class HorseTp extends JavaPlugin {

    private static HorseTp plugin;

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(new onEntityDismount(), this);

    }

    public static HorseTp getPlugin(){
        return plugin;
    }

}
