package ua.fichadev.fichapotion.util;

public class ExpUtil {
   public ExpUtil() {
   }

   public static int getLevelsFromExp(int exp) {
      int level = 0;

      for (int expRequired = getExpForNextLevel(level); exp >= expRequired; expRequired = getExpForNextLevel(++level)) {
         exp -= expRequired;
      }

      return level;
   }

   private static int getExpForNextLevel(int level) {
      if (level < 0) {
         return 0;
      } else if (level < 15) {
         return 2 * level + 7;
      } else {
         return level < 30 ? 5 * level - 38 : 9 * level - 158;
      }
   }
}
