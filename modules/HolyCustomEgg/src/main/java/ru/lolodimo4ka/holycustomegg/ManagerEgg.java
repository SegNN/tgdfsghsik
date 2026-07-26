package ru.lolodimo4ka.holycustomegg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ManagerEgg {
   private String id;
   private String displayName;
   private List<String> lore;
   private Material material;
   private String spawnerName;
   private List<String> spawnerLore;
   private List<ManagerEgg.MobChance> mobs = new ArrayList<>();
   private Random random = new Random();

   public ManagerEgg(String id, String displayName, List<String> lore, Material material, String spawnerName, List<String> spawnerLore) {
      this.id = id;
      this.displayName = displayName;
      this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
      this.material = material;
      this.spawnerName = spawnerName;
      this.spawnerLore = spawnerLore != null ? new ArrayList<>(spawnerLore) : new ArrayList<>();
      this.mobs = new ArrayList<>();
   }

   public void addMob(EntityType entityType, double chance) {
      this.mobs.add(new ManagerEgg.MobChance(entityType, chance));
   }

   public void addMob(EntityType entityType, double chance, boolean babyZombie) {
      this.mobs.add(new ManagerEgg.MobChance(entityType, chance, babyZombie));
   }

   public ItemStack getEgg() {
      return this.createItem(1);
   }

   public ItemStack getSpawner() {
      return this.createMysterySpawner();
   }

   private ItemStack createItem(int amount) {
      ItemStack item = new ItemStack(this.material, amount);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Utils.formatColors(this.displayName));
         meta.setLore(Utils.formatColors(this.lore));
         meta.addEnchant(Enchantment.LURE, 1, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         item.setItemMeta(meta);
      }

      return item;
   }

   private ItemStack createMysterySpawner() {
      ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
      BlockStateMeta meta = (BlockStateMeta)spawnerItem.getItemMeta();
      CreatureSpawner spawnerMeta = (CreatureSpawner)meta.getBlockState();
      EntityType initialType = this.getRandomMob();
      spawnerMeta.setSpawnedType(initialType);
      meta.setBlockState(spawnerMeta);
      meta.setDisplayName(this.getSpawnerName());
      meta.setLore(this.getSpawnerLore());
      NamespacedKey key = new NamespacedKey(HolyCustomEgg.getInstance(), "holy_egg_id");
      meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.id);
      meta.setCustomModelData(null);
      spawnerItem.setItemMeta(meta);
      return spawnerItem;
   }

   public EntityType getRandomMob() {
      if (this.mobs.isEmpty()) {
         return EntityType.ZOMBIE;
      } else {
         double totalChance = this.mobs.stream().mapToDouble(ManagerEgg.MobChance::getChance).sum();
         double randomValue = this.random.nextDouble() * totalChance;
         double currentChance = 0.0;

         for (ManagerEgg.MobChance mob : this.mobs) {
            currentChance += mob.getChance();
            if (randomValue <= currentChance) {
               return mob.getEntityType();
            }
         }

         return this.mobs.get(this.mobs.size() - 1).getEntityType();
      }
   }

   public ManagerEgg.MobChance getRandomMobChance() {
      if (this.mobs.isEmpty()) {
         return new ManagerEgg.MobChance(EntityType.ZOMBIE, 100.0);
      } else {
         double totalChance = this.mobs.stream().mapToDouble(ManagerEgg.MobChance::getChance).sum();
         double randomValue = this.random.nextDouble() * totalChance;
         double currentChance = 0.0;

         for (ManagerEgg.MobChance mob : this.mobs) {
            currentChance += mob.getChance();
            if (randomValue <= currentChance) {
               return mob;
            }
         }

         return this.mobs.get(this.mobs.size() - 1);
      }
   }

   public void spawnRandomMob(Location location) {
      ManagerEgg.MobChance mobChance = this.getRandomMobChance();
      EntityType entityType = mobChance.getEntityType();
      if (entityType != null && entityType.isSpawnable() && entityType.isAlive()) {
         Location centeredLocation = location.clone();
         centeredLocation.setX(Math.floor(centeredLocation.getX()) + 0.5);
         centeredLocation.setZ(Math.floor(centeredLocation.getZ()) + 0.5);
         Block block = location.getBlock();
         if (block.isLiquid() || block.getType() == Material.WATER || block.getType() == Material.LAVA) {
            centeredLocation.setY(location.getY());
         }

         if (mobChance.isBabyZombie() && entityType == EntityType.ZOMBIE) {
            Zombie zombie = (Zombie)centeredLocation.getWorld().spawnEntity(centeredLocation, EntityType.ZOMBIE);
            zombie.setBaby(true);
         } else {
            centeredLocation.getWorld().spawnEntity(centeredLocation, entityType);
         }
      } else {
         location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
      }
   }

   public List<ManagerEgg.MobChance> getMobs() {
      return new ArrayList<>(this.mobs);
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return Utils.formatColors(this.displayName);
   }

   public List<String> getLore() {
      return Utils.formatColors(this.lore);
   }

   public String getSpawnerName() {
      return Utils.formatColors(this.spawnerName);
   }

   public List<String> getSpawnerLore() {
      return Utils.formatColors(this.spawnerLore);
   }

   public Material getMaterial() {
      return this.material;
   }

   public static class MobChance {
      private EntityType entityType;
      private double chance;
      private boolean babyZombie;

      public MobChance(EntityType entityType, double chance) {
         this.entityType = entityType;
         this.chance = chance;
         this.babyZombie = false;
      }

      public MobChance(EntityType entityType, double chance, boolean babyZombie) {
         this.entityType = entityType;
         this.chance = chance;
         this.babyZombie = babyZombie;
      }

      public EntityType getEntityType() {
         return this.entityType;
      }

      public double getChance() {
         return this.chance;
      }

      public boolean isBabyZombie() {
         return this.babyZombie;
      }
   }
}
