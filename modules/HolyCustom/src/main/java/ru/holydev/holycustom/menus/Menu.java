package ru.holydev.holycustom.menus;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {
   protected final List<Button> buttons = new ArrayList<>();

   public Menu() {
   }

   public void onClick(InventoryClickEvent e) {
      this.buttons.forEach(b -> b.onClick(e));
   }

   public abstract void open(Player var1);
}
