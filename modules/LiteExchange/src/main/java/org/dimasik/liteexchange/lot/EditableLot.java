package org.dimasik.liteexchange.lot;

public class EditableLot {
   public int id;
   public String playerName;
   public int coins;
   public int rate;
   public long createTime;

   public EditableLot(int id, String playerName, int coins, int rate, long createTime) {
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

   public void setId(int id) {
      this.id = id;
   }

   public void setPlayerName(String playerName) {
      this.playerName = playerName;
   }

   public void setCoins(int coins) {
      this.coins = coins;
   }

   public void setRate(int rate) {
      this.rate = rate;
   }

   public void setCreateTime(long createTime) {
      this.createTime = createTime;
   }

   public Lot toLot() {
      return new Lot(this.id, this.playerName, this.coins, this.rate, this.createTime);
   }
}
