package org.dimasik.playtimeapi.commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.playtimeapi.Main;
import org.dimasik.playtimeapi.models.PlayerData;
import org.dimasik.playtimeapi.utils.TimeFormatter;

public class PlayTimeCommand implements CommandExecutor {
   private final Main plugin;

   public PlayTimeCommand(Main plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      Object target;
      if (args.length == 0) {
         if (!(sender instanceof Player)) {
            sender.sendMessage("§x§e§7§e§7§e§7Использование §7/playtime [игрок]");
            return true;
         }

         target = (Player)sender;
      } else {
         target = Bukkit.getOfflinePlayer(args[0]);
         if (target == null) {
            sender.sendMessage("§x§e§7§e§7§e§7Игрок §7не найден либо оффлайн§x§e§7§e§7§e§7.");
            return true;
         }
      }

      UUID uuid = ((OfflinePlayer)target).getUniqueId();
      PlayerData data = this.plugin.getPlayerData(uuid);
      if (data == null) {
         sender.sendMessage("§x§e§7§e§7§e§7Данные игрока §7не найдены§x§e§7§e§7§e§7.");
         return true;
      } else {
         long lastActivity = this.plugin.getLastActivity(uuid);
         String message = "§8------------------§7PlayTimeAPI§8------------------\n§x§e§7§e§7§e§7Активность §7"
            + ((OfflinePlayer)target).getName()
            + "\n§x§e§7§e§7§e§7Общее время в игре: §7"
            + TimeFormatter.formatTime(data.getTotalPlayTime())
            + "\n§x§e§7§e§7§e§7Общее время активности в игре: §7"
            + TimeFormatter.formatTime(data.getTotalActiveTime())
            + "\n\n§x§e§7§e§7§e§7Текущая сессия: §8("
            + data.getCurrentServer()
            + ")\n§x§e§7§e§7§e§7Общее время в игре: §7"
            + TimeFormatter.formatTime(data.getSessionPlayTime())
            + "\n§x§e§7§e§7§e§7Общее время активности в игре: §7"
            + TimeFormatter.formatTime(data.getSessionActiveTime())
            + "\n§x§e§7§e§7§e§7Время бездействия: §7"
            + TimeFormatter.formatTime(data.getSessionIdleTime())
            + "\n\n§x§e§7§e§7§e§7Последняя активность: §7"
            + TimeFormatter.formatTimeAgo(lastActivity)
            + " §x§e§7§e§7§e§7назад\n§x§e§7§e§7§e§7Последний вход на анархию: §7"
            + TimeFormatter.formatTimeAgo(data.getLastLogin())
            + " §x§e§7§e§7§e§7назад\n§8---------------------------------------------------";

         for (String s : message.split("\n")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('§', s));
         }

         return true;
      }
   }
}
