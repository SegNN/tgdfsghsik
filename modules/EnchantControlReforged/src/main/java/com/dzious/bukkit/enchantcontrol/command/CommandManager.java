package com.dzious.bukkit.enchantcontrol.command;

import com.dzious.bukkit.enchantcontrol.EnchantControl;

public class CommandManager {
   private EnchantControl plugin;

   public CommandManager(EnchantControl plugin) {
      this.plugin = plugin;
   }

   public void onEnable() {
      this.plugin.getLogManager().logInfo("CommandManager OnEnable");
      this.plugin.getCommand("EnchantControl").setExecutor(new CommandEnchantControl(this.plugin));
   }
}
