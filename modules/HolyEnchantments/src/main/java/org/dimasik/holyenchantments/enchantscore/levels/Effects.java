package org.dimasik.holyenchantments.enchantscore.levels;

import java.util.List;

public class Effects extends AbstractLevel {
   private final List<String> effects;

   public Effects(int chance, int enchantTableChance, List<String> effects) {
      super(chance, enchantTableChance);
      this.effects = effects;
   }

   public List<String> getEffects() {
      return this.effects;
   }
}
