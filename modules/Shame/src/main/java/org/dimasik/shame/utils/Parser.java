package org.dimasik.shame.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class Parser {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
   private static final boolean SUPPORTS_RGB = true;

   public Parser() {
   }

   public static String color(String message) {
      if (message == null) {
         return "";
      } else {
         Matcher matcher = HEX_PATTERN.matcher(message);
         StringBuilder buffer = new StringBuilder();

         while (matcher.find()) {
            String hex = matcher.group(1);

            try {
               String replacement = ChatColor.of("#" + hex).toString();
               matcher.appendReplacement(buffer, replacement);
            } catch (Exception var5) {
               matcher.appendReplacement(buffer, "");
            }
         }

         matcher.appendTail(buffer);
         message = buffer.toString();
         return ChatColor.translateAlternateColorCodes('&', message);
      }
   }
}
