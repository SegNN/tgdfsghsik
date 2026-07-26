package org.dimasik.holyenchantments.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class Parser {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");

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

   public static void sendClickableMessage(Player player, List<String> lines, String url, String hover) {
      for (String line : lines) {
         hover = color(hover);
         BaseComponent[] components = parseColoredText(line);

         for (BaseComponent comp : components) {
            comp.setClickEvent(new ClickEvent(Action.OPEN_URL, url));
            comp.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
         }

         player.spigot().sendMessage(components);
      }
   }

   public static String stripColor(String text) {
      if (text == null) {
         return "";
      } else {
         String result = HEX_PATTERN.matcher(text).replaceAll("");
         return ChatColor.stripColor(result);
      }
   }

   private static BaseComponent[] parseColoredText(String message) {
      List<BaseComponent> components = new ArrayList<>();
      if (message == null) {
         return new BaseComponent[0];
      } else {
         message = ChatColor.translateAlternateColorCodes('&', message);
         StringBuilder builder = new StringBuilder();
         ChatColor currentColor = null;
         boolean bold = false;
         boolean italic = false;
         boolean underlined = false;
         boolean strikethrough = false;
         boolean obfuscated = false;
         int lastIndex = 0;

         for (Matcher matcher = HEX_PATTERN.matcher(message); matcher.find(); lastIndex = matcher.end()) {
            if (matcher.start() > lastIndex) {
               String preceding = message.substring(lastIndex, matcher.start());
               builder.append(preceding);
            }

            String hex = matcher.group(1);

            try {
               currentColor = ChatColor.of("#" + hex);
            } catch (Exception var13) {
               currentColor = null;
            }
         }

         builder.append(message.substring(lastIndex));
         if (!builder.isEmpty()) {
            TextComponent comp = new TextComponent(builder.toString());
            applyFormatting(comp, bold, italic, underlined, strikethrough, obfuscated, currentColor);
            components.add(comp);
         }

         return components.toArray(new BaseComponent[0]);
      }
   }

   public static String formatTime(Timestamp timestamp) {
      if (timestamp == null) {
         return "Не указано";
      } else {
         SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm:ss", new Locale("ru", "RU"));
         String formattedDate = sdf.format(timestamp);
         formattedDate = formattedDate.replaceFirst("([А-Я])", "$1".toLowerCase());
         return formattedDate.replaceAll("янв\\.?", "янв.")
            .replaceAll("фев\\.?", "фев.")
            .replaceAll("мар\\.?", "мар.")
            .replaceAll("апр\\.?", "апр.")
            .replaceAll("май\\.?", "май.")
            .replaceAll("июн\\.?", "июн.")
            .replaceAll("июл\\.?", "июл.")
            .replaceAll("авг\\.?", "авг.")
            .replaceAll("сен\\.?", "сен.")
            .replaceAll("окт\\.?", "окт.")
            .replaceAll("ноя\\.?", "ноя.")
            .replaceAll("дек\\.?", "дек.");
      }
   }

   private static void applyFormatting(
      TextComponent component, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated, ChatColor color
   ) {
      if (bold) {
         component.setBold(true);
      }

      if (italic) {
         component.setItalic(true);
      }

      if (underlined) {
         component.setUnderlined(true);
      }

      if (strikethrough) {
         component.setStrikethrough(true);
      }

      if (obfuscated) {
         component.setObfuscated(true);
      }

      if (color != null) {
         component.setColor(color);
      }
   }

   public static String formatNumber(int number) {
      return String.format(Locale.US, "%,d", number);
   }
}
