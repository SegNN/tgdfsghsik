package org.dimasik.holyenchantments.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EncoderPotionEffect {
   public EncoderPotionEffect() {
   }

   public static void giveEffect(Player player, List<String> effects) {
      List<PotionEffect> potionEffects = getEffects(effects);
      if (!potionEffects.isEmpty()) {
         player.addPotionEffects(potionEffects);
      }
   }

   public static List<PotionEffect> getEffects(List<String> effects) {
      List<PotionEffect> potionEffects = new ArrayList<>();

      for (String str : effects) {
         String[] args = str.split(";");

         PotionEffectType potionEffectType;
         int level;
         int duration;
         boolean ambient;
         boolean particles;
         boolean icon;
         try {
            if (args.length == 3) {
               potionEffectType = PotionEffectType.getByName(args[0].toUpperCase());
               level = Integer.parseInt(args[1]) - 1;
               duration = Integer.parseInt(args[2]) * 20;
               ambient = true;
               particles = true;
               icon = true;
            } else if (args.length == 4) {
               potionEffectType = PotionEffectType.getByName(args[0].toUpperCase());
               level = Integer.parseInt(args[1]) - 1;
               duration = Integer.parseInt(args[2]) * 20;
               ambient = Boolean.parseBoolean(args[3]);
               particles = true;
               icon = true;
            } else if (args.length == 5) {
               potionEffectType = PotionEffectType.getByName(args[0].toUpperCase());
               level = Integer.parseInt(args[1]) - 1;
               duration = Integer.parseInt(args[2]) * 20;
               ambient = Boolean.parseBoolean(args[3]);
               particles = Boolean.parseBoolean(args[4]);
               icon = true;
            } else {
               if (args.length != 6) {
                  continue;
               }

               potionEffectType = PotionEffectType.getByName(args[0].toUpperCase());
               level = Integer.parseInt(args[1]) - 1;
               duration = Integer.parseInt(args[2]) * 20;
               ambient = Boolean.parseBoolean(args[3]);
               particles = Boolean.parseBoolean(args[4]);
               icon = Boolean.parseBoolean(args[5]);
            }
         } catch (Exception var12) {
            continue;
         }

         if (potionEffectType != null) {
            potionEffects.add(new PotionEffect(potionEffectType, duration, level, ambient, particles, icon));
         }
      }

      return potionEffects;
   }
}
