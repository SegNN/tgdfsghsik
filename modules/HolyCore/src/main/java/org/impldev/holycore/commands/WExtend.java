package org.impldev.holycore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.impldev.holycore.utils.Parser;
import org.jetbrains.annotations.NotNull;

/**
 * Команда осталась зарегистрированной намеренно: игроки помнят её по старым голограммам и
 * подсказкам, и молчаливое "неизвестная команда" ничего им не объясняет. Здесь она отвечает,
 * куда переехало расширение миров.
 */
public class WExtend implements CommandExecutor {
   private static final String MESSAGE =
         "&r&x&f&f&2&2&2&2▶&r&f Команда &r&x&f&f&2&2&2&2/wextend была удалена&r&f. "
       + "Расширение миров теперь происходит через инвестиции — &r&x&0&0&d&8&f&f/invest";

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      sender.sendMessage(Parser.color(MESSAGE));
      return true;
   }
}
