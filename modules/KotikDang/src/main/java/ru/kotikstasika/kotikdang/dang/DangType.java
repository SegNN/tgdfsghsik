package ru.kotikstasika.kotikdang.dang;

import org.bukkit.Material;

public enum DangType {
   UNICK(Material.BEDROCK, 3, DangType.Type.UNIK),
   NO_RG(Material.BEDROCK, 1, DangType.Type.NO_RG),
   GRIEF(Material.BEDROCK, 1, DangType.Type.GRIEF),
   LEVEL_1_GRIEF(Material.LODESTONE, 1, DangType.Type.GRIEF),
   LEVEL_2_GRIEF(Material.LODESTONE, 2, DangType.Type.GRIEF),
   LEVEL_3_GRIEF(Material.RESPAWN_ANCHOR, 3, DangType.Type.GRIEF),
   LEVEL_4_GRIEF(Material.RESPAWN_ANCHOR, 4, DangType.Type.GRIEF),
   LEVEL_5_GRIEF(Material.RESPAWN_ANCHOR, 5, DangType.Type.GRIEF);

   private final Material anchorType;
   private final int lootLevel;
   private final DangType.Type dangType;

   private DangType(Material anchorType, int lootLevel, DangType.Type dangType) {
      this.anchorType = anchorType;
      this.lootLevel = lootLevel;
      this.dangType = dangType;
   }

   public Material getAnchorType() {
      return this.anchorType;
   }

   public DangType.Type getDangType() {
      return this.dangType;
   }

   public int getLootLevel() {
      return this.lootLevel;
   }

   public static enum Type {
      GRIEF,
      UNIK,
      NO_RG;

      private Type() {
      }
   }
}
