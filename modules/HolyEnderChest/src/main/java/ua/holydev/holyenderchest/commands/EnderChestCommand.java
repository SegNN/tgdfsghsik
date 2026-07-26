package ua.holydev.holyenderchest.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ua.holydev.holyenderchest.HolyEnderChest;
import ua.holydev.holyenderchest.config.ConfigManager;
import ua.holydev.holyenderchest.database.DatabaseManager;
import ua.holydev.holyenderchest.managers.EnderChestManager;

public class EnderChestCommand implements CommandExecutor, TabCompleter {
   private final HolyEnderChest plugin;
   private final ConfigManager configManager;
   private final DatabaseManager databaseManager;
   private final EnderChestManager enderChestManager;

   public EnderChestCommand(HolyEnderChest plugin) {
      this.plugin = plugin;
      this.configManager = plugin.getConfigManager();
      this.databaseManager = plugin.getDatabaseManager();
      this.enderChestManager = plugin.getEnderChestManager();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         if (!(sender instanceof Player player)) {
            sender.sendMessage(this.configManager.getMessage("player"));
            return true;
         } else if (!player.hasPermission("holyenderchest.ec")) {
            player.sendMessage(this.configManager.getMessage("permission"));
            return true;
         } else {
            this.enderChestManager.openEnderChest(player);
            return true;
         }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
         if (!sender.hasPermission("holyenderchest.ec.admin")) {
            sender.sendMessage(this.configManager.getMessage("permission"));
            return true;
         } else {
            this.showHelp(sender);
            return true;
         }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
         if (!sender.hasPermission("holyenderchest.ec.admin")) {
            sender.sendMessage(this.configManager.getMessage("permission"));
            return true;
         } else {
            this.plugin.reloadConfig();
            this.configManager.loadConfig();
            sender.sendMessage("§aКонфигурация HolyEnderChest перезагружена!");
            return true;
         }
      } else if (args.length == 1) {
         if (sender instanceof Player viewer) {
            String targetName = args[0];
            Player var19 = Bukkit.getPlayerExact(targetName);
            if (!viewer.hasPermission("holyenderchest.ec.view")) {
               viewer.sendMessage(this.configManager.getMessage("permission"));
               return true;
            } else if (var19 != null && this.databaseManager.playerExists(targetName)) {
               boolean allowTake = viewer.hasPermission("holyenderchest.ec.take");
               Inventory readOnlyInventory = this.enderChestManager.getReadOnlyInventory(var19, allowTake);
               if (allowTake) {
                  this.enderChestManager.trackViewedChest(viewer, var19.getUniqueId(), readOnlyInventory);
               }

               viewer.openInventory(readOnlyInventory);
               return true;
            } else {
               viewer.sendMessage(this.configManager.getMessage("player_not_found"));
               return true;
            }
         } else {
            sender.sendMessage(this.configManager.getMessage("player"));
            return true;
         }
      } else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
         if (!sender.hasPermission("holyenderchest.ec.admin")) {
            sender.sendMessage(this.configManager.getMessage("permission"));
            return true;
         } else {
            String targetName = args[1];
            if (!this.databaseManager.playerExists(targetName)) {
               sender.sendMessage(this.configManager.getMessage("player_not_found"));
               return true;
            } else {
               int currentSlots = this.databaseManager.getPlayerSlots(targetName);
               int maxSlots = this.configManager.getInventorySize();
               sender.sendMessage("§7=== §e" + targetName + " §7===");
               sender.sendMessage("§7Разблокировано слотов: §a" + currentSlots + "§7/§e" + maxSlots);
               sender.sendMessage("§7Доступно для разблокировки: §a" + (maxSlots - currentSlots));
               return true;
            }
         }
      } else if (args.length != 3 || !args[0].equalsIgnoreCase("unlock")) {
         sender.sendMessage(this.configManager.getMessage("permission"));
         return true;
      } else if (!sender.hasPermission("holyenderchest.ec.admin")) {
         sender.sendMessage(this.configManager.getMessage("permission"));
         return true;
      } else {
         String targetName = args[1];
         String slotsRaw = args[2];

         int slots;
         try {
            slots = Integer.parseInt(slotsRaw);
         } catch (NumberFormatException var12) {
            sender.sendMessage(this.configManager.getMessage("invalid_number"));
            return true;
         }

         if (slots <= 0) {
            sender.sendMessage(this.configManager.getMessage("invalid_number"));
            return true;
         } else if (!this.databaseManager.playerExists(targetName)) {
            sender.sendMessage(this.configManager.getMessage("player_not_found"));
            return true;
         } else {
            int currentSlots = this.databaseManager.getPlayerSlots(targetName);
            int maxSlots = this.configManager.getInventorySize();
            int availableSlots = maxSlots - currentSlots;
            if (availableSlots <= 0) {
               sender.sendMessage(this.configManager.getMessage("all_slots_unlocked"));
               return true;
            } else if (slots > availableSlots) {
               sender.sendMessage(this.configManager.getMessage("not_enough_available").replace("%available%", String.valueOf(availableSlots)));
               return true;
            } else {
               this.databaseManager.addSlotsToPlayer(targetName, slots);
               sender.sendMessage(this.configManager.getMessage("slots_given").replace("%slots%", String.valueOf(slots)).replace("%player%", targetName));
               Player targetPlayer = Bukkit.getPlayer(targetName);
               if (targetPlayer != null) {
                  this.enderChestManager.removePlayerInventory(targetPlayer);
                  if (targetPlayer.getOpenInventory().getTitle().equals(this.configManager.getInventoryTitle())) {
                     targetPlayer.closeInventory();
                     this.enderChestManager.openEnderChest(targetPlayer);
                  }
               }

               return true;
            }
         }
      }
   }

   private void showHelp(CommandSender sender) {
      sender.sendMessage("§7=== §eHolyEnderChest §7===");
      sender.sendMessage("§7/ec §8- §fОткрыть эндер-сундук");
      sender.sendMessage("§7/ec <ник> §8- §fПосмотреть сундук другого игрока");
      sender.sendMessage("§7/ec help §8- §fПоказать справку");
      sender.sendMessage("§7/ec unlock <ник> <кол-во> §8- §fВыдать слоты");
      sender.sendMessage("§7/ec info <ник> §8- §fПросмотреть прогресс слотов");
      sender.sendMessage("§7/ec reload §8- §fПерезагрузить конфиг");
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> suggestions = new ArrayList<>();
      if (args.length == 1) {
         if (sender.hasPermission("holyenderchest.ec.admin")) {
            suggestions.addAll(Arrays.asList("help", "reload", "unlock", "info"));
         }

         Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
      }

      if (args.length == 2 && (args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("info"))) {
         Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
      }

      return suggestions;
   }
}
