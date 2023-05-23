package me.barny1094875.horsetp.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

import me.barny1094875.horsetp.HorseTp;

public class OnEntityDismount implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    // when a player teleports, they are dismounted from their vehicle
    // so when they get dismounted, teleport the vehicle to them
    public void onEntityDismount(EntityDismountEvent event){
        if(event.getEntity() instanceof Player){

            // When a player dismounts, add them and the vehicle
            // to a cache, which is read on a teleport event to tp them
            Player player = (Player) event.getEntity();

            Entity vehicle = event.getDismounted();
            HorseTp.getVehicleCahce().put(player, vehicle);

            // 5 ticks later, remove them from the cache
            Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                HorseTp.getVehicleCahce().remove(player);
            }, 5);
        }
    }

}
