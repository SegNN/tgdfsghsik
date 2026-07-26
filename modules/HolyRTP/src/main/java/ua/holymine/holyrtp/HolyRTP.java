package ua.holymine.holyrtp;

import org.bukkit.plugin.java.JavaPlugin;
import ua.holymine.holyrtp.commands.RtpCommand;
import ua.holymine.holyrtp.config.ConfigManager;
import ua.holymine.holyrtp.listeners.PlayerMoveListener;
import ua.holymine.holyrtp.managers.CooldownManager;
import ua.holymine.holyrtp.managers.RegionManager;
import ua.holymine.holyrtp.managers.TeleportManager;

public class HolyRTP extends JavaPlugin {
   private static HolyRTP instance;
   private ConfigManager configManager;
   private TeleportManager teleportManager;
   private CooldownManager cooldownManager;
   private RegionManager regionManager;

   public HolyRTP() {
   }

   public void onEnable() {
      instance = this;
      this.configManager = new ConfigManager(this);
      this.cooldownManager = new CooldownManager();
      this.regionManager = new RegionManager(this);
      this.teleportManager = new TeleportManager(this);
      this.getCommand("rtp").setExecutor(new RtpCommand(this));
      this.getCommand("rtp").setTabCompleter(new RtpCommand(this));
      this.getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
      this.getLogger().info("HolyRTP включен");
   }

   public void onDisable() {
      this.getLogger().info("HolyRTP выключен");
   }

   public static HolyRTP getInstance() {
      return instance;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public TeleportManager getTeleportManager() {
      return this.teleportManager;
   }

   public CooldownManager getCooldownManager() {
      return this.cooldownManager;
   }

   public RegionManager getRegionManager() {
      return this.regionManager;
   }
}
