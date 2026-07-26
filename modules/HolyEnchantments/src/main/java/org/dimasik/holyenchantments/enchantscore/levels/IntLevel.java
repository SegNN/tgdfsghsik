package org.dimasik.holyenchantments.enchantscore.levels;

public class IntLevel extends AbstractLevel {
   private final int value;

   public IntLevel(int chance, int enchantTableChance, int value) {
      super(chance, enchantTableChance);
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }
}
