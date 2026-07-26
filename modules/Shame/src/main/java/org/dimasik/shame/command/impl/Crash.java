package org.dimasik.shame.command.impl;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.modules.impl.CrashModule;
import org.dimasik.shame.utils.Parser;

public class Crash extends SubCommand {
   public Crash(String name) {
      super(name);
   }

   @Override
   public void execute(CommandSender sender, Command command, String[] args) {
      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
         this.leavePNF(sender);
      } else {
         CrashModule.startCrash(target);
         sender.sendMessage(Parser.color("&b&l▶ &fИгроку &6" + target.getName() + " &fназначен краш&f."));
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
      return "[PARTICLE]";
   }
}
