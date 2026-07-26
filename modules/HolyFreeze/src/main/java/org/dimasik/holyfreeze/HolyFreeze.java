package org.dimasik.holyfreeze;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class HolyFreeze extends JavaPlugin {
   public static HashMap<UUID, Freeze> freezeHashMap = new HashMap<>();
   public HashMap<UUID, StatusFreeze> statusFreezeHashMap = new HashMap<>();
   private PAPIFreeze papiFreeze;
   private CommandFreeze commandFreeze;
   public static FileConfiguration config;
   public static DatabaseManager database;
   public static String serverName;
   public static boolean enableBlockJoin;
   public static String kickMsg;

   public HolyFreeze() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      config = this.getConfig();
      enableBlockJoin = config.getBoolean("deny-join.enable");
      if (config.getBoolean("deny-join.enable")) {
         serverName = config.getString("deny-join.server");
         kickMsg = config.getString("deny-join.kick-message");
         database = new DatabaseManager(
            serverName,
            config.getString("deny-join.database.host"),
            config.getString("deny-join.database.database"),
            config.getString("deny-join.database.user"),
            config.getString("deny-join.database.password")
         );
      }

      this.commandFreeze = new CommandFreeze(this);
      this.getCommand("freezing").setExecutor(this.commandFreeze);
      this.getCommand("prova").setExecutor(this.commandFreeze);
      this.getCommand("afkstaff").setExecutor(this.commandFreeze);
      this.getServer().getPluginManager().registerEvents(new ListenerFreeze(this), this);
      this.papiFreeze = new PAPIFreeze(this);
      this.papiFreeze.register();
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               HolyFreeze.this.updateStatus(player);
            }
         }
      }).runTaskTimer(this, 0L, 20L);
   }

   public void onDisable() {
      if (database != null) {
         database.removePlayersByServer();
      }

      if (this.papiFreeze != null) {
         this.papiFreeze.unregister();
      }
   }

   public void updateStatus(Player player) {
      if (this.statusFreezeHashMap.get(player.getUniqueId()) != null) {
         if (this.statusFreezeHashMap.get(player.getUniqueId()) == StatusFreeze.AFK) {
            player.sendTitle("", "AFK", 0, 30, 0);
         }

         if (this.statusFreezeHashMap.get(player.getUniqueId()) == StatusFreeze.CHECKING) {
            player.sendTitle("", "На проверке", 0, 30, 0);
         }
      }
   }
}
