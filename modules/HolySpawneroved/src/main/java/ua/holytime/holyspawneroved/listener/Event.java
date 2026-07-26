package ua.holytime.holyspawneroved.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import ua.holytime.holyspawneroved.HolySpawneroved;
import ua.holytime.holyspawneroved.gui.Menu;
import ua.holytime.holyspawneroved.item.EggType;
import ua.holytime.holyspawneroved.item.Item;

public class Event implements Listener {
   private final HolySpawneroved plugin;
   private final Item items;
   private final EggsClick eggsClick;

   public Event(HolySpawneroved plugin) {
      this.plugin = plugin;
      this.items = new Item(plugin);
      this.eggsClick = new EggsClick(plugin);
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent e) {
      InventoryHolder holder = e.getInventory().getHolder();
      if (holder instanceof Menu) {
         e.setCancelled(true);
         if (e.getCurrentItem() == null || !(e.getWhoClicked() instanceof Player)) {
            return;
         }

         this.eggsClick.handleEggClick((Player)e.getWhoClicked(), e.getSlot());
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent e) {
      if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && e.getItem() != null) {
         for (EggType type : EggType.values()) {
            if (e.getItem().isSimilar(this.items.createEgg(type))) {
               e.setCancelled(true);
               return;
            }
         }
      }
   }
}
