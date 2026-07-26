package org.nezxenka.litecontainer.commands.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.litecontainer.commands.SubCommand;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.utils.ItemRarity;

public class AddItemSubCommand implements SubCommand {
   private final LootManager lootManager;

   public AddItemSubCommand(LootManager lootManager) {
      this.lootManager = lootManager;
   }

   @Override
   public void onCommand(CommandSender sender, String[] args) {
      if (sender instanceof Player player) {
         if (args.length < 4) {
            sender.sendMessage("§c ▶ §fИспользование: /container additem <название> <редкость> <стоимость>");
            return;
         }

         try {
            ItemRarity rarity = ItemRarity.valueOf(args[2].toUpperCase());
            double price = Double.parseDouble(args[3]);
            if (player.getInventory().getItemInMainHand() == null) {
               sender.sendMessage("§c ▶ §fВозьмите предмет в руку");
               return;
            }

            this.lootManager.addItem(args[1], rarity, price, player.getInventory().getItemInMainHand());
            sender.sendMessage("§b ▶ §fПредмет '§e" + args[1] + "§f' успешно добавлен");
         } catch (NumberFormatException var7) {
            sender.sendMessage("§c ▶ §fВведенное вами число некорректно");
         } catch (IllegalArgumentException var8) {
            String rarities = Arrays.stream(ItemRarity.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.joining("/"));
            sender.sendMessage("§c ▶ §fДоступные редкости: " + rarities);
         }
      } else {
         sender.sendMessage("§c ▶ §fЭта команда доступна только игрокам");
      }
   }

   @Override
   public List<String> onTabCompleter(CommandSender sender, String[] args) {
      return args.length == 3
         ? Arrays.stream(ItemRarity.values())
            .map(Enum::name)
            .map(String::toLowerCase)
            .filter(rarity -> rarity.startsWith(args[2].toLowerCase()))
            .collect(Collectors.toList())
         : Collections.emptyList();
   }
}
