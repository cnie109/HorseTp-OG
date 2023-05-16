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

        // if the player did not have a vehicle prior to the teleport
        // ignore this handler
        if(HorseTp.getVehicleCahce().get(event.getPlayer()) == null){
            return;
        }


        // run all of this 1 tick after the teleport so that the players location gets updated
        Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
            // this checks to see if the teleport was a result of a dismount, or a /tp
            if (!event.getCause().equals(TeleportCause.UNKNOWN)) {
                Player player = event.getPlayer();
                // get the vehicle from the vehicle cache
                Entity vehicle = HorseTp.getVehicleCahce().get(player);
                // remove the player and vehicle from the cache
                HorseTp.getVehicleCahce().remove(player);

                // check if the area that the player teleported to is a banned area
                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                com.sk89q.worldedit.util.Location worldGuardPlayerLocation = localPlayer.getLocation();

                RegionQuery query = container.createQuery();
                ApplicableRegionSet set = query.getApplicableRegions(worldGuardPlayerLocation);

                // check if the horse-can-tp flag is set to DENY
                if (!set.testState(localPlayer, HorseTp.getHorseTpFlag())) {
                    // only stop the tp if the player is in a minecart of boat
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
                List<Entity> passengerList = vehicle.getPassengers();
                // remove the player from the passenger list
                passengerList.remove(player);
                // eject all of the passengers
                vehicle.eject();

                // load the chunk that the vehicle is in
                World vehicleWorld = vehicle.getWorld();
                Chunk vehicleChunk = vehicle.getChunk();
                vehicleWorld.loadChunk(vehicleChunk);

                // wait 1 tick before teleporting the entity
                Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                    // teleport the vehicle and all of the passengers
                    vehicle.teleport(player.getLocation());
                    passengerList.forEach(entity -> {
                        entity.teleport(player.getLocation());
                        vehicle.addPassenger(entity);
                    });

                    // unload the chunk that the vehicle was in one tick later
                    // also kick the player from the vehicle
                    Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                        vehicleWorld.unloadChunk(vehicleChunk);
                        // eject and re-add all of the passengers so that the client
                        // realizes that the vehicle is there

                        // add a llama spit to the vehicle for 1 tick so that they client can see that
                        // the vehicle
                        // a llama spit is used since it is tiny
                        // it also causes a little effect on the vehicle
                        // spawn the spit at playerX, 10000, playerZ
                        Location spitLocation = player.getLocation();
                        spitLocation.setY(10000);
                        Entity spit = playerWorld.spawnEntity(player.getLocation(), EntityType.LLAMA_SPIT);
                        // add the spit to the vehicle
                        try { // just in case another plugin got rid of the vehicle to prevent an exception
                            vehicle.addPassenger(spit);
                        } catch(Exception e){};
                        // wait 1 tick and then kill the spit
                        Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                            spit.remove();
                        }, 1);
                    }, 1);
                }, 1);
            }
        }, 1);

    }

}
