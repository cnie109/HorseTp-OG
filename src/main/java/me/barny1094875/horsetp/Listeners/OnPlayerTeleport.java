package me.barny1094875.horsetp.Listeners;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.barny1094875.horsetp.HorseTp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.List;

public class OnPlayerTeleport implements Listener {

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // run all of this 1 tick after the teleport so that the players location gets updated
        // and the dismount event is guaranteed to have been called
        Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {

            // if the player did not have a vehicle prior to the teleport
            // ignore this handler
            if(HorseTp.getVehicleCahce().get(event.getPlayer()) == null){
                return;
            }


            // this checks to see if the teleport was a result of a dismount, or a /tp
            // TeleportCause.UNKNOWN is a dismount
            if (!event.getCause().equals(TeleportCause.UNKNOWN)) {
                Player player = event.getPlayer();
                // get the vehicle from the vehicle cache
                Entity vehicle = HorseTp.getVehicleCahce().get(player);
                // remove the player and vehicle from the cache
                // this is necessary to prevent the WorldGuard teleport
                // from triggering this event again
                HorseTp.getVehicleCahce().remove(player);

                // check if the area that the player teleported to is a banned area
                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                com.sk89q.worldedit.util.Location worldGuardPlayerLocation = localPlayer.getLocation();

                RegionQuery query = container.createQuery();
                ApplicableRegionSet set = query.getApplicableRegions(worldGuardPlayerLocation);

                // check if the horse-can-tp flag is set to DENY
                if (!set.testState(localPlayer, HorseTp.getHorseTpFlag())) {
                    // only stop the tp if the player is in a minecart or boat
                    // Otherwise, a protected area could be flooded with boats and minecarts
                    // that can't be destroyed by non-admins
                    if(vehicle instanceof Boat || vehicle instanceof Minecart) {
                        player.sendMessage(Component.text("[HorseTp]")
                                .color(TextColor.color(0, 255, 0))
                                .append(Component.text(" You can't teleport there")
                                        .color(TextColor.color(255, 0, 0))));
                        event.setCancelled(true);
                        // teleport the player back to the original location
                        player.teleport(event.getFrom());
                        return;
                    }
                }
                World playerWorld = player.getWorld();
                // get a list of all the passengers the vehicle had
                // use try-catch to see if the vehicle still exists
                // as it may have been removed by another plugin
                List<Entity> passengerList = null;
                try {
                    passengerList = vehicle.getPassengers();
                } catch(Exception e){return;}
                // remove the player from the passenger list
                passengerList.remove(player);
                // eject all of the passengers
                vehicle.eject();

                // load the chunk that the vehicle is in
                World vehicleWorld = vehicle.getWorld();
                Chunk vehicleChunk = vehicle.getChunk();
                vehicleWorld.loadChunk(vehicleChunk);

                // wait 1 tick before teleporting the entity
                List<Entity> finalPassengerList = passengerList; // apparently this is required for the compiler to be happy ?
                Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                    // teleport the vehicle and all of the passengers
                    // use try-catch to see if the vehicle still exists
                    try {
                        vehicle.teleport(player.getLocation());
                    } catch(Exception e){return;}

                    finalPassengerList.forEach(entity -> {
                        entity.teleport(player.getLocation());
                        vehicle.addPassenger(entity);
                    });

                    // simply hide and show the vehicle
                    // so that the vehicle is not invisible
                    player.hideEntity(HorseTp.getPlugin(), vehicle);
                    player.showEntity(HorseTp.getPlugin(), vehicle);

                    // add the player back to the vehicle
                    vehicle.addPassenger(player);

                    // unload the chunk that the vehicle was in one tick later
                    // also add the player back to the vehicle
                    Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                        vehicleWorld.unloadChunk(vehicleChunk);
                    }, 1);
                }, 1);
            }
        }, 1);

    }

}
