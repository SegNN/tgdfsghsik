package ru.kotikstasika.kotikdang.dang.types;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.DangType;
import ru.kotikstasika.kotikdang.model.GriefDangData;
import ru.kotikstasika.kotikdang.repository.GriefDangRepository;
import ru.kotikstasika.kotikdang.util.WorldEditUtils;
import ru.kotikstasika.kotikdang.util.WorldGuardUtils;

public class DangGrief implements Listener {
   private final KotikDang plugin;
   private final LootAPI lootAPI;
   private final WorldEditUtils worldEditUtils;
   private final WorldGuardUtils worldGuardUtils;
   private final Random random = new Random();
   private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
   private Connection connection;
   private final Map<String, GriefDangData> loadedDangs = new ConcurrentHashMap<>();
   private final Queue<Runnable> databaseQueue = new LinkedList<>();
   private boolean isProcessingQueue = false;
   private final GriefDangRepository repository = new GriefDangRepository();

   public DangGrief(KotikDang plugin) {
      this.plugin = plugin;
      this.lootAPI = new LootAPI();
      this.worldEditUtils = new WorldEditUtils();
      this.worldGuardUtils = new WorldGuardUtils();

      try {
         this.initializeDatabase();
         this.loadAllDangs();
      } catch (SQLException var3) {
         plugin.getLogger().severe("Ошибка инициализации базы данных: " + var3.getMessage());
         var3.printStackTrace();
      }

      Bukkit.getScheduler().runTaskTimer(plugin, this::processDatabaseQueue, 1L, 1L);
   }

   private void processDatabaseQueue() {
      if (!this.isProcessingQueue && !this.databaseQueue.isEmpty()) {
         this.isProcessingQueue = true;

         try {
            Runnable task = this.databaseQueue.poll();
            if (task != null) {
               task.run();
            }
         } finally {
            this.isProcessingQueue = false;
         }
      }
   }

   private void addDatabaseTask(Runnable task) {
      this.databaseQueue.add(task);
   }

   private void initializeDatabase() throws SQLException {
      File dbFile = new File(this.plugin.getDataFolder(), "dangs.db");
      if (!dbFile.exists()) {
         dbFile.getParentFile().mkdirs();

         try {
            dbFile.createNewFile();
         } catch (IOException var4) {
            throw new SQLException("Не удалось создать файл базы данных", var4);
         }
      }

      try {
         Class.forName("org.sqlite.JDBC");
      } catch (ClassNotFoundException var3) {
         throw new RuntimeException(var3);
      }

      this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
      this.connection.setAutoCommit(false);
      this.repository.initSchema(this.connection);
      this.connection.commit();
   }

   private void removeDang(String dangId) {
      this.addDatabaseTask(() -> {
         try {
            this.repository.deleteDang(this.connection, dangId);
            this.connection.commit();
            this.loadedDangs.remove(dangId);
         } catch (SQLException var5) {
            this.plugin.getLogger().severe("Ошибка удаления данжа: " + var5.getMessage());

            try {
               this.connection.rollback();
            } catch (SQLException var4) {
               this.plugin.getLogger().severe("Ошибка отката транзакции: " + var4.getMessage());
            }
         }
      });
   }

   private void saveDang(GriefDangData dangData) {
      this.addDatabaseTask(() -> {
         try {
            this.repository.saveDang(this.connection, dangData);
            this.connection.commit();
         } catch (SQLException var5) {
            this.plugin.getLogger().severe("Ошибка сохранения данжа: " + var5.getMessage());

            try {
               this.connection.rollback();
            } catch (SQLException var4) {
               this.plugin.getLogger().severe("Ошибка отката транзакции: " + var4.getMessage());
            }
         }
      });
   }

   private GriefDangData loadDang(String dangId) {
      try {
         return this.repository.loadDang(this.connection, dangId);
      } catch (SQLException var3) {
         this.plugin.getLogger().severe("Ошибка загрузки данжа: " + var3.getMessage());
         return null;
      }
   }

   public boolean spawnGriefDang(Location location, boolean spawnAtPlayer, String dangName) {
      try {
         ConfigurationSection griefConfig = this.plugin.getConfig().getConfigurationSection("dangs.grief");
         if (griefConfig != null && griefConfig.contains(dangName)) {
            ConfigurationSection dangConfig = griefConfig.getConfigurationSection(dangName);
            if (dangConfig == null) {
               this.plugin.getLogger().warning("Конфигурация для данжа '" + dangName + "' не найдена!");
               return false;
            } else {
               String schemName = dangConfig.getString("schematic");
               if (schemName != null && !schemName.isEmpty()) {
                  int lootLevel = dangConfig.getInt("lootlvl", 3);
                  int griefLevel = dangConfig.getInt("lvlgrief", 1);
                  DangType type = this.getDangTypeByLevel(griefLevel);
                  String dangId = "dang_" + System.currentTimeMillis();
                  String backupName = dangId + ".schem";
                  File backup = this.worldEditUtils.makeRegionBackup(location, 35, backupName);
                  if (!this.worldEditUtils.pasteSchematic(location, schemName)) {
                     this.plugin.getLogger().warning("Не удалось вставить схематику: " + schemName);
                     return false;
                  } else {
                     this.asyncRemoveBarrier(location, 100);
                     this.asyncRemovePiston(location, 100);
                     this.worldGuardUtils.createRegion("kotikdang_grief_" + dangId, location, type);
                     List<Location> chests = this.scanChests(location);
                     Location anchorLoc = this.findAnchorLocation(location, type.getAnchorType());
                     GriefDangData dangData = new GriefDangData(
                        dangId,
                        location,
                        anchorLoc,
                        false,
                        false,
                        griefLevel,
                        lootLevel,
                        dangName,
                        new HashMap<>(),
                        backup != null ? backup.getAbsolutePath() : null,
                        35
                     );

                     for (Location chestLoc : chests) {
                        dangData.getChests().put(this.locationToString(chestLoc), false);
                     }

                     this.saveDang(dangData);
                     this.loadedDangs.put(dangId, dangData);
                     return true;
                  }
               } else {
                  this.plugin.getLogger().warning("Не указано имя схематики для данжа '" + dangName + "'");
                  return false;
               }
            }
         } else {
            this.plugin.getLogger().warning("Грифферский данж '" + dangName + "' не найден в конфиге!");
            return false;
         }
      } catch (Exception var18) {
         this.plugin.getLogger().severe("Ошибка создания грифферского данжа: " + var18.getMessage());
         var18.printStackTrace();
         return false;
      }
   }

   private String locationToString(Location loc) {
      return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
   }

   private Location findAnchorLocation(Location center, Material anchorType) {
      World world = center.getWorld();
      int radius = 50;

      for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
         for (int y = 0; y < world.getMaxHeight(); y++) {
            for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
               Block block = world.getBlockAt(x, y, z);
               if (block.getType() == anchorType) {
                  return block.getLocation();
               }
            }
         }
      }

      return null;
   }

   private List<Location> scanChests(Location center) {
      World world = center.getWorld();
      List<Location> chests = new ArrayList<>();
      int radius = 50;

      for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
         for (int y = 0; y <= 256; y++) {
            for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
               Block block = world.getBlockAt(x, y, z);
               if (block.getState() instanceof Chest) {
                  chests.add(block.getLocation());
               }
            }
         }
      }

      return chests;
   }

   @EventHandler
   public void onChestOpen(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block clickedBlock = event.getClickedBlock();
         if (clickedBlock.getState() instanceof Chest) {
            Location chestLoc = clickedBlock.getLocation();
            String chestLocString = this.locationToString(chestLoc);
            GriefDangData dangData = this.findDangByChest(chestLoc);
            if (dangData != null) {
               if (!dangData.isAnchorDestroyed() && this.hasAnchorOrLodestoneBlocks(dangData.getLocation())) {
                  event.setCancelled(true);
                  DangType type = this.getDangTypeByLevel(dangData.getLevel());
                  String blockName = type.getAnchorType() == Material.LODESTONE ? "Магнетит" : "Якорь возрождения";
                  event.getPlayer()
                     .sendMessage(
                        ChatColor.translateAlternateColorCodes(
                           '&',
                           "&6▶ &fЧтобы &6заполучить доступ &fк сундуку с ценными ресурсами, &6Вы должны загрифферить блок-привет ("
                              + blockName
                              + ") &fсокровища."
                        )
                     );
               } else {
                  Map<String, Boolean> chests = dangData.getChests();
                  if (chests.containsKey(chestLocString)) {
                     Boolean isChestActivated = chests.get(chestLocString);
                     if (!isChestActivated) {
                        if (dangData.isAnchorDestroyed() && !dangData.isUnlocked()) {
                           this.unlockDang(dangData);
                        }

                        this.databaseExecutor.execute(() -> this.updateChestInDatabase(dangData.getId(), chestLocString));
                        chests.remove(chestLocString);
                        Chest chest = (Chest)clickedBlock.getState();
                        this.fillChest(chest, dangData.getLootLevel());
                        this.spawnFirework(chestLoc.clone().add(0.5, 0.5, 0.5));
                        if (this.isDangCompleted(dangData)) {
                           this.completeAndRemoveDang(dangData);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private GriefDangData findDangByChest(Location chestLoc) {
      for (GriefDangData dangData : new ArrayList<>(this.loadedDangs.values())) {
         Location dangLoc = dangData.getLocation();
         if (dangLoc != null && this.isInRegion(chestLoc, dangLoc)) {
            return dangData;
         }
      }

      return null;
   }

   private void updateChestInDatabase(String dangId, String chestLocString) {
      try {
         String sql = "DELETE FROM grief_chests WHERE dang_id = ? AND location = ?";

         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, dangId);
            stmt.setString(2, chestLocString);
            stmt.executeUpdate();
            this.connection.commit();
         }
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка удаления сундука из БД: " + var10.getMessage());

         try {
            this.connection.rollback();
         } catch (SQLException var7) {
            this.plugin.getLogger().severe("Ошибка отката транзакции: " + var7.getMessage());
         }
      }
   }

   public boolean isDatabaseConnected() {
      try {
         return this.connection != null && !this.connection.isClosed();
      } catch (SQLException var2) {
         return false;
      }
   }

   private boolean isDangCompleted(GriefDangData dangData) {
      if (!dangData.isAnchorDestroyed()) {
         return false;
      } else {
         for (Boolean opened : dangData.getChests().values()) {
            if (!opened) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean hasAnchorOrLodestoneBlocks(Location center) {
      if (center == null) {
         return false;
      } else {
         World world = center.getWorld();
         int radius = 30;

         for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
               for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
                  Block block = world.getBlockAt(x, y, z);
                  Material type = block.getType();
                  if (type == Material.LODESTONE || type == Material.RESPAWN_ANCHOR) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public boolean hasAnchorInRegion(Location center) {
      return this.hasAnchorOrLodestoneBlocks(center);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      event.blockList().removeIf(block -> block.getState() instanceof Chest && this.isProtectedChest(block));
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockExplode(BlockExplodeEvent event) {
      event.blockList().removeIf(block -> block.getState() instanceof Chest && this.isProtectedChest(block));
   }

   private boolean isProtectedChest(Block block) {
      if (!(block.getState() instanceof Chest)) {
         return false;
      } else {
         Location chestLoc = block.getLocation();
         String chestLocString = this.locationToString(chestLoc);

         for (GriefDangData dangData : this.loadedDangs.values()) {
            Location dangLoc = dangData.getLocation();
            if (dangLoc != null && this.isInRegion(chestLoc, dangLoc)) {
               if (!dangData.isUnlocked()) {
                  return true;
               }

               Map<String, Boolean> chests = dangData.getChests();
               return chests.containsKey(chestLocString) && !chests.get(chestLocString);
            }
         }

         return false;
      }
   }

   private boolean isInRegion(Location loc, Location center) {
      if (loc != null && center != null) {
         int radius = 25;
         return loc.getWorld().equals(center.getWorld())
            && loc.getX() >= center.getX() - radius
            && loc.getX() <= center.getX() + radius
            && loc.getZ() >= center.getZ() - radius
            && loc.getZ() <= center.getZ() + radius;
      } else {
         return false;
      }
   }

   private void spawnFirework(Location loc) {
      Firework firework = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
      firework.setSilent(true);
      FireworkMeta meta = firework.getFireworkMeta();
      List<Color> rainbowColors = Arrays.asList(
         Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.fromRGB(75, 0, 130), Color.fromRGB(238, 130, 238)
      );
      FireworkEffect effect = FireworkEffect.builder().with(Type.BURST).withColor(rainbowColors).withFade(Color.WHITE).withFlicker().withTrail().build();
      meta.addEffect(effect);
      meta.setPower(0);
      firework.setFireworkMeta(meta);
      BukkitScheduler scheduler = Bukkit.getScheduler();
      scheduler.runTaskLater(this.plugin, firework::detonate, 1L);
   }

   private void unlockDang(GriefDangData dangData) {
      try {
         Location dangLoc = dangData.getLocation();
         if (dangLoc == null) {
            return;
         }

         ProtectedRegion region = WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .get(BukkitAdapter.adapt(dangLoc.getWorld()))
            .getRegion("kotikdang_grief_" + dangData.getId());
         if (region instanceof ProtectedCuboidRegion) {
            this.worldGuardUtils.setFlagsGriefKill((ProtectedCuboidRegion)region);
         }

         dangData.setUnlocked(true);
         Map<String, Boolean> chests = dangData.getChests();

         for (String chestLoc : chests.keySet()) {
            chests.put(chestLoc, false);
         }

         this.saveDang(dangData);
      } catch (Exception var7) {
         this.plugin.getLogger().warning("Ошибка при разблокировке данжа " + dangData.getId() + ": " + var7.getMessage());
      }
   }

   private void fillChest(Chest chest, int lootLevel) {
      chest.getInventory().clear();
      int itemCount = 5 + this.random.nextInt(10);

      for (ItemStack item : this.lootAPI.getRandomItems(itemCount, lootLevel)) {
         if (item != null) {
            int attempts = 0;

            int slot;
            do {
               slot = this.random.nextInt(chest.getInventory().getSize());
            } while (chest.getInventory().getItem(slot) != null && ++attempts < 10);

            if (attempts < 10) {
               chest.getInventory().setItem(slot, item);
            }
         }
      }
   }

   public void loadAllDangs() throws SQLException {
      for (final String dangId : this.repository.loadAllIds(this.connection)) {
         final GriefDangData dangData = this.loadDang(dangId);
         if (dangData != null) {
            this.loadedDangs.put(dangId, dangData);
            (new BukkitRunnable() {
               public void run() {
                  DangType type = DangGrief.this.getDangTypeByLevel(dangData.getLevel());
                  DangGrief.this.worldGuardUtils.createRegion("kotikdang_grief_" + dangId, dangData.getLocation(), type);
                  if (dangData.isUnlocked()) {
                     DangGrief.this.unlockDang(dangData);
                  }
               }
            }).runTask(this.plugin);
         }
      }

      this.connection.commit();
   }

   private DangType getDangTypeByLevel(int level) {
      return switch (level) {
         case 1 -> DangType.LEVEL_1_GRIEF;
         case 2 -> DangType.LEVEL_2_GRIEF;
         case 3 -> DangType.LEVEL_3_GRIEF;
         case 4 -> DangType.LEVEL_4_GRIEF;
         case 5 -> DangType.LEVEL_5_GRIEF;
         default -> DangType.LEVEL_2_GRIEF;
      };
   }

   private void asyncRemoveBarrier(final Location center, final int radius) {
      if (center != null && center.getWorld() != null) {
         final World world = center.getWorld();
         (new BukkitRunnable() {
            private int currentX = center.getBlockX() - radius;
            private final int maxX = center.getBlockX() + radius;
            private int currentZ = center.getBlockZ() - radius;
            private final int maxZ = center.getBlockZ() + radius;
            private static final int BATCH_SIZE = 150;
            private int processedInBatch = 0;

            public void run() {
               while (this.currentX <= this.maxX) {
                  for (; this.currentZ <= this.maxZ; this.currentZ++) {
                     if (world.isChunkLoaded(this.currentX >> 4, this.currentZ >> 4)) {
                        Chunk chunk = world.getChunkAt(this.currentX >> 4, this.currentZ >> 4);

                        for (int y = 0; y <= 255; y++) {
                           Block block = chunk.getBlock(this.currentX & 15, y, this.currentZ & 15);
                           if (block.getType() == Material.BARRIER) {
                              block.setType(Material.AIR, false);
                              this.processedInBatch++;
                              if (this.processedInBatch >= 150) {
                                 this.processedInBatch = 0;
                                 return;
                              }
                           }
                        }

                        this.processedInBatch = 0;
                     }
                  }

                  this.currentZ = center.getBlockZ() - radius;
                  this.currentX++;
               }

               this.cancel();
            }
         }).runTaskTimer(this.plugin, 1L, 1L);
      }
   }

   private void asyncRemovePiston(final Location center, final int radius) {
      if (center != null && center.getWorld() != null) {
         final World world = center.getWorld();
         (new BukkitRunnable() {
            private int currentX = center.getBlockX() - radius;
            private final int maxX = center.getBlockX() + radius;
            private int currentZ = center.getBlockZ() - radius;
            private final int maxZ = center.getBlockZ() + radius;
            private static final int BATCH_SIZE = 150;
            private int processedInBatch = 0;

            public void run() {
               while (this.currentX <= this.maxX) {
                  for (; this.currentZ <= this.maxZ; this.currentZ++) {
                     if (world.isChunkLoaded(this.currentX >> 4, this.currentZ >> 4)) {
                        Chunk chunk = world.getChunkAt(this.currentX >> 4, this.currentZ >> 4);

                        for (int y = 0; y <= 255; y++) {
                           Block block = chunk.getBlock(this.currentX & 15, y, this.currentZ & 15);
                           Material blockType = block.getType();
                           if (blockType.name().endsWith("PISTON")) {
                              block.setType(Material.AIR, false);
                              this.processedInBatch++;
                              if (this.processedInBatch >= 150) {
                                 this.processedInBatch = 0;
                                 return;
                              }
                           }
                        }

                        this.processedInBatch = 0;
                     }
                  }

                  this.currentZ = center.getBlockZ() - radius;
                  this.currentX++;
               }

               this.cancel();
            }
         }).runTaskTimer(this.plugin, 1L, 1L);
      }
   }

   private void completeAndRemoveDang(GriefDangData dangData) {
      this.deleteDangAndRegion(dangData.getId());
   }

   public GriefDangData getDangData(String id) {
      return this.loadedDangs.get(id);
   }

   public boolean restoreDang(GriefDangData data) {
      if (data == null) {
         return false;
      } else {
         Location loc = data.getLocation();
         if (loc == null) {
            return false;
         } else {
            boolean ok = true;

            try {
               this.worldGuardUtils.removeRegion("kotikdang_grief_" + data.getId(), loc);
               if (data.getBackupPath() != null) {
                  ok = this.worldEditUtils.restoreFromBackup(loc, data.getRegionRadius(), data.getBackupPath());
               }
            } catch (Exception var7) {
            }

            try {
               this.removeDang(data.getId());
            } catch (Exception var6) {
            }

            this.loadedDangs.remove(data.getId());

            try {
               if (data.getBackupPath() != null) {
                  File backupFile = new File(data.getBackupPath());
                  if (backupFile.exists()) {
                     backupFile.delete();
                  }
               }
            } catch (Exception var5) {
            }

            return ok;
         }
      }
   }

   public boolean restoreDangById(String id) {
      return this.restoreDang(this.getDangData(id));
   }

   public int removeGrifAlready() {
      int removedCount = 0;

      for (GriefDangData data : new ArrayList<>(this.loadedDangs.values())) {
         if (data != null && data.getLocation() != null && !this.hasAnchorOrLodestoneBlocks(data.getLocation())) {
            try {
               this.worldGuardUtils.removeRegion("kotikdang_grief_" + data.getId(), data.getLocation());
            } catch (Exception var8) {
            }

            try {
               if (data.getBackupPath() != null) {
                  this.worldEditUtils.restoreFromBackup(data.getLocation(), data.getRegionRadius(), data.getBackupPath());
               }
            } catch (Exception var7) {
            }

            try {
               this.removeDang(data.getId());
            } catch (Exception var6) {
            }

            this.loadedDangs.remove(data.getId());

            try {
               if (data.getBackupPath() != null) {
                  File backupFile = new File(data.getBackupPath());
                  if (backupFile.exists()) {
                     backupFile.delete();
                  }
               }
            } catch (Exception var5) {
            }

            removedCount++;
         }
      }

      return removedCount;
   }

   public Set<String> getLoadedDangIds() {
      return new HashSet<>(this.loadedDangs.keySet());
   }

   public Location getDangLocation(String id) {
      GriefDangData data = this.loadedDangs.get(id);
      return data != null ? data.getLocation() : null;
   }

   public String findNearestDangId(Location from) {
      if (from == null) {
         return null;
      } else {
         double best = Double.MAX_VALUE;
         String bestId = null;

         for (Entry<String, GriefDangData> entry : this.loadedDangs.entrySet()) {
            Location dangLoc = entry.getValue().getLocation();
            if (dangLoc != null && dangLoc.getWorld().equals(from.getWorld())) {
               double distance = dangLoc.distanceSquared(from);
               if (distance < best) {
                  best = distance;
                  bestId = entry.getKey();
               }
            }
         }

         return bestId;
      }
   }

   public List<String> getUnraidedDangIds(World world) {
      List<String> ids = new ArrayList<>();

      for (Entry<String, GriefDangData> entry : this.loadedDangs.entrySet()) {
         GriefDangData data = entry.getValue();
         if (data.getLocation() != null
            && (world == null || world.equals(data.getLocation().getWorld()))
            && this.hasAnchorOrLodestoneBlocks(data.getLocation())) {
            ids.add(entry.getKey());
         }
      }

      return ids;
   }

   public String getRandomUnraidedDangId(World world, Random randomGenerator) {
      List<String> ids = this.getUnraidedDangIds(world);
      return ids.isEmpty() ? null : ids.get(randomGenerator.nextInt(ids.size()));
   }

   public void deleteDangAndRegion(String id) {
      GriefDangData data = this.loadedDangs.remove(id);
      Location loc = data != null ? data.getLocation() : null;
      if (loc != null) {
         Bukkit.getScheduler().runTask(this.plugin, () -> this.worldGuardUtils.removeRegion("kotikdang_grief_" + id, loc));
      }

      this.removeDang(id);
   }

   public void deleteAllDangsAndRegions() {
      for (String id : new HashSet<>(this.loadedDangs.keySet())) {
         this.deleteDangAndRegion(id);
      }
   }
}
