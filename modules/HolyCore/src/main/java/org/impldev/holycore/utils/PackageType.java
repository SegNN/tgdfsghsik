package org.impldev.holycore.utils;

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
      return Bukkit.getServer().getClass().getPackage().getName().substring(23);
   }

   private static PackageType[] $values() {
      return new PackageType[]{
         MINECRAFT_SERVER,
         CRAFTBUKKIT,
         CRAFTBUKKIT_BLOCK,
         CRAFTBUKKIT_CHUNKIO,
         CRAFTBUKKIT_COMMAND,
         CRAFTBUKKIT_CONVERSATIONS,
         CRAFTBUKKIT_ENCHANTMENS,
         CRAFTBUKKIT_ENTITY,
         CRAFTBUKKIT_EVENT,
         CRAFTBUKKIT_GENERATOR,
         CRAFTBUKKIT_HELP,
         CRAFTBUKKIT_INVENTORY,
         CRAFTBUKKIT_MAP,
         CRAFTBUKKIT_METADATA,
         CRAFTBUKKIT_POTION,
         CRAFTBUKKIT_PROJECTILES,
         CRAFTBUKKIT_SCHEDULER,
         CRAFTBUKKIT_SCOREBOARD,
         CRAFTBUKKIT_UPDATER,
         CRAFTBUKKIT_UTIL
      };
   }

   private static PackageType[] $values$() {
      return new PackageType[]{
         MINECRAFT_SERVER,
         CRAFTBUKKIT,
         CRAFTBUKKIT_BLOCK,
         CRAFTBUKKIT_CHUNKIO,
         CRAFTBUKKIT_COMMAND,
         CRAFTBUKKIT_CONVERSATIONS,
         CRAFTBUKKIT_ENCHANTMENS,
         CRAFTBUKKIT_ENTITY,
         CRAFTBUKKIT_EVENT,
         CRAFTBUKKIT_GENERATOR,
         CRAFTBUKKIT_HELP,
         CRAFTBUKKIT_INVENTORY,
         CRAFTBUKKIT_MAP,
         CRAFTBUKKIT_METADATA,
         CRAFTBUKKIT_POTION,
         CRAFTBUKKIT_PROJECTILES,
         CRAFTBUKKIT_SCHEDULER,
         CRAFTBUKKIT_SCOREBOARD,
         CRAFTBUKKIT_UPDATER,
         CRAFTBUKKIT_UTIL
      };
   }
}
