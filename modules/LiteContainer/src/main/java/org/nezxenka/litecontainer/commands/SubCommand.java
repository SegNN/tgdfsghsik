package org.nezxenka.litecontainer.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface SubCommand {
   void onCommand(CommandSender var1, String[] var2);

   List<String> onTabCompleter(CommandSender var1, String[] var2);
}
