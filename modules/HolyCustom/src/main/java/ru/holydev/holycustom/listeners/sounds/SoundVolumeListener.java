package ru.holydev.holycustom.listeners.sounds;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.SoundVolumeMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.menus.sounds.SoundHitMenu;
import ru.holydev.holycustom.menus.sounds.SoundVolumeMenu;

public class SoundVolumeListener implements Listener {
   public SoundVolumeListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Player player = (Player)e.getWhoClicked();
         InventoryHolder holder = e.getInventory().getHolder();
         if (holder instanceof SoundVolumeMenuHolder) {
            e.setCancelled(true);
            int slot = e.getSlot();
            if (slot > 8 && slot < 36) {
               double volume = 0.1 + (slot - 9) * 0.1;
               volume = Math.round(volume * 10.0) / 10.0;
               HolyCustom.getInstance();
               HolyCustom.setPlayerData(player, "sound_pitch", String.valueOf(volume));
               MenuItems menuItems = new MenuItems();
               SoundVolumeMenu soundVolumeMenu = new SoundVolumeMenu(menuItems);
               soundVolumeMenu.open(player);
            } else if (slot == 49) {
               MenuItems menuItems = new MenuItems();
               SoundHitMenu mainMenu = new SoundHitMenu(menuItems);
               mainMenu.open(player);
            }
         }
      }
   }
}
