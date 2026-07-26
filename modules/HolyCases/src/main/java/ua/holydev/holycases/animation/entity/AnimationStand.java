package ua.holydev.holycases.animation.entity;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import ua.holydev.holycases.nms.CustomArmorStand;

public class AnimationStand {
   private CustomArmorStand itemNoBlock;
   private CustomArmorStand name;
   private Map<String, Object> data = new HashMap<>();

   public AnimationStand() {
   }

   public void spawn(Location location) {
      Location fixed = location.clone();
      fixed.setYaw(location.getYaw());
      this.itemNoBlock = new CustomArmorStand(fixed) {
         @Override
         public void teleport(Location location) {
            super.teleport(location.clone().add(0.0, -0.45, 0.0));
         }

         @Override
         public Location getLocation() {
            return super.getLocation().add(0.0, -0.45, 0.0);
         }
      };
      this.itemNoBlock.spawn(fixed);
      this.name = new CustomArmorStand(fixed);
      this.name.spawn(fixed);
   }

   public void teleport(Location location) {
      Location fixed = location.clone().add(0.0, -this.getHeight(), 0.0);
      this.name.teleport(fixed);
      this.itemNoBlock.teleport(fixed);
   }

   public World getWorld() {
      return this.name.getLocation().getWorld();
   }

   public Location getLocation() {
      return this.name.getLocation();
   }

   public double getHeight() {
      return this.name.getHeight();
   }

   public void setItem(ItemStack item) {
      if (item.getType().isBlock()) {
         if (this.itemNoBlock.getHelmet() != null) {
            this.itemNoBlock.setHelmet(null);
         }

         this.name.setHelmet(item);
      } else {
         if (this.name.getHelmet() != null) {
            this.name.setHelmet(null);
         }

         this.itemNoBlock.setHelmet(item);
      }
   }

   public ItemStack getItem() {
      return this.itemNoBlock.getHelmet().getType() != Material.AIR ? this.itemNoBlock.getHelmet() : this.name.getHelmet();
   }

   public void setBaby(boolean baby) {
      this.name.setBaby(baby);
      this.itemNoBlock.setBaby(baby);
   }

   public void setName(String name) {
      this.name.setCustomName(name);
      this.name.setCustomNameVisible(true);
   }

   public void setHeadRotation(double x, double y, double z) {
      this.name.setHeadPose(new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z)));
      this.itemNoBlock.setHeadPose(new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z)));
   }

   public void setData(String path, Object o) {
      this.data.put(path, o);
   }

   public Object getData(String path) {
      return this.data.get(path);
   }

   public void remove() {
      this.name.remove();
      this.itemNoBlock.remove();
   }
}
