package org.impldev.holyclans;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class ClanPlaceholders extends PlaceholderExpansion {
   private final HolyClans plugin;

   public ClanPlaceholders(HolyClans plugin) {
      this.plugin = plugin;
   }

   @NotNull
   @Override
   public String getIdentifier() {
      return "holyclans";
   }

   @NotNull
   @Override
   public String getAuthor() {
      return "ImplDev";
   }

   @NotNull
   @Override
   public String getVersion() {
      return "1.0";
   }

   @Override
   public boolean persist() {
      return true;
   }

   @Override
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null) return null;

      Clan clan = this.plugin.data().byPlayer(player.getUniqueId());

      return switch (params) {
         case "name" -> clan != null ? "&7" + clan.name() : "&7Нет";
         case "level" -> clan != null ? String.valueOf(clan.level()) : "0";
         case "points" -> clan != null ? String.valueOf(clan.points()) : "0";
         case "treasury" -> clan != null ? Texts.num(clan.treasury()) : "0";
         case "members" -> clan != null ? String.valueOf(clan.members().size()) : "0";
         case "role" -> {
            if (clan == null) yield "";
            ClanMember member = clan.members().get(player.getUniqueId());
            if (member == null) yield "";
            yield switch (member.role()) {
               case LEADER -> "Лидер";
               case DEPUTY -> "Заместитель";
               case MEMBER -> "Участник";
            };
         }
         default -> null;
      };
   }
}
