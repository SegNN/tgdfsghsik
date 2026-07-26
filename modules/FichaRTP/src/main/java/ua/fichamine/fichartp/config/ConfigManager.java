package ua.fichamine.fichartp.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import ua.fichamine.fichartp.FichaRTP;

public class ConfigManager {
   private final FichaRTP plugin;
   private FileConfiguration config;

   public ConfigManager(FichaRTP plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   public void loadConfig() {
      this.plugin.saveDefaultConfig();
      this.plugin.reloadConfig();
      this.config = this.plugin.getConfig();
   }

   public List<String> getAllowedWorlds() {
      return this.config.getStringList("settings.allowed-worlds");
   }

   public String getTargetWorld() {
      return this.config.getString("settings.target-world", "world");
   }

   public String getSpawnWorld() {
      return this.config.getString("settings.spawn-world", "spawn");
   }

   public boolean isDefaultEnabled() {
      return this.config.getBoolean("default.enabled", true);
   }

   public int getDefaultTeleportDelay() {
      return this.config.getInt("default.teleport-delay", 3);
   }

   public int getDefaultCooldown() {
      return this.config.getInt("default.cooldown", 10);
   }

   public int getDefaultMinRadius() {
      return this.config.getInt("default.min-radius", 1000);
   }

   public int getDefaultMaxRadius() {
      return this.config.getInt("default.max-radius", 10000);
   }

   public boolean isBypassDelayInSpawn() {
      return this.config.getBoolean("default.bypass-delay-in-spawn", true);
   }

   public boolean isLongEnabled() {
      return this.config.getBoolean("long.enabled", true);
   }

   public int getLongCooldown() {
      return this.config.getInt("long.cooldown", 45);
   }

   public int getLongMinRadius() {
      return this.config.getInt("long.min-radius", 15000);
   }

   public int getLongMaxRadius() {
      return this.config.getInt("long.max-radius", 30000);
   }

   public boolean isPlayerEnabled() {
      return this.config.getBoolean("player.enabled", true);
   }

   public int getPlayerCooldown() {
      return this.config.getInt("player.cooldown", 60);
   }

   public int getMinPlayersOnline() {
      return this.config.getInt("player.min-players-online", 5);
   }

   public boolean isIgnoreVanishedPlayers() {
      return this.config.getBoolean("player.ignore-vanished-players", true);
   }

   public boolean isIgnoreInvisiblePlayers() {
      return this.config.getBoolean("player.ignore-invisible-players", true);
   }

   public List<String> getPlayerExcludedWorlds() {
      return this.config.getStringList("player.excluded-worlds");
   }

   public boolean isBaseEnabled() {
      return this.config.getBoolean("base.enabled", true);
   }

   public int getBaseCooldown() {
      return this.config.getInt("base.cooldown", 65);
   }

   public int getBaseMinRadius() {
      return this.config.getInt("base.min-radius-from-region", 50);
   }

   public int getBaseMaxRadius() {
      return this.config.getInt("base.max-radius-from-region", 300);
   }

   public boolean isUseProtectionStones() {
      return this.config.getBoolean("base.use-protectionstones", true);
   }

   public boolean isUseWorldGuard() {
      return this.config.getBoolean("base.use-worldguard", true);
   }

   public List<String> getBaseExcludedWorlds() {
      return this.config.getStringList("base.excluded-worlds");
   }

   public List<String> getUnsafeBlocks() {
      return this.config.getStringList("unsafe-blocks");
   }

   public String getMessage(String path) {
      return this.config.getString("messages." + path, "");
   }

   public String getTeleportingMessage() {
      return this.getMessage("teleporting");
   }

   public String getTeleportCancelledMessage() {
      return this.getMessage("teleport-cancelled");
   }

   public String getTeleportSuccessMessage() {
      return this.getMessage("teleport-success");
   }

   public String getCooldownDefaultMessage() {
      return this.getMessage("cooldown-default");
   }

   public String getCooldownCustomMessage() {
      return this.getMessage("cooldown-custom");
   }

   public String getWorldNotAllowedMessage() {
      return this.getMessage("world-not-allowed");
   }

   public String getNotEnoughPlayersMessage() {
      return this.getMessage("not-enough-players");
   }

   public String getNoPermissionMessage() {
      return this.getMessage("no-permission");
   }

   public String getNoBasesFoundMessage() {
      return this.getMessage("no-bases-found");
   }
}
