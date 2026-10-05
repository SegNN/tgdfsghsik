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

   /**
    * Цены бустера Скупщика по уровням - ровно те, что записаны в вики режима.
    *
    * <p>Списком, а не формулой: цены растут почти геометрически (шаг между соседними
    * уровнями каждый раз умножается примерно на 1.25), но точные значения на середине
    * лестницы отличаются от любой такой формулы на единицы, и подгонять их незачем.
    */
   private static final List<Integer> BUYER_LADDER = List.of(
         100, 138, 185, 244, 318, 410, 525, 669, 849, 1074,
         1355, 1707, 2147, 2697, 3384, 4243, 5317, 6659, 8337, 10434);

   private static final Map<String, List<Integer>> LADDERS = Map.of("buyer", BUYER_LADDER);

   /** Бустеры, которые куплены до конца вайпа, а не на срок. */
   private static final java.util.Set<String> PERMANENT = java.util.Set.of("buyer");

   private static void add(Booster b) {
      BY_ID.put(b.id(), b);
   }

   /**
    * Цена следующего уровня.
    *
    * <p>Если для бустера задана лестница цен, берём её; на последнем уровне возвращаем 0 -
    * улучшать дальше некуда.
    */
   public static int priceFor(Booster booster, int level) {
      List<Integer> ladder = LADDERS.get(booster.id());
      if (ladder == null) {
         return booster.priceFor(level);
      }

      return level >= 1 && level <= ladder.size() ? ladder.get(level - 1) : 0;
   }

   /** Потолок уровня; 0 - потолка нет. */
   public static int maxLevel(Booster booster) {
      List<Integer> ladder = LADDERS.get(booster.id());
      return ladder == null ? 0 : ladder.size();
   }

   public static boolean permanent(Booster booster) {
      return PERMANENT.contains(booster.id());
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
