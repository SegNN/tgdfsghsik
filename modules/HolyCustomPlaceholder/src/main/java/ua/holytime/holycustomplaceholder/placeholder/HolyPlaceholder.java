package ua.holytime.holycustomplaceholder.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.holytime.holycustomplaceholder.HolyCustomPlaceholder;
import ua.holytime.holycustomplaceholder.utils.ColorUtils;

public class HolyPlaceholder extends PlaceholderExpansion {
   public HolyPlaceholder() {
   }

   @NotNull
   public String getIdentifier() {
      return "holyplaceholder";
   }

   @NotNull
   public String getAuthor() {
      return "FichaDev";
   }

   @NotNull
   public String getVersion() {
      return "1.0";
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {

      ConfigurationSection placeholders = HolyCustomPlaceholder.getPlaceholders();
      return placeholders != null ? ColorUtils.parse(placeholders.getString(params, "?")) : "?";
   }

}
