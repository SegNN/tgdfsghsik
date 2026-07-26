package ru.kotikstasika.holyliteend.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import org.bukkit.ChatColor;

public final class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private static final Pattern GRADIENT_PATTERN = Pattern.compile("&x&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])");

   public static String translateColors(String text) {
      if (text == null) {
         return "";
      } else {
         Matcher hexMatcher = HEX_PATTERN.matcher(text);
         StringBuffer hexBuffer = new StringBuffer();

         while (hexMatcher.find()) {
            String hexCode = hexMatcher.group(1);
            String replacement = "&x&"
               + hexCode.charAt(0)
               + "&"
               + hexCode.charAt(1)
               + "&"
               + hexCode.charAt(2)
               + "&"
               + hexCode.charAt(3)
               + "&"
               + hexCode.charAt(4)
               + "&"
               + hexCode.charAt(5);
            hexMatcher.appendReplacement(hexBuffer, replacement);
         }

         hexMatcher.appendTail(hexBuffer);
         Matcher gradientMatcher = GRADIENT_PATTERN.matcher(hexBuffer.toString());
         StringBuffer gradientBuffer = new StringBuffer();

         while (gradientMatcher.find()) {
            String r1 = gradientMatcher.group(1);
            String g1 = gradientMatcher.group(2);
            String b1 = gradientMatcher.group(3);
            String r2 = gradientMatcher.group(4);
            String g2 = gradientMatcher.group(5);
            String b2 = gradientMatcher.group(6);
            String replacement = "&x&" + r1 + "&" + g1 + "&" + b1 + "&" + r2 + "&" + g2 + "&" + b2;
            gradientMatcher.appendReplacement(gradientBuffer, replacement);
         }

         gradientMatcher.appendTail(gradientBuffer);
         return ChatColor.translateAlternateColorCodes('&', gradientBuffer.toString());
      }
   }

   @Generated
   private ColorUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
