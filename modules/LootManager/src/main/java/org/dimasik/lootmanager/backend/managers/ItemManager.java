package org.dimasik.lootmanager.backend.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.LootItem;

public class ItemManager {
   private final LootManager plugin;
   private final YamlConfiguration itemsConfig;
   private final Map<String, LootItem> items = new HashMap<>();

   public ItemManager(LootManager plugin) {
      this.plugin = plugin;
      this.itemsConfig = plugin.getItemsConfig();
      this.loadItems();
   }

   public void clearItems() {
      this.items.clear();
   }

   public void loadItems() {
      for (String id : this.itemsConfig.getKeys(false)) {
         ItemStack item = this.itemsConfig.getItemStack(id + ".item");
         int maxAmount = this.itemsConfig.getInt(id + ".maxAmount");
         double chance = this.itemsConfig.getDouble(id + ".chance");
         int level = this.itemsConfig.getInt(id + ".level");
         String category = this.itemsConfig.getString(id + ".category", "Все подряд");
         this.items.put(id, new LootItem(id, item, maxAmount, chance, level, category));
      }
   }

   public String addItem(ItemStack item, double chance, int maxAmount, int level, String category) {
      String id = UUID.randomUUID().toString().substring(0, 8);
      LootItem lootItem = new LootItem(id, item.clone(), maxAmount, chance, level, category);
      this.items.put(id, lootItem);
      this.saveItemToConfig(lootItem);
      return id;
   }

   public boolean removeItem(String id) {
      if (this.items.containsKey(id)) {
         this.items.remove(id);
         this.itemsConfig.set(id, null);
         this.plugin.saveItemsConfig();
         return true;
      } else {
         return false;
      }
   }

   public boolean setItem(LootItem lootItem) {
      if (this.items.containsKey(lootItem.getId())) {
         this.items.put(lootItem.getId(), lootItem);
         this.saveItemToConfig(lootItem);
         return true;
      } else {
         this.items.put(lootItem.getId(), lootItem);
         this.saveItemToConfig(lootItem);
         return false;
      }
   }

   public LootItem getItem(String id) {
      return this.items.get(id);
   }

   public Collection<LootItem> getAllItems() {
      return this.items.values();
   }

   public List<String> getAllCategories() {
      return this.items.values().stream().map(LootItem::getCategory).distinct().toList();
   }

   public List<String> getAllItemIds() {
      return new ArrayList<>(this.items.keySet());
   }

   public void saveItemToConfig(LootItem lootItem) {
      String id = lootItem.getId();
      this.itemsConfig.set(id + ".item", lootItem.getItem());
      this.itemsConfig.set(id + ".maxAmount", lootItem.getMaxAmount());
      this.itemsConfig.set(id + ".chance", lootItem.getChance());
      this.itemsConfig.set(id + ".level", lootItem.getLevel());
      this.itemsConfig.set(id + ".category", lootItem.getCategory());
      this.items.put(lootItem.getId(), lootItem);
      this.plugin.saveItemsConfig();
   }

   public void removeItemFromConfig(LootItem lootItem) {
      String id = lootItem.getId();
      this.itemsConfig.set(id, null);
      this.plugin.saveItemsConfig();
   }

   public List<ItemStack> getRandomItems(int count, int level) {
      if (this.items.size() < count) {
         return new ArrayList<>();
      } else {
         List<LootItem> eligibleItems = new ArrayList<>();
         double totalChance = 0.0;

         for (LootItem lootItem : this.items.values()) {
            if (lootItem.getLevel() != 0) {
               double modifiedChance = this.calculateModifiedChance(lootItem, level);
               if (modifiedChance > 0.0) {
                  eligibleItems.add(lootItem);
                  totalChance += modifiedChance;
               }
            }
         }

         if (eligibleItems.size() < count) {
            return new ArrayList<>();
         } else {
            List<ItemStack> result = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < count; i++) {
               double randomValue = random.nextDouble() * totalChance;
               double currentSum = 0.0;

               for (LootItem lootItemx : eligibleItems) {
                  currentSum += this.calculateModifiedChance(lootItemx, level);
                  if (randomValue <= currentSum) {
                     ItemStack item = lootItemx.getItem();
                     int amount = lootItemx.getMaxAmount() > 1 ? random.nextInt(lootItemx.getMaxAmount()) + 1 : 1;
                     item.setAmount(amount);
                     result.add(item);
                     break;
                  }
               }
            }

            return result;
         }
      }
   }

   public List<ItemStack> getRandomItemsByCategory(int count, String category) {
      if (this.items.size() < count) {
         return new ArrayList<>();
      } else {
         List<LootItem> eligibleItems = new ArrayList<>();
         double totalChance = 0.0;

         for (LootItem lootItem : this.items.values()) {
            if (lootItem.getCategory().equals(category) && lootItem.getLevel() == 0) {
               eligibleItems.add(lootItem);
               totalChance += lootItem.getChance();
            }
         }

         return (List<ItemStack>)(eligibleItems.size() < count ? new ArrayList<>() : this.selectRandomItems(eligibleItems, count, totalChance));
      }
   }

   public List<ItemStack> getRandomItemsByCategoryByLvL(int count, String category, int level) {
      if (this.items.size() < count) {
         return new ArrayList<>();
      } else {
         List<LootItem> eligibleItems = new ArrayList<>();
         double totalChance = 0.0;

         for (LootItem lootItem : this.items.values()) {
            if (lootItem.getLevel() != 0 && lootItem.getCategory().equals(category) && lootItem.getLevel() == level) {
               eligibleItems.add(lootItem);
               totalChance += lootItem.getChance();
            }
         }

         return (List<ItemStack>)(eligibleItems.size() < count ? new ArrayList<>() : this.selectRandomItems(eligibleItems, count, totalChance));
      }
   }

   public List<ItemStack> getRandomItemsByExactLevel(int count, int level) {
      if (this.items.size() < count) {
         return new ArrayList<>();
      } else {
         List<LootItem> eligibleItems = new ArrayList<>();
         double totalChance = 0.0;

         for (LootItem lootItem : this.items.values()) {
            if (lootItem.getLevel() != 0 && lootItem.getLevel() == level) {
               eligibleItems.add(lootItem);
               totalChance += lootItem.getChance();
            }
         }

         return (List<ItemStack>)(eligibleItems.size() < count ? new ArrayList<>() : this.selectRandomItems(eligibleItems, count, totalChance));
      }
   }

   public List<ItemStack> getItemsByLevel(int level) {
      List<ItemStack> result = new ArrayList<>();
      Random random = new Random();

      for (LootItem lootItem : this.items.values()) {
         if (lootItem.getLevel() != 0 && lootItem.getLevel() == level) {
            ItemStack item = lootItem.getItem();
            int amount = lootItem.getMaxAmount() > 1 ? random.nextInt(lootItem.getMaxAmount()) + 1 : 1;
            item.setAmount(amount);
            result.add(item);
         }
      }

      return result;
   }

   public List<ItemStack> getRandomItemsByLevelRange(int count, int minLevel, int maxLevel) {
      if (this.items.size() < count) {
         return new ArrayList<>();
      } else {
         List<LootItem> eligibleItems = new ArrayList<>();
         double totalChance = 0.0;

         for (LootItem lootItem : this.items.values()) {
            int itemLevel = lootItem.getLevel();
            if (itemLevel != 0 && itemLevel >= minLevel && itemLevel <= maxLevel) {
               eligibleItems.add(lootItem);
               totalChance += lootItem.getChance();
            }
         }

         return (List<ItemStack>)(eligibleItems.size() < count ? new ArrayList<>() : this.selectRandomItems(eligibleItems, count, totalChance));
      }
   }

   private List<ItemStack> selectRandomItems(List<LootItem> eligibleItems, int count, double totalChance) {
      List<ItemStack> result = new ArrayList<>();
      Random random = new Random();

      for (int i = 0; i < count; i++) {
         double randomValue = random.nextDouble() * totalChance;
         double currentSum = 0.0;

         for (LootItem lootItem : eligibleItems) {
            currentSum += lootItem.getChance();
            if (randomValue <= currentSum) {
               ItemStack item = lootItem.getItem();
               int amount = lootItem.getMaxAmount() > 1 ? random.nextInt(lootItem.getMaxAmount()) + 1 : 1;
               item.setAmount(amount);
               result.add(item);
               break;
            }
         }
      }

      return result;
   }

   private double calculateModifiedChance(LootItem lootItem, int level) {
      double baseChance = lootItem.getChance();
      int itemLevel = lootItem.getLevel();
      if (itemLevel < level) {
         return baseChance;
      } else {
         return itemLevel > level ? baseChance / (Math.abs(itemLevel - level) + 1) : baseChance;
      }
   }

   public int syncCategory(String sourceCategory, String targetCategory, int levelIncrease) {
      int syncedCount = 0;
      List<LootItem> itemsToCopy = new ArrayList<>();

      for (LootItem lootItem : this.items.values()) {
         if (lootItem.getCategory().equals(sourceCategory)) {
            itemsToCopy.add(lootItem);
         }
      }

      for (LootItem originalItem : itemsToCopy) {
         String newId = UUID.randomUUID().toString().substring(0, 8);
         int newLevel = Math.max(1, Math.min(100, originalItem.getLevel() + levelIncrease));
         LootItem newItem = new LootItem(newId, originalItem.getItem(), originalItem.getMaxAmount(), originalItem.getChance(), newLevel, targetCategory);
         this.items.put(newId, newItem);
         this.saveItemToConfig(newItem);
         syncedCount++;
      }

      return syncedCount;
   }
}
