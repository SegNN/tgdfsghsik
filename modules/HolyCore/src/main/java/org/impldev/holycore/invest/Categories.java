package org.impldev.holycore.invest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.impldev.holycore.invest.Category.Kind;

/**
 * Набор направлений инвестиций.
 *
 * <p>Значения (потолок уровня, сколько очков нужно на уровень, цена очка и вес одного уровня)
 * подобраны так, чтобы совпадать с тем, что показывал исходный сервер: например, порох на 25-м
 * уровне даёт +375%, то есть 15% за уровень, а автошахта на 33-м сокращает время на 33%, то есть
 * по проценту за уровень.
 */
public final class Categories {
   private static final Map<String, Category> BY_ID = new LinkedHashMap<>();

   private Categories() {
   }

   private static void add(Category c) {
      BY_ID.put(c.id(), c);
   }

   static {
      add(new Category("buyer", 11, Material.DIAMOND, "Товар скупщика", 100, 250, 1_170_000L, 0.0, 0.0,
            Kind.EXTERNAL, List.of(
                  "&r &r&x&f&f&a&2&0&0&l&n▍&r&f Увеличивает стоимость продажи",
                  "&r &r&x&f&f&a&2&0&0&l▍&r&f случайного товара у &r&x&f&f&a&2&0&0скупщика")));

      add(new Category("gunpowder", 13, Material.GUNPOWDER, "Порох", 30, 75, 1_725_000L, 15.0, 0.0,
            Kind.BONUS, List.of(
                  "&r &r&x&f&f&a&2&0&0&l&n▍&r&f Увеличивает количество выпадаемого",
                  "&r &r&x&f&f&a&2&0&0&l▍&r&f пороха с &r&x&f&f&a&2&0&0крипера &r&7(кроме спавнеров)")));

      add(new Category("experience", 15, Material.EXPERIENCE_BOTTLE, "Опыт", 50, 150, 1_250_000L, 5.0, 0.0,
            Kind.BONUS, List.of(
                  "&r &r&x&f&f&a&2&0&0&l&n▍&r&f Увеличивает количество выпадаемого",
                  "&r &r&x&f&f&a&2&0&0&l▍&r&f опыта со всех &r&x&f&f&a&2&0&0мобов на карте")));

      add(new Category("automine", 28, Material.DIAMOND_PICKAXE, "Автошахта", 70, 100, 1_200_000L, 1.0, 600.0,
            Kind.TIME, List.of(
                  "&r &r&x&f&f&a&2&0&0&l&n▍&r&f Уменьшает время обновления",
                  "&r &r&x&f&f&a&2&0&0&l▍&r&f всех &r&x&f&f&a&2&0&0Автошахт&r&f на спавне")));

      add(new Category("voting", 30, Material.CAMPFIRE, "Голосование", 30, 250, 1_125_000L, 1.0, 3900.0,
            Kind.TIME, List.of(
                  "&r &r&x&f&f&a&2&0&0&l&n▍&r&f Уменьшает время, требуемое для",
                  "&r &r&x&f&f&a&2&0&0&l▍&r&f старта &r&x&f&f&a&2&0&0нового голосования&r&f за ивенты")));

      add(new Category("treasures", 32, Material.COMPASS, "Сокровища", 50, 50, 1_950_000L, 1.0, 6000.0,
            Kind.TIME, List.of(
                  "&r &r&x&f&f&a&2&0&0&l&n▍&r&f Ускоряет спавн &r&x&f&f&a&2&0&0всех сокровищ",
                  "&r &r&x&f&f&a&2&0&0&l▍&r&f появляемых в игровом мире")));

      add(new Category("borders", 34, Material.STRUCTURE_VOID, "Границы миров", 20, 75, 16_400_000L, 0.0, 0.0,
            Kind.BORDER, List.of(
                  "&r &r&x&f&f&a&2&0&0&l•&r&f Расширяет радиус границ &r&x&f&f&a&2&0&0мира")));
   }

   public static Map<String, Category> all() {
      return BY_ID;
   }

   public static Category bySlot(int slot) {
      for (Category c : BY_ID.values()) {
         if (c.slot() == slot) {
            return c;
         }
      }

      return null;
   }
}
