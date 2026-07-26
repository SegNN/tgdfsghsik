package org.nezxenka.litecontainer.commands.impl;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.nezxenka.litecontainer.commands.SubCommand;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.config.PluginConfigManager;

public class ReloadSubCommand implements SubCommand {
   private final PluginConfigManager configManager;
   private final LootManager lootManager;

   public ReloadSubCommand(PluginConfigManager configManager, LootManager lootManager) {
      this.configManager = configManager;
      this.lootManager = lootManager;
   }

   @Override
   public void onCommand(CommandSender sender, String[] args) {
      this.configManager.reload();
      this.lootManager.loadAllItems();
      sender.sendMessage("§b ▶ §fПлагин успешно перезагружен");
   }

   @Override
   public List<String> onTabCompleter(CommandSender sender, String[] args) {
      return Collections.emptyList();
   }
}
