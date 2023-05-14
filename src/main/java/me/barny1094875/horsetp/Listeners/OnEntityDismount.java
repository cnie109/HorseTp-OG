package me.barny1094875.horsetp.Listeners;

import java.util.List;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.WorldGuard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

import me.barny1094875.horsetp.HorseTp;

public class OnEntityDismount implements Listener {

    @EventHandler
    // when a player teleports, they are dismounted from their vehicle
    // so when they get dismounted, teleport the vehicle to them
    public void onEntityDismount(EntityDismountEvent event){
        if(event.getEntity() instanceof Player){

            // When a player dismounts, add them and the vehicle
            // to a cache, which is read on a teleport event to tp them
            Player player = (Player) event.getEntity();
            Entity vehicle = event.getDismounted();
            HorseTp.getVehicleCahce().put(player, vehicle);

            // 3 ticks later, remove them from the cache
            Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                HorseTp.getVehicleCahce().remove(player);
            }, 3);
        }
    }

}
