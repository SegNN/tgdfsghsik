package stickdev.siynes.stickhwcustom.menus.sounds;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import stickdev.siynes.stickhwcustom.holders.SoundVolumeMenuHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;

public class SoundVolumeMenu {
   private final MenuItems items;

   public SoundVolumeMenu(MenuItems items) {
      this.items = items;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new SoundVolumeMenuHolder(), 54, "Настройки");

      for (int slot = 9; slot < 36; slot++) {
         double volume = 0.1 + (slot - 9) * 0.1;
         inventory.setItem(slot, this.items.green(volume, player));
      }

      inventory.setItem(49, this.items.saveSound());
      player.openInventory(inventory);
   }
}
