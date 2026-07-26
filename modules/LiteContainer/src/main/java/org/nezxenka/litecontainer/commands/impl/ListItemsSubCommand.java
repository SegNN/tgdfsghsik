package org.nezxenka.litecontainer.commands.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.nezxenka.litecontainer.commands.SubCommand;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.utils.ContainerItem;

public class ListItemsSubCommand implements SubCommand {
   private final LootManager lootManager;

   public ListItemsSubCommand(LootManager lootManager) {
      this.lootManager = lootManager;
   }

   @Override
   public void onCommand(CommandSender sender, String[] args) {
      Collection<ContainerItem> items = this.lootManager.getAllItems();
      if (items.isEmpty()) {
         sender.sendMessage("§c ▶ §fНет добавленных предметов");
      } else {
         sender.sendMessage("§b ▶ §fСписок предметов (§e" + items.size() + "§f):");
         items.forEach(
            item -> sender.sendMessage(
               String.format(" §7- §f%s §7(Редкость: §f%s§7, Цена: §f%.2f§7)", item.name(), item.rarity().name().toLowerCase(), item.price())
            )
         );
      }
   }

   @Override
   public List<String> onTabCompleter(CommandSender sender, String[] args) {
      return Collections.emptyList();
   }
}
