package ru.holydev.holycustom.api;

import java.util.HashMap;
import org.bukkit.entity.Player;
import ru.holydev.holycustom.HolyCustom;

public class CustomAPI {
   public CustomAPI() {
   }

   public String getPlayerData(Player player, String key, String defaultValue) {
      HashMap<String, String> playerData = HolyCustom.getPlayerData(player);
      return playerData != null && playerData.containsKey(key) ? playerData.get(key) : defaultValue;
   }

   public String getPlayerData(Player player, String key) {
      HashMap<String, String> playerData = HolyCustom.getPlayerData(player);
      return playerData != null && playerData.containsKey(key) ? playerData.get(key) : null;
   }
}
