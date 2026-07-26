package org.impldev.holycore.invest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class InvestCommand implements CommandExecutor {
   private final InvestMenu menu;

   public InvestCommand(InvestMenu menu) {
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
