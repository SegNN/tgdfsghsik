package org.dimasik.liteauction.frontend.commands.impl;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.liteauction.frontend.commands.SubCommand;

public class Help extends SubCommand {
   public Help(String name) {
      super(name);
   }

   @Override
   public void execute(org.bukkit.entity.Player player, Command command, String[] args) {
      this.leaveUsage(player);
   }

   @Override
   public List<String> getTabCompletes(CommandSender sender, String[] args) {
      return List.of();
   }

   @Override
   public int getRequiredArgs() {
      return 0;
   }

   @Override
   public String getRequiredPermission() {
      return "";
   }
}
