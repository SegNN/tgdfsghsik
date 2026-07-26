package org.dimasik.holyenchantments.enchantscore.enchantslist.sword;

import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.dimasik.holyenchantments.enchantscore.MEnchant;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;

public class Filter extends MEnchant {
   public Filter(
      String key,
      NamespacedKey namespacedKey,
      String displayName,
      String soundActivateAlone,
      String soundActivateAround,
      Particle particleType,
      List<Material> tools,
      HashMap<Integer, AbstractLevel> levels,
      Integer maxAnvilLevel
   ) {
      super(key, namespacedKey, displayName, soundActivateAlone, soundActivateAround, particleType, tools, levels, maxAnvilLevel);
   }
}
