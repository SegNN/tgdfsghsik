package org.dimasik.mantiitems;

import java.util.HashSet;
import org.bukkit.Location;

public class BlocksMemory {
   private static final HashSet<Location> blocks = new HashSet<>();

   public BlocksMemory() {
   }

   public static boolean contains(Location location) {
      return blocks.contains(location);
   }

   public static void add(Location location) {
      blocks.add(location);
   }

   public static void remove(Location location) {
      blocks.remove(location);
   }
}
