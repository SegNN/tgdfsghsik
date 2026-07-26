package ua.fichadev.fichacases.menu.impl.version.v1_16_5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import ua.fichadev.fichacases.menu.Menu;
import ua.fichadev.fichacases.menu.MenuItem;
import ua.fichadev.fichacases.menu.reflection.ReflectionUtil;

public class MenuImpl implements Menu {
   private Inventory inventory;
   private Map<Integer, MenuItem> items = new HashMap<>();
   private String cooldownType = "LOCAL";
   private int cooldownTime = 10;
   private boolean removeOnClose;
   private Consumer<InventoryClickEvent> onClick = e -> e.setCancelled(true);
   private Consumer<InventoryDragEvent> onDrag = e -> e.setCancelled(true);
   private Consumer<InventoryCloseEvent> onClose = e -> {};
   private Map<String, Object> data = new HashMap<>();

   public MenuImpl() {
      this.inventory = Bukkit.createInventory(null, 9);
   }

   public MenuImpl(int size) {
      this.inventory = Bukkit.createInventory(null, size);
   }

   public MenuImpl(String name) {
      this.inventory = Bukkit.createInventory(null, 9, name);
   }

   public MenuImpl(InventoryType type) {
      this.inventory = Bukkit.createInventory(null, type);
   }

   public MenuImpl cloneForPlayer(Player player) {
      MenuImpl clone = new MenuImpl(this.inventory.getSize(), this.getName());
      this.items.forEach(clone::setItem);
      return clone;
   }

   public MenuImpl(int size, String name) {
      this.inventory = Bukkit.createInventory(null, size, name);
   }

   public MenuImpl(InventoryType type, String name) {
      this.inventory = Bukkit.createInventory(null, type, name);
   }

   @Override
   public int getSize() {
      return this.inventory.getSize();
   }

   @Override
   public String getName() {
      return String.valueOf(ReflectionUtil.getFieldValue("title", ((CraftInventory)this.inventory).getInventory()));
   }

   @Override
   public MenuItem getItem(int slot) {
      return this.items.get(slot);
   }

   @Override
   public void setItem(int slot, MenuItem menuItem) {
      if (menuItem == null) {
         this.inventory.setItem(slot, null);
         this.items.remove(slot);
      } else {
         this.inventory.setItem(slot, menuItem.item());
         this.items.put(slot, menuItem);
      }
   }

   @Override
   public void setCooldown(String type, int time) {
      if (type != null && List.of("global", "local").contains(type.toLowerCase())) {
         this.cooldownType = type;
      }

      if (time > 0) {
         this.cooldownTime = time;
      }
   }

   @Override
   public Entry<String, Integer> getCooldown() {
      return Map.entry(this.cooldownType, this.cooldownTime);
   }

   @Override
   public void removeOnClose(boolean removeOnClose) {
      this.removeOnClose = removeOnClose;
   }

   @Override
   public boolean removeOnClose() {
      return this.removeOnClose;
   }

   @Override
   public Inventory getInventory() {
      return this.inventory;
   }

   @Override
   public void onClick(Consumer<InventoryClickEvent> onClick) {
      this.onClick = onClick;
   }

   @Override
   public Consumer<InventoryClickEvent> onClick() {
      return this.onClick;
   }

   @Override
   public void onDrag(Consumer<InventoryDragEvent> onDrag) {
      this.onDrag = onDrag;
   }

   @Override
   public Consumer<InventoryDragEvent> onDrag() {
      return this.onDrag;
   }

   @Override
   public void onClose(Consumer<InventoryCloseEvent> onClose) {
      this.onClose = onClose;
   }

   @Override
   public Consumer<InventoryCloseEvent> onClose() {
      return this.onClose;
   }

   @Override
   public void open(Player player) {
      player.openInventory(this.inventory);
   }

   @Override
   public void refresh() {
      this.items.forEach((slot, item) -> this.inventory.setItem(slot, item.item()));
   }

   @Override
   public void setData(String key, Object data) {
      this.data.put(key, data);
   }

   @Override
   public Object getData(String key) {
      return this.data.get(key);
   }

   @Override
   public <T> T getData(String key, T def) {
      return (T)Optional.ofNullable(this.data.get(key)).orElse(def);
   }
}
