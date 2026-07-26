package org.dimasik.liteauction.backend.utils.nms;

import lombok.Generated;
import org.bukkit.Bukkit;

public final class VersionUtil {
   public static Class<?> getCraftItemStackClass() throws ClassNotFoundException {
      try {
         String[] parts = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
         if (parts.length < 4) {
            // unmapped Paper (1.20.5+): the relocated package is the only one that exists
            return Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
         }

         return Class.forName("org.bukkit.craftbukkit." + parts[3] + ".inventory.CraftItemStack");
      } catch (ClassNotFoundException var2) {
         return Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
      }
   }

   @Generated
   private VersionUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
