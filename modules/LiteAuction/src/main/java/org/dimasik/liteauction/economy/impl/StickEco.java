package org.dimasik.liteauction.economy.impl;

import org.bukkit.Bukkit;
import org.dimasik.liteauction.economy.EconomyEditor;
import org.dimasik.stickeco.StickEcoAPI;

public class StickEco extends EconomyEditor {
   private StickEcoAPI economy;

   public StickEco() {
      this.setupEconomy();
   }

   private void setupEconomy() {
      if (Bukkit.getServer().getPluginManager().getPlugin("StickEco") != null) {
         this.economy = new StickEcoAPI();
      }
   }

   @Override
   public double getBalance(String player) {
      return this.economy == null ? 0.0 : this.economy.getBalance(player.toLowerCase());
   }

   @Override
   public void addBalance(String player, double count) {
      if (this.economy != null && !(count <= 0.0)) {
         this.economy.addBalance(player.toLowerCase(), count);
      }
   }

   @Override
   public void subtractBalance(String player, double count) {
      if (this.economy != null && !(count <= 0.0)) {
         this.economy.subtractBalance(player.toLowerCase(), count);
      }
   }
}
