package org.nezxenka.litecontainer.utils;

import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class ContainerItem {
   private final String name;
   private final ItemRarity rarity;
   private final double price;
   private final ItemStack itemStack;

   public ContainerItem(String name, ItemRarity rarity, double price, ItemStack itemStack) {
      this.name = name;
      this.rarity = rarity;
      this.price = price;
      this.itemStack = itemStack;
   }

   public String name() {
      return this.name;
   }

   public ItemRarity rarity() {
      return this.rarity;
   }

   public double price() {
      return this.price;
   }

   public ItemStack itemStack() {
      return this.itemStack;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         ContainerItem that = (ContainerItem)obj;
         return Double.compare(that.price, this.price) == 0
            && Objects.equals(this.name, that.name)
            && this.rarity == that.rarity
            && Objects.equals(this.itemStack, that.itemStack);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.rarity, this.price, this.itemStack);
   }

   @Override
   public String toString() {
      return "ContainerItem{name='" + this.name + "', rarity=" + this.rarity + ", price=" + this.price + ", itemStack=" + this.itemStack + "}";
   }
}
