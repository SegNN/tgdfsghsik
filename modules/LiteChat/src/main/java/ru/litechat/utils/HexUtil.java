package ru.litechat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class HexUtil {
   private static final Pattern GRADIENT = Pattern.compile("<(#[A-Fa-f0-9]{6})>(.*?)</(#[A-Fa-f0-9]{6})>");
   private static final Pattern LEGACY_GRADIENT = Pattern.compile("<(&[A-Za-z0-9])>(.*?)</(&[A-Za-z0-9])>");
   private static final Pattern RGB = Pattern.compile("&\\{(#?[A-Fa-f0-9]{6})}");
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   public HexUtil() {
   }

   public static String color(String text) {
      return text == null ? "" : col(text);
   }

   private static String col(String text) {
      Matcher g = GRADIENT.matcher(text);

      while (g.find()) {
         String startHex = g.group(1);
         String between = g.group(2);
         String hex = g.group(3);
         text = text.replace(g.group(0), rgbGradient(between, startHex, hex));
      }

      Matcher l = LEGACY_GRADIENT.matcher(text);

      while (l.find()) {
         char first = l.group(1).charAt(1);
         String hex = l.group(2);
         char second = l.group(3).charAt(1);
         ChatColor firstColor = ChatColor.getByChar(first);
         ChatColor secondColor = ChatColor.getByChar(second);
         if (firstColor == null) {
            firstColor = ChatColor.WHITE;
         }

         if (secondColor == null) {
            secondColor = ChatColor.WHITE;
         }

         text = text.replace(l.group(0), rgbGradient(hex, getHexFromChatColor(firstColor), getHexFromChatColor(secondColor)));
      }

      l = RGB.matcher(text);

      while (l.find()) {
         String hexx = l.group(1);
         if (hexx.startsWith("#")) {
            hexx = hexx.substring(1);
         }

         text = text.replace(l.group(0), translateHex(hexx));
      }

      text = translate(text);
      return translateAlternateColorCodes('&', text);
   }

   public static String removeColors(String text) {
      return text == null ? "" : ChatColor.stripColor(text);
   }

   public static List<Character> charactersWithoutColors(String text) {
      if (text == null) {
         return new ArrayList<>();
      } else {
         text = removeColors(text);
         List<Character> result = new ArrayList<>();

         for (char c : text.toCharArray()) {
            result.add(c);
         }

         return result;
      }
   }

   public static List<String> charactersWithColors(String text) {
      return (List<String>)(text == null ? new ArrayList<>() : charactersWithColors(text, '§'));
   }

   public static List<String> charactersWithColors(String text, char colorSymbol) {
      List<String> result = new ArrayList<>();
      StringBuilder colors = new StringBuilder();
      boolean colorInput = false;
      boolean reading = false;

      for (char c : text.toCharArray()) {
         if (colorInput) {
            colors.append(c);
            colorInput = false;
         } else if (c == colorSymbol) {
            if (!reading) {
               colors = new StringBuilder();
            }

            colorInput = true;
            reading = true;
            colors.append(c);
         } else {
            reading = false;
            result.add("" + colors + c);
         }
      }

      return result;
   }

   private static String rgbGradient(String text, String startHex, String endHex) {
      if (text != null && !text.isEmpty()) {
         text = translateAlternateColorCodes('&', text);
         List<String> characters = charactersWithColors(text);
         if (characters.isEmpty()) {
            return text;
         } else {
            int[] startRGB = hexToRGB(startHex);
            int[] endRGB = hexToRGB(endHex);
            if (characters.size() == 1) {
               String var10000 = translateHex(endHex);
               return var10000 + text;
            } else {
               StringBuilder builder = new StringBuilder();
               double[] red = linear(startRGB[0], endRGB[0], characters.size());
               double[] green = linear(startRGB[1], endRGB[1], characters.size());
               double[] blue = linear(startRGB[2], endRGB[2], characters.size());

               for (int i = 0; i < characters.size(); i++) {
                  String currentText = characters.get(i);
                  String currentHex = String.format("#%02X%02X%02X", (int)Math.round(red[i]), (int)Math.round(green[i]), (int)Math.round(blue[i]));
                  builder.append(translateHex(currentHex)).append(currentText.replace("§r", ""));
               }

               return builder.toString();
            }
         }
      } else {
         return "";
      }
   }

   private static double[] linear(double from, double to, int max) {
      double[] res = new double[max];
      if (max <= 1) {
         res[0] = from;
         return res;
      } else {
         for (int i = 0; i < max; i++) {
            res[i] = from + i * ((to - from) / (max - 1.0));
         }

         return res;
      }
   }

   private static int[] hexToRGB(String hex) {
      if (hex != null && !hex.isEmpty()) {
         if (hex.startsWith("#")) {
            hex = hex.substring(1);
         }

         if (hex.length() != 6) {
            return new int[]{255, 255, 255};
         } else {
            try {
               int r = Integer.parseInt(hex.substring(0, 2), 16);
               int g = Integer.parseInt(hex.substring(2, 4), 16);
               int b = Integer.parseInt(hex.substring(4, 6), 16);
               return new int[]{r, g, b};
            } catch (NumberFormatException var41) {
               ConsoleMSG.color("&c[LiteChat] Ошибка при разборе hex-цвета: " + hex);
               return new int[]{255, 255, 255};
            }
         }
      } else {
         return new int[]{255, 255, 255};
      }
   }

   private static String getHexFromChatColor(ChatColor color) {
      if (color == ChatColor.BLACK) {
         return "#000000";
      } else if (color == ChatColor.DARK_BLUE) {
         return "#0000AA";
      } else if (color == ChatColor.DARK_GREEN) {
         return "#00AA00";
      } else if (color == ChatColor.DARK_AQUA) {
         return "#00AAAA";
      } else if (color == ChatColor.DARK_RED) {
         return "#AA0000";
      } else if (color == ChatColor.DARK_PURPLE) {
         return "#AA00AA";
      } else if (color == ChatColor.GOLD) {
         return "#FFAA00";
      } else if (color == ChatColor.GRAY) {
         return "#AAAAAA";
      } else if (color == ChatColor.DARK_GRAY) {
         return "#555555";
      } else if (color == ChatColor.BLUE) {
         return "#5555FF";
      } else if (color == ChatColor.GREEN) {
         return "#55FF55";
      } else if (color == ChatColor.AQUA) {
         return "#55FFFF";
      } else if (color == ChatColor.RED) {
         return "#FF5555";
      } else if (color == ChatColor.LIGHT_PURPLE) {
         return "#FF55FF";
      } else if (color == ChatColor.YELLOW) {
         return "#FFFF55";
      } else {
         return color == ChatColor.WHITE ? "#FFFFFF" : "#FFFFFF";
      }
   }

   public static String translate(String message) {
      if (message != null && !message.isEmpty()) {
         Matcher matcher = HEX_PATTERN.matcher(message);
         StringBuilder builder = new StringBuilder(message.length() + 32);

         while (matcher.find()) {
            String group = matcher.group(1);
            if (group.length() == 6) {
               char var10002 = group.charAt(0);
               matcher.appendReplacement(
                  builder,
                  "§x§" + var10002 + "§" + group.charAt(1) + "§" + group.charAt(2) + "§" + group.charAt(3) + "§" + group.charAt(4) + "§" + group.charAt(5)
               );
            } else {
               ConsoleMSG.color("&c[LiteChat] Неверный формат hex-цвета: &#" + group);
            }
         }

         message = matcher.appendTail(builder).toString();
         return translateAlternateColorCodes('&', message);
      } else {
         return "";
      }
   }

   private static String translateHex(String hex) {
      if (hex != null && hex.length() == 6) {
         try {
            Integer.parseInt(hex, 16);
            char var10000 = hex.charAt(0);
            return "§x§" + var10000 + "§" + hex.charAt(1) + "§" + hex.charAt(2) + "§" + hex.charAt(3) + "§" + hex.charAt(4) + "§" + hex.charAt(5);
         } catch (NumberFormatException var21) {
            ConsoleMSG.color("&c[LiteChat] Ошибка при разборе hex-цвета: " + hex);
            return "";
         }
      } else {
         ConsoleMSG.color("&c[LiteChat] Неверный hex-цвет: " + hex);
         return "";
      }
   }

   public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
      if (textToTranslate == null) {
         return "";
      } else {
         char[] b = textToTranslate.toCharArray();
         int length = b.length - 1;

         for (int i = 0; i < length; i++) {
            if (b[i] == altColorChar && isValidColorCharacter(b[i + 1])) {
               b[i] = 167;
               b[++i] = Character.toLowerCase(b[i]);
            }
         }

         return new String(b);
      }
   }

   private static boolean isValidColorCharacter(char c) {
      return c >= '0' && c <= '9'
         || c >= 'a' && c <= 'f'
         || c == 'r'
         || c >= 'k' && c <= 'o'
         || c == 'x'
         || c >= 'A' && c <= 'F'
         || c == 'R'
         || c >= 'K' && c <= 'O'
         || c == 'X';
   }
}
