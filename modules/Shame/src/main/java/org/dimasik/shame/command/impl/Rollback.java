package org.dimasik.shame.command.impl;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.modules.impl.RollbackModule;
import org.dimasik.shame.utils.Parser;

public class Rollback extends SubCommand {
   public Rollback(String name) {
      super(name);
   }

   @Override
   public void execute(CommandSender sender, Command command, String[] args) {
      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
         this.leavePNF(sender);
      } else {
         sender.sendMessage(Parser.color("&b&l▶ &fОткат &6начат&f."));
         RollbackModule.startRollback(target);
      }
   }

   @Override
   public List<String> getTabCompletes(int args) {
      return List.of();
   }

   @Override
   public int getRequiredArgs() {
      return 0;
   }

   @Override
   public String getUsage() {
      return "";
   }
}
