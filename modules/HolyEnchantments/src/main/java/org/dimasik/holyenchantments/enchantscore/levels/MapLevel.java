package org.dimasik.holyenchantments.enchantscore.levels;

import java.util.HashMap;
import org.bukkit.Material;

public class MapLevel extends AbstractLevel {
   private final HashMap<Material, Material> materials;

   public MapLevel(int chance, int enchantTableChance, HashMap<Material, Material> materials) {
      super(chance, enchantTableChance);
      this.materials = materials;
   }

   public HashMap<Material, Material> getMaterials() {
      return this.materials;
   }
}
