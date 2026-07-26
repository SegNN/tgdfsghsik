package ua.holymine.holymineskills.command;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holymine.holymineskills.database.DatabaseManager;
import ua.holymine.holymineskills.menu.MenuManager;
import ua.holymine.holymineskills.util.Logger;

public class CommandManager implements CommandExecutor {
   private final JavaPlugin plugin;
   private final MenuManager menuManager;
   private final DatabaseManager databaseManager;

   public CommandManager(JavaPlugin plugin, MenuManager menuManager, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.menuManager = menuManager;
      this.databaseManager = databaseManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      try {
         return command.getName().equalsIgnoreCase("mineskills") ? this.handleMineSkillsCommand(sender, args) : false;
      } catch (Exception var6) {
         Logger.severe("Ошибка при выполнении команды: " + var6.getMessage());
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l▶ &fПроизошла ошибка при выполнении команды!"));
         return true;
      }
   }

   private boolean handleMineSkillsCommand(CommandSender sender, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l▶ &fЭта команда только для игроков!"));
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length == 0) {
            this.menuManager.openMenu(player);
            return true;
         } else if (!player.hasPermission("holymineskills.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l▶ &fУ вас нет прав для использования этой команды!"));
            return true;
         } else if (args.length < 2 && !args[0].equalsIgnoreCase("openmenu")) {
            this.sendHelp(player);
            return true;
         } else if (args[0].equalsIgnoreCase("openmenu")) {
            this.menuManager.openMenu(player);
            return true;
         } else {
            return this.handleAdminCommand(player, args);
         }
      }
   }

   private boolean handleAdminCommand(Player player, String[] args) {
      try {
         OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
         if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&l▶ &fИгрок &6" + args[1] + " &fне найден!"));
            return true;
         } else {
            double points = args.length > 2 ? this.parseDouble(args[2]) : 1.0;
            String var6 = args[0].toLowerCase();
            switch (var6) {
               case "add":
                  this.addPoints(target, points);
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes('&', String.format("&a&l▶ &fДобавлено &a%.1f &fочков игроку &6%s", points, target.getName()))
                  );
                  break;
               case "remove":
                  this.removePoints(target, points);
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes('&', String.format("&a&l▶ &fУбрано &a%.1f &fочков у игрока &6%s", points, target.getName()))
                  );
                  break;
               case "get":
                  this.getPoints(target, player);
                  break;
               case "reset":
                  this.resetPoints(target);
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l▶ &fОчки игрока &6" + target.getName() + " &fсброшены!"));
                  break;
               default:
                  this.sendHelp(player);
                  return true;
            }

            return true;
         }
      } catch (Exception var81) {
         Logger.severe("Ошибка при выполнении административной команды: " + var81.getMessage());
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l▶ &fПроизошла ошибка при выполнении команды!"));
         return true;
      }
   }

   private void addPoints(OfflinePlayer target, double points) {
      try {
         CompletableFuture<Void> future = this.databaseManager.addPointsAsync(target.getUniqueId(), target.getName(), points);
         future.thenRunAsync(() -> {});
      } catch (Exception var6) {
         String var10000 = target.getName();
         Logger.severe("Ошибка при добавлении очков игроку " + var10000 + ": " + var6.getMessage());
      }
   }

   private void removePoints(OfflinePlayer target, double points) {
      try {
         CompletableFuture<Void> future = this.databaseManager.addPointsAsync(target.getUniqueId(), target.getName(), -points);
         future.thenRunAsync(() -> {});
      } catch (Exception var6) {
         String var10000 = target.getName();
         Logger.severe("Ошибка при удалении очков у игрока " + var10000 + ": " + var6.getMessage());
      }
   }

   private void getPoints(OfflinePlayer target, Player admin) {
      try {
         CompletableFuture<Double> future = this.databaseManager.getPointsAsync(target.getUniqueId());
         future.thenAcceptAsync(
            points -> {
               double playerPoints = points != null ? points : 0.0;
               admin.sendMessage(
                  ChatColor.translateAlternateColorCodes('&', String.format("&a&l▶ &fУ игрока &6%s&f: &a%.1f &fочков", target.getName(), playerPoints))
               );
            }
         );
      } catch (Exception var5) {
         String var10000 = target.getName();
         Logger.severe("Ошибка при получении очков игрока " + var10000 + ": " + var5.getMessage());
         admin.sendMessage(ChatColor.RED + "Ошибка при получении очков!");
      }
   }

   private void resetPoints(OfflinePlayer target) {
      try {
         CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(target.getUniqueId());
         pointsFuture.thenAcceptAsync(currentPoints -> {
            if (currentPoints != null && currentPoints > 0.0) {
               this.databaseManager.addPointsAsync(target.getUniqueId(), target.getName(), -currentPoints).thenRunAsync(() -> {});
            }
         });
      } catch (Exception var4) {
         String var10000 = target.getName();
         Logger.severe("Ошибка при сбросе очков игрока " + var10000 + ": " + var4.getMessage());
      }
   }

   private void sendHelp(Player player) {
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l▶ &fКоманды HolyMineSkills:"));
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f  &6/mineskills&f - Открыть меню бонусов"));
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f  &6/mineskills openmenu&f - Открыть меню бонусов"));
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f  &6/mineskills add <игрок> [количество]&f - Добавить очки"));
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f  &6/mineskills remove <игрок> [количество]&f - Убрать очки"));
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f  &6/mineskills get <игрок>&f - Посмотреть очки игрока"));
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f  &6/mineskills reset <игрок>&f - Сбросить очки игрока"));
   }

   private double parseDouble(String value) {
      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException var3) {
         return 1.0;
      }
   }
}
