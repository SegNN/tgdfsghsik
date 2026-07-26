package ua.holydev.holybambuk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public final class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&#([0-9A-F]{6})");
   private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)&x(&[0-9A-F]){6}");

   private ColorUtils() {
   }

   public static String colorize(String input) {
      if (input != null && !input.isEmpty()) {
         Matcher m = HEX_PATTERN.matcher(input);
         StringBuffer sb = new StringBuffer();

         while (m.find()) {
            String hex = m.group(1);
            StringBuilder rep = new StringBuilder("§x");

            for (char c : hex.toCharArray()) {
               rep.append('§').append(c);
            }

            m.appendReplacement(sb, Matcher.quoteReplacement(rep.toString()));
         }

         m.appendTail(sb);
         String s = sb.toString();
         s = LEGACY_HEX_PATTERN.matcher(s).replaceAll(r -> r.group().replace('&', '§'));
         return ChatColor.translateAlternateColorCodes('&', s);
      } else {
         return input;
      }
   }
}
