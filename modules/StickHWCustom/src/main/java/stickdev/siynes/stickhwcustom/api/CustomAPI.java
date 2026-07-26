package stickdev.siynes.stickhwcustom.api;

import java.util.HashMap;
import org.bukkit.entity.Player;
import stickdev.siynes.stickhwcustom.StickHWCustom;

public class CustomAPI {
   public CustomAPI() {
   }

   public String getPlayerData(Player player, String key, String defaultValue) {
      HashMap<String, String> playerData = StickHWCustom.getPlayerData(player);
      return playerData != null && playerData.containsKey(key) ? playerData.get(key) : defaultValue;
   }

   public String getPlayerData(Player player, String key) {
      HashMap<String, String> playerData = StickHWCustom.getPlayerData(player);
      return playerData != null && playerData.containsKey(key) ? playerData.get(key) : null;
   }
}
