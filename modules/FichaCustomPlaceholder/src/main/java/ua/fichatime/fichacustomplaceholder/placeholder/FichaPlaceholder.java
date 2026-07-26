package ua.fichatime.fichacustomplaceholder.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.fichatime.fichacustomplaceholder.FichaCustomPlaceholder;
import ua.fichatime.fichacustomplaceholder.utils.ColorUtils;

public class FichaPlaceholder extends PlaceholderExpansion {
   public FichaPlaceholder() {
   }

   @NotNull
   public String getIdentifier() {
      return "fichaplaceholder";
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

      ConfigurationSection placeholders = FichaCustomPlaceholder.getPlaceholders();
      return placeholders != null ? ColorUtils.parse(placeholders.getString(params, "?")) : "?";
   }

}
