package org.dimasik.eventposilka;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EventPosilka extends JavaPlugin {
   public PosilkaManager posilkaManager;

   public EventPosilka() {
   }

   public void onEnable() {
      this.posilkaManager = new PosilkaManager(this);
      this.getServer().getPluginManager().registerEvents(this.posilkaManager, this);
      this.getCommand("posilka").setExecutor(new EventPosilka.GruzCommand());
   }

   public void onDisable() {
      this.posilkaManager.removePosilka();
   }

   public class GruzCommand implements CommandExecutor {
      public GruzCommand() {
      }

      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (args.length == 0) {
            sender.sendMessage("Использование: /posilka [spawn/remove/settimer/setbreaks]");
            return true;
         } else if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length < 2) {
               sender.sendMessage("Использование: /posilka spawn [default/epic/legendary]");
               return true;
            } else {
               try {
                  PosilkaManager.PosilkaType gruzType = PosilkaManager.PosilkaType.valueOf(args[1].toUpperCase());
                  EventPosilka.this.posilkaManager.start(gruzType);
                  return true;
               } catch (IllegalArgumentException var6) {
                  sender.sendMessage("Использование: /posilka spawn [default/epic/legendary]");
                  return true;
               }
            }
         } else {
            if (args[0].equalsIgnoreCase("settimer")) {
               if (args.length < 2) {
                  sender.sendMessage("Использование: /posilka settimer [время]");
                  return true;
               }

               try {
                  int gruzTimer = Integer.parseInt(args[1]);
                  EventPosilka.this.posilkaManager.updateTimer(gruzTimer);
               } catch (NumberFormatException var8) {
                  sender.sendMessage("Использование: /posilka settimer [время]");
                  return true;
               }
            } else if (args[0].equalsIgnoreCase("setbreaks")) {
               if (args.length < 2) {
                  sender.sendMessage("Использование: /posilka setbreaks [количество]");
                  return true;
               }

               try {
                  int gruzTimer = Integer.parseInt(args[1]);
                  EventPosilka.this.posilkaManager.breaks = gruzTimer;
               } catch (NumberFormatException var7) {
                  sender.sendMessage("Использование: /posilka setbreaks [количество]");
                  return true;
               }
            } else {
               if (!args[0].equalsIgnoreCase("remove")) {
                  sender.sendMessage("Использование: /posilka [spawn/remove/settimer/setbreaks]");
                  return true;
               }

               EventPosilka.this.posilkaManager.removePosilka();
            }

            return true;
         }
      }
   }
}
