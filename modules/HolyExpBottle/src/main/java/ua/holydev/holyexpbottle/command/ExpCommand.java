package ua.holydev.holyexpbottle.command;

import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.holydev.holyexpbottle.HolyExpBottle;
import ua.holydev.holyexpbottle.menu.MenuListener;
import ua.holydev.holyexpbottle.utils.ColorUtils;

public class ExpCommand implements CommandExecutor {
   public ExpCommand() {
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player player = (Player)sender;
         int requiredMinutes = HolyExpBottle.getInstance().getPluginConfig().getInt("game-time.required-minutes", 40);
         int playedTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
         int playedMinutes = playedTicks / 20 / 60;
         if (playedMinutes >= requiredMinutes) {
            MenuListener.openExpMenu(player);
            return true;
         } else {
            for (String message : HolyExpBottle.getInstance().getPluginConfig().getStringList("game-time.message-not-enough")) {
               String formatted = message.replace("{required_minutes}", String.valueOf(requiredMinutes))
                  .replace("{played_minutes}", String.valueOf(playedMinutes));
               player.sendMessage(ColorUtils.colorize(formatted));
            }

            return true;
         }
      }
   }
}
