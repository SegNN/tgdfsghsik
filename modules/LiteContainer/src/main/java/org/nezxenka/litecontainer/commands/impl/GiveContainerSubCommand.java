package org.nezxenka.litecontainer.commands.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.nezxenka.litecontainer.LiteContainer;
import org.nezxenka.litecontainer.commands.SubCommand;
import org.nezxenka.litecontainer.config.DatabaseManager;

public class GiveContainerSubCommand implements SubCommand {
   private final DatabaseManager databaseManager;

   public GiveContainerSubCommand(DatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
   }

   @Override
   public void onCommand(final CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage("§c ▶ §fИспользование: /container givecontainer <игрок> <количество>");
      } else {
         try {
            final int amount = Integer.parseInt(args[2]);
            final String playerName = args[1];
            Bukkit.getScheduler().runTaskAsynchronously(LiteContainer.getInstance(), new Runnable() {
               @Override
               public void run() {
                  int playerCount = GiveContainerSubCommand.this.databaseManager.getValue(playerName);
                  GiveContainerSubCommand.this.databaseManager.setValue(playerName, playerCount + amount);
                  sender.sendMessage("§b ▶ §fВы успешно выдали игроку §e" + playerName + " §f" + amount + " контейнеров");
               }
            });
         } catch (NumberFormatException var5) {
            sender.sendMessage("§c ▶ §fВведенное вами число некорректно");
         }
      }
   }

   @Override
   public List<String> onTabCompleter(CommandSender sender, String[] args) {
      return args.length == 2
         ? Bukkit.getOnlinePlayers()
            .stream()
            .<String>map(OfflinePlayer::getName)
            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
            .collect(Collectors.toList())
         : Collections.emptyList();
   }
}
