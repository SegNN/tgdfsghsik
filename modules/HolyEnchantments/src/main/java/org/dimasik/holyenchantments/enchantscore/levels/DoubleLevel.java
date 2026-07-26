package org.dimasik.holyenchantments.enchantscore.levels;

public class DoubleLevel extends AbstractLevel {
   private final double value;

   public DoubleLevel(int chance, int enchantTableChance, double value) {
      super(chance, enchantTableChance);
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }
}
