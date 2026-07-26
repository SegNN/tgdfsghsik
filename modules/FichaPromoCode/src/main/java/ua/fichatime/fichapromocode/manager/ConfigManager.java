package ua.fichatime.fichapromocode.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ua.fichatime.fichapromocode.FichaPromoCode;

public class ConfigManager {
   private final FichaPromoCode plugin;
   private FileConfiguration config;
   private Map<String, Map<String, Object>> promocodes;

   public ConfigManager(FichaPromoCode plugin) {
      this.plugin = plugin;
      this.reloadConfig();
   }

   public void reloadConfig() {
      this.plugin.reloadConfig();
      this.config = this.plugin.getConfig();
      this.loadPromocodes();
   }

   private void loadPromocodes() {
      ConfigurationSection promoSection = this.config.getConfigurationSection("promocode");
      if (promoSection != null) {
         this.promocodes = new HashMap<>();

         for (String key : promoSection.getKeys(false)) {
            Map<String, Object> promoData = promoSection.getConfigurationSection(key).getValues(false);
            this.promocodes.put(key.toLowerCase(), promoData);
         }
      }
   }

   public Map<String, Map<String, Object>> getPromocodes() {
      return this.promocodes;
   }

   public String getStorageType() {
      return this.config.getString("storage.type", "SQLite");
   }

   public String getMySQLHost() {
      return this.config.getString("MySQL.host");
   }

   public String getMySQLDatabase() {
      return this.config.getString("MySQL.database");
   }

   public String getMySQLUser() {
      return this.config.getString("MySQL.user");
   }

   public String getMySQLPassword() {
      return this.config.getString("MySQL.password");
   }

   public int getRewardPlayer(String promoCode) {
      try {
         Object value = this.promocodes.get(promoCode.toLowerCase()).get("reward_player");
         return value instanceof Boolean && !(Boolean)value ? Integer.MAX_VALUE : (Integer)value;
      } catch (Exception var3) {
         return 0;
      }
   }

   public int getRewardAllPlayer(String promoCode) {
      try {
         Object value = this.promocodes.get(promoCode.toLowerCase()).get("reward_all_player");
         return value instanceof Boolean && !(Boolean)value ? Integer.MAX_VALUE : (Integer)value;
      } catch (Exception var3) {
         return 0;
      }
   }

   public boolean isIPCheckEnabled(String promoCode) {
      try {
         Object value = this.promocodes.get(promoCode.toLowerCase()).get("IP");
         return value instanceof Boolean ? (Boolean)value : false;
      } catch (Exception var3) {
         return false;
      }
   }

   public boolean isShowInCommands(String promoCode) {
      try {
         Object value = this.promocodes.get(promoCode.toLowerCase()).get("show_in_commands");
         return value instanceof Boolean ? (Boolean)value : true;
      } catch (Exception var3) {
         return true;
      }
   }

   public List<String> getRewardCommands(String promoCode) {
      try {
         return (List<String>)this.promocodes.get(promoCode.toLowerCase()).get("reward_commands");
      } catch (Exception var3) {
         return null;
      }
   }

   public List<String> getRewardMessage(String promoCode) {
      try {
         return (List<String>)this.promocodes.get(promoCode.toLowerCase()).get("reward_message");
      } catch (Exception var3) {
         return null;
      }
   }

   public List<String> getRewardMessageLimit(String promoCode) {
      try {
         return (List<String>)this.promocodes.get(promoCode.toLowerCase()).get("reward_message_limit");
      } catch (Exception var3) {
         return null;
      }
   }

   public List<String> getRewardPlayersMessageLimit(String promoCode) {
      try {
         return (List<String>)this.promocodes.get(promoCode.toLowerCase()).get("reward_players_message_limit");
      } catch (Exception var3) {
         return null;
      }
   }

   public List<String> getIPMessageLimit(String promoCode) {
      try {
         return (List<String>)this.promocodes.get(promoCode.toLowerCase()).get("IP_message_limit");
      } catch (Exception var3) {
         return null;
      }
   }

   public List<String> getGroupMessageLimit(String promoCode) {
      try {
         return (List<String>)this.promocodes.get(promoCode.toLowerCase()).get("group_message_limit");
      } catch (Exception var3) {
         return null;
      }
   }

   public String getPromoGroup(String promoCode) {
      try {
         Object value = this.promocodes.get(promoCode.toLowerCase()).get("group");
         return value instanceof String ? (String)value : null;
      } catch (Exception var3) {
         return null;
      }
   }

   public boolean hasGroup(String promoCode) {
      return this.getPromoGroup(promoCode) != null;
   }

   public FileConfiguration getConfig() {
      return this.config;
   }
}
