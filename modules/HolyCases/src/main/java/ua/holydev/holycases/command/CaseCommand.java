package ua.holydev.holycases.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jdbi.v3.core.async.JdbiExecutor;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.cases.CaseManager;
import ua.holydev.holycases.database.dao.PlayerKeysDao;
import ua.holydev.holycases.database.dao.PlayerLuckDao;
import ua.holydev.holycases.lucks.LuckManager;
import ua.holydev.holycases.util.ItemBuilder;

public class CaseCommand implements CommandExecutor {
   private JdbiExecutor executor;
   private CaseManager caseManager;
   private LuckManager luckManager;
   private HolyCases holyCases;

   public CaseCommand(HolyCases plugin, CaseManager caseManager, LuckManager luckManager, JdbiExecutor executor) {
      this.executor = executor;
      this.caseManager = caseManager;
      this.luckManager = luckManager;
      this.holyCases = plugin;
      plugin.getCommand("holycases").setExecutor(this);
      plugin.getCommand("holycases").setTabCompleter(new CaseCommand.CaseCommandTabCompleter(caseManager, luckManager));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
         this.sendUsage(sender);
         return true;
      } else if (args[0].equalsIgnoreCase("place")) {
         if (args.length < 2) {
            sender.sendMessage("§cНедостаточно аргументов!");
            return true;
         } else if (!sender.hasPermission("holycases.command.place")) {
            sender.sendMessage("§cНет прав!");
            return true;
         } else if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
         } else if (this.caseManager.getCaseHolder(args[1]) == null) {
            sender.sendMessage("§cКейс §6" + args[1] + " §cне найден");
            return true;
         } else {
            ItemStack placer = new ItemBuilder(Material.ENDER_CHEST)
               .name(args[1])
               .addLore("§eПКМ по блоку, чтобы сделать его кейсом")
               .addLore("§eЛКМ по сундуку, чтобы удалить его")
               .build();
            ((Player)sender).getInventory().addItem(new ItemStack[]{placer});
            sender.sendMessage("§aПлейсер добавлен в ваш инвентарь");
            return true;
         }
      } else if (!args[0].equalsIgnoreCase("item")) {
         if (args.length == 5 || args.length == 7) {
            if (args[0].equalsIgnoreCase("keys")) {
               if (!sender.hasPermission("holycases.command.keys")) {
                  sender.sendMessage("§cНет прав!");
                  return true;
               }

               if (this.caseManager.getCaseHolder(args[1]) == null) {
                  sender.sendMessage("§cКейс §6" + args[1] + " §cне найден");
                  return true;
               }

               if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("withdraw")) {
                  OfflinePlayer offlinePlayer = Bukkit.getPlayer(args[3]);
                  if (offlinePlayer == null) {
                     offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
                  }

                  if (!isDigits(args[4])) {
                     sender.sendMessage("§6" + args[4] + " §7- §cне число");
                     return true;
                  }

                  if (Integer.parseInt(args[4]) <= 0) {
                     sender.sendMessage("&7Укажите положительное целое число!");
                     return true;
                  }

                  int count = args[2].equalsIgnoreCase("withdraw") ? -Integer.parseInt(args[4]) : Integer.parseInt(args[4]);
                  if (args.length == 7) {
                     if (!isDigits(args[6])) {
                        sender.sendMessage("§6" + args[6] + " §7- §cне число");
                        return true;
                     }

                     if (Integer.parseInt(args[6]) <= 0) {
                        sender.sendMessage("&7Укажите положительное целое число!");
                        return true;
                     }
                  }

                  UUID uuid = offlinePlayer.getUniqueId();
                  if (this.holyCases.isSQL()) {
                     this.executor.useExtension(PlayerKeysDao.class, dao -> dao.addKeys(uuid, args[1], count));
                  } else {
                     this.holyCases.addKeys(offlinePlayer, args[1], count);
                  }

                  sender.sendMessage("Изменили количество ключей от кейса " + args[1] + " у игрока " + args[3] + " на " + count);
                  return true;
               }
            }

            if (args[0].equalsIgnoreCase("luck")) {
               if (!sender.hasPermission("holycases.command.luck")) {
                  sender.sendMessage("§cНет прав!");
                  return true;
               }

               if (!this.luckManager.hasLuck(args[1])) {
                  sender.sendMessage("§cУдача §6" + args[1] + " §cне найдена");
                  return true;
               }

               if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("withdraw")) {
                  OfflinePlayer offlinePlayerx = Bukkit.getPlayer(args[3]);
                  if (offlinePlayerx == null) {
                     offlinePlayerx = Bukkit.getOfflinePlayer(args[3]);
                  }

                  if (!isDigits(args[4])) {
                     sender.sendMessage("§6" + args[4] + " §7- §cне число");
                     return true;
                  }

                  if (Integer.parseInt(args[4]) <= 0) {
                     sender.sendMessage("&7Укажите положительное целое число!");
                     return true;
                  }

                  int countx = args[2].equalsIgnoreCase("withdraw") ? -Integer.parseInt(args[4]) : Integer.parseInt(args[4]);
                  if (args.length == 7) {
                     if (!isDigits(args[6])) {
                        sender.sendMessage("§6" + args[6] + " §7- §cне число");
                        return true;
                     }

                     if (Integer.parseInt(args[6]) <= 0) {
                        sender.sendMessage("&7Укажите положительное целое число!");
                        return true;
                     }
                  }

                  UUID uuid = offlinePlayerx.getUniqueId();
                  if (this.holyCases.isSQL()) {
                     this.executor.useExtension(PlayerLuckDao.class, dao -> dao.addLuck(uuid, args[1], countx));
                  } else {
                     this.holyCases.addLuck(offlinePlayerx, args[1], countx);
                  }

                  sender.sendMessage("Изменили количество удачи " + args[1] + " у игрока " + args[3] + " на " + countx);
                  return true;
               }
            }
         }

         if (args.length == 4) {
            if (args[0].equalsIgnoreCase("keys")) {
               if (!sender.hasPermission("holycases.command.keys")) {
                  sender.sendMessage("§cНет прав!");
                  return true;
               }

               if (this.caseManager.getCaseHolder(args[1]) == null) {
                  sender.sendMessage("§cКейс §6" + args[1] + " §cне найден");
                  return true;
               }

               if (args[2].equalsIgnoreCase("count")) {
                  OfflinePlayer offlinePlayerxx = Bukkit.getPlayer(args[3]);
                  if (offlinePlayerxx == null) {
                     offlinePlayerxx = Bukkit.getOfflinePlayer(args[3]);
                  }

                  UUID uuid = offlinePlayerxx.getUniqueId();
                  if (this.holyCases.isSQL()) {
                     this.executor
                        .withExtension(PlayerKeysDao.class, dao -> dao.getKeys(uuid, args[1]))
                        .whenComplete((countxx, ex) -> sender.sendMessage("У игрока " + args[3] + " - " + countxx + " ключей от кейса " + args[1]));
                  } else {
                     int countxx = this.holyCases.getKeys(offlinePlayerxx, args[1]);
                     sender.sendMessage("У игрока " + args[3] + " - " + countxx + " ключей от кейса " + args[1]);
                  }

                  return true;
               }
            }

            if (args[0].equalsIgnoreCase("luck")) {
               if (!sender.hasPermission("holycases.command.luck")) {
                  sender.sendMessage("§cНет прав!");
                  return true;
               }

               if (!this.luckManager.hasLuck(args[1])) {
                  sender.sendMessage("§cУдача §6" + args[1] + " §cне найдена");
                  return true;
               }

               if (args[2].equalsIgnoreCase("count")) {
                  OfflinePlayer offlinePlayerxxx = Bukkit.getPlayer(args[3]);
                  if (offlinePlayerxxx == null) {
                     offlinePlayerxxx = Bukkit.getOfflinePlayer(args[3]);
                  }

                  UUID uuid = offlinePlayerxxx.getUniqueId();
                  if (this.holyCases.isSQL()) {
                     this.executor
                        .withExtension(PlayerLuckDao.class, dao -> dao.getLuck(uuid, args[1]))
                        .whenComplete((countxx, ex) -> sender.sendMessage("У игрока " + args[3] + " - " + countxx + " удачи " + args[1]));
                  } else {
                     int countxx = this.holyCases.getLucks(offlinePlayerxxx, args[1]);
                     sender.sendMessage("У игрока " + args[3] + " - " + countxx + " удачи " + args[1]);
                  }

                  return true;
               }
            }
         }

         return true;
      } else if (args.length < 2) {
         sender.sendMessage("§cНедостаточно аргументов!");
         return true;
      } else if (!sender.hasPermission("holycases.command.item")) {
         sender.sendMessage("§cНет прав!");
         return true;
      } else if (sender instanceof Player player) {
         if (this.caseManager.getCaseHolder(args[1]) == null) {
            sender.sendMessage("§cКейс §6" + args[1] + " §cне найден");
            return true;
         } else {
            ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
            if (itemStack.getType().isAir()) {
               sender.sendMessage("§cВы должны взять предмет в руку!");
               return true;
            } else {
               itemStack.setAmount(1);
               this.holyCases.setItemStack(args[1], itemStack);
               sender.sendMessage("§aУстановлен предмет для кейса §6" + args[1]);
               return true;
            }
         }
      } else {
         sender.sendMessage("§cТолько для игроков!");
         return true;
      }
   }

   private void sendUsage(CommandSender sender) {
      sender.sendMessage("§6§lКЕЙСЫ: \n");
      if (sender.hasPermission("holycases.command.item")) {
         sender.sendMessage("§f/holycases §6item §f<§6название_кейса§f> - Установить предмет для кейса (для типа открытия item)");
      }

      if (sender.hasPermission("holycases.command.keys")) {
         sender.sendMessage(
            "§f/holycases §6keys §f<§6название_кейса§f> §f<§6add§f/§6withdraw§f> <§6никнейм_игрока§f> <§6кол-во§f> - Добавить/Убрать ключи у игрока"
         );
      }

      if (sender.hasPermission("holycases.command.keys")) {
         sender.sendMessage("§f/holycases §6keys §f<§6название_кейса§f> §6count §f<§6никнейм_игрока§f> - Получить количество ключей у игрока");
      }

      if (sender.hasPermission("holycases.command.luck")) {
         sender.sendMessage(
            "§f/holycases §6luck §f<§6название_удачи§f> §f<§6add§f/§6withdraw§f> <§6никнейм_игрока§f> <§6кол-во§f> - Добавить/Убрать удачу у игрока"
         );
      }

      if (sender.hasPermission("holycases.command.luck")) {
         sender.sendMessage("§f/holycases §6luck §f<§6название_удачи§f> §6count §f<§6никнейм_игрока§f> - Получить количество удачи у игрока");
      }

      if (sender.hasPermission("holycases.command.place")) {
         sender.sendMessage("§f/holycases §6place §f<§6название_кейса§f> - Получить блок для установки кейса");
      }

      sender.sendMessage("§8Если вы ничего не получили, значит у вас нет прав на использование команд плагина.");
   }

   private static class CaseCommandTabCompleter implements TabCompleter {
      private CaseManager caseManager;
      private LuckManager luckManager;

      public CaseCommandTabCompleter(CaseManager caseManager, LuckManager luckManager) {
         this.caseManager = caseManager;
         this.luckManager = luckManager;
      }

      public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
         List<String> subs = new ArrayList<>();
         if (args.length == 1) {
            if (sender.hasPermission("holycases.command.item")) {
               subs.add("item");
            }

            if (sender.hasPermission("holycases.command.keys")) {
               subs.add("keys");
            }

            if (sender.hasPermission("holycases.command.luck")) {
               subs.add("luck");
            }

            if (sender.hasPermission("holycases.command.place")) {
               subs.add("place");
            }

            return (List<String>)StringUtil.copyPartialMatches(args[0], subs, new ArrayList());
         } else if (args.length == 2) {
            if (sender.hasPermission("holycases.command.keys") && args[0].equalsIgnoreCase("keys")
               || sender.hasPermission("holycases.command.place") && args[0].equalsIgnoreCase("place")
               || sender.hasPermission("holycases.command.item") && args[0].equalsIgnoreCase("item")) {
               this.caseManager.getCaseNames().forEach(caseName -> subs.add(caseName));
            }

            if (sender.hasPermission("holycases.command.luck") && args[0].equalsIgnoreCase("luck")) {
               this.luckManager.getLuckNames().forEach(luckName -> subs.add(luckName));
            }

            return (List<String>)StringUtil.copyPartialMatches(args[1], subs, new ArrayList());
         } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("keys") && sender.hasPermission("holycases.command.keys")
               || args[0].equalsIgnoreCase("luck") && sender.hasPermission("holycases.command.luck")) {
               subs.add("add");
               subs.add("withdraw");
               subs.add("count");
            }

            return (List<String>)StringUtil.copyPartialMatches(args[2], subs, new ArrayList());
         } else if ((args.length == 6 || args.length == 7) && (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("withdraw"))) {
            return args.length == 7
               ? (List)StringUtil.copyPartialMatches(args[6], Arrays.asList("1", "2", "3", "4", "5"), new ArrayList())
               : (List)StringUtil.copyPartialMatches(args[5], subs, new ArrayList());
         } else {
            return null;
         }
      }
   }

   /**
    * Replacement for {@code org.apache.commons.lang3.math.NumberUtils#isDigits(String)}, which is
    * no longer reachable through the server API. Same contract: null/empty is not a digit string.
    */
   private static boolean isDigits(String str) {
      if (str == null || str.isEmpty()) {
         return false;
      } else {
         for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }
}
