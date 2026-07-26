package ru.kotikstasika.eventpolana.manager;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.eventpolana.config.PluginConfig;

public class LootManagerBridge {
   private final LootAPI lootAPI = new LootAPI();
   private final PluginConfig config;

   public LootManagerBridge(PluginConfig config) {
      this.config = config;
   }

   public void dropRandomLoot(Location location, int lootLevel) {
      try {
         List<ItemStack> loot = this.lootAPI.getRandomItems(1, lootLevel);
         if (loot == null || loot.isEmpty()) {
            return;
         }

         for (ItemStack item : loot) {
            if (item != null && item.getType() != Material.AIR) {
               location.getWorld().dropItemNaturally(location.add(0.5, 0.5, 0.5), item);
            }
         }

         try {
            Sound sound = Sound.valueOf(this.config.getSoundLootDrop());
            location.getWorld().playSound(location, sound, 1.0F, 1.5F);
         } catch (IllegalArgumentException var6) {
            location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.5F);
         }
      } catch (Exception var7) {
      }
   }
}
