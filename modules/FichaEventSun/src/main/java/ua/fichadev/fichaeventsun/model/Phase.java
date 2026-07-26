package ua.fichadev.fichaeventsun.model;

import ua.fichadev.fichaeventsun.util.ColorUtils;

public enum Phase {
   COINS("&#fcc700Монеты", "&#fcc700монетки"),
   EXPERIENCE("&x&2&C&F&8&0&0Опыт", "&x&2&C&F&8&0&0опыт");

   private final String displayName;
   private final String rewardName;

   private Phase(String displayName, String rewardName) {
      this.displayName = displayName;
      this.rewardName = rewardName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getRewardName() {
      return ColorUtils.colorize(this.rewardName);
   }

   public Phase next() {
      return this == COINS ? EXPERIENCE : COINS;
   }
}
