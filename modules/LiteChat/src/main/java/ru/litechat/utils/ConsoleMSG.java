package ru.litechat.utils;

import org.bukkit.Bukkit;

public class ConsoleMSG {
   public ConsoleMSG() {
   }

   public static void color(String message) {
      if (message != null) {
         Bukkit.getServer().getConsoleSender().sendMessage(HexUtil.color(message));
      }
   }
}
