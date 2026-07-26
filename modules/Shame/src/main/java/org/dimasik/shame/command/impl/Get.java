package org.dimasik.shame.command.impl;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.anticheatstatus.AntiCheatStatus;
import org.dimasik.shame.Shame;
import org.dimasik.shame.command.SubCommand;

public class Get extends SubCommand {
   public Get(String name) {
      super(name);
   }

   @Override
   public void execute(CommandSender sender, Command command, String[] args) {
      if (args.length < 2) {
         this.leaveUsage(sender);
      } else {
         String playerName = args[1];

         try {
            if (Shame.getInstance().getDatabaseManager().nicknameExists(playerName).get()) {
               sender.sendMessage("Статус игрока: 100%, он будет забанен как ударит кого либо");
            } else {
               int points = (Integer)AntiCheatStatus.getInstance().databaseManager.getPlayerPoints(playerName).get();
               int maxPoints = AntiCheatStatus.getInstance().configManager.getInt("ban.value", 600);
               int result = (int)Math.round((double)points / maxPoints * 100.0);
               sender.sendMessage("Статус игрока: " + result + "%");
            }
         } catch (Exception var8) {
            throw new RuntimeException(var8);
         }
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
