package org.nezxenka.litecontainer.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nezxenka.litecontainer.LiteContainer;
import org.nezxenka.litecontainer.commands.impl.AddItemSubCommand;
import org.nezxenka.litecontainer.commands.impl.GiveContainerSubCommand;
import org.nezxenka.litecontainer.commands.impl.ListItemsSubCommand;
import org.nezxenka.litecontainer.commands.impl.ReloadSubCommand;
import org.nezxenka.litecontainer.commands.impl.RemoveItemSubCommand;
import org.nezxenka.litecontainer.config.DatabaseManager;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.config.PluginConfigManager;
import org.nezxenka.litecontainer.menu.ContainerOpenMenu;

public class ContainerCommand implements TabExecutor {
   private final PluginConfigManager configManager;
   private final DatabaseManager databaseManager;
   private final LootManager lootManager;
   private final Map<String, SubCommand> subCommands = new HashMap<>();

   public ContainerCommand(LiteContainer plugin) {
      this.configManager = plugin.getConfigManager();
      this.databaseManager = plugin.getDatabaseManager();
      this.lootManager = plugin.getLootManager();
      this.subCommands.put("additem", new AddItemSubCommand(this.lootManager));
      this.subCommands.put("givecontainer", new GiveContainerSubCommand(this.databaseManager));
      this.subCommands.put("listitems", new ListItemsSubCommand(this.lootManager));
      this.subCommands.put("reload", new ReloadSubCommand(this.configManager, this.lootManager));
      this.subCommands.put("removeitem", new RemoveItemSubCommand(this.lootManager));
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (args.length == 0) {
         if (sender instanceof Player player) {
            new ContainerOpenMenu(this.configManager, this.databaseManager, this.lootManager).show(player);
         } else {
            sender.sendMessage("Эта команда только для игроков");
         }

         return true;
      } else {
         SubCommand subCommand = this.subCommands.get(args[0].toLowerCase());
         if (subCommand == null) {
            sender.sendMessage("Неизвестная подкоманда");
            return true;
         } else if (!sender.hasPermission("litecontainer.command." + args[0].toLowerCase())) {
            sender.sendMessage(this.configManager.getNoPermission());
            return true;
         } else {
            subCommand.onCommand(sender, args);
            return true;
         }
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (args.length == 1) {
         List<String> completions = new ArrayList<>();

         for (String cmd : this.subCommands.keySet()) {
            if (sender.hasPermission("litecontainer.command." + cmd) && cmd.startsWith(args[0].toLowerCase())) {
               completions.add(cmd);
            }
         }

         return completions;
      } else {
         SubCommand subCommand = this.subCommands.get(args[0].toLowerCase());
         return (List<String>)(subCommand != null && sender.hasPermission("litecontainer.command." + args[0].toLowerCase())
            ? subCommand.onTabCompleter(sender, args)
            : new ArrayList<>());
      }
   }
}
