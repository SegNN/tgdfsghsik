package org.impldev.holycore.boosters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

/**
 * Набор бустеров.
 *
 * <p>Числа взяты с исходного сервера: там у каждого бустера были видны бонус первого уровня и
 * бонус следующего (например, опыт +10% и +21%), поэтому шаг задан их разностью. Дальше
 * прогрессия продолжается тем же шагом - это единственное, что можно достроить по двум точкам,
 * не выдумывая формулу.
 */
public final class Boosters {
   private static final Map<String, Booster> BY_ID = new LinkedHashMap<>();

   private Boosters() {
   }

   private static void add(Booster b) {
      BY_ID.put(b.id(), b);
   }

   static {
      add(new Booster("experience", 20, Material.EXPERIENCE_BOTTLE, "Бустер Опыта",
            "&r&x&5&0&f&f&3&f", "&r&x&7&a&f&f&6&f", 10, 11, 138, List.of(
                  "увеличивает количество",
                  "выпадающего опыта с мобов.")));

      add(new Booster("mobmoney", 21, Material.SUNFLOWER, "Бустер Монет с Мобов",
            "&r&x&f&f&c&9&0&0", "&r&x&f&f&c&2&2&d", 25, 19, 75, List.of(
                  "увеличивает количество",
                  "выдаваемых монет с мобов.")));

      add(new Booster("buyer", 22, Material.NETHERITE_INGOT, "Бустер Скупщика",
            "&r&x&f&b&0&f&d&4", "&r&x&f&b&3&b&d&f", 2, 2, 138, List.of(
                  "увеличивает цену продажи",
                  "предметов у Скупщика.")));

      add(new Booster("crops", 23, Material.CARROT, "Бустер Культур",
            "&r&x&f&f&7&0&0&0", "&r&x&f&f&8&4&2&f", 15, 8, 107, List.of(
                  "увеличивает количество",
                  "выпадаемых культур при сборе.")));

      add(new Booster("mobdamage", 24, Material.NETHERITE_SWORD, "Бустер Урона по Мобам",
            "&r&x&f&d&1&8&1&8", "&r&x&f&d&4&f&4&b", 5, 3, 107, List.of(
                  "увеличивает наносимый",
                  "урон по мобам и боссам.")));
   }

   public static Map<String, Booster> all() {
      return BY_ID;
   }

   public static Booster bySlot(int slot) {
      for (Booster b : BY_ID.values()) {
         if (b.slot() == slot) {
            return b;
         }
      }

      return null;
   }
}
