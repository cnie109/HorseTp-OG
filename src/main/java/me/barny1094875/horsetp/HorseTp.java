package me.barny1094875.horsetp;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.barny1094875.horsetp.Listeners.OnEntityDismount;
import me.barny1094875.horsetp.Listeners.OnPlayerTeleport;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class HorseTp extends JavaPlugin {

    private static StateFlag HorseTpFlag;
    private static HorseTp plugin;
    private static HashMap<Player, Entity> vehicleCache = new HashMap<>();

    @Override
    public void onLoad(){
        // add the WorldGuard flag for an area where HorseTp will not work
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            StateFlag flag = new StateFlag("horse-can-tp", true);
            registry.register(flag);
            HorseTpFlag = flag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("tp-animals-only");
            if (existing instanceof StateFlag) {
                HorseTpFlag = (StateFlag) existing;
            } else {
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(new OnEntityDismount(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerTeleport(), this);

    }

    public static HorseTp getPlugin(){
        return plugin;
    }

    public static StateFlag getHorseTpFlag(){
        return HorseTpFlag;
    }

    public static HashMap<Player, Entity> getVehicleCahce(){
        return vehicleCache;
    }

}
