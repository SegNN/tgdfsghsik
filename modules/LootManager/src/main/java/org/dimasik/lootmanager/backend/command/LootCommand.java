package org.dimasik.lootmanager.backend.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.dimasik.lootmanager.frontend.menus.Main;

public class LootCommand implements CommandExecutor, TabCompleter {
   private final LootManager plugin;

   public LootCommand(LootManager plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Only players can use this command!");
         return true;
      } else if (args.length == 0) {
         Player player = (Player)sender;
         new Main(1).setCurrentCategory("Все подряд").setCurrentFilterLevel(0).setPlayer((Player)sender).compile().open();
         return true;
      } else {
         String player = args[0].toLowerCase();
         switch (player) {
            case "additem":
               this.handleAddItem(sender, command, args);
               break;
            case "cloud":
               this.handleCloudCommand(sender, args);
               break;
            case "sync":
               this.handleSyncCommand(sender, args);
               break;
            case "delete":
               this.handleDeleteCommand(sender, args);
               break;
            case "addinv":
               this.handleAddInvCommand(sender, args);
               break;
            default:
               sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Неизвестная команда"));
         }

         return true;
      }
   }

   private void handleCloudCommand(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Использование: &#00D4FB/lootmanager cloud [upload|download|kill] [имя]"));
      } else {
         String var3 = args[1].toLowerCase();
         switch (var3) {
            case "upload":
               this.uploadConfig(sender, args[2]);
               break;
            case "download":
               this.downloadConfig(sender, args[2]);
               break;
            case "kill":
               this.deleteConfig(sender, args[2]);
               break;
            default:
               sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Неизвестное действие. Используйте &#FF2222upload/download/kill"));
         }
      }
   }

   private void uploadConfig(CommandSender sender, String name) {
      String configData = this.plugin.getItemsConfig().saveToString();
      this.plugin.getDatabaseManager().saveConfig(name, configData).thenAccept(success -> {
         if (success) {
            sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Конфигурация &#00D4FB" + name + " &#E7E7E7успешно загружена!"));
         } else {
            sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка при загрузке конфигурации"));
         }
      }).exceptionally(e -> {
         sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка при загрузке: &#FF2222" + e.getMessage()));
         return null;
      });
   }

   private void downloadConfig(CommandSender sender, String name) {
      this.plugin.getDatabaseManager().getConfig(name).thenAccept(configData -> {
         if (configData != null) {
            try {
               this.plugin.getItemManager().clearItems();
               this.plugin.getItemsConfig().loadFromString(configData);
               this.plugin.saveItemsConfig();
               this.plugin.getItemManager().loadItems();
               sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Конфигурация &#00D4FB" + name + " &#E7E7E7успешно скачана!"));
            } catch (InvalidConfigurationException var5) {
               sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка формата конфигурации"));
            }
         } else {
            sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Конфигурация &#00D4FB" + name + " &#E7E7E7не найдена."));
         }
      }).exceptionally(e -> {
         sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка при скачивании: &#FF2222" + e.getMessage()));
         return null;
      });
   }

   private void deleteConfig(CommandSender sender, String name) {
      this.plugin.getDatabaseManager().deleteConfig(name).thenAccept(success -> {
         if (success) {
            sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Конфигурация &#00D4FB" + name + " &#E7E7E7успешно удалена!"));
         } else {
            sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Конфигурация &#FF2222" + name + " &#E7E7E7не найдена."));
         }
      }).exceptionally(e -> {
         sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка при удалении: &#FF2222" + e.getMessage()));
         return null;
      });
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      String filter = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
      if (args.length == 1) {
         String[] commands = new String[]{"additem", "cloud", "sync", "delete", "addinv"};

         for (String cmd : commands) {
            if (cmd.toLowerCase().startsWith(filter)) {
               completions.add(cmd);
            }
         }
      } else if (args.length == 2) {
         String var16 = args[0].toLowerCase();
         switch (var16) {
            case "cloud":
               String[] cloudActions = new String[]{"upload", "download", "kill"};

               for (String action : cloudActions) {
                  if (action.toLowerCase().startsWith(filter)) {
                     completions.add(action);
                  }
               }
               break;
            case "additem":
            case "addinv":
               for (String categoryx : this.plugin.getItemManager().getAllCategories()) {
                  if (categoryx.toLowerCase().startsWith(filter)) {
                     completions.add(categoryx);
                  }
               }
               break;
            case "delete":
               for (String id : this.plugin.getItemManager().getAllItemIds()) {
                  if (id.toLowerCase().startsWith(filter)) {
                     completions.add(id);
                  }
               }
               break;
            case "sync":
               for (String category : this.plugin.getItemManager().getAllCategories()) {
                  if (category.toLowerCase().startsWith(filter)) {
                     completions.add(category);
                  }
               }
         }
      } else if (args.length == 3) {
         String var17 = args[0].toLowerCase();
         switch (var17) {
            case "cloud":
               try {
                  for (String name : this.plugin.getDatabaseManager().getConfigNames().get()) {
                     if (name.toLowerCase().startsWith(filter)) {
                        completions.add(name);
                     }
                  }
               } catch (Exception var15) {
               }
               break;
            case "additem":
            case "addinv":
               for (int i = 0; i <= 100; i += 10) {
                  String val = String.valueOf(i);
                  if (val.startsWith(filter)) {
                     completions.add(val);
                  }
               }
               break;
            case "sync":
               for (String categoryxx : this.plugin.getItemManager().getAllCategories()) {
                  if (categoryxx.toLowerCase().startsWith(filter)) {
                     completions.add(categoryxx);
                  }
               }
         }
      } else if (args.length == 4) {
         String var18 = args[0].toLowerCase();
         switch (var18) {
            case "additem":
               for (int ixxx = 1; ixxx <= 64; ixxx++) {
                  String val = String.valueOf(ixxx);
                  if (val.startsWith(filter)) {
                     completions.add(val);
                  }
               }
               break;
            case "addinv":
               for (int ixx = 1; ixx <= 100; ixx++) {
                  String val = String.valueOf(ixx);
                  if (val.startsWith(filter)) {
                     completions.add(val);
                  }
               }
               break;
            case "sync":
               for (int ix = -10; ix <= 10; ix++) {
                  String val = String.valueOf(ix);
                  if (val.startsWith(filter)) {
                     completions.add(val);
                  }
               }
         }
      } else if (args.length == 5 && args[0].equalsIgnoreCase("additem")) {
         for (int ixxxx = 0; ixxxx <= 100; ixxxx++) {
            String val = String.valueOf(ixxxx);
            if (val.startsWith(filter)) {
               completions.add(val);
            }
         }
      }

      return completions;
   }

   private void handleSyncCommand(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Использование: &#00D4FB/lootmanager sync [категория] [новая_категория] [увеличение_уровня]"));
      } else {
         String sourceCategory = args[1];
         String targetCategory = args[2];
         int levelIncrease = 0;
         if (args.length >= 4) {
            try {
               levelIncrease = Integer.parseInt(args[3]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Неверное значение увеличения уровня. Используется 0."));
            }
         }

         List<String> allCategories = this.plugin.getItemManager().getAllCategories();
         if (!allCategories.contains(sourceCategory)) {
            sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Категория &#FF2222" + sourceCategory + " &#E7E7E7не найдена."));
         } else {
            int syncedCount = this.plugin.getItemManager().syncCategory(sourceCategory, targetCategory, levelIncrease);
            if (syncedCount > 0) {
               String levelMsg = levelIncrease != 0 ? " (уровень увеличен на " + levelIncrease + ")" : "";
               sender.sendMessage(
                  Parser.color(
                     "&#00D4FB▶ &#E7E7E7Скопировано &#00D4FB"
                        + syncedCount
                        + " &#E7E7E7предметов из категории &#00D4FB"
                        + sourceCategory
                        + " &#E7E7E7в категорию &#00D4FB"
                        + targetCategory
                        + levelMsg
                  )
               );
            } else {
               sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7В категории &#FF2222" + sourceCategory + " &#E7E7E7нет предметов для синхронизации."));
            }
         }
      }
   }

   private void handleAddItem(CommandSender sender, Command command, String[] args) {
      if (args.length < 5) {
         sender.sendMessage(
            Parser.color("&#00D4FB▶ &#E7E7E7Использование: &#00D4FB/" + command.getName() + " additem [категория] [шанс] [макс.количество] [уровень]")
         );
      } else {
         Player player = (Player)sender;
         if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
            try {
               String category = args[1];
               double chance = Double.parseDouble(args[2]);
               int maxcount = Integer.parseInt(args[3]);
               int level = Integer.parseInt(args[4]);
               chance = Math.max(chance, 0.0);
               chance = Math.min(chance, 100.0);
               maxcount = Math.max(maxcount, 1);
               maxcount = Math.min(maxcount, 64);
               level = Math.max(level, 0);
               level = Math.min(level, 100);
               this.plugin.getItemManager().addItem(player.getItemInHand(), chance, maxcount, level, category);
               sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Предмет &#00D4FBдобавлен."));
            } catch (NumberFormatException var10) {
               sender.sendMessage(
                  Parser.color("&#00D4FB▶ &#E7E7E7Использование: &#00D4FB/" + command.getName() + " additem [категория] [шанс] [макс.количество] [уровень]")
               );
            }
         } else {
            sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Вы не можете добавить &#FF2222воздух."));
         }
      }
   }

   private void handleDeleteCommand(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Использование: &#00D4FB/lm delete [id]"));
      } else {
         String itemId = args[1];
         if (this.plugin.getItemManager().removeItem(itemId)) {
            sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Предмет с ID &#00D4FB" + itemId + " &#E7E7E7успешно удален."));
         } else {
            sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Предмет с ID &#FF2222" + itemId + " &#E7E7E7не найден."));
         }
      }
   }

   private void handleAddInvCommand(CommandSender sender, String[] args) {
      if (args.length < 4) {
         sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Использование: &#00D4FB/lm addinv [категория] [процент] [лвл]"));
      } else if (!(sender instanceof Player player)) {
         sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Только игроки могут использовать эту команду!"));
      } else {
         try {
            String category = args[1];
            double chance = Double.parseDouble(args[2]);
            int level = Integer.parseInt(args[3]);
            chance = Math.max(chance, 0.0);
            chance = Math.min(chance, 100.0);
            level = Math.max(level, 0);
            level = Math.min(level, 100);
            int addedCount = 0;
            ItemStack[] contents = player.getInventory().getContents();

            for (ItemStack item : contents) {
               if (item != null && item.getType() != Material.AIR) {
                  ItemStack itemToAdd = item.clone();
                  int maxAmount = item.getAmount();
                  maxAmount = Math.max(1, Math.min(maxAmount, 64));
                  itemToAdd.setAmount(1);
                  this.plugin.getItemManager().addItem(itemToAdd, chance, maxAmount, level, category);
                  addedCount++;
               }
            }

            if (addedCount > 0) {
               sender.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Добавлено &#00D4FB" + addedCount + " &#E7E7E7предметов из инвентаря."));
            } else {
               sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ваш инвентарь пуст."));
            }
         } catch (NumberFormatException var16) {
            sender.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Неверный формат числа. Использование: &#00D4FB/lm addinv [категория] [процент] [лвл]"));
         }
      }
   }
}
