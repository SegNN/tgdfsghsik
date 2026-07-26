package ru.holydev.holycustom.menus.prem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.PremPearlMenuHolder;
import ru.holydev.holycustom.items.MenuItems;

public class PremPearlMenu {
   private final MenuItems items;

   public PremPearlMenu(MenuItems items) {
      this.items = items;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new PremPearlMenuHolder(), 54, "Частицы жемчуга");

      for (int i = 0; i <= 1; i++) {
         inventory.setItem(i, this.items.blue());
      }

      for (int i = 7; i <= 8; i++) {
         inventory.setItem(i, this.items.blue());
      }

      for (int i = 9; i <= 17; i++) {
         inventory.setItem(i, this.items.gray());
      }

      for (int i = 36; i <= 44; i++) {
         inventory.setItem(i, this.items.gray());
      }

      for (int i = 45; i <= 53; i++) {
         inventory.setItem(i, this.items.orange());
      }

      inventory.setItem(2, this.items.banner());
      inventory.setItem(3, this.items.banner1());
      inventory.setItem(4, this.items.banner2());
      inventory.setItem(45, this.items.back());
      if (HolyCustom.getInstance().getConfig().contains("pearlparticles.items")) {
         for (String key : HolyCustom.getInstance().getConfig().getConfigurationSection("pearlparticles.items").getKeys(false)) {
            String path = "pearlparticles.items." + key;
            int slot = HolyCustom.getInstance().getConfig().getInt(path + ".slot");
            String particleName = HolyCustom.getInstance().getConfig().getString(path + ".particle");
            String displayName = HolyCustom.getInstance().getConfig().getString(path + ".name");
            String materialName = HolyCustom.getInstance().getConfig().getString(path + ".material");
            if (materialName == null) {
               HolyCustom.getInstance().getLogger().warning("Неправильный материал: " + key);
            } else {
               Material material = Material.valueOf(materialName);
               inventory.setItem(slot, this.items.particleItem(particleName, displayName, material));
            }
         }
      }

      player.openInventory(inventory);
   }
}
