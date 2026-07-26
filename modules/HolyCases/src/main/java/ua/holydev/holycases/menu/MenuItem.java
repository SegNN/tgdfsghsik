package ua.holydev.holycases.menu;

import java.util.function.BiConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface MenuItem {
   ItemStack item();

   void item(ItemStack var1);

   BiConsumer<Player, ClickType> onClick();

   void onClick(BiConsumer<Player, ClickType> var1);
}
