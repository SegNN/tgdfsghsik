package ua.fichamine.fichaautomine.models;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

public class Rarity {
   private final String key;
   private final String displayName;
   private final int chance;
   private final List<BlockChance> blocks;

   public Rarity(String key, String displayName, int chance, List<BlockChance> blocks) {
      this.key = key;
      this.displayName = displayName;
      this.chance = chance;
      this.blocks = blocks;
   }

   public Material getRandomBlock() {
      if (this.blocks != null && !this.blocks.isEmpty()) {
         double random = ThreadLocalRandom.current().nextDouble();
         double cumulativeProbability = 0.0;

         for (BlockChance blockChance : this.blocks) {
            cumulativeProbability += blockChance.getChance();
            if (random <= cumulativeProbability) {
               return blockChance.getMaterial();
            }
         }

         return this.blocks.get(0).getMaterial();
      } else {
         return null;
      }
   }

   public String getKey() {
      return this.key;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public int getChance() {
      return this.chance;
   }

   public List<BlockChance> getBlocks() {
      return this.blocks;
   }
}
