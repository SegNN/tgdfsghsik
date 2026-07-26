package org.impldev.holycore.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Показывает игроку сообщение в том же виде, в каком ванильный сервер отчитывается о выдаче
 * оператора. Настоящих прав команда не выдаёт и никого, кроме отправителя, не уведомляет.
 *
 * <p>Оформление повторяет ванильное: такие отчёты сервер шлёт серым курсивом в квадратных
 * скобках, поэтому и здесь {@link ChatColor#GRAY} + {@link ChatColor#ITALIC}.
 */
public class Wolker implements CommandExecutor {
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      }

      Player player = (Player)sender;
      player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC
            + "[Server: " + player.getName() + " назначен оператором сервера]");
      return true;
   }
}
