package org.dimasik.holyenchantments.utils;

import java.util.HashMap;
import java.util.Map.Entry;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;

public class Random {
   public Random() {
   }

   public static int getPercent() {
      return rnd(1, 100);
   }

   public static int rnd(int min, int max) {
      max -= min;
      double var10000 = Math.random();
      return (int)(var10000 * ++max) + min;
   }

   public static int generateLevelForEnchantTable(HashMap<Integer, AbstractLevel> levels) {
      HashMap<Integer, Integer> prizes = new HashMap<>();
      int allChance = 0;

      for (Entry<Integer, AbstractLevel> entry : levels.entrySet()) {
         int a = entry.getValue().getEnchantTableChance();
         prizes.put(entry.getKey(), a);
         allChance += a;
      }

      int mprize = 1;
      int random = new java.util.Random().nextInt(allChance);
      int a = 0;

      for (Entry<Integer, Integer> e : prizes.entrySet()) {
         a += e.getValue();
         if (random <= a) {
            mprize = e.getKey();
            break;
         }
      }

      return mprize;
   }
}
