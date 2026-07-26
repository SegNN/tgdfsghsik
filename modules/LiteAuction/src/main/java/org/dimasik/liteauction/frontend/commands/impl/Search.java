package org.dimasik.liteauction.frontend.commands.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.storage.models.GuiData;
import org.dimasik.liteauction.backend.utils.ItemNameUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.commands.SubCommand;
import org.dimasik.liteauction.frontend.menus.bids.menus.Main;

public class Search extends SubCommand {
   public Search(String name) {
      super(name);
   }

   @Override
   public void execute(org.bukkit.entity.Player player, Command command, String[] args) {
      ItemNameUtil.loadTranslationsIfNeeded();
      if (args.length >= 2) {
         String find = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
         if (!ItemNameUtil.containsTag(find)) {
            player.sendMessage(
               Parser.color(ConfigManager.getString("design/commands/search.yml", "air-search", "&#FB2222▶ &fПо вашему запросу не найдено ни одного фильтра."))
            );
            return;
         }

         try {
            LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).thenAccept(guiData -> {
               if (guiData.getAuctionType() != AuctionType.MARKET && ConfigManager.getBoolean("settings/settings.yml", "enable-bids", true)) {
                  Main mainx = new Main(1);
                  mainx.setPlayer(player);
                  mainx.setSortingType(guiData.getBidsSortingType());
                  mainx.setCategoryType(guiData.getCategoryType());
                  mainx.setFilters(ItemNameUtil.escapeTag(find));
                  mainx.compile().open();
               } else {
                  org.dimasik.liteauction.frontend.menus.market.menus.Main mainxx = new org.dimasik.liteauction.frontend.menus.market.menus.Main(1);
                  mainxx.setPlayer(player);
                  mainxx.setSortingType(guiData.getMarketSortingType());
                  mainxx.setCategoryType(guiData.getCategoryType());
                  mainxx.setFilters(ItemNameUtil.escapeTag(find));
                  mainxx.compile().open();
               }
            });
         } catch (Exception var7) {
            player.sendMessage(
               Parser.color(ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."))
            );
         }
      } else {
         ItemStack itemStack = player.getItemInHand() != null && !player.getItemInHand().getType().isAir()
            ? player.getItemInHand()
            : player.getInventory().getItemInOffHand();
         if (itemStack == null || itemStack.getType().isAir()) {
            player.sendMessage(
               Parser.color(ConfigManager.getString("design/commands/search.yml", "air-search", "&#FB2222▶ &fПо вашему запросу не найдено ни одного фильтра."))
            );
            return;
         }

         try {
            GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
            if (guiData.getAuctionType() != AuctionType.MARKET && ConfigManager.getBoolean("settings/settings.yml", "enable-bids", true)) {
               Main main = new Main(1);
               main.setPlayer(player);
               main.setSortingType(guiData.getBidsSortingType());
               main.setCategoryType(guiData.getCategoryType());
               main.compile().open();
               main.setFilters(TagUtil.getPartialTags(itemStack));
            } else {
               org.dimasik.liteauction.frontend.menus.market.menus.Main main = new org.dimasik.liteauction.frontend.menus.market.menus.Main(1);
               main.setPlayer(player);
               main.setSortingType(guiData.getMarketSortingType());
               main.setCategoryType(guiData.getCategoryType());
               main.setFilters(TagUtil.getPartialTags(itemStack));
               main.compile().open();
            }
         } catch (Exception var8) {
            player.sendMessage(
               Parser.color(ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."))
            );
         }
      }
   }

   @Override
   public List<String> getTabCompletes(CommandSender sender, String[] args) {
      if (args.length < 2) {
         return List.of();
      } else {
         ItemNameUtil.loadTranslationsIfNeeded();
         String all = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
         List<String> allTags = new ArrayList<>();
         List<String> completions = new ArrayList<>();
         allTags.addAll(ItemNameUtil.getReverseTranslations().keySet());
         allTags.addAll(ItemNameUtil.getCustomTags().keySet());

         try {
            for (int i = 0; i < allTags.size(); i++) {
               String current = allTags.get(i);
               int startIndex = all.length() - args[args.length - 1].length();
               if (startIndex < 0) {
                  startIndex = 0;
               } else if (startIndex > current.length()) {
                  startIndex = current.length();
               }

               if (current.toLowerCase().startsWith(all.toLowerCase())) {
                  completions.add(current.substring(startIndex).startsWith(" ") ? current.substring(startIndex + 1) : current.substring(startIndex));
               }
            }

            return completions;
         } catch (Exception var9) {
            return List.of();
         }
      }
   }

   @Override
   public int getRequiredArgs() {
      return 0;
   }

   @Override
   public String getRequiredPermission() {
      return "";
   }
}
