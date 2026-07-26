package ua.fichatime.fichapromocode.command;

import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ua.fichatime.fichapromocode.manager.ConfigManager;
import ua.fichatime.fichapromocode.storage.Database;
import ua.fichatime.fichapromocode.utils.ColorUtils;

public class PromoStatsCommand implements CommandExecutor {
   private final Database database;
   private final ConfigManager configManager;

   public PromoStatsCommand(Database database, ConfigManager configManager) {
      this.database = database;
      this.configManager = configManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("fichapromocode.stats")) {
         sender.sendMessage(ColorUtils.colorize("&cУ вас нет прав для использования этой команды!"));
         return true;
      } else {
         Map<String, Integer> stats = this.database.getPromoStats();
         Map<String, Map<String, Object>> allPromocodes = this.configManager.getPromocodes();
         sender.sendMessage(ColorUtils.colorize("&6Список промокодов:"));
         int totalActivations = 0;
         if (allPromocodes != null) {
            for (String promoCode : allPromocodes.keySet()) {
               int activations = stats.getOrDefault(promoCode, 0);
               sender.sendMessage(ColorUtils.colorize("&7" + promoCode + " &f- " + activations));
               totalActivations += activations;
            }
         }

         sender.sendMessage(ColorUtils.colorize("&6Всего активаций: &f" + totalActivations));
         return true;
      }
   }
}
