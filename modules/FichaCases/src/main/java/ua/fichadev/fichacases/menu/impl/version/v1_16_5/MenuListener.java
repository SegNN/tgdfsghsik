package ua.fichadev.fichacases.menu.impl.version.v1_16_5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.menu.Menu;
import ua.fichadev.fichacases.menu.MenuItem;

public class MenuListener implements Listener {
   private FichaCases plugin;
   private Map<Inventory, Menu> menus;
   private Map<Player, List<MenuItem>> localCooldown = new HashMap<>();
   private List<MenuItem> globalCooldown = new ArrayList<>();

   public MenuListener(FichaCases plugin, Map<Inventory, Menu> menus) {
      this.plugin = plugin;
      this.menus = menus;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler
   public void onInvClick(InventoryClickEvent e) {
      Menu menu = this.menus.get(e.getView().getTopInventory());
      Player player = (Player)e.getWhoClicked();
      if (menu != null) {
         menu.onClick().accept(e);
         MenuItem menuItem = menu.getItem(e.getRawSlot());
         Entry<String, Integer> cooldown = menu.getCooldown();
         if (menuItem != null && !this.hasCooldown(player, menuItem, cooldown.getKey())) {
            this.addCooldown(player, menuItem, cooldown);
            menuItem.onClick().accept(player, e.getClick());
         }
      }
   }

   @EventHandler
   public void onInvDrag(InventoryDragEvent e) {
      Menu menu = this.menus.get(e.getView().getTopInventory());
      if (menu != null) {
         menu.onDrag().accept(e);
      }
   }

   @EventHandler
   public void onInvClose(InventoryCloseEvent e) {
      Menu menu = this.menus.get(e.getView().getTopInventory());
      if (menu != null) {
         menu.onClose().accept(e);
         if (menu.removeOnClose() && e.getInventory().getViewers().isEmpty()) {
            this.menus.remove(e.getView().getTopInventory());
         }
      }
   }

   private boolean hasCooldown(Player player, MenuItem menuItem, String cooldownType) {
      if (cooldownType.equalsIgnoreCase("global")) {
         return this.globalCooldown.contains(menuItem);
      } else {
         return !cooldownType.equalsIgnoreCase("local") ? false : this.localCooldown.containsKey(player) && this.localCooldown.get(player).contains(menuItem);
      }
   }

   private void addCooldown(final Player player, final MenuItem menuItem, Entry<String, Integer> cooldown) {
      if (cooldown.getKey().equalsIgnoreCase("global")) {
         this.globalCooldown.add(menuItem);
         (new BukkitRunnable() {
            public void run() {
               MenuListener.this.globalCooldown.remove(menuItem);
            }
         }).runTaskLater(this.plugin, cooldown.getValue().intValue());
      } else if (cooldown.getKey().equalsIgnoreCase("local")) {
         List<MenuItem> menuItems = this.localCooldown.getOrDefault(player, new ArrayList<>());
         menuItems.add(menuItem);
         this.localCooldown.put(player, menuItems);
         (new BukkitRunnable() {
            public void run() {
               MenuListener.this.removeCooldown(player, menuItem);
            }
         }).runTaskLater(this.plugin, 1L);
      }
   }

   private void removeCooldown(Player player, MenuItem menuItem) {
      List<MenuItem> menuItems = this.localCooldown.getOrDefault(player, new ArrayList<>());
      menuItems.remove(menuItem);
      this.localCooldown.put(player, menuItems);
   }
}
