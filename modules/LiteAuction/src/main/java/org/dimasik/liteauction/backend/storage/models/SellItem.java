package org.dimasik.liteauction.backend.storage.models;

import java.io.IOException;
import java.util.Set;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemEncryptUtil;

public class SellItem {
   private final int id;
   private String player;
   private String itemStack;
   private Set<String> tags;
   private int price;
   private int amount;
   private boolean byOne;
   private long createTime;
   public boolean fake;

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

   public SellItem clone() {
      return new SellItem(this.id, this.player, this.itemStack, this.tags, this.price, this.amount, this.byOne, this.createTime, this.fake);
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
   public void setPrice(int price) {
      this.price = price;
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public void setByOne(boolean byOne) {
      this.byOne = byOne;
   }

   @Generated
   public void setCreateTime(long createTime) {
      this.createTime = createTime;
   }

   @Generated
   public void setFake(boolean fake) {
      this.fake = fake;
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
   public int getPrice() {
      return this.price;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public boolean isByOne() {
      return this.byOne;
   }

   @Generated
   public long getCreateTime() {
      return this.createTime;
   }

   @Generated
   public boolean isFake() {
      return this.fake;
   }

   @Generated
   public SellItem(int id, String player, String itemStack, Set<String> tags, int price, int amount, boolean byOne, long createTime, boolean fake) {
      this.id = id;
      this.player = player;
      this.itemStack = itemStack;
      this.tags = tags;
      this.price = price;
      this.amount = amount;
      this.byOne = byOne;
      this.createTime = createTime;
      this.fake = fake;
   }
}
