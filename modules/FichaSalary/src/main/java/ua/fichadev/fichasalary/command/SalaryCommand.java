package ua.fichadev.fichasalary.command;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.fichadev.fichasalary.handler.SalaryHandler;
import ua.fichadev.fichasalary.managers.ConfigManager;
import ua.fichadev.fichasalary.managers.DataManager;
import ua.fichadev.fichasalary.utils.ColorUtils;

public class SalaryCommand implements CommandExecutor, TabCompleter {
   private final SalaryHandler salaryHandler;
   private final ConfigManager configManager;
   private final DataManager dataManager;

   public SalaryCommand(SalaryHandler salaryHandler, ConfigManager configManager, DataManager dataManager) {
      this.salaryHandler = salaryHandler;
      this.configManager = configManager;
      this.dataManager = dataManager;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (sender instanceof Player p) {
         int remaining = this.salaryHandler.getRemainingTime(p);
         if (remaining > 0) {
            String nextType = this.salaryHandler.getNextSalaryType(p);
            String timeFormatted = ColorUtils.formatTime(remaining);
            if (nextType.equalsIgnoreCase("sapphire")) {
               int amount = this.configManager.getSapphireBaseAmount()
                  + this.dataManager.getData().getInt(p.getUniqueId().toString() + ".salaryCount", 0) * this.configManager.getSapphireGrowth();
               ColorUtils.sendMessage(
                  p,
                  this.configManager
                     .getConfig()
                     .getString("messages.not-ready-sapphire")
                     .replace("%time%", timeFormatted)
                     .replace("%amount%", ColorUtils.formatNumber(amount))
               );
            } else {
               int salaryCount = this.dataManager.getData().getInt(p.getUniqueId().toString() + ".salaryCount", 0);
               int base = this.configManager
                  .getRankRewards()
                  .getOrDefault(this.salaryHandler.getPrimaryGroup(p).toLowerCase(), this.configManager.getBaseReward());
               int amount = base + salaryCount * this.configManager.getRewardGrowth();
               ColorUtils.sendMessage(
                  p,
                  this.configManager
                     .getConfig()
                     .getString("messages.not-ready-money")
                     .replace("%time%", timeFormatted)
                     .replace("%amount%", ColorUtils.formatNumber(amount))
               );
            }
         } else {
            this.salaryHandler.checkAndGiveSalary(p);
         }

         return true;
      } else {
         sender.sendMessage("Только для игроков");
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
      return args.length == 1 && sender.hasPermission("holysalary.admin") ? Arrays.asList("reload", "stats", "reset") : null;
   }
}
