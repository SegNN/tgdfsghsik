package ru.lolodimo4ka.holygoldspawner.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.lolodimo4ka.holygoldspawner.HolyGoldSpawner;
import ru.lolodimo4ka.holygoldspawner.managers.MessagesManager;
import ru.lolodimo4ka.holygoldspawner.utils.Item;

public class SpawnerCommand implements CommandExecutor, TabCompleter {
   private final HolyGoldSpawner plugin;
   private final Item itemManager;

   public SpawnerCommand(HolyGoldSpawner plugin, Item itemManager) {
      this.plugin = plugin;
      this.itemManager = itemManager;
      plugin.getCommand("holygoldspawner").setExecutor(this);
      plugin.getCommand("holygoldspawner").setTabCompleter(this);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!command.getName().equalsIgnoreCase("holygoldspawner")) {
         return false;
      } else if (args.length == 0) {
         this.sendHelp(sender);
         return true;
      } else {
         String subCommand = args[0].toLowerCase();
         switch (subCommand) {
            case "give":
               return this.giveCommand(sender, args);
            case "reload":
               return this.reloadCommand(sender);
            default:
               this.sendHelp(sender);
               return true;
         }
      }
   }

   private boolean giveCommand(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(this.plugin.getMessagesManager().commandUsageGive);
         return true;
      } else {
         String itemType = args[1].toLowerCase();
         switch (itemType) {
            case "spawner":
               return this.giveSpawnerCommand(sender, Arrays.copyOfRange(args, 2, args.length));
            case "pickaxe":
               return this.givePickaxeCommand(sender, Arrays.copyOfRange(args, 2, args.length));
            default:
               sender.sendMessage(this.plugin.getMessagesManager().commandUnknownItem.replace("%item%", args[1]));
               return true;
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("give");
         completions.add("reload");
      } else if (args.length == 2) {
         String firstArg = args[0].toLowerCase();
         if (firstArg.equals("give")) {
            completions.add("spawner");
            completions.add("pickaxe");
         }
      } else if (args.length == 3) {
         String firstArg = args[0].toLowerCase();
         if (firstArg.equals("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                  completions.add(player.getName());
               }
            }
         }
      } else if (args.length == 4) {
         String firstArg = args[0].toLowerCase();
         if (firstArg.equals("give") && args[1].equalsIgnoreCase("spawner")) {
            completions.add("<прочность>");
         } else if (firstArg.equals("give") && args[1].equalsIgnoreCase("pickaxe")) {
            completions.add("<количество>");
         }
      } else if (args.length == 5) {
         String firstArg = args[0].toLowerCase();
         if (firstArg.equals("give") && args[1].equalsIgnoreCase("spawner")) {
            completions.add("<количество>");
         }
      }

      return completions;
   }

   private void sendHelp(CommandSender sender) {
      MessagesManager messages = this.plugin.getMessagesManager();
      sender.sendMessage(messages.commandHelpHeader);
      sender.sendMessage(messages.commandHelpGiveSpawner);
      sender.sendMessage(messages.commandHelpGivePickaxe);
      sender.sendMessage(messages.commandHelpReload);
      sender.sendMessage(messages.commandHelpFooter);
   }

   private boolean giveSpawnerCommand(CommandSender sender, String[] args) {
      MessagesManager messages = this.plugin.getMessagesManager();
      if (!sender.hasPermission("holygoldspawner.give")) {
         sender.sendMessage(messages.noPermissionMessage);
         return true;
      } else if (args.length < 1) {
         sender.sendMessage(messages.commandUsageGiveSpawner);
         return true;
      } else {
         Player target = Bukkit.getPlayer(args[0]);
         if (target == null) {
            sender.sendMessage(messages.playerNotFoundMessage);
            return true;
         } else {
            int durability = this.plugin.getConfigManager().maxDurability;
            if (args.length > 1) {
               try {
                  durability = Integer.parseInt(args[1]);
                  durability = Math.max(1, Math.min(durability, this.plugin.getConfigManager().maxDurability));
               } catch (NumberFormatException var11) {
                  sender.sendMessage(messages.durabilityNumberError);
                  return true;
               }
            }

            int amount = 1;
            if (args.length > 2) {
               try {
                  amount = Integer.parseInt(args[2]);
                  amount = Math.max(1, amount);
               } catch (NumberFormatException var10) {
                  sender.sendMessage(messages.amountNumberError);
                  return true;
               }
            }

            int remainingAmount = amount;

            while (remainingAmount > 0) {
               int stackAmount = Math.min(remainingAmount, 64);
               ItemStack spawner = this.itemManager.createGoldenSpawner(durability);
               spawner.setAmount(stackAmount);
               if (target.getInventory().firstEmpty() == -1) {
                  target.getWorld().dropItemNaturally(target.getLocation(), spawner);
               } else {
                  target.getInventory().addItem(new ItemStack[]{spawner});
               }

               remainingAmount -= stackAmount;
            }

            sender.sendMessage(messages.spawnerGivenMessage.replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount)));
            return true;
         }
      }
   }

   private boolean givePickaxeCommand(CommandSender sender, String[] args) {
      MessagesManager messages = this.plugin.getMessagesManager();
      if (!sender.hasPermission("holygoldspawner.give")) {
         sender.sendMessage(messages.noPermissionMessage);
         return true;
      } else if (args.length < 1) {
         sender.sendMessage(messages.commandUsageGivePickaxe);
         return true;
      } else {
         Player target = Bukkit.getPlayer(args[0]);
         if (target == null) {
            sender.sendMessage(messages.playerNotFoundMessage);
            return true;
         } else {
            int amount = 1;
            if (args.length > 1) {
               try {
                  amount = Integer.parseInt(args[1]);
                  amount = Math.max(1, amount);
               } catch (NumberFormatException var10) {
                  sender.sendMessage(messages.amountNumberError);
                  return true;
               }
            }

            if (!this.plugin.isCustomEggEnabled()) {
               return true;
            } else {
               ItemStack jackhammer = this.plugin.getCustomEggPlugin().getJakePickaxe();
               int remainingAmount = amount;

               while (remainingAmount > 0) {
                  int stackAmount = Math.min(remainingAmount, 64);
                  ItemStack pickaxe = jackhammer.clone();
                  pickaxe.setAmount(stackAmount);
                  if (target.getInventory().firstEmpty() == -1) {
                     target.getWorld().dropItemNaturally(target.getLocation(), pickaxe);
                  } else {
                     target.getInventory().addItem(new ItemStack[]{pickaxe});
                  }

                  remainingAmount -= stackAmount;
               }

               sender.sendMessage(messages.jackhammerGivenMessage.replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount)));
               return true;
            }
         }
      }
   }

   private boolean reloadCommand(CommandSender sender) {
      MessagesManager messages = this.plugin.getMessagesManager();
      if (!sender.hasPermission("holygoldspawner.reload")) {
         sender.sendMessage(messages.noPermissionMessage);
         return true;
      } else {
         this.plugin.getConfigManager().reloadConfig();
         this.plugin.getMessagesManager().reloadMessages();
         this.plugin.getSpawnerManager().reloadConfig();
         this.plugin.getWorldGuardManager().reload();
         sender.sendMessage(messages.reloadSuccessMessage);
         return true;
      }
   }
}
