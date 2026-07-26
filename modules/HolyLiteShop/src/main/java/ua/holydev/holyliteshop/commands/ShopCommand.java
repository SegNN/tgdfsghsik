package ua.holydev.holyliteshop.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.gui.ShopGUI;
import ua.holydev.holyliteshop.gui.WarehouseGUI;

public class ShopCommand implements CommandExecutor, TabCompleter {
   private final HolyLiteShop plugin;

   public ShopCommand(HolyLiteShop plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Эта команда для игроков");
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length > 0 && args[0].equalsIgnoreCase("chest")) {
            new WarehouseGUI(this.plugin, player, 0).open();
            return true;
         } else {
            this.plugin.getShopManager().refreshPlayerShop(player.getUniqueId(), false);
            new ShopGUI(this.plugin, player).open();
            return true;
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("chest");
      }

      return completions;
   }
}
