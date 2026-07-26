package org.dimasik.liteauction.frontend.commands.models;

import lombok.Generated;
import org.bukkit.inventory.ItemStack;

public class AutoSellData {
   public int price;
   public ItemStack itemStack;
   public boolean full;

   @Generated
   public int getPrice() {
      return this.price;
   }

   @Generated
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @Generated
   public boolean isFull() {
      return this.full;
   }

   @Generated
   public void setPrice(int price) {
      this.price = price;
   }

   @Generated
   public void setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   @Generated
   public void setFull(boolean full) {
      this.full = full;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof AutoSellData other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getPrice() != other.getPrice()) {
         return false;
      } else if (this.isFull() != other.isFull()) {
         return false;
      } else {
         Object this$itemStack = this.getItemStack();
         Object other$itemStack = other.getItemStack();
         return this$itemStack == null ? other$itemStack == null : this$itemStack.equals(other$itemStack);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof AutoSellData;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getPrice();
      result = result * 59 + (this.isFull() ? 79 : 97);
      Object $itemStack = this.getItemStack();
      return result * 59 + ($itemStack == null ? 43 : $itemStack.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "AutoSellData(price=" + this.getPrice() + ", itemStack=" + this.getItemStack() + ", full=" + this.isFull() + ")";
   }

   @Generated
   public AutoSellData(int price, ItemStack itemStack, boolean full) {
      this.price = price;
      this.itemStack = itemStack;
      this.full = full;
   }
}
