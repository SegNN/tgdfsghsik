package org.dimasik.liteauction.frontend.menus.abst;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteauction.LiteAuction;

public abstract class AbstractListener implements Listener {
   public AbstractListener() {
   }

   @EventHandler
   public abstract void onClick(InventoryClickEvent var1);

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
   }

   private void register(JavaPlugin plugin) {
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public void register() {
      this.register(LiteAuction.getInstance());
   }
}
