package org.impldev.holycore.boosters;

import java.util.List;
import org.bukkit.Material;

/**
 * Описание одного бустера.
 *
 * @param id        ключ хранения и плейсхолдеров
 * @param slot      место в меню
 * @param material  иконка
 * @param title     название без процента - процент дорисовывается по текущему уровню
 * @param color     цвет названия
 * @param accent    цвет блока "Эффект бустера"
 * @param base      бонус первого уровня, в процентах
 * @param step      прибавка за каждый следующий уровень, в процентах
 * @param price     цена улучшения первого уровня в сапфирах
 * @param effect    две строки описания эффекта
 */
public record Booster(
      String id,
      int slot,
      Material material,
      String title,
      String color,
      String accent,
      int base,
      int step,
      int price,
      List<String> effect) {

   /** Бонус на данном уровне. Нулевой уровень - бустера нет. */
   public int bonus(int level) {
      return level <= 0 ? 0 : this.base + this.step * (level - 1);
   }

   /** Цена следующего улучшения растёт вместе с уровнем. */
   public int priceFor(int level) {
      return this.price * Math.max(1, level);
   }
}
