package ua.holydev.holyliteshop.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.data.ShopItem;
import ua.holydev.holyliteshop.utils.ColorUtils;
import ua.holydev.holyliteshop.utils.MessageUtils;

public class LiteShopCommand implements CommandExecutor, TabCompleter {
   private final HolyLiteShop plugin;

   public LiteShopCommand(HolyLiteShop plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("liteshop.admin")) {
         sender.sendMessage(MessageUtils.getMessage(this.plugin, "no_permission"));
         return true;
      } else if (args.length == 0) {
         this.sendHelp(sender);
         return true;
      } else {
         String var5 = args[0].toLowerCase();
         switch (var5) {
            case "add":
               return this.handleAdd(sender, args);
            case "list":
               this.plugin.getDataManager().sendItemsList(sender);
               break;
            case "remove":
               return this.handleRemove(sender, args);
            case "refresh":
               this.plugin.getShopManager().refreshAllPlayers();
               sender.sendMessage(MessageUtils.getMessage(this.plugin, "items_refreshed_all"));
               break;
            default:
               this.sendHelp(sender);
         }

         return true;
      }
   }

   private boolean handleAdd(CommandSender sender, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(MessageUtils.getMessage(this.plugin, "players_only"));
         return true;
      } else {
         Player player = (Player)sender;
         ItemStack item = player.getInventory().getItemInMainHand();
         if (item != null && !item.getType().isAir()) {
            if (args.length < 3) {
               player.sendMessage(MessageUtils.getMessage(this.plugin, "add_usage"));
               return true;
            } else {
               String type = args[1].toLowerCase();
               if (!type.equals("shop") && !type.equals("auction")) {
                  player.sendMessage(MessageUtils.getMessage(this.plugin, "invalid_type"));
                  return true;
               } else {
                  try {
                     int price = Integer.parseInt(args[2]);
                     boolean isAuction = type.equals("auction");
                     int stepPrice = 0;
                     boolean isSapphire = true;
                     if (isAuction) {
                        if (args.length < 5) {
                           player.sendMessage(MessageUtils.getMessage(this.plugin, "auction_usage"));
                           return true;
                        }

                        String currency = args[3].toLowerCase();
                        if (!currency.equals("sapphire") && !currency.equals("money")) {
                           player.sendMessage(MessageUtils.getMessage(this.plugin, "invalid_currency"));
                           return true;
                        }

                        isSapphire = currency.equals("sapphire");
                        stepPrice = Integer.parseInt(args[4]);
                     }

                     String itemName = this.plugin.getDataManager().addShopItem(item, price, isAuction, stepPrice, isSapphire);
                     String typeText = isAuction ? "аукционный" : "обычный";
                     String message = MessageUtils.getMessage(this.plugin, "item_added")
                        .replace("%type%", typeText)
                        .replace("%itemName%", itemName)
                        .replace("%price%", String.valueOf(price));
                     player.sendMessage(message);
                  } catch (NumberFormatException var13) {
                     player.sendMessage(MessageUtils.getMessage(this.plugin, "invalid_price"));
                  }

                  return true;
               }
            }
         } else {
            player.sendMessage(MessageUtils.getMessage(this.plugin, "hold_item"));
            return true;
         }
      }
   }

   private boolean handleRemove(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(MessageUtils.getMessage(this.plugin, "remove_usage"));
         return true;
      } else {
         String itemName = args[1];
         if (this.plugin.getDataManager().removeShopItem(itemName)) {
            String msg = MessageUtils.getMessage(this.plugin, "item_removed").replace("%itemName%", itemName);
            sender.sendMessage(msg);
         } else {
            String msg = MessageUtils.getMessage(this.plugin, "item_not_found").replace("%itemName%", itemName);
            sender.sendMessage(msg);
         }

         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("liteshop.admin")) {
         return completions;
      } else if (args.length == 1) {
         completions.add("add");
         completions.add("list");
         completions.add("remove");
         completions.add("refresh");
         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else if (args.length != 2) {
         if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            completions.add("<цена>");
         }

         if (args.length == 4 && args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("auction")) {
            completions.add("sapphire");
            completions.add("money");
         }

         if (args.length == 5 && args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("auction")) {
            completions.add("<цена_шага>");
         }

         return completions;
      } else {
         if (args[0].equalsIgnoreCase("add")) {
            completions.add("shop");
            completions.add("auction");
         } else if (args[0].equalsIgnoreCase("remove")) {
            for (ShopItem item : this.plugin.getDataManager().getAvailableItems()) {
               completions.add(item.getName());
            }
         }

         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
      }
   }

   private void sendHelp(CommandSender sender) {
      sender.sendMessage(ColorUtils.color("/liteshop add shop <цена> - Добавить предмет в магазин"));
      sender.sendMessage(ColorUtils.color("/liteshop add auction <цена> <sapphire|money> <цена_шага> - Добавить предмет на аукцион"));
      sender.sendMessage(ColorUtils.color("/liteshop list - Список всех предметов"));
      sender.sendMessage(ColorUtils.color("/liteshop remove <название> - Удалить предмет"));
      sender.sendMessage(ColorUtils.color("/liteshop refresh - Обновить ассортимент"));
   }
}
