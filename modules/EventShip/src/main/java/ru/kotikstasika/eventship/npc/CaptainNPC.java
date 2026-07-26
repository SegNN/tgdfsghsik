package ru.kotikstasika.eventship.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.enums.KeyType;
import ru.kotikstasika.eventship.hologram.HologramManager;
import ru.kotikstasika.eventship.items.KeyItem;

public class CaptainNPC {
   private final EventShip plugin;
   private final HologramManager hologramManager;
   private final Location location;
   private WanderingTrader captain;
   private String hologramId;
   private boolean hasKey = false;
   private final Random random = new Random();
   private BukkitTask lookAtTask;
   private BukkitTask validationTask;

   public CaptainNPC(EventShip plugin, HologramManager hologramManager, Location location) {
      this.plugin = plugin;
      this.hologramManager = hologramManager;
      this.location = location;
      this.spawnCaptain();
      this.createHologram();
      this.startLookingAtPlayers();
      this.startValidationTask();
   }

   private void spawnCaptain() {
      this.removeOldCaptain();
      this.captain = (WanderingTrader)this.location.getWorld().spawnEntity(this.location, EntityType.WANDERING_TRADER);
      this.captain.setCustomNameVisible(false);
      this.captain.setAI(false);
      this.captain.setInvulnerable(true);
      this.captain.setCollidable(false);
      this.captain.setSilent(true);
      this.captain.setRemoveWhenFarAway(false);
      this.captain.setGravity(false);
      this.captain.setPersistent(true);
      this.captain.setCanPickupItems(false);
      this.captain.addScoreboardTag("EventShipCaptain");
   }

   private void removeOldCaptain() {
      Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(WanderingTrader.class).forEach(trader -> {
         if (trader.getScoreboardTags().contains("EventShipCaptain")) {
            trader.remove();
         }
      }));
   }

   private void startValidationTask() {
      if (this.validationTask != null) {
         this.validationTask.cancel();
      }

      this.validationTask = (new BukkitRunnable() {
         public void run() {
            CaptainNPC.this.cleanupNearbyTraders();
            if (CaptainNPC.this.captain == null || !CaptainNPC.this.captain.isValid()) {
               CaptainNPC.this.validateCaptain();
            } else if (CaptainNPC.this.hasKey) {
               ItemStack currentItem = CaptainNPC.this.captain.getEquipment().getItemInMainHand();
               if (currentItem == null || currentItem.getType().isAir()) {
                  CaptainNPC.this.forceSpawnKey();
               }
            }
         }
      }).runTaskTimer(this.plugin, 1L, 20L);
   }

   private void cleanupNearbyTraders() {
      if (this.location != null && this.location.getWorld() != null) {
         List<WanderingTrader> nearby = new ArrayList<>();

         for (WanderingTrader t : this.location.getWorld().getEntitiesByClass(WanderingTrader.class)) {
            if (t.getLocation().distanceSquared(this.location) <= 36.0) {
               nearby.add(t);
            }
         }

         if (!nearby.isEmpty()) {
            List<WanderingTrader> tagged = new ArrayList<>();
            List<WanderingTrader> others = new ArrayList<>();

            for (WanderingTrader tx : nearby) {
               if (tx.getScoreboardTags().contains("EventShipCaptain")) {
                  tagged.add(tx);
               } else {
                  others.add(tx);
               }
            }

            for (WanderingTrader txx : others) {
               try {
                  txx.remove();
               } catch (Exception var14) {
               }
            }

            if (tagged.isEmpty()) {
               this.spawnCaptain();
            } else {
               WanderingTrader keep = tagged.get(0);
               double best = keep.getLocation().distanceSquared(this.location);

               for (int i = 1; i < tagged.size(); i++) {
                  WanderingTrader txx = tagged.get(i);
                  double d = txx.getLocation().distanceSquared(this.location);
                  if (d < best) {
                     try {
                        keep.remove();
                     } catch (Exception var12) {
                     }

                     keep = txx;
                     best = d;
                  } else {
                     try {
                        txx.remove();
                     } catch (Exception var13) {
                     }
                  }
               }

               this.captain = keep;
               this.captain.setAI(false);
               this.captain.setInvulnerable(true);
               this.captain.setCollidable(false);
               this.captain.setSilent(true);
               this.captain.setRemoveWhenFarAway(false);
               this.captain.setGravity(false);
               this.captain.setPersistent(true);
               this.captain.setCanPickupItems(false);
            }
         }
      }
   }

   private void validateCaptain() {
      if (this.captain == null || !this.captain.isValid()) {
         List<WanderingTrader> found = new ArrayList<>();
         Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(WanderingTrader.class).forEach(t -> {
            if (t.getScoreboardTags().contains("EventShipCaptain")) {
               found.add(t);
            }
         }));
         if (found.size() > 1) {
            found.stream().filter(v -> !v.equals(this.captain)).forEach(Entity::remove);
         }

         if (!found.isEmpty()) {
            this.captain = found.get(0);
         } else if (this.location != null) {
            this.spawnCaptain();
            return;
         }
      }

      if (this.captain != null && this.captain.isValid()) {
         this.captain.setAI(false);
         this.captain.setInvulnerable(true);
         this.captain.setCollidable(false);
         this.captain.setSilent(true);
         this.captain.setRemoveWhenFarAway(false);
         this.captain.setGravity(false);
         this.captain.setPersistent(true);
         this.captain.setCanPickupItems(false);
         if (this.hasKey && (this.captain.getEquipment().getItemInMainHand() == null || this.captain.getEquipment().getItemInMainHand().getType().isAir())) {
            this.forceSpawnKey();
         }
      }
   }

   private void createHologram() {
      List<String> lines;
      if (this.hasKey) {
         lines = this.plugin.getConfig().getStringList("messages.captainHologram.hasKey");
      } else {
         lines = this.plugin.getConfig().getStringList("messages.captainHologram.noKey");
      }

      String hologramId = this.hologramManager.createHologram(this.location.clone().add(0.0, 2.9, 0.0), lines);
      if (hologramId != null) {
         this.hologramId = hologramId;
      }
   }

   private void startLookingAtPlayers() {
      if (this.lookAtTask != null) {
         this.lookAtTask.cancel();
      }

      this.lookAtTask = (new BukkitRunnable() {
         public void run() {
            if (CaptainNPC.this.captain != null && CaptainNPC.this.captain.isValid()) {
               Player nearest = null;
               double minDistance = Double.MAX_VALUE;
               double searchRadius = 10.0;

               for (Player p : CaptainNPC.this.captain.getWorld().getPlayers()) {
                  if (p.isValid() && p.isOnline()) {
                     double d = p.getLocation().distanceSquared(CaptainNPC.this.captain.getLocation());
                     if (d < minDistance && d <= searchRadius * searchRadius) {
                        minDistance = d;
                        nearest = p;
                     }
                  }
               }

               if (nearest != null) {
                  Location captainLoc = CaptainNPC.this.captain.getLocation();
                  Vector direction = nearest.getLocation().toVector().subtract(captainLoc.toVector()).normalize();
                  float yaw = (float)Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
                  float pitch = (float)Math.toDegrees(Math.asin(-direction.getY()));
                  Location newLoc = captainLoc.clone();
                  newLoc.setYaw(yaw);
                  newLoc.setPitch(pitch);
                  if (Math.abs(newLoc.getYaw() - captainLoc.getYaw()) > 3.0F || Math.abs(newLoc.getPitch() - captainLoc.getPitch()) > 3.0F) {
                     CaptainNPC.this.captain.teleport(newLoc);
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 10L);
   }

   public void updateHologram() {
      if (this.hologramId != null) {
         List<String> lines;
         if (this.hasKey) {
            lines = this.plugin.getConfig().getStringList("messages.captainHologram.hasKey");
         } else {
            lines = this.plugin.getConfig().getStringList("messages.captainHologram.noKey");
         }

         this.hologramManager.updateHologram(this.hologramId, lines);
      }
   }

   public void spawnKey() {
      this.hasKey = true;
      this.updateHologram();
      Bukkit.getScheduler().runTaskLater(this.plugin, this::updateHologram, 1L);
      this.forceSpawnKey();
      if (this.captain != null && this.captain.isValid()) {
         Location loc = this.captain.getLocation();
         World world = loc.getWorld();
         world.playSound(loc, Sound.ENTITY_VILLAGER_YES, 0.6F, 1.2F);

         for (int i = 0; i < 25; i++) {
            double offsetX = (Math.random() - 0.5) * 1.2;
            double offsetZ = (Math.random() - 0.5) * 1.2;
            Location particleLoc = loc.clone().add(offsetX, 0.1, offsetZ);
            world.spawnParticle(Particle.LAVA, particleLoc, 1, 0.0, 0.35, 0.0, 0.12);
         }
      }
   }

   private void forceSpawnKey() {
      if (this.captain != null && this.captain.isValid()) {
         KeyType keyType = this.getRandomKeyType();
         ItemStack key = KeyItem.createKey(keyType, this.plugin);
         this.attemptSetMainHand(key, 0);
      }
   }

   private void attemptSetMainHand(ItemStack key, int attempt) {
      if (this.captain != null && this.captain.isValid() && this.hasKey) {
         if (attempt <= 5) {
            this.captain.getEquipment().setItemInMainHand(key);
            this.captain.getEquipment().setItemInOffHand(null);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (this.captain != null && this.captain.isValid() && this.hasKey) {
                  ItemStack currentItem = this.captain.getEquipment().getItemInMainHand();
                  if (currentItem == null || currentItem.getType().isAir()) {
                     this.attemptSetMainHand(key, attempt + 1);
                  }
               }
            }, 4L);
         }
      }
   }

   public void removeKey() {
      this.hasKey = false;
      if (this.captain != null && this.captain.isValid()) {
         this.captain.getEquipment().setItemInMainHand(null);
         this.captain.getEquipment().setItemInOffHand(null);
         this.captain.getWorld().playSound(this.captain.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.6F, 1.0F);
      }

      this.updateHologram();
      this.startKeyRespawnTimer();
   }

   private void startKeyRespawnTimer() {
      int interval = this.getKeyRespawnInterval();
      (new BukkitRunnable() {
            public void run() {
               if (CaptainNPC.this.captain != null
                  && CaptainNPC.this.captain.isValid()
                  && !CaptainNPC.this.hasKey
                  && CaptainNPC.this.plugin.getEventManager() != null
                  && CaptainNPC.this.plugin.getEventManager().isActive()) {
                  CaptainNPC.this.spawnKey();
               }
            }
         })
         .runTaskLater(this.plugin, interval * 20L);
   }

   private int getKeyRespawnInterval() {
      if (this.plugin.getEventManager() != null && this.plugin.getEventManager().getRarity() != null) {
         switch (this.plugin.getEventManager().getRarity()) {
            case DEFAULT:
               return 90;
            case PROSPEROUS:
               return 78;
            case LUXURIOUS:
               return 60;
            default:
               return 90;
         }
      } else {
         return 90;
      }
   }

   public void pushPlayerAway(Player player) {
      if (this.captain != null) {
         Vector velocity = player.getLocation().toVector().subtract(this.captain.getLocation().toVector()).normalize().multiply(5 + this.random.nextInt(6));
         velocity.setY(0.05);
         player.setVelocity(velocity);
         player.damage(5.0);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
      }
   }

   private KeyType getRandomKeyType() {
      KeyType[] types = KeyType.values();
      return types[this.random.nextInt(types.length)];
   }

   public boolean hasKey() {
      return this.hasKey;
   }

   public ItemStack getKey() {
      return this.captain != null && this.captain.isValid() ? this.captain.getEquipment().getItemInMainHand() : null;
   }

   public Location getLocation() {
      return this.location;
   }

   public void remove() {
      if (this.lookAtTask != null) {
         this.lookAtTask.cancel();
         this.lookAtTask = null;
      }

      if (this.validationTask != null) {
         this.validationTask.cancel();
         this.validationTask = null;
      }

      if (this.captain != null && !this.captain.isDead()) {
         this.captain.remove();
         this.captain = null;
      }

      if (this.hologramId != null) {
         this.hologramManager.removeHologram(this.hologramId);
         this.hologramId = null;
      }

      this.removeOldCaptain();
   }
}
