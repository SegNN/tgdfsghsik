package org.impldev.holyclans.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Цвет брони» (54 слота) — выбор подсветки соклановцев.
 */
public final class GlowMenu extends ClanMenu {
   // Слоты для размещения цветов (до 24)
   private static final int[] COLOR_SLOTS = {
         1, 2, 3, 4, 5, 6, 7,
         10, 11, 12, 13, 14, 15, 16,
         19, 20, 21, 22, 23, 24, 25,
         29, 31, 33
   };

   // Дефолтные цвета, если конфиг не задан
   private static final int[] DEFAULT_COLORS = {
         0xFF0000, 0xFF8C00, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0x8B00FF,
         0xFF69B4, 0xFF00FF, 0xFFFFFF, 0xC0C0C0, 0x808080, 0x000000, 0x8B0000,
         0x000080, 0x008000, 0x008080, 0x800080, 0xFF7F50, 0x32CD32, 0xFFD700,
         0xE6E6FA, 0x33CCFF
   };

   private final int[] colors;
   private final Map<Integer, Integer> slotToColorIndex = new HashMap<>();

   public GlowMenu(HolyClans plugin, Clan clan) {
      this.inventory = Bukkit.createInventory(this, 54, Texts.color("&rЦвет брони"));

      // Загружаем цвета
      List<Integer> configColors = plugin.getConfig().getIntegerList("glow-colors");
      if (configColors.isEmpty()) {
         this.colors = DEFAULT_COLORS;
      } else {
         this.colors = configColors.stream().mapToInt(Integer::intValue).toArray();
      }

      // Размещаем цветные нагрудники
      int count = Math.min(this.colors.length, COLOR_SLOTS.length);
      for (int i = 0; i < count; i++) {
         int slot = COLOR_SLOTS[i];
         boolean selected = clan.glowColor() == this.colors[i];
         this.inventory.setItem(slot, Menus.coloredChestplate(this.colors[i], selected));
         this.slotToColorIndex.put(slot, i);
      }

      // Нижняя панель
      for (int s : new int[]{45, 47, 48, 49, 50, 51, 53}) {
         this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      }

      // Слот 46: Текущая подсветка
      String modeLine;
      switch (clan.glowMode()) {
         case "TEMP" -> modeLine = "&r&x&9&c&f&9&f&f● &r&fВключена временно";
         case "ON" -> modeLine = "&r&x&9&c&f&9&f&f● &r&fВключена";
         default -> modeLine = "&r&x&f&f&7&0&0&0✔ Выключена";
      }
      this.inventory.setItem(46, Menus.item(Material.HOPPER,
            "&r &r&x&0&0&d&8&f&fТекущая подсветка брони",
            List.of(
                  "OFF".equals(clan.glowMode()) ? "&r&x&f&f&7&0&0&0✔ Выключена" : "&r&x&9&c&f&9&f&f● &r&fВыключена",
                  "TEMP".equals(clan.glowMode()) ? "&r&x&f&f&7&0&0&0✔ Включена временно" : "&r&x&9&c&f&9&f&f● &r&fВключена временно",
                  "ON".equals(clan.glowMode()) ? "&r&x&f&f&7&0&0&0✔ Включена" : "&r&x&9&c&f&9&f&f● &r&fВключена"
            )));

      // Слот 52: Назад
      this.inventory.setItem(52, Menus.back());
   }

   /**
    * Индекс цвета по слоту, или -1 если слот не цветной.
    */
   public int colorIndexAt(int slot) {
      Integer idx = this.slotToColorIndex.get(slot);
      return idx == null ? -1 : idx;
   }

   /**
    * RGB цвета по индексу.
    */
   public int colorRgb(int index) {
      return this.colors[index];
   }
}
