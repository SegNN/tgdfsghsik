package org.dimasik.holyenchantments.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.holyenchantments.enchantscore.EnchantManager;
import org.dimasik.holyenchantments.enchantscore.MEnchant;

public class EnchantCommand extends AbstractCommand {
   private final EnchantManager enchantManager;

   public EnchantCommand(EnchantManager enchantManager) {
      super("holyenchantments");
      this.enchantManager = enchantManager;
   }

   @Override
   public void execute(CommandSender sender, String label, String[] args) {
      if (sender.hasPermission("holyenchantments.admin")) {
         if (args.length == 0) {
            sender.sendMessage("/holyenchantments enchant <Ник> <Чар> <Уровень>");
            sender.sendMessage("/holyenchantments give <Чар> <Уровень>");
            sender.sendMessage("/holyenchantments unbreakable <true/false>");
         } else if (args[0].equalsIgnoreCase("enchant")) {
            if (args.length < 4) {
               sender.sendMessage("/holyenchantments enchant <Ник> <Чар> <Уровень>");
            } else {
               Player player = Bukkit.getPlayer(args[1]);
               if (player == null) {
                  sender.sendMessage("Игрок не найден!");
               } else {
                  MEnchant menchant = this.enchantManager.getEnchantsNameSpaced().get(this.enchantManager.getEnchantsNames().get(args[2].toLowerCase()));
                  if (menchant == null) {
                     sender.sendMessage("Доступные зачарования: " + String.join(", ", this.enchantManager.getEnchantsNames().keySet()));
                  } else {
                     ItemStack item = player.getInventory().getItemInMainHand();
                     if (!item.getType().isAir() && menchant.getTools().contains(item.getType())) {
                        int level;
                        try {
                           level = Integer.parseInt(args[3]);
                        } catch (NumberFormatException var11) {
                           sender.sendMessage("Уровень указан неверно");
                           return;
                        }

                        item.setItemMeta(this.enchantManager.applyEnchant(item.getItemMeta(), menchant, level));
                        player.setItemInHand(item);
                        player.updateInventory();
                        sender.sendMessage("Зачарование успешно наложено!");
                     } else {
                        sender.sendMessage("У игрока в руке предмет, на который нельзя наложить зачарование!");
                     }
                  }
               }
            }
         } else if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
               sender.sendMessage("/holyenchantments give <Чар> <Уровень>");
            } else {
               Player player = (Player)sender;

               int level;
               try {
                  level = Integer.parseInt(args[2]);
               } catch (NumberFormatException var10) {
                  sender.sendMessage("Уровень указан неверно");
                  return;
               }

               MEnchant menchant = this.enchantManager.getEnchantsNameSpaced().get(this.enchantManager.getEnchantsNames().get(args[1].toLowerCase()));
               if (menchant == null) {
                  sender.sendMessage("Доступные зачарования: " + String.join(", ", this.enchantManager.getEnchantsNames().keySet()));
               } else {
                  player.getInventory().addItem(new ItemStack[]{this.enchantManager.getEnchantBook(menchant, level)});
                  sender.sendMessage("Книга с зачарованием выдана!");
               }
            }
         } else if (args[0].equalsIgnoreCase("unbreakable")) {
            if (args.length < 2) {
               sender.sendMessage("/holyenchantments unbreakable <true/false>");
            } else {
               Player player = (Player)sender;

               boolean unbreakable;
               try {
                  unbreakable = Boolean.parseBoolean(args[1]);
               } catch (Exception var9) {
                  sender.sendMessage("Параметр указан неверно");
                  return;
               }

               ItemStack item = player.getInventory().getItemInMainHand();
               if (item.getType().isAir()) {
                  sender.sendMessage("У игрока в руке предмет, на который нельзя наложить неразрушимость!");
                  return;
               }

               ItemMeta itemMeta = item.getItemMeta();
               itemMeta.setUnbreakable(unbreakable);
               item.setItemMeta(itemMeta);
               sender.sendMessage("OK.");
            }
         }
      }
   }
}
