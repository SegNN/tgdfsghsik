package org.dimasik.liteexchange.lot;

public class Lot {
   private int id;
   private String playerName;
   private int coins;
   private int rate;
   private long createTime;

   public Lot(int id, String playerName, int coins, int rate, long createTime) {
      this.id = id;
      this.playerName = playerName;
      this.coins = coins;
      this.rate = rate;
      this.createTime = createTime;
   }

   public int getId() {
      return this.id;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public int getCoins() {
      return this.coins;
   }

   public int getRate() {
      return this.rate;
   }

   public long getCreateTime() {
      return this.createTime;
   }

   public EditableLot toEditable() {
      return new EditableLot(this.id, this.playerName, this.coins, this.rate, this.createTime);
   }
}
