package org.impldev.holybuyer;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;

/** Цвета и форматирование чисел ровно в том виде, в каком они были в меню скупщика. */
public final class Texts {
   public static final String ORANGE = "&x&f&f&7&0&0&0";
   public static final String CYAN = "&x&0&0&d&8&f&f";
   public static final String POINTS = "&x&0&5&f&b&0&0";
   public static final String COINS = "&x&f&f&c&9&0&0";
   public static final String RED = "&x&f&f&2&2&2&2";
   public static final String GREEN = "&x&4&0&e&3&2&0";
   public static final String INVEST = "&x&f&b&0&f&d&4";
   public static final String GRAY = "&x&e&7&e&7&e&7";
   public static final String BLUE = "&x&9&c&f&9&f&f";

   private Texts() {
   }

   public static String color(String text) {
      return ChatColor.translateAlternateColorCodes('&', text);
   }

   public static List<String> color(List<String> lines) {
      List<String> out = new ArrayList<>(lines.size());
      for (String line : lines) {
         out.add(color(line));
      }

      return out;
   }

   /**
    * Разряды отделяются пробелом: {@code 17 626 745}.
    *
    * <p>Вручную, а не через {@code String.format} с локалью - французская локаль ставит
    * неразрывный пробел, и в игре он рисуется другой шириной, чем обычный.
    */
   public static String num(long value) {
      String digits = Long.toString(Math.abs(value));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < digits.length(); i++) {
         if (i > 0 && (digits.length() - i) % 3 == 0) {
            sb.append(' ');
         }

         sb.append(digits.charAt(i));
      }

      return (value < 0 ? "-" : "") + sb;
   }

   public static String num(double value) {
      return num(Math.round(value));
   }

   /** Одна цифра после запятой - так подписаны очки за сдачу. */
   public static String dec1(double value) {
      long whole = (long)value;
      long frac = Math.round(Math.abs(value - whole) * 10.0);
      if (frac == 10L) {
         whole += value < 0 ? -1 : 1;
         frac = 0L;
      }

      return num(whole) + "." + frac;
   }

   /** Проценты со знаком: {@code +13} или {@code -48}. */
   public static String signed(double percent) {
      long rounded = Math.round(percent);
      return (rounded >= 0L ? "+" : "-") + Math.abs(rounded);
   }

   public static String signed1(double percent) {
      return (percent >= 0.0 ? "+" : "-") + dec1(Math.abs(percent));
   }
}
