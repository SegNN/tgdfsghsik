package org.dimasik.liteauction.economy.impl;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.dimasik.liteauction.economy.EconomyEditor;

public class VaultEco extends EconomyEditor {
   private Economy economy;

   public VaultEco() {
      this.setupEconomy();
   }

   private void setupEconomy() {
      if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
         RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp != null) {
            this.economy = (Economy)rsp.getProvider();
         }
      }
   }

   @Override
   public double getBalance(String player) {
      if (this.economy == null) {
         return 0.0;
      } else {
         OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
         return this.economy.getBalance(offlinePlayer);
      }
   }

   @Override
   public void addBalance(String player, double count) {
      if (this.economy != null && !(count <= 0.0)) {
         OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
         this.economy.depositPlayer(offlinePlayer, count);
      }
   }

   @Override
   public void subtractBalance(String player, double count) {
      if (this.economy != null && !(count <= 0.0)) {
         OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
         this.economy.withdrawPlayer(offlinePlayer, count);
      }
   }
}
