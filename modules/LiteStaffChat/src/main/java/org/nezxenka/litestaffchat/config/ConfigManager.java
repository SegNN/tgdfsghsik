package org.nezxenka.litestaffchat.config;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.nezxenka.litestaffchat.LiteStaffChat;

public class ConfigManager {
   private final LiteStaffChat plugin;
   private final Map<ConfigManager.ChatMode, String> chatFormats = new EnumMap<>(ConfigManager.ChatMode.class);
   private FileConfiguration config;

   public ConfigManager(LiteStaffChat plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   public void loadConfig() {
      this.config = this.plugin.getConfig();

      for (ConfigManager.ChatMode mode : ConfigManager.ChatMode.values()) {
         String format = this.config.getString("chat-format." + mode.name().toLowerCase());
         if (format != null) {
            this.chatFormats.put(mode, format);
         }
      }
   }

   public void reloadConfig() {
      this.plugin.reloadConfig();
      this.chatFormats.clear();
      this.loadConfig();
   }

   public String getChatFormat(ConfigManager.ChatMode mode) {
      return this.chatFormats.getOrDefault(mode, this.chatFormats.get(ConfigManager.ChatMode.GLOBAL));
   }

   public String getMessage(String path) {
      return this.config.getString("messages." + path, "");
   }

   public static enum ChatMode {
      LOCAL,
      GLOBAL,
      CLASSIC,
      LITE;

      private ChatMode() {
      }
   }
}
