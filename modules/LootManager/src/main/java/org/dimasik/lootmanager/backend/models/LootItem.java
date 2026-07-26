package org.dimasik.lootmanager.backend.models;

import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.lootmanager.LootManager;

public class LootItem {
   private final String id;
   private ItemStack item;
   private int maxAmount;
   private double chance;
   private int level;
   private String category;

   public LootItem(String id, ItemStack item, int maxAmount, double chance, int level, String category) {
      this.id = id;
      this.item = item;
      this.maxAmount = maxAmount;
      this.chance = chance;
      this.level = level;
      this.category = category != null && !category.isEmpty() ? category : "Все подряд";
   }

   public void setCategory(String category) {
      this.category = category != null && !category.isEmpty() ? category : "Все подряд";
   }

   public static LootItem fromItemStack(ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta meta = itemStack.getItemMeta();
         NamespacedKey key = new NamespacedKey(LootManager.getInstance(), "loot_item_data");
         String data = (String)meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
         if (data == null) {
            return null;
         } else {
            String[] parts = data.split(";");
            if (parts.length != 5) {
               return null;
            } else {
               ItemStack newItem = itemStack.clone();
               newItem.setAmount(1);
               return new LootItem(parts[0], newItem, Integer.parseInt(parts[1]), Double.parseDouble(parts[2]), Integer.parseInt(parts[3]), parts[4]);
            }
         }
      } else {
         return null;
      }
   }

   public ItemStack getItem() {
      return this.item.clone();
   }

   public LootItem clone() {
      return new LootItem(this.id, this.item, this.maxAmount, this.chance, this.level, this.category);
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public int getMaxAmount() {
      return this.maxAmount;
   }

   @Generated
   public double getChance() {
      return this.chance;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }

   @Generated
   public String getCategory() {
      return this.category;
   }

   @Generated
   public void setItem(ItemStack item) {
      this.item = item;
   }

   @Generated
   public void setMaxAmount(int maxAmount) {
      this.maxAmount = maxAmount;
   }

   @Generated
   public void setChance(double chance) {
      this.chance = chance;
   }

   @Generated
   public void setLevel(int level) {
      this.level = level;
   }
}
