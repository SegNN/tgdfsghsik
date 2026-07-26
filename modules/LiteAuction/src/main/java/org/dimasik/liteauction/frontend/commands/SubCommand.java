package org.dimasik.liteauction.frontend.commands;

import java.util.List;
import lombok.Generated;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.exceptions.NotAPlayerException;
import org.dimasik.liteauction.backend.utils.format.Parser;

public abstract class SubCommand {
   private String name;

   public SubCommand(String name) {
      this.name = name;
   }

   public abstract void execute(Player var1, Command var2, String[] var3);

   public abstract List<String> getTabCompletes(CommandSender var1, String[] var2) throws NotAPlayerException;

   public abstract int getRequiredArgs();

   public void register() {
      LiteAuction.getInstance().getCommandExecutor().getSubCommands().put(this.name, this);
   }

   public abstract String getRequiredPermission();

   public void leaveUsage(Player player) {
      ConfigManager.getStringList("design/commands/main.yml", "usage-message").stream().map(Parser::color).forEach(player::sendMessage);
   }

   public Player requirePlayer(CommandSender sender) {
      if (sender instanceof Player player) {
         return player;
      } else {
         throw new NotAPlayerException();
      }
   }

   @Generated
   public String getName() {
      return this.name;
   }
}
