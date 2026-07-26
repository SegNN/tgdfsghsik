package ru.kotikstasika.eventpolana.manager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;
import ru.kotikstasika.eventpolana.EventPolana;
import ru.kotikstasika.eventpolana.config.PluginConfig;
import ru.kotikstasika.eventpolana.model.PolanaRarity;
import ru.kotikstasika.eventpolana.model.PolanaState;
import ru.kotikstasika.eventpolana.service.HologramService;
import ru.kotikstasika.eventpolana.service.SchematicService;
import ru.kotikstasika.eventpolana.service.WorldGuardService;

public class PolanaManager {
   private static final String REGION_ID = "polana_event";
   private static final Set<Material> DANGEROUS_LIQUIDS = EnumSet.of(
      Material.WATER, Material.LAVA, Material.KELP, Material.KELP_PLANT, Material.SEAGRASS, Material.TALL_SEAGRASS
   );
   private final EventPolana plugin;
   private final PluginConfig cfg;
   private final SchematicService schematicService;
   private final WorldGuardService wgService;
   private final FlowerManager flowerManager;
   private final RegionBarrierCleaner barrierCleaner;
   private final HologramService hologramService;
   private final Random random = ThreadLocalRandom.current();
   private int locationRetryCount = 0;
   private boolean isSearchingLocation = false;
   private PolanaState state = PolanaState.NOT_SPAWNED;
   private Location polanaLocation;
   private BukkitTask autoRemoveTask;
   private BukkitTask lookAtTask;
   private Villager gardener;
   private boolean shearsTaken = false;
   private PolanaRarity rarity;

   public PolanaManager(
      EventPolana plugin,
      PluginConfig cfg,
      SchematicService schematicService,
      WorldGuardService wgService,
      FlowerManager flowerManager,
      RegionBarrierCleaner barrierCleaner,
      HologramService hologramService
   ) {
      this.plugin = plugin;
      this.cfg = cfg;
      this.schematicService = schematicService;
      this.wgService = wgService;
      this.flowerManager = flowerManager;
      this.barrierCleaner = barrierCleaner;
      this.hologramService = hologramService;
   }

   public void spawnPolana(PolanaRarity rarity) {
      if (!this.isSearchingLocation && this.state != PolanaState.SPAWNED) {
         this.rarity = rarity;
         final World world = Bukkit.getWorld(this.cfg.getWorldName());
         if (world == null) {
            this.plugin.getLogger().warning("&cМир + " + world + " не найден!");
         } else {
            this.isSearchingLocation = true;
            (new BukkitRunnable() {
               public void run() {
                  Location foundLocation = PolanaManager.this.findSuitableLocation(world);
                  Bukkit.getScheduler().runTask(PolanaManager.this.plugin, () -> {
                     if (foundLocation == null) {
                        PolanaManager.this.handleLocationSearchFailure();
                     } else {
                        PolanaManager.this.handleSuccessfulLocationFound(foundLocation);
                     }
                  });
               }
            }).runTaskAsynchronously(this.plugin);
         }
      }
   }

   private Location findSuitableLocation(World world) {
      int attempts = 0;
      int maxAttempts = 50;
      int spawnRadius = (int)(world.getWorldBorder().getSize() / 2.0) - 10;

      while (attempts < maxAttempts) {
         attempts++;
         Location randomLoc = this.generateRandomLocation(world, spawnRadius);
         if (this.isLocationSuitable(randomLoc)) {
            return randomLoc;
         }
      }

      return null;
   }

   private Location generateRandomLocation(World world, int spawnRadius) {
      int x = this.random.nextInt(spawnRadius * 2) - spawnRadius;
      int z = this.random.nextInt(spawnRadius * 2) - spawnRadius;
      int y = world.getHighestBlockYAt(x, z);
      return new Location(world, x, y, z);
   }

   private boolean isLocationSuitable(Location loc) {
      if (this.isInLiquid(loc)) {
         return false;
      } else {
         return !this.hasFlatSurface(loc) ? false : this.hasSolidBase(loc);
      }
   }

   private boolean isInLiquid(Location loc) {
      Block block = loc.getBlock();
      Block blockAbove = loc.clone().add(0.0, 1.0, 0.0).getBlock();
      return DANGEROUS_LIQUIDS.contains(block.getType()) || DANGEROUS_LIQUIDS.contains(blockAbove.getType()) || block.isLiquid() || blockAbove.isLiquid();
   }

   private boolean hasFlatSurface(Location center) {
      int radius = 3;
      int centerY = center.getBlockY();
      int allowedHeightDifference = 1;

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            Block block = center.clone().add(x, 0.0, z).getBlock();
            if (Math.abs(block.getY() - centerY) > allowedHeightDifference) {
               return false;
            }

            if (DANGEROUS_LIQUIDS.contains(block.getType())) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean hasSolidBase(Location loc) {
      Block blockBelow = loc.clone().subtract(0.0, 1.0, 0.0).getBlock();
      Material type = blockBelow.getType();
      return type.isSolid() && !DANGEROUS_LIQUIDS.contains(type) && !blockBelow.isLiquid();
   }

   private void handleLocationSearchFailure() {
      if (this.locationRetryCount < 100) {
         this.locationRetryCount++;
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.isSearchingLocation = false;
            this.spawnPolana(this.rarity);
         }, 100L);
      } else {
         this.locationRetryCount = 0;
         this.isSearchingLocation = false;
      }
   }

   private void handleSuccessfulLocationFound(Location found) {
      this.locationRetryCount = 0;
      this.polanaLocation = found.clone();
      if (!this.schematicService.loadAndPasteSchematic(this.plugin.getDataFolder(), this.cfg.getSchematicName(), this.polanaLocation)) {
         this.plugin.getLogger().warning(this.cfg.getMessageSchematicNotFound());
         this.isSearchingLocation = false;
      } else {
         if (this.wgService != null) {
            this.wgService.createRegion("polana_event", this.polanaLocation, this.cfg.getFlowerBreakRadius());
         }

         this.spawnGardener();
         this.startFlowerSpawning();
         this.state = PolanaState.SPAWNED;
         EventManager.getInstance().events.remove("polana");
         this.announceSpawn();
         this.barrierCleaner.asyncRemoveBarrier(this.polanaLocation, 150);
         this.scheduleEventRemoval();
         this.isSearchingLocation = false;
      }
   }

   public void removePolana() {
      if (this.state != PolanaState.NOT_SPAWNED) {
         this.hologramService.remove();
         this.wgService.removeRegion("polana_event", this.polanaLocation);
         this.schematicService.undoLastPaste();
         if (this.gardener != null) {
            this.gardener.remove();
            this.gardener = null;
         }

         this.flowerManager.stop();
         if (this.autoRemoveTask != null) {
            this.autoRemoveTask.cancel();
         }

         if (this.lookAtTask != null) {
            this.lookAtTask.cancel();
         }

         EventManager.getInstance().events.remove("polana");
         this.state = PolanaState.NOT_SPAWNED;
         this.polanaLocation = null;
         this.rarity = null;
         this.locationRetryCount = 0;
         this.isSearchingLocation = false;
      }
   }

   private void startFlowerSpawning() {
      long interval;
      switch (this.rarity) {
         case EPIC:
            interval = 20L * this.cfg.getSpawnFlowerSecondsEpic();
            break;
         case LEGENDARY:
            interval = 20L * this.cfg.getSpawnFlowerSecondsLegendary();
            break;
         default:
            interval = 20L * this.cfg.getSpawnFlowerSecondsDefault();
      }

      this.flowerManager.start(this.polanaLocation, this.cfg.getFlowerSpawnRadius(), interval);
   }

   private void spawnGardener() {
      Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(Villager.class).forEach(villager -> {
         if (villager.getScoreboardTags().contains("EventPolanaNPC")) {
            villager.remove();
         }
      }));
      Location villagerLoc = this.polanaLocation.clone().add(0.5, 1.0, 0.5);
      this.gardener = (Villager)villagerLoc.getWorld().spawnEntity(villagerLoc, EntityType.VILLAGER);
      this.gardener.setRemoveWhenFarAway(false);
      this.gardener.setAI(false);
      this.gardener.setInvulnerable(true);
      this.gardener.setCollidable(false);
      this.gardener.setSilent(true);
      this.gardener.setGravity(false);
      this.gardener.setPersistent(true);
      this.gardener.setRemoveWhenFarAway(false);
      this.gardener.setCanPickupItems(false);

      try {
         this.gardener.setProfession(Profession.valueOf(this.cfg.getGardenerProfession()));
         this.gardener.setVillagerType(Type.valueOf(this.cfg.getGardenerVillagerType()));
      } catch (IllegalArgumentException var3) {
         this.gardener.setProfession(Profession.FARMER);
         this.gardener.setVillagerType(Type.PLAINS);
      }

      this.gardener.setVillagerLevel(this.cfg.getGardenerLevel());
      this.gardener.setAdult();
      this.gardener.setAgeLock(true);
      this.gardener.addScoreboardTag("EventPolanaNPC");
      this.updateGardenerHand();
      this.hologramService.createOrUpdate(villagerLoc.clone().add(0.0, 2.8, 0.0), !this.shearsTaken);
      this.startLookingAtPlayers();
      Bukkit.getScheduler().runTaskLater(this.plugin, this::validateGardener, 1L);
   }

   private void validateGardener() {
      if (this.state == PolanaState.SPAWNED) {
         List<Villager> found = new ArrayList<>();
         Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(Villager.class).forEach(v -> {
            if (v.getScoreboardTags().contains("EventPolanaNPC")) {
               found.add(v);
            }
         }));
         if (found.size() > 1) {
            found.stream().filter(v -> !v.equals(this.gardener)).forEach(Entity::remove);
         }

         if (this.gardener == null || !this.gardener.isValid()) {
            if (!found.isEmpty()) {
               this.gardener = found.get(0);
            } else if (this.polanaLocation != null) {
               this.spawnGardener();
            }
         }

         if (this.gardener != null) {
            this.gardener.setProfession(Profession.FARMER);
            this.gardener.setVillagerType(Type.PLAINS);
            this.gardener.setVillagerLevel(5);
            this.gardener.setAdult();
            this.gardener.setAgeLock(true);
            this.gardener.setRemoveWhenFarAway(false);
            this.updateGardenerHand();
         }
      }
   }

   private void updateGardenerHand() {
      if (this.gardener != null) {
         if (this.gardener.getEquipment() != null) {
            if (this.shearsTaken) {
               this.gardener.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
               this.gardener.getEquipment().setItemInMainHand(new ItemStack(Material.SHEARS));
            }

            this.gardener.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
         }
      }
   }

   private void startLookingAtPlayers() {
      if (this.lookAtTask != null) {
         this.lookAtTask.cancel();
      }

      this.lookAtTask = (new BukkitRunnable() {
         public void run() {
            if (PolanaManager.this.gardener != null && PolanaManager.this.gardener.isValid()) {
               Player nearest = null;
               double minDistance = Double.MAX_VALUE;
               double searchRadius = PolanaManager.this.cfg.getGardenerPlayerSearchRadius();

               for (Player p : PolanaManager.this.gardener.getWorld().getPlayers()) {
                  if (p.isValid() && p.isOnline()) {
                     double d = p.getLocation().distanceSquared(PolanaManager.this.gardener.getLocation());
                     if (d < minDistance && d <= searchRadius * searchRadius) {
                        minDistance = d;
                        nearest = p;
                     }
                  }
               }

               if (nearest != null) {
                  Location loc = PolanaManager.this.gardener.getLocation();
                  loc.setDirection(nearest.getLocation().subtract(loc).toVector());
                  PolanaManager.this.gardener.teleport(loc);
               }
            } else {
               if (PolanaManager.this.polanaLocation != null) {
                  PolanaManager.this.spawnGardener();
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, this.cfg.getGardenerLookUpdateInterval());
   }

   private void scheduleEventRemoval() {
      if (this.autoRemoveTask != null && !this.autoRemoveTask.isCancelled()) {
         this.autoRemoveTask.cancel();
      }

      long duration;
      switch (this.rarity) {
         case EPIC:
            duration = 1200L * this.cfg.getDeleteMinutesEpic();
            break;
         case LEGENDARY:
            duration = 1200L * this.cfg.getDeleteMinutesLegendary();
            break;
         default:
            duration = 1200L * this.cfg.getDeleteMinutesDefault();
      }

      this.autoRemoveTask = (new BukkitRunnable() {
         public void run() {
            PolanaManager.this.removePolana();
         }
      }).runTaskLater(this.plugin, duration);
   }

   private void announceSpawn() {
      String rarityName;
      switch (this.rarity) {
         case EPIC:
            rarityName = this.cfg.getMessageRarityNameEpic();
            break;
         case LEGENDARY:
            rarityName = this.cfg.getMessageRarityNameLegendary();
            break;
         default:
            rarityName = this.cfg.getMessageRarityNameDefault();
      }

      EventManager.getInstance()
         .events
         .put(
            "polana",
            new Event(
               this.cfg.getMessageSpawnTitle(),
               ChatColor.translateAlternateColorCodes('&', "§a▶ " + this.cfg.getMessageSpawnTitle() + " &fнаходится на координатах ")
                  + "§a"
                  + this.polanaLocation.getBlockX()
                  + " "
                  + this.polanaLocation.getBlockY()
                  + " "
                  + this.polanaLocation.getBlockZ()
                  + " "
            )
         );
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&4&3&B&C&0&6◀&x&4&3&B&C&0&6&m                              &x&4&3&B&C&0&6▶"));
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&4&3&B&C&0&6&n▍&f  " + this.cfg.getMessageSpawnTitle()));
      Bukkit.broadcastMessage(
         ChatColor.translateAlternateColorCodes('&', "&x&4&3&B&C&0&6&n▍&f Редкость: " + ChatColor.translateAlternateColorCodes('&', rarityName))
      );
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&4&3&B&C&0&6&n▍&f Появилась на координатах"));
      String coordsMessage = this.cfg
         .formatMessage(
            this.cfg.getMessageCoordinatesFormat(),
            "x",
            String.valueOf(this.polanaLocation.getBlockX()),
            "y",
            String.valueOf(this.polanaLocation.getBlockY()),
            "z",
            String.valueOf(this.polanaLocation.getBlockZ())
         );
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&4&3&B&C&0&6▍&f " + coordsMessage));
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&4&3&B&C&0&6◀&x&4&3&B&C&0&6&m                              &x&4&3&B&C&0&6▶"));
   }

   public PolanaState getState() {
      return this.state;
   }

   public Location getPolanaLocation() {
      return this.polanaLocation;
   }

   public PolanaRarity getRarity() {
      return this.rarity;
   }

   public boolean isShearsTaken() {
      return this.shearsTaken;
   }

   public Villager getGardener() {
      return this.gardener;
   }

   public boolean isSearchingLocation() {
      return this.isSearchingLocation;
   }

   public void setShearsTaken(boolean value) {
      this.shearsTaken = value;
      if (this.gardener != null && this.gardener.isValid()) {
         this.hologramService.createOrUpdate(this.gardener.getLocation().clone().add(0.0, 2.8, 0.0), !this.shearsTaken);
         this.updateGardenerHand();
      }
   }

   public void restartFlowerSpawning() {
      if (this.state == PolanaState.SPAWNED && this.polanaLocation != null) {
         this.startFlowerSpawning();
      }
   }
}
