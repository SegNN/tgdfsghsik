package org.dimasik.liteauction.frontend.commands.impl;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.commands.SubCommand;

public class Sound extends SubCommand {
   public Sound(String name) {
      super(name);
   }

   @Override
   public void execute(org.bukkit.entity.Player player, Command command, String[] args) {
      try {
         if (LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
            player.sendMessage(Parser.color(ConfigManager.getString("design/commands/sound.yml", "on", "&#00D5FB▶ &fЗвуки &#05F700включены&f.")));
            LiteAuction.getInstance().getDatabaseManager().getSoundsManager().setSoundToggle(player.getName(), false);
         } else {
            player.sendMessage(Parser.color(ConfigManager.getString("design/commands/sound.yml", "on", "&#00D5FB▶ &fЗвуки &#FB2222выключены&f.")));
            LiteAuction.getInstance().getDatabaseManager().getSoundsManager().setSoundToggle(player.getName(), true);
         }
      } catch (Exception var5) {
      }
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
