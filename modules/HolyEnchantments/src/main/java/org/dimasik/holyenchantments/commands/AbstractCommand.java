package org.dimasik.holyenchantments.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
   public AbstractCommand(String command) {
      PluginCommand pluginCommand = HolyEnchantments.getInstance().getCommand(command);
      if (pluginCommand != null) {
         pluginCommand.setExecutor(this);
         pluginCommand.setTabCompleter(this);
      }
   }

   public abstract void execute(CommandSender var1, String var2, String[] var3);

   public List<String> complete(CommandSender sender, String[] args) {
      return null;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

      this.execute(sender, label, args);
      return true;
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

      return this.filter(this.complete(sender, args), args);
   }

   @Nullable
   private List<String> filter(List<String> list, String[] args) {
      if (list == null) {
         return null;
      } else {
         String last = args[args.length - 1];
         List<String> result = new ArrayList<>();

         for (String arg : list) {
            if (arg.toLowerCase().startsWith(last.toLowerCase())) {
               result.add(arg);
            }
         }

         return result;
      }
   }

}
