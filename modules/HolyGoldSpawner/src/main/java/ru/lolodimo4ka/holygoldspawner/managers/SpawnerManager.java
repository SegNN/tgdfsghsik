package ru.lolodimo4ka.holygoldspawner.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.lolodimo4ka.holygoldspawner.HolyGoldSpawner;
import ru.lolodimo4ka.holygoldspawner.database.Database;
import ru.lolodimo4ka.holygoldspawner.events.Config;
import ru.lolodimo4ka.holygoldspawner.utils.Item;

public class SpawnerManager {
   private final JavaPlugin plugin;
   private final Config configManager;
   private final MessagesManager messagesManager;
   private final Item itemManager;
   private final Database database;
   private final Set<Location> goldenSpawners = ConcurrentHashMap.newKeySet();
   private final Map<Location, Integer> spawnerDurability = new ConcurrentHashMap<>();
   private final Map<Location, UUID> spawnerOwners = new ConcurrentHashMap<>();
   private final Map<Location, Boolean> spawnerActivity = new ConcurrentHashMap<>();
   private final Map<Location, Long> spawnerPlacementTime = new ConcurrentHashMap<>();
   private final Set<UUID> playersWithCooldown = ConcurrentHashMap.newKeySet();
   private double currentMinCoins;
   private double currentMaxCoins;
   private long lastIncreaseTime;

   public SpawnerManager(JavaPlugin plugin, Config configManager, Item itemManager, MessagesManager messagesManager, Database database) {
      this.plugin = plugin;
      this.configManager = configManager;
      this.messagesManager = messagesManager;
      this.itemManager = itemManager;
      this.database = database;
      this.currentMinCoins = configManager.minCoins;
      this.currentMaxCoins = configManager.maxCoins;
      this.lastIncreaseTime = System.currentTimeMillis();
      if (database != null && database.isConnected()) {
         this.loadData();
      } else {
         plugin.getLogger().warning("Database not connected, skipping spawner loading");
      }

      this.startTasks();
   }

   public void loadData() {
      if (this.database != null && this.database.isConnected()) {
         this.database.loadSpawners(this);
         this.plugin.getLogger().info("Spawners data loaded from database");
      } else {
         this.plugin.getLogger().warning("Cannot load spawners: database not connected!");
      }
   }

   public void saveData() {
      if (this.database != null && this.database.isConnected()) {
         this.database.saveSpawners(this);
      }
   }

   public void addSpawnerToMemory(Location location, UUID owner, int durability, long placementTime) {
      this.goldenSpawners.add(location);
      this.spawnerOwners.put(location, owner);
      this.spawnerDurability.put(location, durability);
      this.spawnerActivity.put(location, false);
      this.spawnerPlacementTime.put(location, placementTime);
      this.configureSpawner(location);
   }

   private void startTasks() {
      this.startSpawnerCheckTask();
      this.startDurabilityCheckTask();
      this.startDailyIncreaseTask();
      if (this.configManager.neighborPenaltyEnabled) {
         this.startNeighborCheckTask();
      }
   }

   private void startSpawnerCheckTask() {
      (new BukkitRunnable() {
         public void run() {
            SpawnerManager.this.checkPlayersNearSpawners();
         }
      }).runTaskTimer(this.plugin, 0L, this.configManager.checkInterval * 20L);
   }

   private void startDurabilityCheckTask() {
      (new BukkitRunnable() {
         public void run() {
            SpawnerManager.this.checkSpawnersDurability();
         }
      }).runTaskTimer(this.plugin, 0L, this.configManager.durabilityCheckInterval * 20L);
   }

   private void startDailyIncreaseTask() {
      if (this.configManager.dailyIncreaseEnabled) {
         (new BukkitRunnable() {
            public void run() {
               SpawnerManager.this.applyDailyIncrease();
            }
         }).runTaskTimer(this.plugin, 0L, this.configManager.dailyIncreaseInterval * 20L);
      }
   }

   private void startNeighborCheckTask() {
      (new BukkitRunnable() {
         public void run() {
            SpawnerManager.this.checkSpawnerDensity();
         }
      }).runTaskTimer(this.plugin, 0L, this.configManager.neighborCheckInterval * 20L);
   }

   private void applyDailyIncrease() {
      this.currentMinCoins = this.currentMinCoins + this.configManager.dailyMinIncrease;
      this.currentMaxCoins = this.currentMaxCoins + this.configManager.dailyMaxIncrease;
      this.lastIncreaseTime = System.currentTimeMillis();
   }

   public void resetMoneyUp() {
      this.currentMinCoins = this.configManager.minCoins;
      this.currentMaxCoins = this.configManager.maxCoins;
      this.lastIncreaseTime = System.currentTimeMillis();
   }

   public double getBaseMinCoins() {
      return this.configManager.minCoins;
   }

   public double getBaseMaxCoins() {
      return this.configManager.maxCoins;
   }

   private void checkPlayersNearSpawners() {
      for (Location spawnerLocation : this.goldenSpawners) {
         if (!this.canInteractWithSpawner(spawnerLocation)) {
            this.spawnerActivity.put(spawnerLocation, false);
         } else {
            Player closestPlayer = this.findClosestPlayer(spawnerLocation);
            boolean isActive = closestPlayer != null;
            this.spawnerActivity.put(spawnerLocation, isActive);
            if (isActive) {
               double penalty = this.getNeighborPenalty(spawnerLocation);
               HolyGoldSpawner mainPlugin = (HolyGoldSpawner)this.plugin;
               mainPlugin.giveRandomCoins(closestPlayer, penalty, this.currentMinCoins, this.currentMaxCoins);
            }
         }
      }
   }

   private boolean canInteractWithSpawner(Location location) {
      try {
         Block block = location.getBlock();
         if (block.getType() != Material.SPAWNER) {
            this.removeSpawner(location);
            return false;
         } else {
            return true;
         }
      } catch (Exception var3) {
         return false;
      }
   }

   private Player findClosestPlayer(Location spawnerLocation) {
      Player closestPlayer = null;
      double closestDistance = Double.MAX_VALUE;

      for (Player player : Bukkit.getOnlinePlayers()) {
         if (!this.playersWithCooldown.contains(player.getUniqueId()) && player.getWorld().equals(spawnerLocation.getWorld())) {
            double distance = player.getLocation().distance(spawnerLocation);
            if (distance <= this.configManager.spawnerRadius && distance < closestDistance) {
               closestDistance = distance;
               closestPlayer = player;
            }
         }
      }

      return closestPlayer;
   }

   private void checkSpawnersDurability() {
      Iterator<Location> iterator = this.goldenSpawners.iterator();

      while (iterator.hasNext()) {
         Location location = iterator.next();
         if (!this.canInteractWithSpawner(location)) {
            iterator.remove();
            this.cleanupSpawnerData(location);
            if (this.database != null && this.database.isConnected()) {
               this.database.removeSpawner(location);
            }
         } else {
            boolean isActive = this.spawnerActivity.getOrDefault(location, false);
            if (!this.configManager.durabilityOnlyWhenActive || isActive) {
               int durability = this.spawnerDurability.getOrDefault(location, this.configManager.maxDurability);
               durability -= this.configManager.durabilityLossPerCheck;
               if (durability <= 0) {
                  durability = 0;
               }

               this.spawnerDurability.put(location, durability);
               if (this.database != null && this.database.isConnected()) {
                  this.database.updateDurability(location, durability);
               }

               if (durability <= 0 || this.checkSpawnerBreak(durability)) {
                  iterator.remove();
                  this.cleanupSpawnerData(location);
                  if (this.database != null && this.database.isConnected()) {
                     this.database.removeSpawner(location);
                  }

                  if (location.getBlock().getType() == Material.SPAWNER) {
                     location.getBlock().setType(Material.AIR);
                  }
               }
            }
         }
      }
   }

   private void cleanupSpawnerData(Location location) {
      this.spawnerDurability.remove(location);
      this.spawnerOwners.remove(location);
      this.spawnerActivity.remove(location);
      this.spawnerPlacementTime.remove(location);
   }

   private void checkSpawnerDensity() {
   }

   private boolean checkSpawnerBreak(int durability) {
      if (durability <= 0) {
         return true;
      } else {
         double breakChance = (this.configManager.maxDurability - durability) * this.configManager.breakChanceMultiplier;
         return Math.random() < breakChance;
      }
   }

   private double getNeighborPenalty(Location location) {
      if (!this.configManager.neighborPenaltyEnabled) {
         return 0.0;
      } else {
         int nearbySpawners = 0;

         for (Location spawnerLocation : this.goldenSpawners) {
            if (!spawnerLocation.equals(location)
               && spawnerLocation.getWorld().equals(location.getWorld())
               && spawnerLocation.distance(location) <= this.configManager.neighborCheckRadius) {
               nearbySpawners++;
            }
         }

         if (nearbySpawners == 0) {
            return 0.0;
         } else {
            double penalty = nearbySpawners * this.configManager.penaltyPerSpawner;
            return Math.min(penalty, this.configManager.maxPenalty) / 100.0;
         }
      }
   }

   public String maskCoordinates(int coordinate, String coordType, boolean isHeight) {
      if (isHeight) {
         return String.valueOf(coordinate);
      } else {
         return !this.configManager.coordsEncryptionEnabled ? String.valueOf(coordinate) : this.applyMasking(coordinate);
      }
   }

   private String applyMasking(int coordinate) {
      boolean isNegative = coordinate < 0;
      String coordString = String.valueOf(Math.abs(coordinate));
      int length = coordString.length();
      String pattern;
      if (length == 1) {
         pattern = this.configManager.pattern1digit;
      } else if (length == 2) {
         pattern = this.configManager.pattern2digits;
      } else if (length == 3) {
         pattern = this.configManager.pattern3digits;
      } else {
         pattern = this.configManager.pattern4digits;
      }

      String masked = this.applyPattern(coordString, pattern);
      return isNegative ? "-" + masked : masked;
   }

   private String applyPattern(String coordinate, String pattern) {
      StringBuilder result = new StringBuilder();
      int coordinatePosition = 0;

      for (int i = 0; i < pattern.length(); i++) {
         char character = pattern.charAt(i);
         if (character == 8226 && coordinatePosition < coordinate.length()) {
            result.append(coordinate.charAt(coordinatePosition++));
         } else {
            result.append(character);
         }
      }

      return result.toString();
   }

   public boolean addSpawner(Location location, UUID owner, int durability, Player player) {
      if (location.getBlock().getType() != Material.SPAWNER) {
         return false;
      } else {
         this.goldenSpawners.add(location);
         this.spawnerOwners.put(location, owner);
         this.spawnerDurability.put(location, durability);
         this.spawnerActivity.put(location, false);
         long placementTime = System.currentTimeMillis();
         this.spawnerPlacementTime.put(location, placementTime);
         this.configureSpawner(location);
         if (this.database != null && this.database.isConnected()) {
            this.database.addSpawner(location, owner, durability);
         }

         return true;
      }
   }

   public boolean addSpawner(Location location, UUID owner, int durability) {
      if (location.getBlock().getType() != Material.SPAWNER) {
         return false;
      } else {
         this.goldenSpawners.add(location);
         this.spawnerOwners.put(location, owner);
         this.spawnerDurability.put(location, durability);
         this.spawnerActivity.put(location, false);
         long placementTime = System.currentTimeMillis();
         this.spawnerPlacementTime.put(location, placementTime);
         this.configureSpawner(location);
         if (this.database != null && this.database.isConnected()) {
            this.database.addSpawner(location, owner, durability);
         }

         return true;
      }
   }

   public void removeSpawner(Location location) {
      this.cleanupSpawnerData(location);
      this.goldenSpawners.remove(location);
      if (this.database != null && this.database.isConnected()) {
         this.database.removeSpawner(location);
      }
   }

   public boolean containsSpawner(Location location) {
      return this.goldenSpawners.contains(location);
   }

   public int getSpawnerDurability(Location location) {
      return this.spawnerDurability.getOrDefault(location, this.configManager.maxDurability);
   }

   public void setSpawnerDurability(Location location, int durability) {
      this.spawnerDurability.put(location, durability);
      if (this.database != null && this.database.isConnected()) {
         this.database.updateDurability(location, durability);
      }
   }

   private void configureSpawner(Location location) {
      Block block = location.getBlock();
      if (block.getType() == Material.SPAWNER) {
         if (block.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner)block.getState();

            try {
               EntityType displayEntity = EntityType.valueOf(this.configManager.getDisplayEntity());
               spawner.setSpawnedType(displayEntity);
            } catch (IllegalArgumentException var5) {
               spawner.setSpawnedType(EntityType.PIG);
            }

            spawner.setDelay(32767);
            spawner.update();
         }
      }
   }

   public void resetSpawnerTimer(CreatureSpawner spawner) {
      spawner.setDelay(32767);
      spawner.update();
   }

   public Set<Location> getGoldenSpawners() {
      return this.goldenSpawners;
   }

   public boolean isSpawnerActive(Location location) {
      return this.spawnerActivity.getOrDefault(location, false);
   }

   public double getCurrentMinCoins() {
      return this.currentMinCoins;
   }

   public double getCurrentMaxCoins() {
      return this.currentMaxCoins;
   }

   public UUID getSpawnerOwner(Location location) {
      return this.spawnerOwners.get(location);
   }

   public boolean hasSpawnerOwner(Location location) {
      return this.spawnerOwners.containsKey(location);
   }

   public Long getSpawnerPlacementTime(Location location) {
      return this.spawnerPlacementTime.get(location);
   }

   public void addPlayerCooldown(UUID playerId) {
      this.playersWithCooldown.add(playerId);
   }

   public void removePlayerCooldown(UUID playerId) {
      this.playersWithCooldown.remove(playerId);
   }

   public boolean hasPlayerCooldown(UUID playerId) {
      return this.playersWithCooldown.contains(playerId);
   }

   public void clearAllSpawners() {
      for (Location location : this.goldenSpawners) {
         if (location.getBlock().getType() == Material.SPAWNER) {
            location.getBlock().setType(Material.AIR);
         }
      }

      this.goldenSpawners.clear();
      this.spawnerDurability.clear();
      this.spawnerOwners.clear();
      this.spawnerActivity.clear();
      this.spawnerPlacementTime.clear();
      if (this.database != null && this.database.isConnected()) {
         this.database.saveSpawners(this);
      }
   }

   public int getTotalSpawners() {
      return this.goldenSpawners.size();
   }

   public List<Location> getSpawnersInWorld(String worldName) {
      List<Location> spawnersInWorld = new ArrayList<>();

      for (Location location : this.goldenSpawners) {
         if (location.getWorld().getName().equals(worldName)) {
            spawnersInWorld.add(location);
         }
      }

      return spawnersInWorld;
   }

   public Map<UUID, Integer> getSpawnersByOwner() {
      Map<UUID, Integer> spawnersByOwner = new HashMap<>();

      for (UUID owner : this.spawnerOwners.values()) {
         spawnersByOwner.put(owner, spawnersByOwner.getOrDefault(owner, 0) + 1);
      }

      return spawnersByOwner;
   }

   public Map<Location, Long> getSpawnerPlacementTimes() {
      return new HashMap<>(this.spawnerPlacementTime);
   }

   public void setSpawnerPlacementTime(Location location, long placementTime) {
      this.spawnerPlacementTime.put(location, placementTime);
   }

   public List<Location> getSpawnersByPlayer(UUID playerUUID) {
      List<Location> playerSpawners = new ArrayList<>();

      for (Location location : this.goldenSpawners) {
         if (this.spawnerOwners.containsKey(location) && this.spawnerOwners.get(location).equals(playerUUID)) {
            playerSpawners.add(location);
         }
      }

      return playerSpawners;
   }

   public boolean hasSpawnerAtExactLocation(Location location) {
      for (Location spawnerLocation : this.goldenSpawners) {
         if (spawnerLocation.getWorld().equals(location.getWorld())
            && spawnerLocation.getBlockX() == location.getBlockX()
            && spawnerLocation.getBlockY() == location.getBlockY()
            && spawnerLocation.getBlockZ() == location.getBlockZ()) {
            return true;
         }
      }

      return false;
   }

   public Location getSpawnerAtExactLocation(Location location) {
      for (Location spawnerLocation : this.goldenSpawners) {
         if (spawnerLocation.getWorld().equals(location.getWorld())
            && spawnerLocation.getBlockX() == location.getBlockX()
            && spawnerLocation.getBlockY() == location.getBlockY()
            && spawnerLocation.getBlockZ() == location.getBlockZ()) {
            return spawnerLocation;
         }
      }

      return null;
   }

   public Map<String, Object> getSpawnerStatistics() {
      Map<String, Object> stats = new HashMap<>();
      stats.put("totalSpawners", this.goldenSpawners.size());
      stats.put("activeSpawners", this.getActiveSpawnersCount());
      stats.put("totalOwners", this.spawnerOwners.size());
      stats.put("averageDurability", this.getAverageDurability());
      stats.put("worldDistribution", this.getWorldDistribution());
      return stats;
   }

   private int getActiveSpawnersCount() {
      int count = 0;

      for (Boolean active : this.spawnerActivity.values()) {
         if (active) {
            count++;
         }
      }

      return count;
   }

   private double getAverageDurability() {
      if (this.spawnerDurability.isEmpty()) {
         return 0.0;
      } else {
         double total = 0.0;

         for (int durability : this.spawnerDurability.values()) {
            total += durability;
         }

         return total / this.spawnerDurability.size();
      }
   }

   private Map<String, Integer> getWorldDistribution() {
      Map<String, Integer> distribution = new HashMap<>();

      for (Location location : this.goldenSpawners) {
         String worldName = location.getWorld().getName();
         distribution.put(worldName, distribution.getOrDefault(worldName, 0) + 1);
      }

      return distribution;
   }

   public void reconfigureAllSpawners() {
      for (Location location : this.goldenSpawners) {
         this.configureSpawner(location);
      }
   }

   public Location getNearestSpawner(Player player) {
      Location nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (Location location : this.goldenSpawners) {
         if (location.getWorld().equals(player.getWorld())) {
            double distance = player.getLocation().distance(location);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = location;
            }
         }
      }

      return nearest;
   }

   public List<Location> getSpawnersInRadius(Location center, double radius) {
      List<Location> spawnersInRadius = new ArrayList<>();

      for (Location location : this.goldenSpawners) {
         if (location.getWorld().equals(center.getWorld()) && location.distance(center) <= radius) {
            spawnersInRadius.add(location);
         }
      }

      return spawnersInRadius;
   }

   public void reloadConfig() {
      this.currentMinCoins = this.configManager.minCoins;
      this.currentMaxCoins = this.configManager.maxCoins;
      this.reconfigureAllSpawners();
   }

   public Database getDatabaseManager() {
      return this.database;
   }

   public CompletableFuture<Void> saveDataAsync() {
      return this.database != null && this.database.isConnected() ? this.database.saveSpawners(this) : CompletableFuture.completedFuture(null);
   }

   public void saveDataSync() {
      if (this.database != null && this.database.isConnected()) {
         this.database.saveSpawnersSync(this);
      }
   }
}
