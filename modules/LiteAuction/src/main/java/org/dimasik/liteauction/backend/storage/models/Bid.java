package org.dimasik.liteauction.backend.storage.models;

import lombok.Generated;

public class Bid {
   private int id;
   private int itemId;
   private String player;
   private int price;

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public int getItemId() {
      return this.itemId;
   }

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public int getPrice() {
      return this.price;
   }

   @Generated
   public void setId(int id) {
      this.id = id;
   }

   @Generated
   public void setItemId(int itemId) {
      this.itemId = itemId;
   }

   @Generated
   public void setPlayer(String player) {
      this.player = player;
   }

   @Generated
   public void setPrice(int price) {
      this.price = price;
   }

   @Generated
   public Bid(int id, int itemId, String player, int price) {
      this.id = id;
      this.itemId = itemId;
      this.player = player;
      this.price = price;
   }
}
