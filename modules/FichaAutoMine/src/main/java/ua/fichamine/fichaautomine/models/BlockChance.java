package ua.fichamine.fichaautomine.models;

import org.bukkit.Material;

public class BlockChance {
   private final Material material;
   private final double chance;

   public BlockChance(String materialName, double chance) {
      this.material = Material.getMaterial(materialName.toUpperCase());
      this.chance = chance;
   }

   public Material getMaterial() {
      return this.material;
   }

   public double getChance() {
      return this.chance;
   }
}
