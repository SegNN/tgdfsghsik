package org.impldev.holycore.invest;

import java.util.List;
import org.bukkit.Material;

/**
 * Описание одного направления инвестиций.
 *
 * @param id        ключ в хранилище и в плейсхолдерах
 * @param slot      место в меню
 * @param material  предмет-иконка
 * @param title     название без счётчика уровня - счётчик дорисовывается на месте
 * @param maxLevel  потолок прокачки
 * @param need      сколько очков нужно вложить для следующего уровня
 * @param price     цена одного очка в валюте сервера
 * @param perLevel  сколько процентов даёт один уровень
 * @param baseValue исходное значение для направлений, где показывается "по умолчанию"
 * @param kind      как считать и показывать эффект
 * @param lore      строки описания эффекта
 */
public record Category(
      String id,
      int slot,
      Material material,
      String title,
      int maxLevel,
      int need,
      long price,
      double perLevel,
      double baseValue,
      Kind kind,
      List<String> lore) {

   public enum Kind {
      /** эффект растёт: "Сейчас: +375%" */
      BONUS,
      /** эффект сокращает время: "По умолчанию: 10м." / "Сейчас: 6м., 42с. (-33%)" */
      TIME,
      /** радиусы границ миров показываются отдельным блоком */
      BORDER,
      /** эффект живёт в другом плагине, здесь только счётчик уровня */
      EXTERNAL
   }
}
