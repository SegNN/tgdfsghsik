package ua.holymine.holyrtp.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBarUtils {
   public ActionBarUtils() {
   }

   public static void sendActionBar(Player player, String message) {
      if (player != null && player.isOnline()) {
         String coloredMessage = ColorUtils.colorize(message);
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(coloredMessage));
      }
   }

   public static void clearActionBar(Player player) {
      sendActionBar(player, "");
   }
}
