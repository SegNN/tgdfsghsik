package ru.holydev.holycustom.api;

import java.awt.Color;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.holydev.holycustom.HolyCustom;

public class CustomPlaceholder extends PlaceholderExpansion {
   public CustomPlaceholder() {
   }

   @NotNull
   public String getIdentifier() {
      return "holycustom";
   }

   @NotNull
   public String getAuthor() {
      return "HolyDev";
   }

   @NotNull
   public String getVersion() {
      return "1.0";
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {
      if (params == null || player == null) {
         return "";
      } else if (!params.equalsIgnoreCase("name")) {
         return player.getName();
      } else {
         return player != null && HolyCustom.playerData.get(player).containsKey("color_name")
            ? this.colorizeNickname(player.getName(), HolyCustom.playerData.get(player).get("color_name"), "#FFFFFF", true)
            : player.getName();
      }
   }

   private String colorizeNickname(String playerName, String color1, String color2, Boolean bold) {
      int length = playerName.length();
      ChatColor startColor = ChatColor.of(color1);
      ChatColor endColor = ChatColor.of(color2);
      TextComponent coloredNickname = new TextComponent();

      for (int i = 0; i < length; i++) {
         double ratio = (double)i / (length - 1);
         int red = (int)(startColor.getColor().getRed() * (1.0 - ratio) + endColor.getColor().getRed() * ratio);
         int green = (int)(startColor.getColor().getGreen() * (1.0 - ratio) + endColor.getColor().getGreen() * ratio);
         int blue = (int)(startColor.getColor().getBlue() * (1.0 - ratio) + endColor.getColor().getBlue() * ratio);
         ChatColor color = ChatColor.of(new Color(red, green, blue));
         TextComponent letter = new TextComponent(String.valueOf(playerName.charAt(i)));
         letter.setColor(color);
         if (bold) {
            letter.setBold(true);
         }

         coloredNickname.addExtra(letter);
      }

      return TextComponent.toLegacyText(new BaseComponent[]{coloredNickname});
   }
}
