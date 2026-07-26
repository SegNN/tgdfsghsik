package ua.fichadev.fichaeventsun.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichaeventsun.FichaEventSun;
import ua.fichadev.fichaeventsun.util.ColorUtils;

public class LootCommand implements CommandExecutor, TabCompleter {
   private final FichaEventSun plugin;

   public LootCommand(FichaEventSun plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         return true;
      } else {
         String var5 = args[0].toLowerCase();
         switch (var5) {
            case "addloot":
               return this.handleAddLoot(sender, args);
            case "removeloot":
               return this.handleRemoveLoot(sender, args);
            case "list":
               return this.handleList(sender);
            default:
               return true;
         }
      }
   }

   private boolean handleAddLoot(CommandSender sender, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ColorUtils.colorize("это консоль"));
         return true;
      } else if (args.length < 2) {
         sender.sendMessage(ColorUtils.colorize("/fichasun addloot <ids>"));
         return true;
      } else {
         Player player = (Player)sender;
         ItemStack hand = player.getInventory().getItemInMainHand();
         if (hand != null && hand.getType() != Material.AIR) {
            String id = args[1].toLowerCase();
            if (this.plugin.getCustomLootManager().addItem(id, hand)) {
               String itemName = this.getItemDisplayName(hand);
               sender.sendMessage(ColorUtils.colorize("предмет " + itemName + " добавлен в лут с id: " + id));
            } else {
               sender.sendMessage(ColorUtils.colorize("предмет уже существует с таким id"));
            }

            return true;
         } else {
            sender.sendMessage(ColorUtils.colorize("возьмите предмет в руку"));
            return true;
         }
      }
   }

   private boolean handleRemoveLoot(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(ColorUtils.colorize("/fichasun removeloot <id>"));
         return true;
      } else {
         String id = args[1].toLowerCase();
         if (this.plugin.getCustomLootManager().removeItem(id)) {
            sender.sendMessage(ColorUtils.colorize("предмет с id " + id + " удален из лута"));
         } else {
            sender.sendMessage(ColorUtils.colorize("предмет с iD " + id + " не найден"));
         }

         return true;
      }
   }

   private boolean handleList(CommandSender sender) {
      List<String> ids = this.plugin.getCustomLootManager().getItemIds();
      if (ids.isEmpty()) {
         sender.sendMessage(ColorUtils.colorize("список лута пуст"));
         return true;
      } else {
         sender.sendMessage(ColorUtils.colorize("custom лут (&b" + ids.size() + "&f):"));

         for (String id : ids) {
            ItemStack item = this.plugin.getCustomLootManager().getItem(id);
            if (item != null) {
               String name = this.getItemDisplayName(item);
               int amount = item.getAmount();
               sender.sendMessage(ColorUtils.colorize(name + " &7- " + amount + " шт."));
            }
         }

         return true;
      }
   }

   private String getItemDisplayName(ItemStack item) {
      if (item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
         }
      }

      return item.getType().name();
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         return this.filterStartsWith(Arrays.asList("addloot", "removeloot", "list"), args[0]);
      } else {
         return (List<String>)(args.length == 2 && args[0].equalsIgnoreCase("removeloot")
            ? this.filterStartsWith(this.plugin.getCustomLootManager().getItemIds(), args[1])
            : new ArrayList<>());
      }
   }

   private List<String> filterStartsWith(List<String> list, String prefix) {
      String lower = prefix.toLowerCase();
      return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
   }
}
