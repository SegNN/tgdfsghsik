package org.dimasik.shame.command.impl;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.modules.impl.BanModule;
import org.dimasik.shame.utils.Pair;

public class Ban extends SubCommand {
   public Ban(String name) {
      super(name);
   }

   @Override
   public void execute(CommandSender sender, Command command, String[] args) {
      if (args.length < 2) {
         this.leaveUsage(sender);
      } else {
         String playerName = args[1];
         Player player = Bukkit.getPlayer(playerName);
         boolean ban = true;
         Boolean newAnim = null;
         boolean now = false;

         for (int i = 2; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.equals("new")) {
               if (newAnim != null && !newAnim) {
                  return;
               }

               newAnim = true;
            } else if (arg.equals("old")) {
               if (newAnim != null && newAnim) {
                  return;
               }

               newAnim = false;
            }
         }

         for (int ix = 2; ix < args.length; ix++) {
            String arg = args[ix].toLowerCase();
            if (arg.equals("no")) {
               ban = false;
            }

            if (arg.equals("-n")) {
               now = true;
            }
         }

         if (newAnim == null) {
            newAnim = new Random().nextBoolean();
         }

         String reason = Config.getString(
            "ban.reason", "30d &cОбнаружено сторонние ПО. &7&o(Ошибка? - свяжитесь с разработчиком &f&l@HolyDev_ac_bot &7&o) --sender=&c&lАнтиЧит"
         );
         String broadcastCmd = Config.replace(
            Config.getString("ban.broadcast", "litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом."),
            new Pair<>("%player%", playerName)
         );
         if (player == null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + playerName + " " + reason);
            if (!broadcastCmd.isEmpty()) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCmd);
            }
         } else {
            BanModule.instant.put(player, now);
            BanModule.startBanAnimation(player, reason, ban, newAnim);
         }
      }
   }

   @Override
   public List<String> getTabCompletes(int args) {
      if (args == 1) {
         return List.of("no", "new", "old", "-n");
      } else {
         return args >= 2 ? List.of("no", "new", "old", "-n") : List.of();
      }
   }

   @Override
   public int getRequiredArgs() {
      return 0;
   }

   @Override
   public String getUsage() {
      return "(no)";
   }
}
