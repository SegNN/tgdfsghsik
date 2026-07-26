package org.impldev.holycore.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.impldev.holycore.HolyCore;

public class EntityExtension implements Listener {
   private final HolyCore plugin;

   public EntityExtension(HolyCore plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onDeath(EntityDeathEvent event) {
      EntityType entityType = event.getEntityType();
      if (entityType == EntityType.CREEPER) {
         this.handleCreeperDrops(event);
      }

      if (entityType == EntityType.GHAST) {
         this.handleGhastDrops(event);
      }

      if (entityType == EntityType.VINDICATOR) {
         this.handleVindicatorDrops(event);
      }

      if (entityType == EntityType.EVOKER) {
         this.handleEvokerDrops(event);
      }
   }

   private void handleCreeperDrops(EntityDeathEvent event) {
      List<ItemStack> drops = new ArrayList<>();

      for (ItemStack drop : event.getDrops()) {
         if (drop.getType() == Material.GUNPOWDER) {
            int random = new Random().nextInt(4);
            if (random == 0) {
               drops.add(drop);
            } else if (random == 1) {
               ItemStack newDrop = drop.clone();
               newDrop.setAmount(new Random().nextInt(drop.getAmount()) + 1);
               drops.add(newDrop);
            }
         } else {
            drops.add(drop);
         }
      }

      event.getDrops().clear();
      event.getDrops().addAll(drops);
   }

   private void handleGhastDrops(EntityDeathEvent event) {
      List<ItemStack> drops = new ArrayList<>();

      for (ItemStack drop : event.getDrops()) {
         if (drop.getType() != Material.GUNPOWDER) {
            drops.add(drop);
         }
      }

      event.getDrops().clear();
      event.getDrops().addAll(drops);
   }

   private void handleVindicatorDrops(EntityDeathEvent event) {
      List<ItemStack> drops = new ArrayList<>();

      for (ItemStack drop : event.getDrops()) {
         if (drop.getType() != Material.EMERALD) {
            drops.add(drop);
         }
      }

      event.getDrops().clear();
      event.getDrops().addAll(drops);
   }

   private void handleEvokerDrops(EntityDeathEvent event) {
      List<ItemStack> drops = new ArrayList<>();

      for (ItemStack drop : event.getDrops()) {
         if (drop.getType() != Material.TOTEM_OF_UNDYING) {
            drops.add(drop);
         } else if (new Random().nextInt(25) == 0) {
            drops.add(drop);
         }
      }

      event.getDrops().clear();
      event.getDrops().addAll(drops);
   }

   @EventHandler
   public void onCreatureSpawn(CreatureSpawnEvent event) {
      int random = new Random().nextInt(12);
      if (event.getSpawnReason() == SpawnReason.NATURAL && random != 0) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onExplode(EntityExplodeEvent event) {
      switch (event.getEntityType()) {
         case END_CRYSTAL:
         case CREEPER:
            event.blockList().clear();
      }
   }

   @EventHandler
   public void onSpawn(CreatureSpawnEvent event) {
      if (event.getSpawnReason() == SpawnReason.BUILD_WITHER) {
         event.setCancelled(true);
      }
   }
}
