package org.dimasik.holyenchantments.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class MColor {
   public MColor() {
   }

   @NotNull
   public static String prefix(String from) {
      Pattern pattern = Pattern.compile("#[a-fA-F\u0000-9]{6}");

      for (Matcher matcher = pattern.matcher(from); matcher.find(); matcher = pattern.matcher(from)) {
         String hexCode = from.substring(matcher.start(), matcher.end());
         String replaceSharp = hexCode.replace('#', 'x');
         char[] ch = replaceSharp.toCharArray();
         StringBuilder builder = new StringBuilder();

         for (char c : ch) {
            builder.append("&").append(c);
         }

         from = from.replace(hexCode, builder.toString());
      }

      String var10000 = ChatColor.translateAlternateColorCodes('&', from);

      return var10000;
   }

}
