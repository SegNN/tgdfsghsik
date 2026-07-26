package ru.nezxenka.holykits.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.nezxenka.holykits.HolyKits;
import ru.nezxenka.holykits.managers.Kit;
import ru.nezxenka.holykits.managers.KitManager;

public class KitTabCompleter implements TabCompleter {
   private final HolyKits plugin;

   public KitTabCompleter(HolyKits plugin) {
      this.plugin = plugin;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!(sender instanceof Player)) {
         return completions;
      } else {
         Player player = (Player)sender;
         KitManager kitManager = this.plugin.getKitManager();
         if (args.length == 1) {
            completions.add("shop");
            List<String> kitNames = kitManager.getAllKits()
               .stream()
               .map(Kit::getName)
               .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
               .collect(Collectors.toList());
            completions.addAll(kitNames);
            completions = completions.stream().filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
         } else if (args.length == 2) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
               if (player1.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                  completions.add(player1.getName());
               }
            }
         }

         return completions;
      }
   }
}
