package org.dimasik.mantiitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class SchematicBlock {
   private final Location location;
   private final Material material;
   private final BlockData blockData;

   public SchematicBlock(Location location, Material material, BlockData blockData) {
      this.location = location;
      this.material = material;
      this.blockData = blockData;
   }

   public Location getLocation() {
      return this.location;
   }

   public Material getMaterial() {
      return this.material;
   }

   public BlockData getBlockData() {
      return this.blockData;
   }
}
