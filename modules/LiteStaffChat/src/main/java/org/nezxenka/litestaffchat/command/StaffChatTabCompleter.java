package org.nezxenka.litestaffchat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class StaffChatTabCompleter implements TabCompleter {
   private static final List<String> MODES = Collections.unmodifiableList(Arrays.asList("LOCAL", "GLOBAL", "LITE", "CLASSIC", "CURRENT"));

   public StaffChatTabCompleter() {
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
      if (args.length != 1) {
         return Collections.emptyList();
      } else {
         String input = args[0].toUpperCase();
         return (List<String>)(input.isEmpty() ? new ArrayList<>(MODES) : MODES.stream().filter(mode -> mode.startsWith(input)).collect(Collectors.toList()));
      }
   }
}
