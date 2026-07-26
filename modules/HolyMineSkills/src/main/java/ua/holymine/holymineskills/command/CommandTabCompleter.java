package ua.holymine.holymineskills.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandTabCompleter implements TabCompleter {
   private static final List<String> SUBCOMMANDS = Arrays.asList("openmenu", "add", "remove", "get", "reset");

   public CommandTabCompleter() {
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!command.getName().equalsIgnoreCase("mineskills")) {
         return null;
      } else if (args.length == 1) {
         String sub = args[0].toLowerCase(Locale.ROOT);
         return SUBCOMMANDS.stream().filter(s -> s.startsWith(sub)).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("add") || sub.equals("remove") || sub.equals("get") || sub.equals("reset")) {
               String prefix = args[1].toLowerCase(Locale.ROOT);
               return Bukkit.getOnlinePlayers()
                  .stream()
                  .<String>map(OfflinePlayer::getName)
                  .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                  .collect(Collectors.toList());
            }
         }

         if (args.length == 3) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("add") || sub.equals("remove")) {
               List<String> suggestions = new ArrayList<>();
               suggestions.add("1");
               suggestions.add("10");
               suggestions.add("100");
               suggestions.add("1000");
               return suggestions;
            }
         }

         return new ArrayList<>();
      }
   }
}
