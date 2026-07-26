package ua.holydev.holycases.reward;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

public class EntityReward {
   private Reward reward;
   private ArmorStand entity;
   private ArmorStand name;
   private Map<String, Object> data = new HashMap<>();

   public EntityReward(Reward reward) {
      this.reward = reward;
   }

   public void spawn(Location location) {
      this.entity = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class);
      this.entity.setInvisible(true);
      this.entity.setGravity(false);
      this.name = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class);
      this.name.setInvisible(true);
      this.name.setGravity(false);
   }

   public void teleport(Location location) {
      this.name.teleport(location.clone().add(0.0, -this.getHeight(), 0.0));
      this.entity
         .teleport(
            (this.getItem() != null && !this.getItem().getType().isBlock() ? location.clone().add(0.0, -0.45, 0.0) : location)
               .clone()
               .add(0.0, -this.getHeight(), 0.0)
         );
   }

   public World getWorld() {
      return this.entity.getWorld();
   }

   public Location getLocation() {
      return this.entity.getLocation();
   }

   public double getHeight() {
      return this.name.getHeight();
   }

   public Reward getReward() {
      return this.reward;
   }

   public void setItem(ItemStack item) {
      if (this.getItem() != null && !this.getItem().getType().isBlock()) {
         this.entity.teleport(this.entity.getLocation().clone().add(0.0, 0.45, 0.0));
      }

      if (item != null && !item.getType().isBlock()) {
         this.entity.teleport(this.entity.getLocation().clone().add(0.0, -0.45, 0.0));
      }

      this.entity.getEquipment().setHelmet(item);
   }

   public ItemStack getItem() {
      return this.entity.getEquipment().getHelmet();
   }

   public void setName(String name) {
      this.name.setCustomName(name);
      this.name.setCustomNameVisible(true);
   }

   public void show() {
   }

   public void setData(String path, Object o) {
      this.data.put(path, o);
   }

   public Object getData(String path) {
      return this.data.get(path);
   }
}
