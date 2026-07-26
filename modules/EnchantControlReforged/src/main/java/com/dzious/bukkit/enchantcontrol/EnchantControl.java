package com.dzious.bukkit.enchantcontrol;

import com.dzious.bukkit.enchantcontrol.command.CommandManager;
import com.dzious.bukkit.enchantcontrol.listener.ListenerManager;
import com.dzious.bukkit.enchantcontrol.plugin.EnchantmentManager;
import com.dzious.bukkit.enchantcontrol.plugin.PlayersManager;
import com.dzious.bukkit.enchantcontrol.utils.ConfigManager;
import com.dzious.bukkit.enchantcontrol.utils.LogManager;
import java.util.logging.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantControl extends JavaPlugin {
   private static EnchantControl INSTANCE;
   private LogManager logManager;
   private ConfigManager configManager;
   private CommandManager commandManager;
   private EnchantmentManager enchantmentManager;
   private ListenerManager listenerManager;
   private PlayersManager playersManager;
   private NamespacedKey key;

   public EnchantControl() {
   }

   public void onLoad() {
   }

   public void onEnable() {
      Logger logger = Logger.getLogger("Minecraft");
      logger.info("Enchant Control starting...");
      INSTANCE = this;
      this.key = new NamespacedKey(INSTANCE, "ENCHANT_CONTROL");
      this.configManager = new ConfigManager(INSTANCE);
      this.logManager = new LogManager(INSTANCE, logger, this.configManager);
      this.enchantmentManager = new EnchantmentManager(INSTANCE);
      this.commandManager = new CommandManager(INSTANCE);
      this.commandManager.onEnable();
      this.listenerManager = new ListenerManager(INSTANCE);
      this.listenerManager.onEnable();
      this.playersManager = new PlayersManager();
      this.logManager.logInfo("Enchant Control started !");
   }

   public void onDisable() {
      this.logManager.logInfo("Enchant Control stopping...");
      this.logManager.logInfo("Enchant Control stopped !");
   }

   public void disablePlugin() {
      this.logManager.logSevere("This is a fatal error, disabling Enchant Control");
      this.setEnabled(false);
   }

   public LogManager getLogManager() {
      return this.logManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public EnchantmentManager getEnchantmentManager() {
      return this.enchantmentManager;
   }

   public PlayersManager getPlayersManager() {
      return this.playersManager;
   }

   public NamespacedKey getNamespacedKey() {
      return this.key;
   }

   public static enum TagType {
      EVENT;

      private TagType() {
      }
   }
}
