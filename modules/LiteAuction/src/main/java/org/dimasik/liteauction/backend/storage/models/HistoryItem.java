package org.dimasik.liteauction.backend.storage.models;

import java.io.IOException;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemEncryptUtil;

public class HistoryItem {
   private final int id;
   private String player;
   private String buyer;
   private String itemStack;
   private int amount;
   private int price;
   private long time;
   private boolean displayed;

   public ItemStack decodeItemStack() {
      try {
         return ItemEncryptUtil.decodeItem(this.itemStack);
      } catch (ClassNotFoundException | IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   public void encodeAndPutItemStack(ItemStack itemStack) {
      try {
         this.itemStack = ItemEncryptUtil.encodeItem(itemStack.asOne());
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   public HistoryItem clone() {
      return new HistoryItem(this.id, this.player, this.buyer, this.itemStack, this.amount, this.price, this.time, this.displayed);
   }

   @Generated
   public void setPlayer(String player) {
      this.player = player;
   }

   @Generated
   public void setBuyer(String buyer) {
      this.buyer = buyer;
   }

   @Generated
   public void setItemStack(String itemStack) {
      this.itemStack = itemStack;
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public void setPrice(int price) {
      this.price = price;
   }

   @Generated
   public void setTime(long time) {
      this.time = time;
   }

   @Generated
   public void setDisplayed(boolean displayed) {
      this.displayed = displayed;
   }

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public String getBuyer() {
      return this.buyer;
   }

   @Generated
   public String getItemStack() {
      return this.itemStack;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public int getPrice() {
      return this.price;
   }

   @Generated
   public long getTime() {
      return this.time;
   }

   @Generated
   public boolean isDisplayed() {
      return this.displayed;
   }

   @Generated
   public HistoryItem(int id, String player, String buyer, String itemStack, int amount, int price, long time, boolean displayed) {
      this.id = id;
      this.player = player;
      this.buyer = buyer;
      this.itemStack = itemStack;
      this.amount = amount;
      this.price = price;
      this.time = time;
      this.displayed = displayed;
   }
}
