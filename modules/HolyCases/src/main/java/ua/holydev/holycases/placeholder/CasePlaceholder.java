package ua.holydev.holycases.placeholder;

import java.util.concurrent.CompletableFuture;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.cases.CaseManager;
import ua.holydev.holycases.lucks.Luck;
import ua.holydev.holycases.lucks.LuckManager;

public class CasePlaceholder extends PlaceholderExpansion {
   private final LuckManager luckManager;
   private final CaseManager caseManager;
   private final HolyCases plugin;

   public CasePlaceholder(HolyCases plugin, LuckManager luckManager, CaseManager caseManager) {
      this.plugin = plugin;
      this.luckManager = luckManager;
      this.caseManager = caseManager;
   }

   @NotNull
   public String getIdentifier() {
      return "cases";
   }

   @NotNull
   public String getAuthor() {
      return "eXo8_";
   }

   @NotNull
   public String getVersion() {
      return "1.0";
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      if (player == null) {
         return "Игрок не найден";
      } else if (identifier.equalsIgnoreCase("luck15_amount")) {
         Luck luck15 = this.luckManager.getLuckByName("luck15");
         return luck15 != null ? String.valueOf(luck15.getLuckFor(player).join()) : "0";
      } else if (identifier.equalsIgnoreCase("luck30_amount")) {
         Luck luck30 = this.luckManager.getLuckByName("luck30");
         return luck30 != null ? String.valueOf(luck30.getLuckFor(player).join()) : "0";
      } else if (identifier.startsWith("all_amount")) {
         String allKeys = this.plugin.getKeysForAllCases(player);
         return allKeys != null ? allKeys : "0";
      } else if (identifier.endsWith("_amount")) {
         String caseName = identifier.substring(0, identifier.length() - 7);
         if (this.caseManager.getCaseHolder(caseName) == null) {
            return "0";
         } else {
            CompletableFuture<Integer> keys = this.plugin.getKeysC(player, caseName);
            return String.valueOf(keys);
         }
      } else {
         return "Плейсхлдер не найден";
      }
   }

   public String replacePlaceholders(String input, OfflinePlayer player) {
      String result = this.replaceCaseKeys(input, player);
      return this.replaceLuck(result, player);
   }

   private String replaceCaseKeys(String input, OfflinePlayer player) {
      if (input.contains("_amount%")) {
         for (String caseName : this.caseManager.getCaseNames()) {
            if (input.contains("%cases_" + caseName + "_amount%")) {
               int keys = this.plugin.getKeys(player, caseName);
               input = input.replace("%cases_" + caseName + "_amount%", String.valueOf(keys));
            }
         }
      }

      return input;
   }

   private String replaceLuck(String input, OfflinePlayer player) {
      if (this.luckManager != null) {
         for (Luck luck : this.luckManager.getLucks()) {
            String placeholder = "%cases_" + luck.getName() + "_amount%";
            if (input.contains(placeholder)) {
               int luckAmount = luck.getLuckFor(player.getPlayer()).join();
               input = input.replace(placeholder, String.valueOf(luckAmount));
            }
         }
      }

      return input;
   }
}
