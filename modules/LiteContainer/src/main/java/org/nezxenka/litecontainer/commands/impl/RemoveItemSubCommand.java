package org.nezxenka.litecontainer.commands.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.nezxenka.litecontainer.commands.SubCommand;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.utils.ContainerItem;

public class RemoveItemSubCommand implements SubCommand {
   private final LootManager lootManager;

   public RemoveItemSubCommand(LootManager lootManager) {
      this.lootManager = lootManager;
   }

   @Override
   public void onCommand(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage("§c ▶ §fИспользование: /container removeitem <название>");
      } else {
         if (this.lootManager.removeItem(args[1])) {
            sender.sendMessage("§b ▶ §fПредмет '§e" + args[1] + "§f' успешно удален");
         } else {
            sender.sendMessage("§c ▶ §fПредмет с названием '§e" + args[1] + "§f' не найден");
         }
      }
   }

   @Override
   public List<String> onTabCompleter(CommandSender sender, String[] args) {
      return args.length == 2
         ? this.lootManager
            .getAllItems()
            .stream()
            .map(ContainerItem::name)
            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
            .collect(Collectors.toList())
         : Collections.emptyList();
   }
}
