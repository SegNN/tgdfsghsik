package org.dimasik.shame.command;

import java.util.List;
import lombok.Generated;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.shame.Shame;
import org.dimasik.shame.utils.Parser;

public abstract class SubCommand {
   private String name;

   public SubCommand(String name) {
      this.name = name;
   }

   public abstract void execute(CommandSender var1, Command var2, String[] var3);

   public abstract List<String> getTabCompletes(int var1);

   public abstract int getRequiredArgs();

   public abstract String getUsage();

   public void register() {
      Shame.getInstance().getCommandExecutor().getSubCommands().put(this.name, this);
   }

   public void leaveUsage(CommandSender sender) {
      sender.sendMessage(Parser.color("&b&l▶ &fИспользование: &6/shame " + this.name + " [игрок] " + this.getUsage()));
   }

   public void leavePNF(CommandSender sender) {
      sender.sendMessage(Parser.color("&b&l▶ &fИгрок &6не найден&f."));
   }

   @Generated
   public String getName() {
      return this.name;
   }
}
