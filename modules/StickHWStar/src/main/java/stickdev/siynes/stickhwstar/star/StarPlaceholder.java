package stickdev.siynes.stickhwstar.star;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import stickdev.siynes.stickhwstar.manager.ConfigManager;
import stickdev.siynes.stickhwstar.utils.HexUtil;

public class StarPlaceholder extends PlaceholderExpansion {
   private final StarManager starManager;
   private final ConfigManager configManager;

   public StarPlaceholder(StarManager starManager, ConfigManager configManager) {
      this.starManager = starManager;
      this.configManager = configManager;
   }

   public String getIdentifier() {
      return "stickhwstar";
   }

   public String getAuthor() {
      return "OnesDev";
   }

   public String getVersion() {
      return "1.0";
   }

   public boolean persist() {
      return true;
   }

   @Nullable
   public String onPlaceholderRequest(Player player, String params) {
      if (player == null) {
         return "";
      } else if (params.equalsIgnoreCase("stars")) {
         int starCount = this.starManager.get(player);
         return HexUtil.translate(this.configManager.getStarForCount(starCount));
      } else {
         return null;
      }
   }
}
