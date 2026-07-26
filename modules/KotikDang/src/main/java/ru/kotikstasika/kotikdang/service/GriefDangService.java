package ru.kotikstasika.kotikdang.service;

import org.bukkit.Location;
import ru.kotikstasika.kotikdang.dang.types.DangGrief;

public class GriefDangService {
   private final DangGrief dangGrief;

   public GriefDangService(DangGrief dangGrief) {
      this.dangGrief = dangGrief;
   }

   public void deleteAll() {
      this.dangGrief.deleteAllDangsAndRegions();
   }

   public boolean deleteNearest(Location from) {
      String id = this.dangGrief.findNearestDangId(from);
      if (id == null) {
         return false;
      } else {
         this.dangGrief.deleteDangAndRegion(id);
         return true;
      }
   }

   public DangGrief getDangGrief() {
      return this.dangGrief;
   }
}
