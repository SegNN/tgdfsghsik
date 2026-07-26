package org.dimasik.liteexchange.lot;

public class RateData {
   public int rate;
   public int sourceRate;
   public int coins;
   public boolean forceClose = false;
   public boolean breaked = false;

   public RateData(int rate, int coins) {
      this.rate = rate;
      this.sourceRate = rate;
      this.coins = coins;
   }
}
