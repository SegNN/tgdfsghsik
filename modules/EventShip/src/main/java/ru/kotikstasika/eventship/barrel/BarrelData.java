package ru.kotikstasika.eventship.barrel;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import ru.kotikstasika.eventship.enums.KeyType;

public class BarrelData {
   private Inventory inventory;
   private Location location;
   private KeyType keyType;
   private String hologramId;
   private boolean open = true;

   public BarrelData(Inventory inventory, Location location, KeyType keyType) {
      this.inventory = inventory;
      this.location = location;
      this.keyType = keyType;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public Location getLocation() {
      return this.location;
   }

   public String getHologramId() {
      return this.hologramId;
   }

   public void setHologramId(String hologramId) {
      this.hologramId = hologramId;
   }

   public boolean isOpen() {
      return this.open;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   public void setKeyType(KeyType keyType) {
      this.keyType = keyType;
   }
}
