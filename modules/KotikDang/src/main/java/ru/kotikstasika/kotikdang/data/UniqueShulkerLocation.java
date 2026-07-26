package ru.kotikstasika.kotikdang.data;

import java.util.UUID;
import org.bukkit.Location;

public class UniqueShulkerLocation {
   private final UUID id;
   private final Location location;
   private final String name;
   private final int lootLevel;
   private Rarity currentRarity;
   private String hologramId;

   public UniqueShulkerLocation(UUID id, Location location, String name, int lootLevel) {
      this.id = id;
      this.location = location;
      this.name = name;
      this.lootLevel = lootLevel;
      this.currentRarity = Rarity.random();
   }

   public UUID getId() {
      return this.id;
   }

   public Location getLocation() {
      return this.location;
   }

   public String getName() {
      return this.name;
   }

   public int getLootLevel() {
      return this.lootLevel;
   }

   public Rarity getCurrentRarity() {
      return this.currentRarity;
   }

   public void setCurrentRarity(Rarity rarity) {
      this.currentRarity = rarity;
   }

   public String getHologramId() {
      return this.hologramId;
   }

   public void setHologramId(String hologramId) {
      this.hologramId = hologramId;
   }

   public void respawn() {
      this.currentRarity = Rarity.random();
   }
}
