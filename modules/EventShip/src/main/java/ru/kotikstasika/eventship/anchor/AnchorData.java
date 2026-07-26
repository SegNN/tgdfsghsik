package ru.kotikstasika.eventship.anchor;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public class AnchorData {
   private final Location location;
   private String hologramId;
   private int usedKeys = 0;
   private boolean activated = false;
   private boolean openPhase = false;
   private Inventory inventory;

   public AnchorData(Location location) {
      this.location = location;
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

   public int getUsedKeys() {
      return this.usedKeys;
   }

   public void addKey() {
      this.usedKeys++;
   }

   public void setUsedKeys(int usedKeys) {
      this.usedKeys = usedKeys;
   }

   public boolean isActivated() {
      return this.activated;
   }

   public void setActivated(boolean activated) {
      this.activated = activated;
   }

   public boolean isOpenPhase() {
      return this.openPhase;
   }

   public void setOpenPhase(boolean openPhase) {
      this.openPhase = openPhase;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }
}
