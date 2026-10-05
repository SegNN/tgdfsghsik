package org.impldev.holyclans;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Цвета и утилиты для текста HolyClans.
 * Цвета из оригинальных дампов меню.
 */
public final class Texts {
   // Основные цвета из дампов
   public static final TextColor BLUE = TextColor.fromHexString("#00d8ff");
   public static final TextColor ORANGE_GOLD = TextColor.fromHexString("#ff7000");
   public static final TextColor GOLD = TextColor.fromHexString("#ffc900");
   public static final TextColor RED = TextColor.fromHexString("#ff2222");
   public static final TextColor GREEN = TextColor.fromHexString("#05fb00");
   public static final TextColor GRAY = TextColor.fromHexString("#d9d9d9");
   public static final TextColor LIGHT_GRAY = TextColor.fromHexString("#e7e7e7");
   public static final TextColor WHITE = TextColor.fromHexString("#ffffff");
   public static final TextColor CLAN_NAME = TextColor.fromHexString("#fdc653");

   private static final LegacyComponentSerializer LEGACY =
         LegacyComponentSerializer.builder()
               .hexColors()
               .useUnusualXRepeatedCharacterHexFormat()
               .character('&')
               .build();

   private Texts() {}

   /** Парсит legacy строку с &-кодами в Component. */
   public static Component color(String legacy) {
      return LEGACY.deserialize(legacy).decoration(TextDecoration.ITALIC, false);
   }

   /** Форматирует число с пробелами (1 000 000). */
   public static String num(long value) {
      String raw = Long.toString(value);
      StringBuilder sb = new StringBuilder();
      int len = raw.length();
      for (int i = 0; i < len; i++) {
         if (i > 0 && (len - i) % 3 == 0) {
            sb.append(' ');
         }
         sb.append(raw.charAt(i));
      }
      return sb.toString();
   }

   /** Форматирует число с пробелами, поддержка отрицательных. */
   public static String num(double value) {
      return num((long) value);
   }
}
