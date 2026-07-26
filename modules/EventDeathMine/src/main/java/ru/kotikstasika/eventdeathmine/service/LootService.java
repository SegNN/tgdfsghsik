package ru.kotikstasika.eventdeathmine.service;

import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.eventdeathmine.EventDeathMine;

public class LootService {
   private final LootAPI lootAPI = new LootAPI();
   private EventDeathMine plugin;

   public void giveRandomLoot(Player player, int min, int max) {
      List<ItemStack> loot = this.lootAPI.getRandomItems(min, max);
      if (loot != null && !loot.isEmpty()) {
         this.addItemInventory(player.getInventory(), loot.get(0), player.getLocation());
      }
   }

   public void giveLootForBlock(Player player, Material blockType) {
      int min = 1;
      int max = 1;
      if (blockType == Material.CRYING_OBSIDIAN) {
         min = 2;
         max = 2;
      } else if (blockType == Material.ANCIENT_DEBRIS) {
         min = 3;
         max = 3;
      }

      this.giveRandomLoot(player, min, max);
   }

   public void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      for (int id = 0; id < inventory.getStorageContents().length; id++) {
         ItemStack item = inventory.getItem(id);
         if (item == null || item.getType().isAir()) {
            inventory.addItem(new ItemStack[]{itemStack});
            return;
         }

         if (item.isSimilar(itemStack)) {
            int count = item.getMaxStackSize() - item.getAmount();
            if (count > 0) {
               if (itemStack.getAmount() <= count) {
                  inventory.addItem(new ItemStack[]{itemStack});
                  return;
               }

               ItemStack i = itemStack.clone();
               i.setAmount(count);
               inventory.addItem(new ItemStack[]{i});
               itemStack.setAmount(itemStack.getAmount() - count);
            }
         }
      }

      Bukkit.getScheduler().runTask(this.plugin, () -> location.getWorld().dropItemNaturally(location, itemStack));
   }

   @Generated
   public LootService(EventDeathMine plugin) {
      this.plugin = plugin;
   }
}
