package org.impldev.holycore.invest;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Отдаёт состояние инвестиций наружу: %holyinvest_level_<id>%, %holyinvest_bonus_<id>%,
 * %holyinvest_time_<id>%, %holyinvest_points%.
 *
 * <p>Через них направления, живущие в других плагинах (скупщик, автошахты, голосование,
 * сокровища), могут брать величину эффекта из своих конфигов, не переписывая их код.
 */
public final class InvestPlaceholders extends PlaceholderExpansion {
   private final InvestData data;
   private final InvestEffects effects;

   public InvestPlaceholders(InvestData data, InvestEffects effects) {
      this.data = data;
      this.effects = effects;
   }

   @Override
   public @NotNull String getIdentifier() {
      return "holyinvest";
   }

   @Override
   public @NotNull String getAuthor() {
      return "HolyZako";
   }

   @Override
   public @NotNull String getVersion() {
      return "1.0";
   }

   @Override
   public boolean persist() {
      return true;
   }

   @Override
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (params.equalsIgnoreCase("points")) {
         return player == null ? "0" : String.valueOf(this.data.points(player.getUniqueId()));
      }

      if (params.equalsIgnoreCase("week")) {
         return player == null ? "0" : String.valueOf(this.data.weekPoints(player.getUniqueId()));
      }

      int split = params.indexOf('_');
      if (split <= 0) {
         return null;
      }

      String what = params.substring(0, split);
      String id = params.substring(split + 1);
      Category category = Categories.all().get(id);
      if (category == null) {
         return null;
      }

      return switch (what.toLowerCase()) {
         case "level" -> String.valueOf(this.data.level(id));
         case "progress" -> String.valueOf(this.data.progress(id));
         case "need" -> String.valueOf(category.need());
         case "bonus" -> String.valueOf(Math.round((this.effects.bonus(id) - 1.0) * 100.0));
         case "time" -> String.valueOf(Math.round(this.effects.timeFor(category)));
         default -> null;
      };
   }
}
