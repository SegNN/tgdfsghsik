package ru.holydev.holycustom;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.holydev.holycustom.api.CustomPlaceholder;
import ru.holydev.holycustom.commands.CustomCommand;
import ru.holydev.holycustom.db.DatabaseManager;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.items.StarItem;
import ru.holydev.holycustom.listeners.EventListener;
import ru.holydev.holycustom.listeners.colorname.ColorNameListener;
import ru.holydev.holycustom.listeners.particles.ParticleListener;
import ru.holydev.holycustom.listeners.prem.PremDeathMeListener;
import ru.holydev.holycustom.listeners.prem.PremDeathPlayerListener;
import ru.holydev.holycustom.listeners.prem.PremPearlListener;
import ru.holydev.holycustom.listeners.sounds.SoundDeathMeListener;
import ru.holydev.holycustom.listeners.sounds.SoundDeathPlayerListener;
import ru.holydev.holycustom.listeners.sounds.SoundHitListener;
import ru.holydev.holycustom.listeners.sounds.SoundLowHealListener;
import ru.holydev.holycustom.listeners.sounds.SoundVolumeListener;
import ru.holydev.holycustom.listeners.star.StarListener;
import ru.holydev.holycustom.listeners.traps.TrapsListener;
import ru.holydev.holycustom.manager.ConfigManager;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holystar.HolyStar;

public final class HolyCustom extends JavaPlugin implements Listener {
   private static HolyStar holyStar;
   private static HolyCustom instance;
   public static MenuItems menuItems;
   public static StarItem starItem;
   public static MainMenu mainMenu;
   public DatabaseManager dbm;
   public static final HashMap<Player, HashMap<String, String>> playerData = new HashMap<>();
   public static CustomPlaceholder customPlaceholder;
   private static ConfigManager configManager;

   public HolyCustom() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      holyStar = (HolyStar)this.getServer().getPluginManager().getPlugin("HolyStar");
      configManager = new ConfigManager(this);
      this.dbm = new DatabaseManager(this, configManager);
      instance = this;
      menuItems = new MenuItems();
      mainMenu = new MainMenu(menuItems);
      this.getCommand("custom").setExecutor(new CustomCommand(mainMenu));
      this.registerEvents();
      customPlaceholder = new CustomPlaceholder();
      customPlaceholder.register();
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
         for (Player player : Bukkit.getOnlinePlayers()) {
            DatabaseManager.getPlayerAsync(player.getName()).thenAccept(data -> {
               HashMap<String, String> playerMap = data != null ? DatabaseManager.fromBase64(data) : new HashMap<>();
               Bukkit.getScheduler().runTask(this, () -> playerData.put(player, playerMap));
            });
         }
      });
      (new BukkitRunnable() {
         public void run() {
            HolyCustom.this.saveAllPlayerDataAsync();
         }
      }).runTaskTimerAsynchronously(this, 0L, 2400L);
   }

   private void registerEvents() {
      this.getServer().getPluginManager().registerEvents(new TrapsListener(), this);
      this.getServer().getPluginManager().registerEvents(new ParticleListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PremPearlListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new PremDeathMeListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new ColorNameListener(), this);
      this.getServer().getPluginManager().registerEvents(new PremDeathPlayerListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new EventListener(holyStar), this);
      this.getServer().getPluginManager().registerEvents(new StarListener(), this);
      this.getServer().getPluginManager().registerEvents(this, this);
      this.getServer().getPluginManager().registerEvents(new SoundLowHealListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new SoundDeathPlayerListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new SoundDeathMeListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new SoundHitListener(this, configManager), this);
      this.getServer().getPluginManager().registerEvents(new SoundVolumeListener(), this);
   }

   private void saveAllPlayerDataAsync() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         HashMap<String, String> data = playerData.get(player);
         if (data != null) {
            DatabaseManager.setPlayerAsync(player.getName(), DatabaseManager.toBase64(data));
         }
      }
   }

   public void onDisable() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         HashMap<String, String> data = playerData.get(player);
         if (data != null) {
            DatabaseManager.setPlayerAsync(player.getName(), DatabaseManager.toBase64(data)).join();
         }
      }
   }

   public static HolyCustom getInstance() {
      return instance;
   }

   public static HashMap<String, String> getPlayerData(Player player) {
      return playerData.get(player);
   }

   public static String getPlayerData(Player player, String key) {
      HashMap<String, String> data = playerData.get(player);
      return data != null ? data.getOrDefault(key, "null") : "null";
   }

   public static void setPlayerData(Player player, String key, String value) {
      HashMap<String, String> data = playerData.get(player);
      if (data != null) {
         data.put(key, value);
      } else {
         data = new HashMap<>();
         data.put(key, value);
         playerData.put(player, data);
      }
   }

   public static HolyStar getHolyStar() {
      return holyStar;
   }
}
