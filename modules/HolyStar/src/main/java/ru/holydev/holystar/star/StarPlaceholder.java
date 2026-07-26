package ru.holydev.holystar.star;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.holydev.holystar.manager.ConfigManager;
import ru.holydev.holystar.utils.HexUtil;

public class StarPlaceholder extends PlaceholderExpansion {
   private final StarManager starManager;
   private final ConfigManager configManager;

   public StarPlaceholder(StarManager starManager, ConfigManager configManager) {
      this.starManager = starManager;
      this.configManager = configManager;
   }

   public String getIdentifier() {
      return "holystar";
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
