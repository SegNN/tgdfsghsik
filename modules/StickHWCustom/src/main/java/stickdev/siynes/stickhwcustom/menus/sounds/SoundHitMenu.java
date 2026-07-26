package stickdev.siynes.stickhwcustom.menus.sounds;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.holders.SoundHitMenuHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;

public class SoundHitMenu {
   private final MenuItems items;

   public SoundHitMenu(MenuItems items) {
      this.items = items;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new SoundHitMenuHolder(), 54, "Звуки");

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
      if (StickHWCustom.getInstance().getConfig().contains("hitsounds.items")) {
         for (String key : StickHWCustom.getInstance().getConfig().getConfigurationSection("hitsounds.items").getKeys(false)) {
            String path = "hitsounds.items." + key;
            int slot = StickHWCustom.getInstance().getConfig().getInt(path + ".slot");
            String soundName = StickHWCustom.getInstance().getConfig().getString(path + ".sound");
            String displayName = StickHWCustom.getInstance().getConfig().getString(path + ".name");
            String materialName = StickHWCustom.getInstance().getConfig().getString(path + ".material");
            if (materialName == null) {
               StickHWCustom.getInstance().getLogger().warning("Неправильный материал: " + key);
            } else {
               Material material = Material.valueOf(materialName);
               inventory.setItem(slot, this.items.soundItem(player, "hit_sound", soundName, displayName, material));
            }
         }
      }

      player.openInventory(inventory);
   }
}
