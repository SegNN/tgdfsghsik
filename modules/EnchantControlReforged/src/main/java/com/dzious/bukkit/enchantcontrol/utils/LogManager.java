package com.dzious.bukkit.enchantcontrol.utils;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LogManager {
   private EnchantControl plugin;
   private Logger logger;
   private boolean debugEnable = false;
   private String debugPlayer = null;

   public LogManager(EnchantControl plugin, Logger logger, ConfigManager configManager) {
      this.plugin = plugin;
      this.logger = logger;
      if (configManager.doPathExist("debug.enable")) {
         this.debugEnable = configManager.getBooleanFromPath("debug.enable");
      }

      if (configManager.doPathExist("debug.player")) {
         this.debugPlayer = configManager.getStringFromPath("debug.player");
      }
   }

   public void logInfo(String msg) {
      this.logger.info(msg);
   }

   public void logWarning(String msg) {
      this.logger.warning(msg);
   }

   public void logSevere(String msg) {
      this.logger.severe(msg);
   }

   public void logDebugConsole(String msg) {
      if (this.isDebugEnable()) {
         this.logger.info("[" + ChatColor.RED + this.plugin.getName() + ChatColor.WHITE + "]" + ChatColor.BLUE + "[Debug]" + ChatColor.WHITE + " : " + msg);
      }
   }

   public void logDebugPlayer(String msg) {
      if (this.isDebugEnable() && this.getDebugPlayer() != null) {
         for (Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (p.getName() == this.getDebugPlayer()) {
               p.sendMessage("[" + this.plugin.getName() + "] Debug : " + msg);
            }
         }
      }
   }

   public void logException(Exception e) {
      if (this.isDebugEnable()) {
         e.printStackTrace();
      }
   }

   private boolean isDebugEnable() {
      return this.debugEnable;
   }

   private String getDebugPlayer() {
      return this.debugPlayer;
   }
}
