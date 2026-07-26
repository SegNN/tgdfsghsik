package org.dimasik.liteauction.frontend.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.exceptions.NotAPlayerException;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.menus.bids.menus.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandExecutor implements TabExecutor {
   private final HashMap<String, SubCommand> subCommands = new HashMap<>();

   public CommandExecutor() {
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (sender instanceof Player player) {
         if (args.length < 1) {
            try {
               LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).thenAccept(guiData -> {
                  if (guiData.getAuctionType() != AuctionType.MARKET && ConfigManager.getBoolean("settings/settings.yml", "enable-bids", true)) {
                     Main main = new Main(1);
                     main.setPlayer(player);
                     main.setSortingType(guiData.getBidsSortingType());
                     main.setCategoryType(guiData.getCategoryType());
                     main.compile().open();
                  } else {
                     org.dimasik.liteauction.frontend.menus.market.menus.Main main = new org.dimasik.liteauction.frontend.menus.market.menus.Main(1);
                     main.setPlayer(player);
                     main.setSortingType(guiData.getMarketSortingType());
                     main.setCategoryType(guiData.getCategoryType());
                     main.compile().open();
                  }
               });
            } catch (Exception var8) {
               var8.printStackTrace();
               player.sendMessage(
                  Parser.color(ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."))
               );
            }

            return true;
         } else {
            String subCommandKey = args[0].toLowerCase();
            SubCommand subCommand = this.subCommands.get(subCommandKey);
            if (subCommand != null && (subCommand.getRequiredPermission().isEmpty() || player.hasPermission(subCommand.getRequiredPermission()))) {
               if (subCommand.getRequiredArgs() + 1 > args.length) {
                  return true;
               } else {
                  CompletableFuture.runAsync(() -> subCommand.execute(player, command, args));
                  return true;
               }
            } else {
               ConfigManager.getStringList("design/commands/main.yml", "usage-message").stream().map(Parser::color).forEach(player::sendMessage);
               return true;
            }
         }
      } else {
         return true;
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         for (SubCommand subCommand : this.subCommands.values()) {
            if (subCommand.getRequiredPermission().isEmpty() || commandSender.hasPermission(subCommand.getRequiredPermission())) {
               completions.add(subCommand.getName());
            }
         }
      } else {
         SubCommand subCommandx = this.subCommands.get(args[0]);
         if (subCommandx != null && (subCommandx.getRequiredPermission().isEmpty() || commandSender.hasPermission(subCommandx.getRequiredPermission()))) {
            try {
               completions.addAll(subCommandx.getTabCompletes(commandSender, args));
            } catch (NotAPlayerException var9) {
               completions.add("Exception : NotAPlayerException at executing " + subCommandx.getName());
            }
         }
      }

      List<String> completions1 = new ArrayList<>();

      for (String s : completions) {
         if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
            completions1.add(s);
         }
      }

      return completions1;
   }

   @Generated
   public HashMap<String, SubCommand> getSubCommands() {
      return this.subCommands;
   }
}
