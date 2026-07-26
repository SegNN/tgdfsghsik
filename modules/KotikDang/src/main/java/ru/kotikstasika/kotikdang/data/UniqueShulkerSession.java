package ru.kotikstasika.kotikdang.data;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.kotikstasika.kotikdang.tasks.UniqueShulkerOpenTask;

public class UniqueShulkerSession {
   private final UUID sessionId = UUID.randomUUID();
   private final Player player;
   private final UniqueShulkerLocation shulkerLocation;
   private final Inventory inventory;
   private final long startTime;
   private final int duration;
   private UniqueShulkerOpenTask task;
   private boolean inventoryOpen = true;
   private String openingHologramId;

   public UniqueShulkerSession(Player player, UniqueShulkerLocation shulkerLocation, Inventory inventory) {
      this.player = player;
      this.shulkerLocation = shulkerLocation;
      this.inventory = inventory;
      this.startTime = System.currentTimeMillis();
      this.duration = shulkerLocation.getCurrentRarity().getDuration();
   }

   public UUID getSessionId() {
      return this.sessionId;
   }

   public Player getPlayer() {
      return this.player;
   }

   public UniqueShulkerLocation getShulkerLocation() {
      return this.shulkerLocation;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public int getDuration() {
      return this.duration;
   }

   public int getRemainingSeconds() {
      long elapsed = (System.currentTimeMillis() - this.startTime) / 1000L;
      return Math.max(0, this.duration - (int)elapsed);
   }

   public UniqueShulkerOpenTask getTask() {
      return this.task;
   }

   public void setTask(UniqueShulkerOpenTask task) {
      this.task = task;
   }

   public boolean isInventoryOpen() {
      return this.inventoryOpen;
   }

   public void setInventoryOpen(boolean inventoryOpen) {
      this.inventoryOpen = inventoryOpen;
   }

   public String getOpeningHologramId() {
      return this.openingHologramId;
   }

   public void setOpeningHologramId(String openingHologramId) {
      this.openingHologramId = openingHologramId;
   }

   public void cancel() {
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel();
      }
   }
}
