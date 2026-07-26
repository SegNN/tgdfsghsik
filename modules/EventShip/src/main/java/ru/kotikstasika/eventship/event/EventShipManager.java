package ru.kotikstasika.eventship.event;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.anchor.RespawnAnchorManager;
import ru.kotikstasika.eventship.barrel.BarrelManager;
import ru.kotikstasika.eventship.enums.EventRarity;
import ru.kotikstasika.eventship.hologram.HologramManager;
import ru.kotikstasika.eventship.loot.LootManager;
import ru.kotikstasika.eventship.npc.CaptainNPC;
import ru.kotikstasika.eventship.service.LocationService;
import ru.kotikstasika.eventship.service.RegionBarrierCleaner;
import ru.kotikstasika.eventship.service.SchematicService;
import ru.kotikstasika.eventship.service.WorldGuardService;

public class EventShipManager {
   private final EventShip plugin;
   private final HologramManager hologramManager;
   private final LootManager lootManager;
   private final BarrelManager barrelManager;
   private final RespawnAnchorManager anchorManager;
   private final LocationService locationService;
   private final SchematicService schematicService;
   private final WorldGuardService worldGuardService;
   private final RegionBarrierCleaner barrierCleaner;
   private final Random random = new Random();
   private Location shipLocation;
   private CaptainNPC captain;
   private EventRarity rarity;
   private boolean isActive = false;
   private boolean isSearchingLocation = false;
   private BukkitTask eventTimerTask;
   private BukkitTask keySpawnTask;

   public EventShipManager(EventShip plugin) {
      this.plugin = plugin;
      this.hologramManager = new HologramManager(plugin);
      this.lootManager = new LootManager(plugin);
      this.barrelManager = new BarrelManager(plugin, this.hologramManager, this.lootManager);
      this.anchorManager = new RespawnAnchorManager(plugin, this.hologramManager, this.lootManager);
      this.locationService = new LocationService(plugin);
      this.schematicService = new SchematicService();
      this.worldGuardService = new WorldGuardService();
      this.barrierCleaner = new RegionBarrierCleaner(plugin);
   }

   public boolean spawnEvent(Location location) {
      if (this.isActive) {
         this.removeEvent();
      }

      this.isActive = false;
      this.shipLocation = location;
      if (this.rarity == null) {
         this.rarity = this.getRandomRarity();
      }

      boolean schematicSuccess = this.spawnShipSchematic(location);
      if (!schematicSuccess) {
         this.plugin.getLogger().warning("Не удалось загрузить схему корабля!");
         return false;
      } else {
         CompletableFuture.runAsync(() -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.completeEventSpawn(location)));
         return true;
      }
   }

   private void completeEventSpawn(Location location) {
      CompletableFuture.runAsync(() -> this.worldGuardService.createRegion("eventship_ship", location, 50));
      CompletableFuture.runAsync(() -> this.spawnCaptainAsync(location));
      CompletableFuture.runAsync(() -> this.spawnBarrelsAsync(location));
      this.scanAnchorsForHolograms(location, this.plugin.getConfig().getInt("settings.barrelSearchRadius", 30));
      this.startAsyncEventTimer();
      this.isActive = true;
      CompletableFuture.runAsync(() -> {
         this.broadcastSpawnMessageAsync();
         this.playSpawnSoundAsync(location);
      });
      EventManager.getInstance()
         .events
         .put(
            "ship",
            new Event(
               "ship",
               "&x&A&0&5&2&0&0▶ &x&A&0&5&2&0&0Т&x&A&A&5&7&0&0а&x&B&4&5&C&0&0и&x&B&E&6&1&0&0н&x&C&8&6&6&0&0с&x&D&3&6&C&0&0т&x&D&D&7&1&0&0в&x&E&7&7&6&0&0е&x&F&1&7&B&0&0н&x&F&B&8&0&0&0н&x&F&1&7&B&0&0ы&x&E&7&7&6&0&0й &x&D&D&7&1&0&0к&x&D&3&6&C&0&0о&x&C&8&6&6&0&0р&x&B&E&6&1&0&0а&x&B&4&5&C&0&0б&x&A&A&5&7&0&0л&x&A&0&5&2&0&0ь&f находится на координатах &x&A&0&5&2&0&0"
                  + this.shipLocation.getBlockX()
                  + " "
                  + this.shipLocation.getBlockY()
                  + " "
                  + this.shipLocation.getBlockZ()
                  + "&f."
            )
         );
   }

   private void scanAnchorsForHolograms(Location center, int radius) {
      if (center != null && center.getWorld() != null && radius > 0) {
         final World world = center.getWorld();
         final int minX = center.getBlockX() - radius;
         final int maxX = center.getBlockX() + radius;
         final int minZ = center.getBlockZ() - radius;
         final int maxZ = center.getBlockZ() + radius;
         final int worldMinY = Math.max(0, center.getBlockY() - radius);
         final int worldMaxY = Math.min(world.getMaxHeight() - 1, center.getBlockY() + radius);
         int batchPerTick = 1200;
         (new BukkitRunnable() {
            int x = minX;
            int y = worldMinY;
            int z = minZ;

            public void run() {
               int processed = 0;

               while (this.x <= maxX && processed < 1200) {
                  while (this.z <= maxZ && processed < 1200) {
                     Chunk chunk = world.getChunkAt(this.x >> 4, this.z >> 4);
                     if (!chunk.isLoaded()) {
                        chunk.load(true);
                     }

                     while (this.y <= worldMaxY && processed < 1200) {
                        Block block = world.getBlockAt(this.x, this.y, this.z);
                        if (block.getType() == Material.RESPAWN_ANCHOR) {
                           EventShipManager.this.anchorManager.initializeAnchor(block);
                        }

                        processed++;
                        this.y++;
                     }

                     this.y = worldMinY;
                     this.z++;
                     continue;

                     if (this.z > maxZ) {
                        this.z = minZ;
                        this.x++;
                     }
                  }
                  break;
               }

               if (this.x > maxX) {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 10L, 1L);
      }
   }

   public boolean spawnEventInWater(EventRarity specifiedRarity) {
      if (this.isSearchingLocation) {
         return false;
      } else {
         this.isSearchingLocation = true;
         World world = this.plugin.getServer().getWorld(this.plugin.getConfig().getString("settings.world", "world"));
         if (world == null) {
            this.isSearchingLocation = false;
            return false;
         } else {
            CompletableFuture<Location> locationFuture = this.locationService.findWaterLocationAsync(world, 200);
            locationFuture.thenAcceptAsync(waterLocation -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
               this.isSearchingLocation = false;
               if (waterLocation != null) {
                  if (specifiedRarity != null) {
                     this.rarity = specifiedRarity;
                  } else {
                     this.rarity = this.getRandomRarity();
                  }

                  if (!this.spawnEvent(waterLocation)) {
                     this.plugin.getLogger().warning("Ошибка при создании ивента корабля!");
                     this.isSearchingLocation = false;
                  }
               } else {
                  CompletableFuture<Location> fallbackFuture = CompletableFuture.supplyAsync(() -> this.createWaterPoolAsync(world));
                  fallbackFuture.thenAcceptAsync(fallbackLocation -> {
                     if (fallbackLocation != null) {
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                           if (specifiedRarity != null) {
                              this.rarity = specifiedRarity;
                           } else {
                              this.rarity = this.getRandomRarity();
                           }

                           if (!this.spawnEvent(fallbackLocation)) {
                              this.plugin.getLogger().warning("Ошибка при создании ивента в искусственном водоеме!");
                              this.isSearchingLocation = false;
                           }
                        });
                     }
                  });
               }
            })).exceptionally(throwable -> {
               this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                  this.isSearchingLocation = false;
                  this.plugin.getLogger().severe("Критическая ошибка при поиске локации: " + throwable.getMessage());
               });
               return null;
            });
            return true;
         }
      }
   }

   private boolean spawnShipSchematic(Location location) {
      try {
         String schemName = this.plugin.getConfig().getString("settings.schem", "event_ship.schem");
         File dataFolder = this.plugin.getDataFolder();
         return this.schematicService.loadAndPasteSchematic(dataFolder, schemName, location);
      } catch (Exception var4) {
         this.plugin.getLogger().warning("Ошибка при загрузке схемы корабля: " + var4.getMessage());
         return false;
      }
   }

   private void spawnCaptainAsync(Location location) {
      this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
         Location captainLocation = location.clone().add(0.0, 1.0, 0.0);

         for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
               Location blockLocation = location.clone().add(x, 0.0, z);
               if (blockLocation.getBlock().getType() == Material.SMITHING_TABLE) {
                  captainLocation = blockLocation.clone().add(0.5, 1.0, 0.5);
                  break;
               }
            }
         }

         if (this.captain != null) {
            this.captain.remove();
            this.captain = null;
         }

         this.captain = new CaptainNPC(this.plugin, this.hologramManager, captainLocation);
         this.captain.spawnKey();
      });
   }

   private void spawnBarrelsAsync(Location location) {
      this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
         int radius = this.plugin.getConfig().getInt("settings.barrelSearchRadius");
         CompletableFuture.runAsync(() -> {
            int processedBlocks = 0;
            int maxBlocksPerTick = 5;

            for (int x = -radius; x <= radius; x++) {
               for (int y = -radius; y <= radius; y++) {
                  for (int z = -radius; z <= radius; z++) {
                     int finalX = x;
                     int finalY = y;
                     int finalZ = z;
                     if (processedBlocks >= maxBlocksPerTick) {
                        try {
                           Thread.sleep(50L);
                           processedBlocks = 0;
                        } catch (InterruptedException var12) {
                           Thread.currentThread().interrupt();
                           break;
                        }
                     }

                     this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        Location blockLocation = location.clone().add(finalX, finalY, finalZ);
                        Block block = blockLocation.getBlock();
                        if (block.getType() == Material.BARREL) {
                           this.barrelManager.initializeBarrel(block);
                        } else if (block.getType() == Material.RESPAWN_ANCHOR) {
                           this.anchorManager.initializeAnchor(block);
                        }
                     });
                     processedBlocks++;
                  }
               }
            }

            this.barrierCleaner.asyncRemoveBarrier(location, radius);
         });
      });
   }

   private void broadcastSpawnMessageAsync() {
      List<String> messages = this.plugin.getConfig().getStringList("messages.spawnmessage");
      String rarityKey = this.rarity.getDisplayName();
      String formattedRarity = this.plugin.getConfig().getString("messages.rarity." + rarityKey, rarityKey).replace("&", "§");

      for (String message : messages) {
         String finalMessage = message.replace("%rarity%", formattedRarity)
            .replace("%x%", String.valueOf(this.shipLocation.getBlockX()))
            .replace("%y%", String.valueOf(this.shipLocation.getBlockY()))
            .replace("%z%", String.valueOf(this.shipLocation.getBlockZ()))
            .replace("&", "§");
         this.plugin.getServer().broadcastMessage(finalMessage);
      }
   }

   private void playSpawnSoundAsync(Location location) {
      try {
         Sound sound = Sound.valueOf(String.valueOf(Sound.ENTITY_PILLAGER_AMBIENT));
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> location.getWorld().playSound(location, sound, 1.0F, 1.0F));
      } catch (Exception var3) {
         this.plugin.getLogger().warning("Ошибка при воспроизведении звука: " + var3.getMessage());
      }
   }

   private void startAsyncEventTimer() {
      int duration = this.getEventDuration();
      this.eventTimerTask = (new BukkitRunnable() {
         public void run() {
            if (EventShipManager.this.isActive) {
               EventShipManager.this.removeEvent();
            }
         }
      }).runTaskLater(this.plugin, duration * 20L);
   }

   private EventRarity getRandomRarity() {
      EventRarity[] rarities = EventRarity.values();
      return rarities[this.random.nextInt(rarities.length)];
   }

   private int getEventDuration() {
      if (this.rarity != null) {
         switch (this.rarity) {
            case DEFAULT:
               return 780;
            case PROSPEROUS:
               return 900;
            case LUXURIOUS:
               return 1020;
            default:
               return 780;
         }
      } else {
         return 780;
      }
   }

   public void removeEvent() {
      this.hologramManager.removeAllHolograms();
      EventManager.getInstance().events.remove("ship");
      if (this.schematicService != null) {
         this.schematicService.undoLastPaste();
      }

      Location loc = this.captain.getLocation();
      World world = loc.getWorld();

      for (int i = 0; i < 15; i++) {
         double offsetX = (Math.random() - 0.5) * 1.2;
         double offsetZ = (Math.random() - 0.5) * 1.2;
         Location particleLoc = loc.clone().add(offsetX, 0.1, offsetZ);
         world.spawnParticle(Particle.LAVA, particleLoc, 1, 0.0, 0.35, 0.0, 0.12);
      }

      if (this.captain != null) {
         this.captain.remove();
         this.captain = null;
      }

      if (this.eventTimerTask != null) {
         this.eventTimerTask.cancel();
         this.eventTimerTask = null;
      }

      if (this.keySpawnTask != null) {
         this.keySpawnTask.cancel();
         this.keySpawnTask = null;
      }

      this.barrelManager.removeAllBarrels();
      this.anchorManager.removeAllAnchors();
      if (this.shipLocation != null) {
         this.worldGuardService.removeRegion("eventship_ship", this.shipLocation);
      }

      this.shipLocation = null;
      this.rarity = null;
      this.isActive = false;
   }

   public boolean isActive() {
      return this.isActive;
   }

   public boolean isSearchingLocation() {
      return this.isSearchingLocation;
   }

   public Location getShipLocation() {
      return this.shipLocation;
   }

   public CaptainNPC getCaptain() {
      return this.captain;
   }

   public BarrelManager getBarrelManager() {
      return this.barrelManager;
   }

   public RespawnAnchorManager getAnchorManager() {
      return this.anchorManager;
   }

   public EventRarity getRarity() {
      return this.rarity;
   }

   private Location createWaterPoolAsync(World world) {
      try {
         int x = this.random.nextInt(2000) - 1000;
         int z = this.random.nextInt(2000) - 1000;
         int y = 64;
         Location location = new Location(world, x, y, z);

         for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
               for (int dy = 0; dy <= 3; dy++) {
                  int finalDx = dx;
                  int finalDz = dz;
                  int finalDy = dy;
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                     Location blockLoc = location.clone().add(finalDx, finalDy, finalDz);
                     if (finalDy == 0) {
                        blockLoc.getBlock().setType(Material.STONE);
                     } else {
                        blockLoc.getBlock().setType(Material.WATER);
                     }
                  });
                  if (Math.abs(dx) + Math.abs(dz) + Math.abs(dy) % 5 == 0) {
                     try {
                        Thread.sleep(1L);
                     } catch (InterruptedException var13) {
                        Thread.currentThread().interrupt();
                        return null;
                     }
                  }
               }
            }
         }

         return location;
      } catch (Exception var14) {
         return null;
      }
   }
}
