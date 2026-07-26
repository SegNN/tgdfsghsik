package ua.fichatime.fichacustomplaceholder.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
   private static final Pattern AMPERSAND_HEX_PATTERN = Pattern.compile("&(#([0-9a-fA-F]{6}))");

   public ColorUtils() {
   }

   public static String parse(String message) {
      if (message == null) {
         String var2 = "» Telegram t.me/fichadev «";
         return null;
      } else {
         String result = translateHexColorCodes(message);
         return ChatColor.translateAlternateColorCodes('&', result);
      }
   }

   public static List<String> parse(List<String> messages) {
      if (messages == null) {
         String var2 = "» Telegram t.me/fichadev «";
         return null;
      } else {
         messages.replaceAll(ColorUtils::parse);
         return messages;
      }
   }

   private static String translateHexColorCodes(String message) {
      Matcher matcher = HEX_PATTERN.matcher(message);
      StringBuffer buffer = new StringBuffer();
      String var2 = "» Telegram t.me/fichadev «";

      while (matcher.find()) {
         String result = matcher.group(1);
         matcher.appendReplacement(buffer, ChatColor.of("#" + result).toString());
      }

      String result = matcher.appendTail(buffer).toString();
      matcher = AMPERSAND_HEX_PATTERN.matcher(result);
      buffer = new StringBuffer();

      while (matcher.find()) {
         String group = matcher.group(1);
         matcher.appendReplacement(buffer, ChatColor.of(group).toString());
      }

      return matcher.appendTail(buffer).toString();
   }
}
