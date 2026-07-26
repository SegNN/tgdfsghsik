package ru.holydev.holycustom.menus.sounds;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.SoundDeathMeMenuHolder;
import ru.holydev.holycustom.items.MenuItems;

public class SoundDeathMeMenu {
   private final MenuItems items;

   public SoundDeathMeMenu(MenuItems items) {
      this.items = items;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new SoundDeathMeMenuHolder(), 54, "Звуки смерти");

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

      inventory.setItem(2, this.items.banner_crit());
      inventory.setItem(3, this.items.banner_death_me());
      inventory.setItem(4, this.items.banner_lowheal());
      inventory.setItem(5, this.items.banner_kill());
      inventory.setItem(45, this.items.back());
      if (HolyCustom.getInstance().getConfig().contains("deathme.items")) {
         for (String key : HolyCustom.getInstance().getConfig().getConfigurationSection("deathme.items").getKeys(false)) {
            String path = "deathme.items." + key;
            int slot = HolyCustom.getInstance().getConfig().getInt(path + ".slot");
            String soundName = HolyCustom.getInstance().getConfig().getString(path + ".sound");
            String displayName = HolyCustom.getInstance().getConfig().getString(path + ".name");
            String materialName = HolyCustom.getInstance().getConfig().getString(path + ".material");
            if (materialName == null) {
               HolyCustom.getInstance().getLogger().warning("Неправильный материал: " + key);
            } else {
               Material material = Material.valueOf(materialName);
               inventory.setItem(slot, this.items.soundItem(player, "death_sound", soundName, displayName, material));
            }
         }
      }

      player.openInventory(inventory);
   }
}
