package ru.holydev.holycustom.listeners;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.db.DatabaseManager;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.items.StarItem;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holystar.HolyStar;

public class EventListener implements Listener {
   private final HolyStar holyStar;
   private final MenuItems menuItems;
   private final StarItem starItem;

   public EventListener(HolyStar holyStar) {
      this.holyStar = holyStar;
      this.menuItems = new MenuItems();
      this.starItem = new StarItem();
   }

   @EventHandler
   public void onClick(InventoryClickEvent e) {
      Inventory inventory = e.getClickedInventory();
      if (inventory != null && inventory.getHolder() != null) {
         InventoryHolder holder = inventory.getHolder();
         if (holder instanceof MainMenu) {
            e.setCancelled(true);
            MainMenu menu = (MainMenu)holder;
            menu.onClick(e);
         }
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      CompletableFuture.runAsync(
         () -> DatabaseManager.getPlayerAsync(event.getPlayer().getName())
            .thenAccept(
               data -> {
                  HashMap<String, String> playerMap = data != null ? DatabaseManager.fromBase64(data) : new HashMap<>();
                  HolyCustom.getInstance()
                     .getServer()
                     .getScheduler()
                     .runTask(HolyCustom.getInstance(), () -> HolyCustom.playerData.put(event.getPlayer(), playerMap));
               }
            )
      );
   }

   @EventHandler
   public void onLeave(PlayerQuitEvent event) {
      CompletableFuture.runAsync(() -> {
         HashMap<String, String> data = HolyCustom.playerData.remove(event.getPlayer());
         if (data != null) {
            DatabaseManager.setPlayerAsync(event.getPlayer().getName(), DatabaseManager.toBase64(data));
         }
      });
   }
}
