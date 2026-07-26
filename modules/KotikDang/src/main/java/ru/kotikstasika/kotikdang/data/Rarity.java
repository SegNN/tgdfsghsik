package ru.kotikstasika.kotikdang.data;

import org.bukkit.DyeColor;

public enum Rarity {
   PURPLE("purple", 60, DyeColor.PURPLE),
   RED("red", 90, DyeColor.RED);

   private final String name;
   private final int duration;
   private final DyeColor color;

   private Rarity(String name, int duration, DyeColor color) {
      this.name = name;
      this.duration = duration;
      this.color = color;
   }

   public String getName() {
      return this.name;
   }

   public int getDuration() {
      return this.duration;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public static Rarity fromString(String name) {
      for (Rarity rarity : values()) {
         if (rarity.name.equalsIgnoreCase(name)) {
            return rarity;
         }
      }

      return null;
   }

   public static Rarity random() {
      return values()[(int)(Math.random() * values().length)];
   }
}
