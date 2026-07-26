package ru.kotikstasika.kotikdang.model;

import java.util.Map;
import org.bukkit.Location;

public class GriefDangData {
   private final String id;
   private final Location location;
   private final Location anchor;
   private boolean unlocked;
   private boolean anchorDestroyed;
   private final int level;
   private final int lootLevel;
   private final String name;
   private final Map<String, Boolean> chests;
   private final String backupPath;
   private final int regionRadius;

   public GriefDangData(
      String id,
      Location location,
      Location anchor,
      boolean unlocked,
      boolean anchorDestroyed,
      int level,
      int lootLevel,
      String name,
      Map<String, Boolean> chests,
      String backupPath,
      int regionRadius
   ) {
      this.id = id;
      this.location = location;
      this.anchor = anchor;
      this.unlocked = unlocked;
      this.anchorDestroyed = anchorDestroyed;
      this.level = level;
      this.lootLevel = lootLevel;
      this.name = name;
      this.chests = chests;
      this.backupPath = backupPath;
      this.regionRadius = regionRadius;
   }

   public String getId() {
      return this.id;
   }

   public Location getLocation() {
      return this.location;
   }

   public Location getAnchor() {
      return this.anchor;
   }

   public boolean isUnlocked() {
      return this.unlocked;
   }

   public void setUnlocked(boolean unlocked) {
      this.unlocked = unlocked;
   }

   public boolean isAnchorDestroyed() {
      return this.anchorDestroyed;
   }

   public void setAnchorDestroyed(boolean anchorDestroyed) {
      this.anchorDestroyed = anchorDestroyed;
   }

   public int getLevel() {
      return this.level;
   }

   public int getLootLevel() {
      return this.lootLevel;
   }

   public String getName() {
      return this.name;
   }

   public Map<String, Boolean> getChests() {
      return this.chests;
   }

   public String getBackupPath() {
      return this.backupPath;
   }

   public int getRegionRadius() {
      return this.regionRadius;
   }
}
