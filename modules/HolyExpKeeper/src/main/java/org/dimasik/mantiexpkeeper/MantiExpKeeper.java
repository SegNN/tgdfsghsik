package org.dimasik.mantiexpkeeper;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.mantiexpkeeper.database.DatabaseManager;
import org.dimasik.mantiexpkeeper.database.FileDatabaseManager;
import org.dimasik.mantiexpkeeper.database.MySQLDatabaseManager;

public final class MantiExpKeeper extends JavaPlugin {
   private DatabaseManager dbManager;
   private Executor databaseExecutor;
   public MantiExpKeeperListener mantiExpKeeperListener;
   public MantiExpKeeperCommand mantiExpKeeperCommand;
   public MantiExpItemUtil mantiExpItemUtil;
   public String effectsTitle = "Эффекты на 24 часа";
   public String itemsTitle = "Предметы Хранителя опыта";
   public String effectsAbilities = "Эффекты Хранителя опыта";
   public YamlConfiguration config;
   public static boolean allowForAll = false;
   public static String[] alwaysEffects = new String[0];

   public MantiExpKeeper() {
   }

   public void onEnable() {
      if (!this.getDataFolder().exists()) {
         this.getDataFolder().mkdirs();
      }

      this.saveDefaultConfig();
      this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
      this.mantiExpItemUtil = new MantiExpItemUtil(this);
      this.mantiExpKeeperListener = new MantiExpKeeperListener(this);
      this.getServer().getPluginManager().registerEvents(this.mantiExpKeeperListener, this);
      this.getServer().getPluginManager().registerEvents(this.mantiExpItemUtil, this);
      this.mantiExpKeeperCommand = new MantiExpKeeperCommand(this);
      this.getCommand("holyexpkeeper").setExecutor(this.mantiExpKeeperCommand);
      this.getCommand("expkeeper").setExecutor(this.mantiExpKeeperCommand);
      this.databaseExecutor = Executors.newFixedThreadPool(5);
      String dbType = this.config.getString("database.type", "file").toLowerCase();
      if (dbType.equals("mysql")) {
         this.dbManager = new MySQLDatabaseManager(this.config, this.getLogger());
      } else {
         this.dbManager = new FileDatabaseManager(this.getDataFolder(), this.config, this.getLogger());
      }

      try {
         this.dbManager.init();
      } catch (Exception var3) {
         this.getLogger().severe("Ошибка при инициализации базы данных: " + var3.getMessage());
         var3.printStackTrace();
      }
   }

   public void onDisable() {
      if (this.dbManager != null) {
         this.dbManager.close();
      }
   }

   public CompletableFuture<Void> addTime(String player, String ability, int hours) {
      long newTime = System.currentTimeMillis() + hours * 3600 * 1000;
      return this.dbManager
         .cleanExpiredAbilities(this.databaseExecutor)
         .thenComposeAsync(v -> this.dbManager.addTime(this.databaseExecutor, player, ability, newTime), this.databaseExecutor)
         .thenRunAsync(() -> {
            Player target = Bukkit.getPlayer(player);
            if (target != null) {
               Bukkit.getScheduler().runTask(this, () -> this.mantiExpKeeperListener.checkEffects(target));
            }
         }, this.databaseExecutor);
   }

   public CompletableFuture<Void> addInf(String player, String ability) {
      return this.dbManager
         .cleanExpiredAbilities(this.databaseExecutor)
         .thenComposeAsync(v -> this.dbManager.addInf(this.databaseExecutor, player, ability), this.databaseExecutor)
         .thenRunAsync(() -> {
            Player target = Bukkit.getPlayer(player);
            if (target != null) {
               Bukkit.getScheduler().runTask(this, () -> this.mantiExpKeeperListener.checkEffects(target));
            }
         }, this.databaseExecutor);
   }

   public CompletableFuture<Void> removePlayer(String player, String ability) {
      return this.dbManager
         .cleanExpiredAbilities(this.databaseExecutor)
         .thenComposeAsync(v -> this.dbManager.removePlayer(this.databaseExecutor, player, ability), this.databaseExecutor)
         .thenRunAsync(() -> {
            Player target = Bukkit.getPlayer(player);
            if (target != null) {
               Bukkit.getScheduler().runTask(this, () -> this.mantiExpKeeperListener.checkEffects(target));
            }
         }, this.databaseExecutor);
   }

   public CompletableFuture<Boolean> hasAbility(String player, String ability) {
      if (allowForAll) {
         return CompletableFuture.completedFuture(true);
      } else {
         return Arrays.stream(alwaysEffects).anyMatch(ability::equalsIgnoreCase)
            ? CompletableFuture.completedFuture(true)
            : this.dbManager
               .cleanExpiredAbilities(this.databaseExecutor)
               .thenComposeAsync(v -> this.dbManager.hasAbility(this.databaseExecutor, player, ability), this.databaseExecutor);
      }
   }

   public CompletableFuture<String> getAbilityTime(String player, String ability) {
      if (allowForAll) {
         return CompletableFuture.completedFuture("**.**.****  **:**:**");
      } else {
         return Arrays.stream(alwaysEffects).anyMatch(ability::equalsIgnoreCase)
            ? CompletableFuture.completedFuture("**.**.****  **:**:**")
            : this.dbManager
               .cleanExpiredAbilities(this.databaseExecutor)
               .thenComposeAsync(v -> this.dbManager.getAbilityTime(this.databaseExecutor, player, ability), this.databaseExecutor);
      }
   }

   public int getPlayerExp(Player player) {
      long playerExperience = getTotalExpToLevel(player.getLevel()) + Math.round((float)getXpToLevelUpFrom(player.getLevel()) * player.getExp());
      return (int)playerExperience;
   }

   public static long getTotalExpToLevel(int level) {
      if (level <= 16) {
         return level * level + 6 * level;
      } else {
         return level <= 31 ? (long)(2.5 * level * level - 40.5 * level + 360.0) : (long)(4.5 * level * level - 162.5 * level + 2220.0);
      }
   }

   public static long getXpToLevelUpFrom(int level) {
      if (level <= 15) {
         return 2 * level + 7;
      } else {
         return level <= 30 ? 5 * level - 38 : 9L * level - 158L;
      }
   }

   public void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      for (int id = 0; id < inventory.getStorageContents().length; id++) {
         ItemStack item = inventory.getItem(id);
         if (item == null || item.getType().isAir()) {
            inventory.addItem(new ItemStack[]{itemStack});
            return;
         }

         if (item.isSimilar(itemStack)) {
            int count = item.getMaxStackSize() - item.getAmount();
            if (count > 0) {
               if (itemStack.getAmount() <= count) {
                  inventory.addItem(new ItemStack[]{itemStack});
                  return;
               }

               ItemStack itemClone = itemStack.clone();
               itemClone.setAmount(count);
               inventory.addItem(new ItemStack[]{itemClone});
               itemStack.setAmount(itemStack.getAmount() - count);
            }
         }
      }

      Bukkit.getScheduler().runTask(this, () -> location.getWorld().dropItemNaturally(location, itemStack));
   }
}
