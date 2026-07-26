package ua.fichatime.fichaspawneroved.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtils {
   public SoundUtils() {
   }

   public static void playSound(Player player, String soundName) {
      if (soundName != null) {
         try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
         } catch (IllegalArgumentException var3) {
            Bukkit.getLogger().warning("Invalid sound: " + soundName);
         }
      }
   }
}
