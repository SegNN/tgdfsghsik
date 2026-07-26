package org.dimasik.lootmanager;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.backend.managers.ItemManager;

public class LootAPI {
   private final ItemManager itemManager;

   public LootAPI() {
      this.itemManager = LootManager.getInstance().itemManager;
   }

   public List<ItemStack> getRandomItems(int count, int level) {
      return this.itemManager.getRandomItems(count, level);
   }

   public List<ItemStack> getRandomItemsByCategory(int count, String category) {
      return this.itemManager.getRandomItemsByCategory(count, category);
   }

   public List<ItemStack> getRandomItemsByCategoryByLvL(int count, String category, int level) {
      return this.itemManager.getRandomItemsByCategoryByLvL(count, category, level);
   }

   public List<ItemStack> getRandomItemsByExactLevel(int count, int level) {
      return this.itemManager.getRandomItemsByExactLevel(count, level);
   }

   public List<ItemStack> getItemsByLevel(int level) {
      return this.itemManager.getItemsByLevel(level);
   }

   public List<ItemStack> getItemsByLevel(int count, int level) {
      return this.itemManager.getRandomItemsByExactLevel(count, level);
   }

   public List<ItemStack> getRandomItemsByLevelRange(int count, int minLevel, int maxLevel) {
      return this.itemManager.getRandomItemsByLevelRange(count, minLevel, maxLevel);
   }
}
