package ua.fichamine.fichartp;

import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichartp.commands.RtpCommand;
import ua.fichamine.fichartp.config.ConfigManager;
import ua.fichamine.fichartp.listeners.PlayerMoveListener;
import ua.fichamine.fichartp.managers.CooldownManager;
import ua.fichamine.fichartp.managers.RegionManager;
import ua.fichamine.fichartp.managers.TeleportManager;

public class FichaRTP extends JavaPlugin {
   private static FichaRTP instance;
   private ConfigManager configManager;
   private TeleportManager teleportManager;
   private CooldownManager cooldownManager;
   private RegionManager regionManager;

   public FichaRTP() {
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
      this.getLogger().info("FichaRTP включен");
   }

   public void onDisable() {
      this.getLogger().info("FichaRTP выключен");
   }

   public static FichaRTP getInstance() {
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
