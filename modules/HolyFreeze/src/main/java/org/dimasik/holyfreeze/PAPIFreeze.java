package org.dimasik.holyfreeze;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIFreeze extends PlaceholderExpansion {
   private final HolyFreeze plugin;

   public PAPIFreeze(HolyFreeze plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      return "holyfreeze";
   }

   @NotNull
   public String getAuthor() {
      return "Dimasik";
   }

   @NotNull
   public String getVersion() {
      return "1.0.0";
   }

   public boolean persist() {
      return true;
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {
      if (!params.equalsIgnoreCase("status")) {
         return null;
      } else if (this.plugin.statusFreezeHashMap.get(player.getUniqueId()) == null) {
         return "";
      } else {
         switch ((StatusFreeze)this.plugin.statusFreezeHashMap.get(player.getUniqueId())) {
            case NONE:
               return "";
            case CHECKING:
               return ChatColor.translateAlternateColorCodes('&', "&x&E&5&3&1&F&B На проверке&f");
            case AFK:
               return ChatColor.translateAlternateColorCodes('&', "&x&E&5&3&1&F&B AFK&f");
            default:
               return null;
         }
      }
   }
}
