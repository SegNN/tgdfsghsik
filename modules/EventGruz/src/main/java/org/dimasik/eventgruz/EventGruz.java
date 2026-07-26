package org.dimasik.eventgruz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EventGruz extends JavaPlugin {
   public GruzManager gruzManager;

   public EventGruz() {
   }

   public void onEnable() {
      this.gruzManager = new GruzManager(this);
      this.getServer().getPluginManager().registerEvents(this.gruzManager, this);
      this.getCommand("gruz").setExecutor(new EventGruz.GruzCommand());
   }

   public void onDisable() {
      this.gruzManager.removeGruz();
   }

   public class GruzCommand implements CommandExecutor {
      public GruzCommand() {
      }

      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (args.length == 0) {
            sender.sendMessage("Использование: /gruz [spawn/remove/settimer]");
            return true;
         } else {
            if (args[0].equalsIgnoreCase("spawn")) {
               if (args.length < 2) {
                  sender.sendMessage("Использование: /gruz spawn [peaceful/explosive/death]");
                  return true;
               }

               try {
                  GruzManager.GruzType gruzType = GruzManager.GruzType.valueOf(args[1].toUpperCase());
                  EventGruz.this.gruzManager.start(gruzType);
               } catch (IllegalArgumentException var7) {
                  sender.sendMessage("Использование: /gruz spawn [peaceful/explosive/death]");
                  return true;
               }
            } else if (args[0].equalsIgnoreCase("settimer")) {
               if (args.length < 2) {
                  sender.sendMessage("Использование: /gruz settimer [время]");
                  return true;
               }

               try {
                  int gruzTimer = Integer.parseInt(args[1]);
                  EventGruz.this.gruzManager.setGruzTimer(gruzTimer);
               } catch (NumberFormatException var6) {
                  sender.sendMessage("Использование: /gruz settimer [время]");
                  return true;
               }
            } else {
               if (!args[0].equalsIgnoreCase("remove")) {
                  sender.sendMessage("Использование: /gruz [spawn/remove/settimer]");
                  return true;
               }

               EventGruz.this.gruzManager.removeGruz();
            }

            return true;
         }
      }
   }
}
