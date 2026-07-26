package org.dimasik.eventzamok;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootAPI;

public class ZamokShulker {
   private Location location;
   private ZamokShulker.ShulkerType type;
   private int hits;
   private int requiredHits;

   public ZamokShulker(Location location, ZamokShulker.ShulkerType type) {
      this.location = location;
      this.type = type;
      this.hits = 0;
      this.requiredHits = type.getRequiredHits();
   }

   public void spawn() {
      Block block = this.location.getBlock();
      block.setType(this.type.getMaterial());
      this.location.getWorld().createExplosion(this.location, 4.0F, false, false);
      if (block.getState() instanceof ShulkerBox) {
         ShulkerBox shulker = (ShulkerBox)block.getState();
         Directional directional = (Directional)shulker.getBlockData();
         directional.setFacing(BlockFace.UP);
         shulker.setBlockData(directional);
         shulker.update();
      }
   }

   public void hit() {
      this.hits++;
   }

   public boolean isBroken() {
      return this.hits >= this.requiredHits;
   }

   public void dropItems() {
      int dropCount = this.type.getRandomDropCount();
      int level = 1;
      boolean explode = false;
      switch (this.type) {
         case GOLD_RUSH_YELLOW:
            level = 2;
            explode = true;
            break;
         case GOLD_RUSH_RED:
            level = 3;
      }

      if (explode && new Random().nextBoolean()) {
         this.location.getWorld().createExplosion(this.location, 4.0F, false, false);
      } else {
         for (ItemStack itemStack : new LootAPI().getRandomItems(dropCount, level)) {
            this.location.getWorld().dropItemNaturally(this.location, itemStack);
         }
      }
   }

   public void remove() {
      this.location.getBlock().setType(Material.AIR);
   }

   public Location getLocation() {
      return this.location;
   }

   public ZamokShulker.ShulkerType getType() {
      return this.type;
   }

   public int getHits() {
      return this.hits;
   }

   public void setHits(int hits) {
      this.hits = hits;
   }

   public int getRequiredHits() {
      return this.requiredHits;
   }

   public static enum ShulkerType {
      DEFAULT(Material.GRAY_SHULKER_BOX, 5, 3, 6),
      EPIC(Material.PURPLE_SHULKER_BOX, 10, 5, 8),
      LEGENDARY(Material.LIGHT_BLUE_SHULKER_BOX, 15, 7, 10),
      GOLD_RUSH_YELLOW(Material.YELLOW_SHULKER_BOX, 20, 5, 10),
      GOLD_RUSH_RED(Material.RED_SHULKER_BOX, 50, 7, 12);

      private final Material material;
      private final int requiredHits;
      private final int minDrops;
      private final int maxDrops;

      private ShulkerType(Material material, int requiredHits, int minDrops, int maxDrops) {
         this.material = material;
         this.requiredHits = requiredHits;
         this.minDrops = minDrops;
         this.maxDrops = maxDrops;
      }

      public Material getMaterial() {
         return this.material;
      }

      public int getRequiredHits() {
         return this.requiredHits;
      }

      public int getRandomDropCount() {
         return new Random().nextInt(this.maxDrops - this.minDrops + 1) + this.minDrops;
      }
   }
}
