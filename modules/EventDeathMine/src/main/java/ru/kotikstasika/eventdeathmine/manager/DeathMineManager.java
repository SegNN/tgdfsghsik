package ru.kotikstasika.eventdeathmine.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;
import ru.kotikstasika.eventdeathmine.EventDeathMine;
import ru.kotikstasika.eventdeathmine.config.ConfigManager;
import ru.kotikstasika.eventdeathmine.model.DeathMineState;
import ru.kotikstasika.eventdeathmine.service.LootService;
import ru.kotikstasika.eventdeathmine.service.RegionService;
import ru.kotikstasika.eventdeathmine.service.SchematicService;
import ru.kotikstasika.eventdeathmine.task.BossBarUpdaterTask;
import ru.kotikstasika.eventdeathmine.task.MineTimerTask;
import ru.kotikstasika.eventdeathmine.task.ParticleCubeTask;
import ru.kotikstasika.eventdeathmine.task.RegionBarrierCleaner;
import ru.kotikstasika.eventdeathmine.task.ReplaceMovingPistonTask;
import ru.kotikstasika.eventdeathmine.util.ColorUtil;

public class DeathMineManager {
   private RegionBarrierCleaner barrierCleaner;
   private final JavaPlugin plugin;
   private final ConfigManager configManager;
   private final Random random = new Random();
   private final Set<Player> playersInRange = new HashSet<>();
   private final List<Location> activeCubes = new ArrayList<>();
   private final List<BukkitTask> particleTasks = new ArrayList<>();
   private final Map<String, Integer> playerMinedBlocks = new HashMap<>();
   private final LootService lootService;
   private final RegionService regionService;
   private final SchematicService schematicService;
   private DeathMineState state = DeathMineState.NOT_SPAWNED;
   private Location mineLocation;
   private EditSession editSession;
   private BossBar bossBar;
   private BukkitTask bossBarUpdateTask;
   private BukkitTask timerTask;
   private int timeLeftSeconds = 1200;
   private long startTimeMillis = 0L;
   private int totalDurationSeconds = 1200;
   private final Set<Location> resourceLocations = new HashSet<>();
   private int resourceInitialCount = 0;
   private int resourceRemainingCount = 0;
   private String displayName = "&4&lСмертельная шахта";
   private String color = "&4";
   public int SCAN_RADIUS = 40;
   public int BOSSBAR_VISIBLE_RADIUS = 100;
   public String DEFAULT_WORLD_NAME = "world";
   public String SCHEMATIC_NAME = "event_death_mine.schem";
   public int MAX_SPAWN_ATTEMPTS = 50;

   public DeathMineManager(EventDeathMine plugin) {
      this.plugin = plugin;
      this.configManager = new ConfigManager(plugin);
      this.lootService = new LootService(plugin);
      this.regionService = new RegionService();
      this.schematicService = new SchematicService(plugin);
      this.loadConfig();
      this.barrierCleaner = new RegionBarrierCleaner(plugin);
   }

   private void loadConfig() {
      this.DEFAULT_WORLD_NAME = this.configManager.worldName();
      this.SCAN_RADIUS = this.configManager.scanRadius();
      this.BOSSBAR_VISIBLE_RADIUS = this.configManager.bossbarRadius();
      this.MAX_SPAWN_ATTEMPTS = this.configManager.maxSpawnAttempts();
      this.timeLeftSeconds = this.configManager.defaultDurationSeconds();
      this.SCHEMATIC_NAME = this.configManager.schematicName();
      this.displayName = this.configManager.displayName();
      this.color = this.configManager.accentColor();
   }

   public void startBossBarUpdater() {
      this.stopBossBarUpdater();
      this.bossBarUpdateTask = new BossBarUpdaterTask(this).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void stopBossBarUpdater() {
      if (this.bossBarUpdateTask != null && !this.bossBarUpdateTask.isCancelled()) {
         this.bossBarUpdateTask.cancel();
      }
   }

   public void spawnMine(CommandSender feedback) {
      if (this.state != DeathMineState.NOT_SPAWNED) {
         if (feedback != null) {
            feedback.sendMessage(ChatColor.RED + "Шахта уже существует!");
         }
      } else {
         World world = Bukkit.getWorld(this.DEFAULT_WORLD_NAME);
         if (world == null) {
            this.plugin.getLogger().warning("Мир '" + this.DEFAULT_WORLD_NAME + "' не найден!");
            if (feedback != null) {
               feedback.sendMessage(ChatColor.RED + "Мир не найден!");
            }
         } else {
            Location foundLocation = this.findValidLocation(world);
            if (foundLocation == null) {
               this.plugin.getLogger().warning("Не удалось найти подходящее место для шахты!");
               if (feedback != null) {
                  feedback.sendMessage(ChatColor.RED + "Не удалось найти место для шахты!");
               }
            } else {
               this.mineLocation = foundLocation;
               CompletableFuture<Clipboard> future = this.schematicService.readClipboardAsync(this.SCHEMATIC_NAME);
               future.whenComplete(
                  (clipboard, throwable) -> {
                     if (throwable == null && clipboard != null) {
                        this.plugin
                           .getServer()
                           .getScheduler()
                           .runTask(
                              this.plugin,
                              () -> {
                                 boolean pasted = this.pasteClipboardSync(clipboard, this.mineLocation);
                                 if (!pasted) {
                                    if (feedback != null) {
                                       feedback.sendMessage(ChatColor.RED + "Не удалось вставить схематику!");
                                    }
                                 } else {
                                    this.regionService.createRegion(this.mineLocation, 40);
                                    this.createBossBar();
                                    this.startTimer();
                                    this.startTimeMillis = System.currentTimeMillis();
                                    this.createCubeParticles(this.mineLocation, this.configManager.cubeSize(), this.configManager.cubeDurationSeconds());
                                    this.barrierCleaner.asyncRemoveBarrier(this.mineLocation, this.configManager.barrierRadius());
                                    new ReplaceMovingPistonTask(this, this.mineLocation, 35, 4000).runTaskTimer(this.plugin, 1L, 1L);
                                    this.state = DeathMineState.SPAWNED;
                                    EventManager.getInstance().events.remove("death_mine");
                                    String header = ColorUtil.colorize(this.displayName);
                                    String body = this.buildEventMessage();
                                    EventManager.getInstance().events.put("death_mine", new Event(header, body));
                                    Bukkit.broadcastMessage(
                                       ChatColor.translateAlternateColorCodes(
                                          '&',
                                          "&x&5&9&0&0&0&0◀&x&5&B&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&E&0&0&0&0&m &x&6&0&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&E&0&0&0&0&m &x&7&0&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&3&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&0&0&0&0&0&m &x&6&E&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&0&0&0&0&0&m &x&5&E&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&B&0&0&0&0&m &x&5&9&0&0&0&0▶"
                                       )
                                    );
                                    Bukkit.broadcastMessage(
                                       ChatColor.translateAlternateColorCodes(
                                          '&',
                                          "&x&5&9&0&0&0&0&n▍&f &x&5&9&0&0&0&0&lС&x&5&C&0&0&0&0&lм&x&6&0&0&0&0&0&lе&x&6&3&0&0&0&0&lр&x&6&6&0&0&0&0&lт&x&6&9&0&0&0&0&lе&x&6&D&0&0&0&0&lл&x&7&0&0&0&0&0&lь&x&7&3&0&0&0&0&lн&x&6&F&0&0&0&0&lа&x&6&C&0&0&0&0&lя &x&6&4&0&0&0&0&lш&x&6&0&0&0&0&0&lа&x&5&D&0&0&0&0&lх&x&5&9&0&0&0&0&lта ☠"
                                       )
                                    );
                                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&5&9&0&0&0&0&n▍&f Появилась на координатах"));
                                    Bukkit.broadcastMessage(
                                       ChatColor.translateAlternateColorCodes(
                                          '&',
                                          "&x&5&9&0&0&0&0▍&f X: &x&7&3&0&0&0&0"
                                             + this.mineLocation.getBlockX()
                                             + " &fY: &x&7&3&0&0&0&0"
                                             + this.mineLocation.getBlockY()
                                             + " &fZ: &x&7&3&0&0&0&0"
                                             + this.mineLocation.getBlockZ()
                                       )
                                    );
                                    Bukkit.broadcastMessage(
                                       ChatColor.translateAlternateColorCodes(
                                          '&',
                                          "&x&5&9&0&0&0&0◀&x&5&B&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&E&0&0&0&0&m &x&6&0&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&E&0&0&0&0&m &x&7&0&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&3&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&0&0&0&0&0&m &x&6&E&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&0&0&0&0&0&m &x&5&E&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&B&0&0&0&0&m &x&5&9&0&0&0&0▶"
                                       )
                                    );
                                    if (feedback != null) {
                                       String createdMsg = this.plugin.getConfig().getString("messages.created", "");
                                       feedback.sendMessage(ColorUtil.colorize(createdMsg));
                                    }
                                 }
                              }
                           );
                     } else {
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                           this.plugin.getLogger().warning("Не удалось загрузить схематику шахты: " + (throwable != null ? throwable.getMessage() : "unknown"));
                           if (feedback != null) {
                              feedback.sendMessage(ChatColor.RED + "Не удалось загрузить схематику шахты!");
                           }
                        });
                     }
                  }
               );
            }
         }
      }
   }

   private boolean pasteClipboardSync(Clipboard clipboard, Location location) {
      try {
         ClipboardHolder holder = new ClipboardHolder(clipboard);
         this.editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(location.getWorld()), -1);
         BlockVector3 to = BlockVector3.at(location.getX(), location.getY(), location.getZ());
         Operation operation = holder.createPaste(this.editSession).to(to).ignoreAirBlocks(true).build();
         Operations.complete(operation);
         this.editSession.flushSession();
         return true;
      } catch (Exception var6) {
         this.plugin.getLogger().warning("Ошибка при вставке схематики: " + var6.getMessage());
         return false;
      }
   }

   public void removeMine(CommandSender feedback) {
      if (this.state == DeathMineState.NOT_SPAWNED) {
         if (feedback != null) {
            String notStarted = this.plugin.getConfig().getString("messages.not_started", "");
            feedback.sendMessage(ColorUtil.colorize(notStarted));
         }
      } else {
         this.broadcastTopMined();
         if (this.mineLocation != null) {
            this.regionService.removeRegion(this.mineLocation);
         }

         if (this.editSession != null) {
            try {
               this.editSession.undo(this.editSession);
            } catch (Throwable var6) {
            }
         }

         if (this.bossBar != null) {
            this.bossBar.removeAll();
            this.bossBar = null;
         }

         for (BukkitTask task : this.particleTasks) {
            try {
               task.cancel();
            } catch (Throwable var5) {
            }
         }

         this.particleTasks.clear();
         this.activeCubes.clear();
         if (this.timerTask != null && !this.timerTask.isCancelled()) {
            this.timerTask.cancel();
         }

         this.playersInRange.clear();
         EventManager.getInstance().events.remove("death_mine");
         this.state = DeathMineState.NOT_SPAWNED;
         this.mineLocation = null;
         this.timeLeftSeconds = 1200;
         this.playerMinedBlocks.clear();
         this.resourceLocations.clear();
         this.resourceInitialCount = 0;
         this.resourceRemainingCount = 0;
         if (feedback != null) {
            String removedMsg = this.plugin.getConfig().getString("messages.removed", "");
            feedback.sendMessage(ColorUtil.colorize(removedMsg));
         }
      }
   }

   public void setTimerSeconds(int seconds, CommandSender feedback) {
      if (this.state == DeathMineState.NOT_SPAWNED) {
         if (feedback != null) {
            feedback.sendMessage(ChatColor.RED + "Шахта не существует!");
         }
      } else {
         this.timeLeftSeconds = Math.max(0, seconds);
         this.updateBossBar();
         if (feedback != null) {
            int minutes = this.timeLeftSeconds / 60;
            int sec = this.timeLeftSeconds % 60;
            String template = this.plugin.getConfig().getString("messages.timer_set", "");
            template = template.replace("%minutes%", String.valueOf(minutes)).replace("%seconds%", String.valueOf(sec));
            feedback.sendMessage(ColorUtil.colorize(template));
         }
      }
   }

   public void teleportToMine(Player player) {
      if (this.state != DeathMineState.SPAWNED || this.mineLocation == null) {
         String notStarted = this.plugin.getConfig().getString("messages.not_started", "");
         player.sendMessage(ColorUtil.colorize(notStarted));
      } else if (!player.hasPermission("deathmine.tp")) {
         player.sendMessage(ChatColor.RED + "У вас нет прав на телепортацию!");
      } else {
         Location tpLocation = this.mineLocation.clone().add(0.5, 2.0, 0.5);
         tpLocation.setYaw(player.getLocation().getYaw());
         tpLocation.setPitch(player.getLocation().getPitch());
         player.teleport(tpLocation);
         String tpMsg = this.plugin.getConfig().getString("messages.teleported", "");
         player.sendMessage(ColorUtil.colorize(tpMsg));
      }
   }

   public boolean isInsideAnyCube(Location location) {
      for (Location cubeCenter : this.activeCubes) {
         if (this.isInCube(location, cubeCenter, 50)) {
            return true;
         }
      }

      return false;
   }

   public boolean isAllowedMineBlock(Material material) {
      return material == Material.OBSIDIAN
         || material == Material.CRYING_OBSIDIAN
         || material == Material.ANCIENT_DEBRIS
         || material == Material.NETHERITE_BLOCK;
   }

   public boolean isBlockInMineArea(Location blockLoc) {
      if (this.state == DeathMineState.SPAWNED && this.mineLocation != null) {
         return !blockLoc.getWorld().equals(this.mineLocation.getWorld())
            ? false
            : blockLoc.distanceSquared(this.mineLocation) <= this.SCAN_RADIUS * this.SCAN_RADIUS;
      } else {
         return false;
      }
   }

   public void onTickBossBarVisibility() {
      if (this.state == DeathMineState.SPAWNED && this.bossBar != null && this.mineLocation != null) {
         Set<Player> currentPlayersInRange = new HashSet<>();
         double radiusSquared = this.BOSSBAR_VISIBLE_RADIUS * this.BOSSBAR_VISIBLE_RADIUS;

         for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(this.mineLocation.getWorld())) {
               if (player.getLocation().distanceSquared(this.mineLocation) <= radiusSquared) {
                  currentPlayersInRange.add(player);
                  if (!this.playersInRange.contains(player)) {
                     this.bossBar.addPlayer(player);
                  }
               } else if (this.playersInRange.contains(player)) {
                  this.bossBar.removePlayer(player);
               }
            }
         }

         this.playersInRange.removeIf(p -> {
            if (!currentPlayersInRange.contains(p)) {
               this.bossBar.removePlayer(p);
               return true;
            } else {
               return false;
            }
         });
         this.playersInRange.addAll(currentPlayersInRange);
      }
   }

   private void createBossBar() {
      if (this.bossBar != null) {
         this.bossBar.removeAll();
      }

      this.bossBar = Bukkit.createBossBar(ColorUtil.colorize("&fДо исчезновения &6\"Смертельной шахты\""), BarColor.PINK, BarStyle.SOLID, new BarFlag[0]);
      this.bossBar.setVisible(true);
      this.updateBossBar();
   }

   private void startTimer() {
      if (this.timerTask != null && !this.timerTask.isCancelled()) {
         this.timerTask.cancel();
      }

      this.timerTask = new MineTimerTask(this).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void onTimerTick() {
      if (this.timeLeftSeconds <= 0) {
         this.removeMine(null);
      } else {
         this.timeLeftSeconds--;
         this.updateBossBar();
         if (EventManager.getInstance().events.containsKey("death_mine") && this.mineLocation != null) {
            Event event = (Event)EventManager.getInstance().events.get("death_mine");
            event.message = this.buildEventMessage();
         }
      }
   }

   private String buildEventMessage() {
      long elapsedSec = 0L;
      if (this.startTimeMillis > 0L) {
         elapsedSec = (System.currentTimeMillis() - this.startTimeMillis) / 1000L;
      }

      return ColorUtil.colorize(
         this.color
            + "▶ "
            + this.displayName
            + " &fна координатах "
            + this.color
            + this.mineLocation.getBlockX()
            + " "
            + this.mineLocation.getBlockY()
            + " "
            + this.mineLocation.getBlockZ()
            + " &fуже "
            + this.color
            + String.format("%02d сек.", elapsedSec)
      );
   }

   private void updateBossBar() {
      if (this.bossBar != null) {
         int minutes = this.timeLeftSeconds / 60;
         int seconds = this.timeLeftSeconds % 60;
         double timeProgress = Math.max(0.0, Math.min(1.0, (double)this.timeLeftSeconds / Math.max(1, this.totalDurationSeconds)));
         int percent = this.computeResourcePercent();
         String title = ColorUtil.colorize(
            "&fДо исчезновения &6\"Смертельной шахты\"&f: " + String.format("%02d:%02d", minutes, seconds) + " &f( " + percent + "% &f)"
         );
         this.bossBar.setTitle(title);
         this.bossBar.setProgress(timeProgress);
      }
   }

   public void registerResourceBlock(Location loc) {
      if (loc != null) {
         this.resourceLocations.add(loc);
         this.resourceInitialCount = this.resourceLocations.size();
         this.resourceRemainingCount = this.resourceInitialCount;
      }
   }

   public void onResourceBlockBroken(Location loc) {
      if (loc != null) {
         if (this.resourceLocations.remove(loc)) {
            this.resourceRemainingCount = Math.max(0, this.resourceRemainingCount - 1);
         }
      }
   }

   public boolean isTrackedResourceBlock(Location loc) {
      return this.resourceLocations.contains(loc);
   }

   public void incrementMined(String playerName) {
      if (playerName != null && !playerName.isEmpty()) {
         this.playerMinedBlocks.merge(playerName, 1, Integer::sum);
      }
   }

   private void broadcastTopMined() {
      if (!this.playerMinedBlocks.isEmpty()) {
         Bukkit.broadcastMessage(
            ChatColor.translateAlternateColorCodes(
               '&',
               "&x&E&7&F&F&0&0▶ &fПоздровляем участников мероприятия \"Смертельная шахта\", которые добыли наибольшее количество блоков, среди всех игроков сервера:"
            )
         );
         this.playerMinedBlocks
            .entrySet()
            .stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(5L)
            .forEachOrdered(new Consumer<Entry<String, Integer>>() {
               int rank = 1;

               public void accept(Entry<String, Integer> e) {
                  String line = String.format("&x&2&2&F&F&0&0%d. &f%s — &x&E&7&F&F&0&0%d блоков.", this.rank, e.getKey(), e.getValue());
                  Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', line));
                  this.rank++;
               }
            });
      }
   }

   private int computeResourcePercent() {
      if (this.resourceInitialCount <= 0) {
         return 0;
      } else {
         double p = this.resourceRemainingCount * 100.0 / this.resourceInitialCount;
         return (int)Math.round(p);
      }
   }

   private void createCubeParticles(Location center, int size, long durationSeconds) {
      this.activeCubes.add(center);
      BukkitTask task = new ParticleCubeTask(this, center, size, durationSeconds).runTaskTimer(this.plugin, 0L, 5L);
      this.particleTasks.add(task);
   }

   private boolean isInCube(Location location, Location center, int size) {
      double half = size / 2.0;
      double minX = center.getX() - half;
      double minY = center.getY() - half;
      double minZ = center.getZ() - half;
      double maxX = center.getX() + half;
      double maxY = center.getY() + half;
      double maxZ = center.getZ() + half;
      return location.getX() >= minX
         && location.getX() <= maxX
         && location.getY() >= minY
         && location.getY() <= maxY
         && location.getZ() >= minZ
         && location.getZ() <= maxZ;
   }

   private Location findValidLocation(World world) {
      for (int i = 0; i < this.MAX_SPAWN_ATTEMPTS; i++) {
         int x = this.random.nextInt(5001) - 2500;
         int z = this.random.nextInt(5001) - 2500;
         int y = world.getHighestBlockYAt(x, z);
         Location loc = new Location(world, x, y, z);
         Block groundBlock = loc.getBlock().getRelative(0, -1, 0);
         Material type = groundBlock.getType();
         if (type.isSolid() && !groundBlock.isLiquid()) {
            String name = type.toString();
            if (!name.contains("WATER") && !name.contains("LAVA") && this.isAreaValid(loc, 10)) {
               return loc;
            }
         }
      }

      return null;
   }

   private boolean isAreaValid(Location center, int radius) {
      World world = center.getWorld();
      int baseY = center.getBlockY();

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            Block block = world.getBlockAt(center.getBlockX() + x, baseY, center.getBlockZ() + z);
            Material type = block.getType();
            if (block.isLiquid()) {
               return false;
            }

            String name = type.toString();
            if (name.contains("WATER") || name.contains("LAVA")) {
               return false;
            }
         }
      }

      return true;
   }

   @Generated
   public JavaPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public List<Location> getActiveCubes() {
      return this.activeCubes;
   }

   @Generated
   public LootService getLootService() {
      return this.lootService;
   }

   @Generated
   public DeathMineState getState() {
      return this.state;
   }
}
