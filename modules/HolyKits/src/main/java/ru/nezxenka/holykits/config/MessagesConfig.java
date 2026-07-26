package ru.nezxenka.holykits.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.nezxenka.holykits.HolyKits;

public class MessagesConfig {
   private final HolyKits plugin;
   private File messagesFile;
   private FileConfiguration messagesConfig;
   private final Map<String, String> permissionMessages = new HashMap<>();

   public MessagesConfig(HolyKits plugin) {
      this.plugin = plugin;
      this.loadMessages();
   }

   private void loadMessages() {
      this.messagesFile = new File(this.plugin.getDataFolder(), "messages.yml");
      if (!this.messagesFile.exists()) {
         this.plugin.saveResource("messages.yml", false);
      }

      this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
      this.permissionMessages.clear();
      if (this.messagesConfig.isConfigurationSection("kit-permissions")) {
         for (String key : this.messagesConfig.getConfigurationSection("kit-permissions").getKeys(false)) {
            String message = this.messagesConfig.getString("kit-permissions." + key);
            this.permissionMessages.put(key.toLowerCase(), this.translateHexColors(message));
         }
      }
   }

   public void reload() {
      this.loadMessages();
   }

   public String getNoPermissionMessage(String kitName) {
      String message = this.permissionMessages.get(kitName.toLowerCase());
      if (message != null) {
         return message;
      } else {
         String defaultMessage = this.messagesConfig.getString("default-no-permission", "§c▶ §fУ вас нет прав на использование этого набора");
         return this.translateHexColors(defaultMessage);
      }
   }

   private String translateHexColors(String text) {
      if (text == null) {
         return "";
      } else {
         text = text.replaceAll("&#([A-Fa-f0-9]{6})", "§x§$1");
         text = text.replaceAll("§x§([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])", "§x§$1§$2§$3§$4§$5§$6");
         return text.replace("&", "§");
      }
   }
}
