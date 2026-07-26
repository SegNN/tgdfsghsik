package org.dimasik.liteexchange.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.dimasik.liteexchange.LiteExchange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoinsPlaceholder extends PlaceholderExpansion {
   public CoinsPlaceholder() {
   }

   @NotNull
   public String getIdentifier() {
      return "exchange";
   }

   @NotNull
   public String getAuthor() {
      return "Dimasik";
   }

   @NotNull
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
      } else if (params.equalsIgnoreCase("coins")) {
         try {
            return String.valueOf(LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get());
         } catch (Exception var4) {
            return "Неизвестно";
         }
      } else if (params.equalsIgnoreCase("coins_formatted")) {
         try {
            return Parser.formatNumber(LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get());
         } catch (Exception var5) {
            return "Неизвестно";
         }
      } else {
         return null;
      }
   }
}
