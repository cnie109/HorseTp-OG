package me.barny1094875.horsetp.Listeners;

import me.barny1094875.horsetp.HorseTp;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.List;

public class onEntityDismount implements Listener {

    @EventHandler
    // when a player teleports, they are dismounted from their vehicle
    // so when they get dismounted, teleport the vehicle to them
    public void onEntityDismount(EntityDismountEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            World playerWorld = player.getWorld();
            Entity vehicle = event.getDismounted();
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
//                    vehicle.eject();
//                    Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
//                        passengerList.forEach(entity -> {
//                            entity.teleport(player.getLocation());
//                            vehicle.addPassenger(entity);
//                        });
//                    }, 1);

                    // add a cat to the vehicle for 1 tick so that they client can see that
                    // the vehicle
                    // there's no particular reason for it being a cat, I just wanted it to be
                    // spawn the cat at playerX, 10000, playerZ
                    Location catLocation = player.getLocation();
                    catLocation.setY(10000);
                    Cat cat = (Cat) playerWorld.spawnEntity(player.getLocation(), EntityType.CAT);
                    // add the cat to the vehicle
                    vehicle.addPassenger(cat);
                    // wait 1 tick and then kill the cat
                    Bukkit.getScheduler().runTaskLater(HorseTp.getPlugin(), () -> {
                        cat.remove();
                    }, 1);
                }, 1);
            }, 1);
        }
    }

}
