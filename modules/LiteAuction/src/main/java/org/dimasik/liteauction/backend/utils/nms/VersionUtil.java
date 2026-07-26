package org.dimasik.liteauction.backend.utils.nms;

import lombok.Generated;
import org.bukkit.Bukkit;

public final class VersionUtil {
   public static Class<?> getCraftItemStackClass() throws ClassNotFoundException {
      try {
         String packageName = Bukkit.getServer().getClass().getPackage().getName();
         String version = packageName.split("\\.")[3];
         return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
      } catch (ClassNotFoundException var2) {
         return Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
      }
   }

   @Generated
   private VersionUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
