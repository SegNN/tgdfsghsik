package ua.holymine.holyautomine.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ua.holymine.holyautomine.HolyAutoMine;
import ua.holymine.holyautomine.mines.EndNetherMine;
import ua.holymine.holyautomine.mines.OverworldMine;

public class MineExpansion extends PlaceholderExpansion {
   private final HolyAutoMine plugin;
   private boolean registered = false;

   public MineExpansion(HolyAutoMine plugin) {
      this.plugin = plugin;
      this.register();
   }

   public String getIdentifier() {
      return "holyautomine";
   }

   public String getAuthor() {
      return "smail";
   }

   public String getVersion() {
      return "1.0";
   }

   public boolean register() {
      if (!this.registered) {
         this.registered = super.register();
      }

      return this.registered;
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      OverworldMine overworldMine = this.plugin.getOverworldMine();
      EndNetherMine endNetherMine = this.plugin.getEndNetherMine();
      switch (identifier) {
         case "overworld_nowmine":
            return overworldMine.getCurrentDisplayName() != null ? overworldMine.getCurrentDisplayName() : "&cНеизвестно";
         case "overworld_nextmine":
            return overworldMine.getNextDisplayName() != null ? overworldMine.getNextDisplayName() : "&cНеизвестно";
         case "overworld_minutes":
            return String.valueOf(overworldMine.getRemainingTime() / 60000L);
         case "overworld_seconds":
            long overworldSeconds = overworldMine.getRemainingTime() / 1000L % 60L;
            return String.valueOf(overworldSeconds);
         case "endnether_nowmine":
            return endNetherMine.getCurrentDisplayName() != null ? endNetherMine.getCurrentDisplayName() : "&cНеизвестно";
         case "endnether_nextmine":
            return endNetherMine.getNextDisplayName() != null ? endNetherMine.getNextDisplayName() : "&cНеизвестно";
         case "endnether_minutes":
            return String.valueOf(endNetherMine.getRemainingTime() / 60000L);
         case "endnether_seconds":
            long endnetherSeconds = endNetherMine.getRemainingTime() / 1000L % 60L;
            return String.valueOf(endnetherSeconds);
         default:
            return null;
      }
   }

   public void unregisterExpansion() {
      if (this.registered) {
         this.unregister();
         this.registered = false;
      }
   }
}
