package org.dimasik.buyer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuyerPlaceholderExpansion extends PlaceholderExpansion {
   private final Buyer plugin;

   public BuyerPlaceholderExpansion(Buyer plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      return "buyer";
   }

   @NotNull
   public String getAuthor() {
      return "Dimasik";
   }

   @NotNull
   public String getVersion() {
      return "1.0";
   }

   public String onPlaceholderRequest(Player player, @NotNull String identifier) {
      if (player == null) {
         return "";
      } else if (identifier.equals("koeff")) {
         try {
            double koeff = this.plugin.koefficentManager.getKoefficent(player.getName()).get();
            return String.format("%.2f", koeff);
         } catch (Exception var5) {
            return "1.00";
         }
      } else if (identifier.equals("points")) {
         try {
            double points = this.plugin.koefficentManager.getPoints(player.getName()).get();
            return String.format("%.2f", points);
         } catch (Exception var6) {
            return "0.00";
         }
      } else {
         return null;
      }
   }
}
