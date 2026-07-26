package ru.lolodimo4ka.holycustomegg;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ru.lolodimo4ka.holygoldspawner.HolyGoldSpawner;

public class ListenerEgg implements Listener {
   private final HolyCustomEgg plugin;
   private final Map<String, ManagerEgg> eggs;

   public ListenerEgg(HolyCustomEgg plugin, Map<String, ManagerEgg> eggs) {
      this.plugin = plugin;
      this.eggs = eggs;
   }

   private boolean isVanillaSpawnEgg(Material material) {
      return material.name().endsWith("_SPAWN_EGG");
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
         Player player = event.getPlayer();
         ItemStack item = event.getItem();
         Block clickedBlock = event.getClickedBlock();
         if (item != null) {
            if (this.isVanillaSpawnEgg(item.getType()) && !Utils.isCustomEgg(item, this.eggs)) {
               event.setCancelled(true);
               if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
                  try {
                     Plugin goldSpawnerPlugin = this.plugin.getServer().getPluginManager().getPlugin("HolyGoldSpawner");
                     if (goldSpawnerPlugin != null && goldSpawnerPlugin.isEnabled()) {
                        HolyGoldSpawner goldSpawner = (HolyGoldSpawner)goldSpawnerPlugin;
                        if (goldSpawner.getSpawnerManager().containsSpawner(clickedBlock.getLocation())) {
                           return;
                        }
                     }
                  } catch (Exception var11) {
                  }

                  this.plugin.handleVanillaEggOnSpawner(player, clickedBlock, item);
               } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
                  EntityType entityType = this.getEntityTypeFromEggMaterial(item.getType());
                  if (entityType != null) {
                     Location spawnLocation = clickedBlock.getRelative(event.getBlockFace()).getLocation().add(0.5, 0.0, 0.5);

                     try {
                        clickedBlock.getWorld().spawnEntity(spawnLocation, entityType);
                     } catch (Exception var10) {
                     }

                     if (player.getGameMode() != GameMode.CREATIVE) {
                        this.reduceItemAmount(player, item);
                     }
                  }
               }
            } else {
               if (Utils.isCustomEgg(item, this.eggs)) {
                  event.setCancelled(true);
                  ManagerEgg egg = Utils.findEggByItem(item, this.eggs);
                  if (egg == null) {
                     return;
                  }

                  if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
                     try {
                        Plugin goldSpawnerPlugin = this.plugin.getServer().getPluginManager().getPlugin("HolyGoldSpawner");
                        if (goldSpawnerPlugin != null && goldSpawnerPlugin.isEnabled()) {
                           HolyGoldSpawner goldSpawner = (HolyGoldSpawner)goldSpawnerPlugin;
                           if (goldSpawner.getSpawnerManager().containsSpawner(clickedBlock.getLocation())) {
                              return;
                           }
                        }
                     } catch (Exception var12) {
                     }

                     this.handleSpawnerSetup(player, clickedBlock, item, egg);
                     return;
                  }

                  Location spawnLocation;
                  if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                     if (clickedBlock == null) {
                        return;
                     }

                     if (clickedBlock.getType() == Material.DISPENSER) {
                        return;
                     }

                     spawnLocation = clickedBlock.getRelative(event.getBlockFace()).getLocation().add(0.5, 0.0, 0.5);
                  } else {
                     RayTraceResult rayTrace = player.rayTraceBlocks(10.0);
                     if (rayTrace != null && rayTrace.getHitBlock() != null) {
                        Vector hitPosition = rayTrace.getHitPosition();
                        spawnLocation = hitPosition.toLocation(player.getWorld());
                     } else {
                        Location playerLoc = player.getLocation();
                        Vector direction = playerLoc.getDirection().multiply(2);
                        spawnLocation = playerLoc.add(direction).add(0.0, 1.0, 0.0);
                     }
                  }

                  this.spawnMobFromEgg(player, item, egg, spawnLocation);
               }
            }
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      ItemStack item = event.getItemInHand();
      Block block = event.getBlock();
      if (block.getType() == Material.SPAWNER) {
         ManagerEgg egg = Utils.findEggBySpawnerNBT(item, this.eggs, this.plugin);
         if (egg != null) {
            this.handleCustomSpawnerPlace(event, block, egg);
         }
      }
   }

   @EventHandler
   public void onBlockDispense(BlockDispenseEvent event) {
      ItemStack item = event.getItem();
      if (Utils.isCustomEgg(item, this.eggs)) {
         event.setCancelled(true);
      }
   }

   private void handleSpawnerSetup(Player player, Block spawnerBlock, ItemStack eggItem, ManagerEgg egg) {
      CreatureSpawner spawner = (CreatureSpawner)spawnerBlock.getState();
      this.setupCustomSpawner(spawner, egg);
      if (player.getGameMode() != GameMode.CREATIVE) {
         this.reduceItemAmount(player, eggItem);
      }
   }

   private void handleCustomSpawnerPlace(BlockPlaceEvent event, Block block, ManagerEgg egg) {
      CreatureSpawner spawner = (CreatureSpawner)block.getState();
      this.setupCustomSpawner(spawner, egg);
   }

   private void setupCustomSpawner(CreatureSpawner spawner, ManagerEgg egg) {
      Location location = spawner.getLocation();
      spawner.setSpawnedType(egg.getRandomMob());
      spawner.update(false);
      HolyCustomEgg.ActiveSpawner activeSpawner = this.plugin.new ActiveSpawner(location, egg, System.currentTimeMillis());
      this.plugin.getActiveSpawners().put(location, activeSpawner);
      this.plugin.getDatabase().saveActiveSpawner(location, egg.getId(), System.currentTimeMillis());
   }

   private void spawnMobFromEgg(Player player, ItemStack item, ManagerEgg egg, Location location) {
      if (egg != null) {
         ManagerEgg.MobChance mobChance = egg.getRandomMobChance();
         EntityType entityType = mobChance.getEntityType();
         if (entityType == null || !entityType.isSpawnable() || !entityType.isAlive()) {
            entityType = EntityType.ZOMBIE;
         }

         try {
            if (mobChance.isBabyZombie() && entityType == EntityType.ZOMBIE) {
               Zombie zombie = (Zombie)location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
               zombie.setBaby(true);
            } else {
               location.getWorld().spawnEntity(location, entityType);
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
               this.reduceItemAmount(player, item);
            }
         } catch (Exception var8) {
            location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            if (player.getGameMode() != GameMode.CREATIVE) {
               this.reduceItemAmount(player, item);
            }
         }
      }
   }

   private void reduceItemAmount(Player player, ItemStack item) {
      if (item.getAmount() > 1) {
         item.setAmount(item.getAmount() - 1);
      } else {
         player.getInventory().removeItem(new ItemStack[]{item});
      }
   }

   private EntityType getEntityTypeFromEggMaterial(Material material) {
      String name = material.name();
      if (!name.endsWith("_SPAWN_EGG")) {
         return null;
      } else {
         String entityName = name.substring(0, name.length() - "_SPAWN_EGG".length());
         if (entityName.equals("MOOSHROOM")) {
            return EntityType.MOOSHROOM;
         } else {
            try {
               return EntityType.valueOf(entityName);
            } catch (IllegalArgumentException var5) {
               return null;
            }
         }
      }
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent event) {
      Chunk loadedChunk = event.getChunk();
      Map<Location, HolyCustomEgg.ActiveSpawner> activeSpawners = this.plugin.getActiveSpawners();
      this.plugin
         .getServer()
         .getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               for (Entry<Location, HolyCustomEgg.ActiveSpawner> entry : activeSpawners.entrySet()) {
                  Location location = entry.getKey();
                  if (location.getWorld() == loadedChunk.getWorld()
                     && location.getBlockX() >> 4 == loadedChunk.getX()
                     && location.getBlockZ() >> 4 == loadedChunk.getZ()) {
                     this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        Block block = location.getBlock();
                        if (block.getType() == Material.SPAWNER) {
                           HolyCustomEgg.ActiveSpawner spawner = entry.getValue();
                           CreatureSpawner creatureSpawner = (CreatureSpawner)block.getState();
                           creatureSpawner.setSpawnedType(spawner.getEgg().getRandomMob());
                           creatureSpawner.update(false);
                        }
                     });
                  }
               }
            }
         );
   }
}
