package ua.holydev.holyeventsun.manager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ua.holydev.holyeventsun.HolyEventSun;
import ua.holydev.holyeventsun.model.Phase;
import ua.holydev.holyeventsun.util.ColorUtils;

public class RewardManager {
   private final HolyEventSun plugin;
   private static final DecimalFormat MONEY_FORMAT;

   public RewardManager(HolyEventSun plugin) {
      this.plugin = plugin;
   }

   public List<Player> getPlayersInRadius() {
      Location coreLoc = this.plugin.getCfg().getCoreLocation();
      if (coreLoc != null && coreLoc.getWorld() != null) {
         int radius = this.plugin.getCfg().getRadius();
         int radiusSq = radius * radius;
         List<Player> nearby = new ArrayList<>();

         for (Player player : coreLoc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(coreLoc) <= radiusSq) {
               nearby.add(player);
            }
         }

         return nearby;
      } else {
         return new ArrayList<>();
      }
   }

   public void distributeRewards() {
      List<Player> players = this.getPlayersInRadius();
      if (!players.isEmpty()) {
         Phase phase = this.plugin.getPhaseManager().getCurrentPhase();
         int count = players.size();
         switch (phase) {
            case COINS:
               double totalMoney = this.randomDouble(this.plugin.getCfg().getMoneyMin(), this.plugin.getCfg().getMoneyMax());
               double moneyPerPlayer = totalMoney / count;

               for (Player player : players) {
                  this.plugin.getEconomy().depositPlayer(player, moneyPerPlayer);
                  String msg = this.plugin.getCfg().getMoneyMessage().replace("%amount%", MONEY_FORMAT.format(moneyPerPlayer));
                  player.sendMessage(ColorUtils.colorize(msg));
               }
               break;
            case EXPERIENCE:
               int totalExp = this.randomInt(this.plugin.getCfg().getExpMin(), this.plugin.getCfg().getExpMax());
               int expPerPlayer = Math.max(1, totalExp / count);

               for (Player player : players) {
                  player.giveExp(expPerPlayer);
               }
         }
      }
   }

   private double randomDouble(double min, double max) {
      return min >= max ? min : min + ThreadLocalRandom.current().nextDouble() * (max - min);
   }

   private int randomInt(int min, int max) {
      return min >= max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
   }

   static {
      DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
      symbols.setGroupingSeparator(' ');
      symbols.setDecimalSeparator('.');
      MONEY_FORMAT = new DecimalFormat("#,##0.00", symbols);
   }
}
