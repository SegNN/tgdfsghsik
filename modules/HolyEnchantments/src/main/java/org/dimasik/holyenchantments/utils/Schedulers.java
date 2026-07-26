package org.dimasik.holyenchantments.utils;

import org.bukkit.Bukkit;
import org.dimasik.holyenchantments.HolyEnchantments;

public class Schedulers {
   private static HolyEnchantments plugin;

   public Schedulers() {
   }

   public static void load(HolyEnchantments plugin) {
      Schedulers.plugin = plugin;
   }

   public static void sync(Runnable runnable) {
      Bukkit.getScheduler().runTask(plugin, runnable);
   }

   public static void async(Runnable runnable) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
   }
}
