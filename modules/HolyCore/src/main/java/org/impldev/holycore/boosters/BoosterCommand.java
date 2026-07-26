package org.impldev.holycore.boosters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BoosterCommand implements CommandExecutor {
   private final BoosterMenu menu;

   public BoosterCommand(BoosterMenu menu) {
      this.menu = menu;
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (sender instanceof Player player) {
         this.menu.open(player);
      }

      return true;
   }
}
