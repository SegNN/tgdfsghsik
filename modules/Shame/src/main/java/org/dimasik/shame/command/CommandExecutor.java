package org.dimasik.shame.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.shame.utils.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandExecutor implements TabExecutor {
   private final HashMap<String, SubCommand> subCommands = new HashMap<>();

   public CommandExecutor() {
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (args.length < 1) {
         sender.sendMessage(Parser.color("&b&l▶ &fИспользование: &6/shame [" + String.join("/", this.subCommands.keySet()) + "]"));
         return true;
      } else {
         String subCommandKey = args[0].toLowerCase();
         SubCommand subCommand = this.subCommands.get(subCommandKey);
         if (subCommand == null) {
            sender.sendMessage(Parser.color("&b&l▶ &fИспользование: &6/shame [" + String.join("/", this.subCommands.keySet()) + "]"));
            return true;
         } else if (subCommand.getRequiredArgs() + 2 > args.length) {
            sender.sendMessage(Parser.color("&b&l▶ &fИспользование: &6/shame " + subCommandKey + " [игрок] " + subCommand.getUsage()));
            return true;
         } else {
            subCommand.execute(sender, command, args);
            return true;
         }
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.addAll(this.subCommands.keySet());
      } else if (args.length == 2) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            completions.add(player.getName());
         }
      } else {
         SubCommand subCommand = this.subCommands.get(args[0]);
         if (subCommand != null) {
            completions.addAll(subCommand.getTabCompletes(args.length - 2));
         }
      }

      List<String> completions1 = new ArrayList<>();

      for (String s : completions) {
         if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
            completions1.add(s);
         }
      }

      return completions1;
   }

   @Generated
   public HashMap<String, SubCommand> getSubCommands() {
      return this.subCommands;
   }
}
