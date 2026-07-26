package org.dimasik.mantiitems.particles;

import org.bukkit.Bukkit;

public enum PackageType {
   MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()),
   CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()),
   CRAFTBUKKIT_BLOCK(CRAFTBUKKIT, "block"),
   CRAFTBUKKIT_CHUNKIO(CRAFTBUKKIT, "chunkio"),
   CRAFTBUKKIT_COMMAND(CRAFTBUKKIT, "command"),
   CRAFTBUKKIT_CONVERSATIONS(CRAFTBUKKIT, "conversations"),
   CRAFTBUKKIT_ENCHANTMENS(CRAFTBUKKIT, "enchantments"),
   CRAFTBUKKIT_ENTITY(CRAFTBUKKIT, "entity"),
   CRAFTBUKKIT_EVENT(CRAFTBUKKIT, "event"),
   CRAFTBUKKIT_GENERATOR(CRAFTBUKKIT, "generator"),
   CRAFTBUKKIT_HELP(CRAFTBUKKIT, "help"),
   CRAFTBUKKIT_INVENTORY(CRAFTBUKKIT, "inventory"),
   CRAFTBUKKIT_MAP(CRAFTBUKKIT, "map"),
   CRAFTBUKKIT_METADATA(CRAFTBUKKIT, "metadata"),
   CRAFTBUKKIT_POTION(CRAFTBUKKIT, "potion"),
   CRAFTBUKKIT_PROJECTILES(CRAFTBUKKIT, "projectiles"),
   CRAFTBUKKIT_SCHEDULER(CRAFTBUKKIT, "scheduler"),
   CRAFTBUKKIT_SCOREBOARD(CRAFTBUKKIT, "scoreboard"),
   CRAFTBUKKIT_UPDATER(CRAFTBUKKIT, "updater"),
   CRAFTBUKKIT_UTIL(CRAFTBUKKIT, "util");

   private final String path;

   private PackageType(String path) {
      this.path = path;
   }

   private PackageType(PackageType parent, String path) {
      this(parent + "." + path);
   }

   public String getPath() {
      return this.path;
   }

   public Class<?> getClass(String className) throws ClassNotFoundException {
      return Class.forName(this + "." + className);
   }

   @Override
   public String toString() {
      return this.path;
   }

   public static String getServerVersion() {
      // Paper is unmapped since 1.20.5, so org.bukkit.craftbukkit has no vX_Y_RZ suffix and the
      // old substring(23) threw StringIndexOutOfBounds. Rebuild the legacy shape from the API
      // version so callers that parse it (Particles reads split("_")[1]) keep working.
      String pkg = Bukkit.getServer().getClass().getPackage().getName();
      int idx = pkg.lastIndexOf(".v1_");
      if (idx >= 0) {
         return pkg.substring(idx + 1);
      } else {
         String[] parts = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
         return "v" + parts[0] + "_" + (parts.length > 1 ? parts[1] : "0") + "_R1";
      }
   }
}
