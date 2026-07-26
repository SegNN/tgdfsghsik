package ua.holydev.holycases.menu;

import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public interface Menu {
   int getSize();

   String getName();

   MenuItem getItem(int var1);

   void setItem(int var1, MenuItem var2);

   void setCooldown(String var1, int var2);

   Entry<String, Integer> getCooldown();

   void removeOnClose(boolean var1);

   boolean removeOnClose();

   Inventory getInventory();

   void onClick(Consumer<InventoryClickEvent> var1);

   Consumer<InventoryClickEvent> onClick();

   void onDrag(Consumer<InventoryDragEvent> var1);

   Consumer<InventoryDragEvent> onDrag();

   void onClose(Consumer<InventoryCloseEvent> var1);

   Consumer<InventoryCloseEvent> onClose();

   void open(Player var1);

   void refresh();

   void setData(String var1, Object var2);

   Object getData(String var1);

   <T> T getData(String var1, T var2);
}
