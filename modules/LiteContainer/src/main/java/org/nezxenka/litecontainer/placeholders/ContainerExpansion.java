package org.nezxenka.litecontainer.placeholders;

import java.text.DecimalFormat;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.nezxenka.litecontainer.LiteContainer;

public class ContainerExpansion extends PlaceholderExpansion {
   private final LiteContainer plugin;
   private final DecimalFormat formatter = new DecimalFormat("#,###");

   public ContainerExpansion(LiteContainer plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      return "litecontainer";
   }

   @NotNull
   public String getAuthor() {
      return "nezxenka";
   }

   @NotNull
   public String getVersion() {
      return "1.0";
   }

   public boolean persist() {
      return true;
   }

   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null) {
         return "0";
      } else if (params.equals("sapphires")) {
         double sapphires = this.plugin.getDatabaseManager().getTotalSapphires(player.getName());
         return String.format("%.2f", sapphires);
      } else if (params.equals("sapphires_formatted")) {
         double sapphires = this.plugin.getDatabaseManager().getTotalSapphires(player.getName());
         return this.formatter.format(sapphires);
      } else if (params.equals("lifetime_sapphires")) {
         double lifetimeSapphires = this.plugin.getDatabaseManager().getLifetimeSapphires(player.getName());
         return String.format("%.2f", lifetimeSapphires);
      } else if (params.equals("lifetime_sapphires_formatted")) {
         double lifetimeSapphires = this.plugin.getDatabaseManager().getLifetimeSapphires(player.getName());
         return this.formatter.format(lifetimeSapphires);
      } else if (params.equals("containers")) {
         int containers = this.plugin.getDatabaseManager().getValue(player.getName());
         return String.valueOf(containers);
      } else if (params.startsWith("top_player_")) {
         int index = this.parseIndex(params, "top_player_");
         if (index < 1) {
            return "---";
         } else {
            List<String> topPlayers = this.plugin.getDatabaseManager().getTopPlayersByLifetimeSapphires();
            return index > topPlayers.size() ? "---" : this.plugin.getDatabaseManager().getRealName(topPlayers.get(index - 1));
         }
      } else if (params.startsWith("top_sapphires_")) {
         int index = this.parseIndex(params, "top_sapphires_");
         if (index < 1) {
            return "---";
         } else {
            List<Double> topSapphires = this.plugin.getDatabaseManager().getTopSapphires();
            if (index > topSapphires.size()) {
               return "---";
            } else {
               double sapphires = topSapphires.get(index - 1);
               return String.format("%.2f", sapphires);
            }
         }
      } else if (params.startsWith("top_sapphires_formatted_")) {
         int index = this.parseIndex(params, "top_sapphires_formatted_");
         if (index < 1) {
            return "---";
         } else {
            List<Double> topSapphires = this.plugin.getDatabaseManager().getTopSapphires();
            if (index > topSapphires.size()) {
               return "---";
            } else {
               double sapphires = topSapphires.get(index - 1);
               return this.formatter.format(sapphires);
            }
         }
      } else {
         return "0";
      }
   }

   private int parseIndex(String param, String prefix) {
      try {
         return Integer.parseInt(param.replace(prefix, ""));
      } catch (NumberFormatException var4) {
         return -1;
      }
   }
}
