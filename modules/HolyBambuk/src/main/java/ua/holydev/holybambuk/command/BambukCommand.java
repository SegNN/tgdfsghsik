package ua.holydev.holybambuk.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holybambuk.item.BambukItem;

public final class BambukCommand implements CommandExecutor, TabCompleter {
   private static final List<String> SUB = Collections.singletonList("give");
   private final BambukItem item;

   public BambukCommand(BambukItem item) {
      this.item = item;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0 || !args[0].equalsIgnoreCase("give")) {
         sender.sendMessage("§e/bambuk give <ник>");
         return true;
      } else if (!sender.hasPermission("holybambuk.give")) {
         sender.sendMessage("§cНет прав.");
         return true;
      } else if (args.length < 2) {
         sender.sendMessage("§e/bambuk give <ник>");
         return true;
      } else {
         Player target = Bukkit.getPlayerExact(args[1]);
         if (target == null) {
            sender.sendMessage("§cИгрок не найден: " + args[1]);
            return true;
         } else {
            target.getInventory().addItem(new ItemStack[]{this.item.create()});
            target.sendMessage("§aВы получили ЛуК БамБук.");
            if (!sender.equals(target)) {
               sender.sendMessage("§aВыдано игроку " + target.getName() + ".");
            }

            return true;
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("holybambuk.give")) {
         return Collections.emptyList();
      } else if (args.length == 1) {
         return this.filter(SUB, args[0]);
      } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
         List<String> names = new ArrayList<>();

         for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
         }

         return this.filter(names, args[1]);
      } else {
         return Collections.emptyList();
      }
   }

   private List<String> filter(List<String> source, String prefix) {
      String p = prefix == null ? "" : prefix.toLowerCase();
      List<String> out = new ArrayList<>();

      for (String s : source) {
         if (s.toLowerCase().startsWith(p)) {
            out.add(s);
         }
      }

      return out;
   }
}
