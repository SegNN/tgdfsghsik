package ua.holydev.holycases.nms;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

/**
 * Wrapper around a real (server side) {@link ArmorStand}.
 *
 * <p>The 1.16.5 implementation was a bare {@code EntityArmorStand} that was never added to the
 * world and whose packets were broadcast to <b>every player of the world</b>, so a normal server
 * side entity is behaviourally equivalent. The stands are marked non persistent so that a hard
 * crash can never leave them behind in the region files.
 */
public class CustomArmorStand {
   protected static List<CustomArmorStand> handlerList = new ArrayList<>();

   private Location location;
   private ArmorStand stand;
   private boolean isSpawned;
   private String customName;
   private boolean customNameVisible;
   private boolean marker;
   private boolean baby;
   private ItemStack helmet;
   private EulerAngle headPose = EulerAngle.ZERO;

   public CustomArmorStand(Location location) {
      this.location = location.clone();
      handlerList.add(this);
   }

   private void addToWorld(Location location) {
      if (this.stand == null) {
         this.stand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setPersistent(false);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCollidable(false);
            stand.setInvulnerable(true);
            stand.setDisabledSlots(
               EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            );
            stand.setMarker(this.marker);
            stand.setSmall(this.baby);
            stand.setCustomNameVisible(this.customNameVisible);
            if (this.customName != null) {
               stand.setCustomName(this.customName);
            }

            if (this.helmet != null) {
               stand.getEquipment().setHelmet(this.helmet, true);
            }

            stand.setHeadPose(this.headPose);
         });
      }
   }

   public void spawn(Location location) {
      this.addToWorld(location);
      this.pureTeleport(location);
      this.isSpawned = true;
   }

   private void pureTeleport(Location location) {
      this.location = location.clone();
      if (this.stand != null) {
         this.stand.teleport(this.location);
      }
   }

   public void teleport(Location location) {
      this.pureTeleport(location);
   }

   public void setCustomName(String name) {
      this.customName = name;
      this.customNameVisible = true;
      if (this.stand != null) {
         this.stand.setCustomName(name);
         this.stand.setCustomNameVisible(true);
      }
   }

   public void setCustomNameVisible(boolean visible) {
      this.customNameVisible = visible;
      if (this.stand != null) {
         this.stand.setCustomNameVisible(visible);
      }
   }

   public void setNameVisible(boolean visible) {
      this.setCustomNameVisible(visible);
   }

   public Location getLocation() {
      return this.location.clone();
   }

   public double getHeight() {
      return this.stand == null ? 0.0 : this.stand.getHeight();
   }

   public void setHelmet(ItemStack item) {
      this.helmet = item;
      if (this.stand != null) {
         // silent: the packet based 1.16.5 stand never played the vanilla equip sound
         this.stand.getEquipment().setHelmet(item, true);
      }
   }

   public ItemStack getHelmet() {
      ItemStack item = this.stand == null ? this.helmet : this.stand.getEquipment().getHelmet();
      return item == null ? new ItemStack(Material.AIR) : item;
   }

   public void setHeadPose(EulerAngle angle) {
      this.headPose = angle;
      if (this.stand != null) {
         this.stand.setHeadPose(angle);
      }
   }

   public void destroy() {
      this.isSpawned = false;
      if (this.stand != null) {
         this.stand.remove();
         this.stand = null;
      }
   }

   public void remove() {
      this.destroy();
      handlerList.remove(this);
   }

   public boolean isSpawned() {
      return this.isSpawned;
   }

   /**
    * The stands are non persistent, so unloading their chunk silently deletes the entity. The
    * 1.16.5 stands were packet only and simply got re-sent, so to keep the same observable
    * behaviour they are recreated when their chunk comes back. Safe to call at any time.
    */
   void respawnIfMissing() {
      if (this.isSpawned && (this.stand == null || !this.stand.isValid())) {
         this.stand = null;
         this.addToWorld(this.location);
      }
   }

   boolean isAt(org.bukkit.Chunk chunk) {
      return this.location != null
         && this.location.getWorld() != null
         && this.location.getWorld().equals(chunk.getWorld())
         && this.location.getBlockX() >> 4 == chunk.getX()
         && this.location.getBlockZ() >> 4 == chunk.getZ();
   }

   public void setMarker(boolean marker) {
      this.marker = marker;
      if (this.stand != null) {
         this.stand.setMarker(marker);
      }
   }

   public void setBaby(boolean baby) {
      this.baby = baby;
      if (this.stand != null) {
         this.stand.setSmall(baby);
      }
   }
}
