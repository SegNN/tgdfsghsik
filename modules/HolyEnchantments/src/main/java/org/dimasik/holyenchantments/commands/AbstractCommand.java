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
      if (sender == null) {
         $$$reportNull$$$1(0);
      }

      if (command == null) {
         $$$reportNull$$$1(1);
      }

      if (label == null) {
         $$$reportNull$$$1(2);
      }

      if (args == null) {
         $$$reportNull$$$1(3);
      }

      if (sender == null) {
         $$$reportNull$$$0(0);
      }

      if (command == null) {
         $$$reportNull$$$0(1);
      }

      if (label == null) {
         $$$reportNull$$$0(2);
      }

      if (args == null) {
         $$$reportNull$$$0(3);
      }

      this.execute(sender, label, args);
      return true;
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (sender == null) {
         $$$reportNull$$$1(4);
      }

      if (command == null) {
         $$$reportNull$$$1(5);
      }

      if (label == null) {
         $$$reportNull$$$1(6);
      }

      if (args == null) {
         $$$reportNull$$$1(7);
      }

      if (sender == null) {
         $$$reportNull$$$0(4);
      }

      if (command == null) {
         $$$reportNull$$$0(5);
      }

      if (label == null) {
         $$$reportNull$$$0(6);
      }

      if (args == null) {
         $$$reportNull$$$0(7);
      }

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

   private static void $$$reportNull$$$0(int var0) {
      Object[] var10001 = new Object[3];
      switch (var0) {
         case 0:
         case 4:
         default:
            var10001[0] = "sender";
            break;
         case 1:
         case 5:
            var10001[0] = "command";
            break;
         case 2:
         case 6:
            var10001[0] = "label";
            break;
         case 3:
         case 7:
            var10001[0] = "args";
      }

      var10001[1] = "org/dimasik/holyenchantments/commands/AbstractCommand";
      switch (var0) {
         case 0:
         case 1:
         case 2:
         case 3:
         default:
            var10001[2] = "onCommand";
            break;
         case 4:
         case 5:
         case 6:
         case 7:
            var10001[2] = "onTabComplete";
      }

      throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", var10001));
   }
}
