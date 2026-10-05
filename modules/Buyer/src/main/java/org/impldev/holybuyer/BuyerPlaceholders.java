package org.impldev.holybuyer;

import java.util.Locale;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Плейсхолдеры скупщика.
 *
 * <p>Идентификатор и первые два имени оставлены как были у прежнего плагина: на
 * {@code %buyer_points%} завязаны голограмма топа скупщика и таблица ajLeaderboards, которая
 * ведёт борду {@code buyer_points} с самого начала вайпа. Переименование обнулило бы её.
 */
public final class BuyerPlaceholders extends PlaceholderExpansion {
   private final HolyBuyer plugin;

   public BuyerPlaceholders(HolyBuyer plugin) {
      this.plugin = plugin;
   }

   @NotNull
   @Override
   public String getIdentifier() {
      return "buyer";
   }

   @NotNull
   @Override
   public String getAuthor() {
      return "HolyZako";
   }

   @NotNull
   @Override
   public String getVersion() {
      return "2.0";
   }

   @Override
   public boolean persist() {
      return true;
   }

   @Override
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null) {
         return null;
      }

      BuyerData.Profile profile = this.plugin.data().peek(player.getUniqueId());
      Player online = player.isOnline() ? player.getPlayer() : null;

      return switch (params.toLowerCase(Locale.ROOT)) {
         case "points" -> String.format(Locale.US, "%.2f", profile.points);
         // Множитель, а не проценты: так его и печатал прежний скупщик.
         case "koeff" -> String.format(Locale.US, "%.2f",
               1.0 + this.plugin.prices().baseCoef(online, profile) / 100.0);
         case "points_formatted" -> Texts.num(profile.points);
         case "lifetime" -> Texts.num(profile.lifetime);
         case "percent" -> Texts.signed(this.plugin.prices().baseCoef(online, profile));
         case "top_category" -> {
            String id = this.plugin.data().topCategory(profile);
            Catalogue.Category category = id == null ? null : this.plugin.catalogue().category(id);
            yield category == null ? "нет" : category.name();
         }
         case "auto" -> switch (profile.autoMode) {
            case ON -> "включено";
            case UNTIL_QUIT -> "включено до выхода";
            case OFF -> "выключено";
         };
         default -> null;
      };
   }
}
