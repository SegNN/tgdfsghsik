package ua.fichadev.fichacases.menu;

import java.util.function.BiConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public interface MenuManager {
   Menu createMenu();

   Menu createMenu(String var1);

   Menu createMenu(int var1);

   Menu createMenu(InventoryType var1);

   Menu createMenu(String var1, int var2);

   Menu createMenu(String var1, InventoryType var2);

   MenuItem createItem(ItemStack var1);

   MenuItem createItem(ItemStack var1, BiConsumer<Player, ClickType> var2);
}
