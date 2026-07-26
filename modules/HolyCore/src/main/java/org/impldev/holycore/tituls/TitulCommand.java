package org.impldev.holycore.tituls;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.impldev.holycore.utils.Parser;
import org.jetbrains.annotations.NotNull;

/** /tituls - свои титулы, /tituls ник - чужие. */
public final class TitulCommand implements CommandExecutor {
   private final TitulMenu menu;

   public TitulCommand(TitulMenu menu) {
      this.menu = menu;
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player player)) {
         return true;
      }

      if (args.length == 0) {
         this.menu.open(player, player, 1);
         return true;
      }

      Player target = Bukkit.getPlayerExact(args[0]);
      if (target == null) {
         // владение титулом - это право LuckPerms, а права оффлайн-игрока без обращения к
         // LuckPerms не прочитать, поэтому чужой список доступен только пока игрок в сети
         player.sendMessage(Parser.color("&r&x&f&f&2&2&2&2▶ &r&fИгрок &r&x&f&f&2&2&2&2"
               + args[0] + "&r&f сейчас не в сети."));
         return true;
      }

      this.menu.open(player, target, 1);
      return true;
   }
}
