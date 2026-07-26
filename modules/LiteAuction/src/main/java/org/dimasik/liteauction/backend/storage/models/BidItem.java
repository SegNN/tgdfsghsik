package org.dimasik.liteauction.backend.storage.models;

import java.io.IOException;
import java.util.Set;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemEncryptUtil;

public class BidItem {
   private int id;
   private String player;
   private String itemStack;
   private Set<String> tags;
   private int startPrice;
   private int currentPrice;
   private int step;
   private long expiryTime;
   private long createTime;

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

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public String getItemStack() {
      return this.itemStack;
   }

   @Generated
   public Set<String> getTags() {
      return this.tags;
   }

   @Generated
   public int getStartPrice() {
      return this.startPrice;
   }

   @Generated
   public int getCurrentPrice() {
      return this.currentPrice;
   }

   @Generated
   public int getStep() {
      return this.step;
   }

   @Generated
   public long getExpiryTime() {
      return this.expiryTime;
   }

   @Generated
   public long getCreateTime() {
      return this.createTime;
   }

   @Generated
   public void setId(int id) {
      this.id = id;
   }

   @Generated
   public void setPlayer(String player) {
      this.player = player;
   }

   @Generated
   public void setItemStack(String itemStack) {
      this.itemStack = itemStack;
   }

   @Generated
   public void setTags(Set<String> tags) {
      this.tags = tags;
   }

   @Generated
   public void setStartPrice(int startPrice) {
      this.startPrice = startPrice;
   }

   @Generated
   public void setCurrentPrice(int currentPrice) {
      this.currentPrice = currentPrice;
   }

   @Generated
   public void setStep(int step) {
      this.step = step;
   }

   @Generated
   public void setExpiryTime(long expiryTime) {
      this.expiryTime = expiryTime;
   }

   @Generated
   public void setCreateTime(long createTime) {
      this.createTime = createTime;
   }

   @Generated
   public BidItem(int id, String player, String itemStack, Set<String> tags, int startPrice, int currentPrice, int step, long expiryTime, long createTime) {
      this.id = id;
      this.player = player;
      this.itemStack = itemStack;
      this.tags = tags;
      this.startPrice = startPrice;
      this.currentPrice = currentPrice;
      this.step = step;
      this.expiryTime = expiryTime;
      this.createTime = createTime;
   }
}
