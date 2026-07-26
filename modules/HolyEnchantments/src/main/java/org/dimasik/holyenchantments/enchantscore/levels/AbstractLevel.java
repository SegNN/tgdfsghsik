package org.dimasik.holyenchantments.enchantscore.levels;

import java.util.Random;

public class AbstractLevel {
   private final int chance;
   private final int enchantTableChance;

   public AbstractLevel(int chance, int enchantTableChance) {
      this.chance = chance;
      this.enchantTableChance = enchantTableChance;
   }

   public boolean random() {
      return new Random().nextDouble() <= this.chance;
   }

   public int getChance() {
      return this.chance;
   }

   public int getEnchantTableChance() {
      return this.enchantTableChance;
   }
}
