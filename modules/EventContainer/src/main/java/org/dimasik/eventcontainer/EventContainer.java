package org.dimasik.eventcontainer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EventContainer extends JavaPlugin {
   public ContainerManager containerManager;
   private static EventContainer instance;
   public static ContainerHolder holder;

   public EventContainer() {
   }

   public void onEnable() {
      holder = new ContainerHolder();
      instance = this;
      this.containerManager = new ContainerManager(this);
      this.getServer().getPluginManager().registerEvents(this.containerManager, this);
      this.getCommand("econtainer").setExecutor(new EventContainer.GruzCommand());
   }

   public static EventContainer getInstance() {
      return instance;
   }

   public void onDisable() {
      this.containerManager.removeContainer();
   }

   public class GruzCommand implements CommandExecutor {
      public GruzCommand() {
      }

      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (args.length == 0) {
            sender.sendMessage("Использование: /econtainer [spawn/remove/settimer/setitems]");
            return true;
         } else if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length < 2) {
               sender.sendMessage("Использование: /econtainer spawn [default/epic]");
               return true;
            } else {
               try {
                  ContainerManager.ContainerType gruzType = ContainerManager.ContainerType.valueOf(args[1].toUpperCase());
                  EventContainer.this.containerManager.start(gruzType);
                  return true;
               } catch (IllegalArgumentException var6) {
                  sender.sendMessage("Использование: /econtainer spawn [default/epic]");
                  return true;
               }
            }
         } else {
            if (args[0].equalsIgnoreCase("settimer")) {
               if (args.length < 2) {
                  sender.sendMessage("Использование: /econtainer settimer [время]");
                  return true;
               }

               try {
                  int gruzTimer = Integer.parseInt(args[1]);
                  EventContainer.this.containerManager.updateTimer(gruzTimer);
               } catch (NumberFormatException var8) {
                  sender.sendMessage("Использование: /econtainer settimer [время]");
                  return true;
               }
            } else if (args[0].equalsIgnoreCase("setitems")) {
               if (args.length < 2) {
                  sender.sendMessage("Использование: /econtainer setitems [количество]");
                  return true;
               }

               try {
                  int gruzTimer = Integer.parseInt(args[1]);
                  EventContainer.this.containerManager.items = gruzTimer;
               } catch (NumberFormatException var7) {
                  sender.sendMessage("Использование: /econtainer setitems [количество]");
                  return true;
               }
            } else {
               if (!args[0].equalsIgnoreCase("remove")) {
                  sender.sendMessage("Использование: /econtainer [spawn/remove/settimer/setitems]");
                  return true;
               }

               EventContainer.this.containerManager.removeContainer();
            }

            return true;
         }
      }
   }
}
