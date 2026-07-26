package ua.fichatime.fichapromocode.command;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichatime.fichapromocode.FichaPromoCode;
import ua.fichatime.fichapromocode.manager.ConfigManager;
import ua.fichatime.fichapromocode.storage.Database;
import ua.fichatime.fichapromocode.utils.ColorUtils;

public class PromoCommandExecutor implements Listener {
   private final FichaPromoCode plugin;
   private final Database database;
   private final ConfigManager configManager;

   public PromoCommandExecutor(FichaPromoCode plugin, Database database, ConfigManager configManager) {
      this.plugin = plugin;
      this.database = database;
      this.configManager = configManager;
   }

   @EventHandler
   public void onCommand(PlayerCommandPreprocessEvent event) {
      final Player player = event.getPlayer();
      String command = event.getMessage();
      if (command.startsWith("/") && command.length() > 1) {
         String commandWithoutSlash = command.substring(1);
         String[] args = commandWithoutSlash.split(" ");
         final String promoCode = args[0].toLowerCase();
         if (this.configManager.getConfig().getConfigurationSection("promocode." + promoCode) != null) {
            event.setCancelled(true);
            (new BukkitRunnable() {
                  public void run() {
                     String ipAddress = player.getAddress().getAddress().getHostAddress();
                     String group = PromoCommandExecutor.this.configManager.getPromoGroup(promoCode);
                     if (group != null && PromoCommandExecutor.this.database.hasPlayerActivatedGroup(player.getName(), group)) {
                        List<String> groupLimitMessages = PromoCommandExecutor.this.configManager.getGroupMessageLimit(promoCode);
                        if (groupLimitMessages != null && !groupLimitMessages.isEmpty()) {
                           for (String message : groupLimitMessages) {
                              player.sendMessage(ColorUtils.colorize(message));
                           }
                        } else {
                           List<String> limitMessages = PromoCommandExecutor.this.configManager.getRewardMessageLimit(promoCode);
                           if (limitMessages != null) {
                              for (String message : limitMessages) {
                                 player.sendMessage(ColorUtils.colorize(message));
                              }
                           } else {
                              player.sendMessage(ColorUtils.colorize("&c▶ &fВы уже активировали промокод из этой группы!"));
                           }
                        }
                     } else if (PromoCommandExecutor.this.database.hasPlayerActivated(player.getName(), promoCode)) {
                        List<String> limitMessages = PromoCommandExecutor.this.configManager.getRewardMessageLimit(promoCode);
                        if (limitMessages != null) {
                           for (String message : limitMessages) {
                              player.sendMessage(ColorUtils.colorize(message));
                           }
                        } else {
                           player.sendMessage(ColorUtils.colorize("&c▶ &fВы уже активировали данный промокод!"));
                        }
                     } else if (PromoCommandExecutor.this.configManager.isIPCheckEnabled(promoCode)
                        && PromoCommandExecutor.this.database.hasIPActivated(ipAddress, promoCode)) {
                        List<String> ipLimitMessages = PromoCommandExecutor.this.configManager.getIPMessageLimit(promoCode);
                        if (ipLimitMessages != null) {
                           for (String message : ipLimitMessages) {
                              player.sendMessage(ColorUtils.colorize(message));
                           }
                        } else {
                           player.sendMessage(ColorUtils.colorize("&c▶ &fВы уже активировали этот промокод на этом айпи!"));
                        }
                     } else if (!PromoCommandExecutor.this.database.canActivatePromo(promoCode)) {
                        List<String> playersLimitMessages = PromoCommandExecutor.this.configManager.getRewardPlayersMessageLimit(promoCode);
                        if (playersLimitMessages != null) {
                           for (String message : playersLimitMessages) {
                              player.sendMessage(ColorUtils.colorize(message));
                           }
                        } else {
                           player.sendMessage(ColorUtils.colorize("&c▶ &fПревышено максимальное количество активаций у данного промокода!"));
                        }
                     } else {
                        PromoCommandExecutor.this.database.activatePromo(player.getName(), ipAddress, promoCode, group);
                        List<String> commands = PromoCommandExecutor.this.configManager.getRewardCommands(promoCode);
                        if (commands != null) {
                           for (String cmd : commands) {
                              String finalCmd = cmd.replace("%player%", player.getName());
                              Bukkit.getScheduler()
                                 .runTask(PromoCommandExecutor.this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd));
                           }
                        }

                        List<String> messages = PromoCommandExecutor.this.configManager.getRewardMessage(promoCode);
                        if (messages != null) {
                           for (String message : messages) {
                              player.sendMessage(ColorUtils.colorize(message.replace("%code%", promoCode)));
                           }
                        }
                     }
                  }
               })
               .runTaskAsynchronously(this.plugin);
         }
      }
   }
}
