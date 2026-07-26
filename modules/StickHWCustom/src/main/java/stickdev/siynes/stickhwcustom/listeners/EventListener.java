package stickdev.siynes.stickhwcustom.listeners;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.db.DatabaseManager;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.items.StarItem;
import stickdev.siynes.stickhwcustom.menus.MainMenu;
import stickdev.siynes.stickhwstar.StickHWStar;

public class EventListener implements Listener {
   private final StickHWStar stickHWStar;
   private final MenuItems menuItems;
   private final StarItem starItem;

   public EventListener(StickHWStar stickHWStar) {
      this.stickHWStar = stickHWStar;
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
                  StickHWCustom.getInstance()
                     .getServer()
                     .getScheduler()
                     .runTask(StickHWCustom.getInstance(), () -> StickHWCustom.playerData.put(event.getPlayer(), playerMap));
               }
            )
      );
   }

   @EventHandler
   public void onLeave(PlayerQuitEvent event) {
      CompletableFuture.runAsync(() -> {
         HashMap<String, String> data = StickHWCustom.playerData.remove(event.getPlayer());
         if (data != null) {
            DatabaseManager.setPlayerAsync(event.getPlayer().getName(), DatabaseManager.toBase64(data));
         }
      });
   }
}
