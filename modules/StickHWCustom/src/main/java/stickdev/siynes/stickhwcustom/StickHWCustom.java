package stickdev.siynes.stickhwcustom;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import stickdev.siynes.stickhwcustom.api.CustomPlaceholder;
import stickdev.siynes.stickhwcustom.commands.CustomCommand;
import stickdev.siynes.stickhwcustom.db.DatabaseManager;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.items.StarItem;
import stickdev.siynes.stickhwcustom.listeners.EventListener;
import stickdev.siynes.stickhwcustom.listeners.colorname.ColorNameListener;
import stickdev.siynes.stickhwcustom.listeners.particles.ParticleListener;
import stickdev.siynes.stickhwcustom.listeners.prem.PremDeathMeListener;
import stickdev.siynes.stickhwcustom.listeners.prem.PremDeathPlayerListener;
import stickdev.siynes.stickhwcustom.listeners.prem.PremPearlListener;
import stickdev.siynes.stickhwcustom.listeners.sounds.SoundDeathMeListener;
import stickdev.siynes.stickhwcustom.listeners.sounds.SoundDeathPlayerListener;
import stickdev.siynes.stickhwcustom.listeners.sounds.SoundHitListener;
import stickdev.siynes.stickhwcustom.listeners.sounds.SoundLowHealListener;
import stickdev.siynes.stickhwcustom.listeners.sounds.SoundVolumeListener;
import stickdev.siynes.stickhwcustom.listeners.star.StarListener;
import stickdev.siynes.stickhwcustom.listeners.traps.TrapsListener;
import stickdev.siynes.stickhwcustom.manager.ConfigManager;
import stickdev.siynes.stickhwcustom.menus.MainMenu;
import stickdev.siynes.stickhwstar.StickHWStar;

public final class StickHWCustom extends JavaPlugin implements Listener {
   private static StickHWStar stickHWStar;
   private static StickHWCustom instance;
   public static MenuItems menuItems;
   public static StarItem starItem;
   public static MainMenu mainMenu;
   public DatabaseManager dbm;
   public static final HashMap<Player, HashMap<String, String>> playerData = new HashMap<>();
   public static CustomPlaceholder customPlaceholder;
   private static ConfigManager configManager;

   public StickHWCustom() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      stickHWStar = (StickHWStar)this.getServer().getPluginManager().getPlugin("StickHWStar");
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
            StickHWCustom.this.saveAllPlayerDataAsync();
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
      this.getServer().getPluginManager().registerEvents(new EventListener(stickHWStar), this);
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

   public static StickHWCustom getInstance() {
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

   public static StickHWStar getStickHWStar() {
      return stickHWStar;
   }
}
