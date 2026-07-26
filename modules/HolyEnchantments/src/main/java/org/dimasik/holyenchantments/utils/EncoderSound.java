package org.dimasik.holyenchantments.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EncoderSound {
   public EncoderSound() {
   }

   public static void playSound(Location location, String soundString) {
      playSound(location, (Player)null, soundString);
   }

   public static void playSound(Player player, String soundString) {
      playSound((Location)null, player, soundString);
   }

   public static void playSound(Location location, Player player, String soundString) {
      if (!soundString.isEmpty()) {
         String[] args = soundString.split(";");
         if (args.length != 0) {
            Sound sound;
            float volume;
            float pitch;
            if (args.length == 1) {
               try {
                  sound = Sound.valueOf(args[0]);
               } catch (IllegalArgumentException var11) {
                  var11.printStackTrace();
                  return;
               }

               volume = 1.0F;
               pitch = 1.0F;
            } else {
               if (args.length != 3) {
                  new RuntimeException("Неверное количество аргументов").printStackTrace();
                  return;
               }

               try {
                  sound = Sound.valueOf(args[0]);
               } catch (IllegalArgumentException var10) {
                  new RuntimeException("Неверный формат звука").printStackTrace();
                  return;
               }

               try {
                  volume = (float)Double.parseDouble(args[1]);
                  pitch = (float)Double.parseDouble(args[2]);
               } catch (NumberFormatException var9) {
                  new RuntimeException("Неверный формат чисел").printStackTrace();
                  return;
               }
            }

            if (player != null) {
               player.playSound(player.getLocation(), sound, volume, pitch);
            } else {
               for (Player online : Bukkit.getOnlinePlayers()) {
                  if (online.getWorld() == location.getWorld() && online.getLocation().distance(location) <= 10.0) {
                     online.playSound(online.getLocation(), sound, volume, pitch);
                  }
               }
            }
         }
      }
   }
}
