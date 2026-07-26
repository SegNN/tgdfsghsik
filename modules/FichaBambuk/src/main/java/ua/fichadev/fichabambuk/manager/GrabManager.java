package ua.fichadev.fichabambuk.manager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ua.fichadev.fichabambuk.item.BambukItem;

public final class GrabManager {
   private static final double INITIAL_SPEED = 2.0;
   private static final double GRAVITY = 0.05;
   private static final int MAX_TICKS = 200;
   private static final double SMASH_HEIGHT_THRESHOLD = 8.0;
   private final Plugin plugin;
   private final BambukItem item;
   private final Map<UUID, GrabManager.Grab> grabs = new ConcurrentHashMap<>();
   private final Map<UUID, GrabManager.Ride> rides = new ConcurrentHashMap<>();
   private final Set<UUID> fallGrace = ConcurrentHashMap.newKeySet();

   public GrabManager(Plugin plugin, BambukItem item) {
      this.plugin = plugin;
      this.item = item;
   }

   public boolean isGrabbing(UUID shooter) {
      return this.grabs.containsKey(shooter);
   }

   public boolean hasFallGrace(UUID target) {
      return this.fallGrace.contains(target);
   }

   public void clearFallGrace(UUID target) {
      this.fallGrace.remove(target);
   }

   public void start(Player shooter) {
      if (!this.grabs.containsKey(shooter.getUniqueId())) {
         double radius = this.plugin.getConfig().getDouble("grab-radius", 50.0);
         Player target = this.findTarget(shooter, radius);
         boolean hadArrow = shooter.getInventory().contains(Material.ARROW)
            || shooter.getInventory().contains(Material.SPECTRAL_ARROW)
            || shooter.getInventory().contains(Material.TIPPED_ARROW);
         boolean tempArrow = false;
         if (!hadArrow) {
            shooter.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 1)});
            tempArrow = true;
         }

         if (target == null) {
            if (tempArrow) {
               this.removeOneArrow(shooter);
            }

            shooter.sendMessage("§7Цель не найдена.");
         } else {
            GrabManager.Grab grab = new GrabManager.Grab(
               shooter.getUniqueId(), target.getUniqueId(), tempArrow, target.getWalkSpeed(), shooter.getAllowFlight()
            );
            shooter.setAllowFlight(true);
            target.setWalkSpeed(1.0E-4F);
            this.grabs.put(shooter.getUniqueId(), grab);
            grab.taskId = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.tick(grab), 1L, 1L).getTaskId();
         }
      }
   }

   private void tick(GrabManager.Grab grab) {
      Player shooter = Bukkit.getPlayer(grab.shooterId);
      Player target = Bukkit.getPlayer(grab.targetId);
      if (shooter != null && shooter.isOnline() && target != null && target.isOnline() && !target.isDead()) {
         Vector dir = shooter.getEyeLocation().getDirection().clone();
         Location desired = shooter.getEyeLocation().clone().add(dir.multiply(2.0));
         desired.setY(desired.getY() - 0.5);
         Location cur = target.getLocation();
         Vector delta = desired.toVector().subtract(cur.toVector());
         double len = delta.length();
         double step = Math.min(len, 1.2);
         if (len > 0.05) {
            Vector move = delta.normalize().multiply(step);
            Location next = cur.clone().add(move);
            next.setYaw(shooter.getLocation().getYaw() + 180.0F);
            next.setPitch(0.0F);
            target.teleport(next);
         }

         target.setFallDistance(0.0F);
      } else {
         this.cancelGrab(shooter);
      }
   }

   private void restoreStates(Player shooter, GrabManager.Grab grab) {
      if (shooter != null && shooter.isOnline()) {
         GameMode gm = shooter.getGameMode();
         if (gm != GameMode.CREATIVE && gm != GameMode.SPECTATOR) {
            shooter.setAllowFlight(grab.prevShooterAllowFlight);
            if (!grab.prevShooterAllowFlight) {
               shooter.setFlying(false);
            }
         }
      }

      Player target = Bukkit.getPlayer(grab.targetId);
      if (target != null && target.isOnline()) {
         target.setWalkSpeed(grab.prevTargetWalkSpeed);
      }
   }

   public void cancelGrab(Player shooter) {
      if (shooter != null) {
         GrabManager.Grab grab = this.grabs.remove(shooter.getUniqueId());
         if (grab != null) {
            Bukkit.getScheduler().cancelTask(grab.taskId);
            this.restoreStates(shooter, grab);
            if (grab.tempArrow) {
               this.removeOneArrow(shooter);
            }
         }
      }
   }

   public void release(Player shooter) {
      if (shooter != null) {
         GrabManager.Grab grab = this.grabs.remove(shooter.getUniqueId());
         if (grab != null) {
            Bukkit.getScheduler().cancelTask(grab.taskId);
            this.restoreStates(shooter, grab);
            if (grab.tempArrow) {
               this.removeOneArrow(shooter);
            }

            Player target = Bukkit.getPlayer(grab.targetId);
            if (target != null && target.isOnline() && !target.isDead()) {
               Location start = shooter.getEyeLocation().clone();
               Vector velocity = start.getDirection().multiply(2.0);
               Arrow arrow = start.getWorld().spawnArrow(start, start.getDirection(), 1.0F, 0.0F);
               arrow.setShooter(shooter);
               arrow.setPickupStatus(PickupStatus.DISALLOWED);
               arrow.setGravity(false);
               arrow.setDamage(0.0);
               arrow.setVelocity(new Vector(0, 0, 0));
               GrabManager.Ride ride = new GrabManager.Ride(arrow, target.getUniqueId(), start, velocity);
               this.rides.put(target.getUniqueId(), ride);
               ride.taskId = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.tickRide(ride), 1L, 1L).getTaskId();
            }
         }
      }
   }

   private void tickRide(GrabManager.Ride ride) {
      Player target = Bukkit.getPlayer(ride.targetId);
      if (target != null && target.isOnline() && !target.isDead()) {
         ride.ticks++;
         if (ride.ticks > 200) {
            this.finalizeRide(ride, ride.pos.toLocation(ride.start.getWorld()));
         } else {
            World world = ride.start.getWorld();
            Location from = ride.pos.toLocation(world);
            Vector step = ride.velocity.clone();
            double length = step.length();
            if (length < 0.01) {
               this.finalizeRide(ride, from);
            } else {
               Vector dir = step.clone().normalize();
               RayTraceResult hit = world.rayTraceBlocks(from, dir, length + 0.1, FluidCollisionMode.NEVER, true);
               if (hit != null) {
                  Location hitLoc = hit.getHitPosition().toLocation(world);
                  ride.pos = hitLoc.toVector();
                  this.finalizeRide(ride, hitLoc);
               } else {
                  Location next = from.clone().add(step);
                  ride.pos = next.toVector();
                  if (next.getY() > ride.maxY) {
                     ride.maxY = next.getY();
                  }

                  ride.velocity.add(new Vector(0.0, -0.05, 0.0));
                  if (ride.arrow.isValid()) {
                     Location arrowLoc = next.clone();
                     arrowLoc.setDirection(ride.velocity);
                     ride.arrow.teleport(arrowLoc);
                     ride.arrow.setVelocity(new Vector(0, 0, 0));
                  }

                  Location tLoc = next.clone();
                  tLoc.setYaw(target.getLocation().getYaw());
                  tLoc.setPitch(target.getLocation().getPitch());
                  target.teleport(tLoc);
                  target.setFallDistance(0.0F);
               }
            }
         }
      } else {
         this.abortRide(ride);
      }
   }

   private void abortRide(GrabManager.Ride ride) {
      Bukkit.getScheduler().cancelTask(ride.taskId);
      this.rides.remove(ride.targetId);
      if (ride.arrow.isValid()) {
         ride.arrow.remove();
      }
   }

   private void finalizeRide(GrabManager.Ride ride, Location hit) {
      Bukkit.getScheduler().cancelTask(ride.taskId);
      this.rides.remove(ride.targetId);
      if (ride.arrow.isValid()) {
         ride.arrow.remove();
      }

      Player target = Bukkit.getPlayer(ride.targetId);
      if (target != null && target.isOnline() && !target.isDead()) {
         UUID tid = target.getUniqueId();
         this.fallGrace.add(tid);
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.fallGrace.remove(tid), 200L);
         boolean hasTotem = this.isTotem(target.getInventory().getItemInMainHand()) || this.isTotem(target.getInventory().getItemInOffHand());
         if (hasTotem) {
            double fallHeight = ride.maxY - hit.getY();
            boolean bigFall = fallHeight >= 8.0;
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               if (target.isOnline() && !target.isDead()) {
                  if (bigFall) {
                     this.playMaceSmashEffect(target.getLocation());
                  }

                  target.damage(2000.0);
               }
            });
         }
      }
   }

   private void playMaceSmashEffect(Location loc) {
      World world = loc.getWorld();
      if (world != null) {
         Location c = loc.clone().add(0.0, 0.2, 0.0);
         world.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 0.6F, 1.2F);
         world.spawnParticle(Particle.EXPLOSION_NORMAL, c, 8, 0.4, 0.1, 0.4, 0.05);
      }
   }

   private boolean isTotem(ItemStack stack) {
      return stack != null && stack.getType() == Material.TOTEM_OF_UNDYING;
   }

   public void releaseAll() {
      for (UUID id : this.grabs.keySet().toArray(new UUID[0])) {
         this.cancelGrab(Bukkit.getPlayer(id));
      }

      for (UUID tid : this.rides.keySet().toArray(new UUID[0])) {
         GrabManager.Ride r = this.rides.get(tid);
         if (r != null) {
            this.abortRide(r);
         }
      }

      this.fallGrace.clear();
   }

   public void onQuit(UUID playerId) {
      Player p = Bukkit.getPlayer(playerId);
      if (p != null && this.grabs.containsKey(playerId)) {
         this.cancelGrab(p);
      }

      GrabManager.Ride r = this.rides.get(playerId);
      if (r != null) {
         this.abortRide(r);
      }

      this.fallGrace.remove(playerId);
   }

   private void removeOneArrow(Player p) {
      if (p != null) {
         ItemStack[] contents = p.getInventory().getContents();

         for (int i = 0; i < contents.length; i++) {
            ItemStack s = contents[i];
            if (s != null && s.getType() == Material.ARROW) {
               if (s.getAmount() <= 1) {
                  contents[i] = null;
               } else {
                  s.setAmount(s.getAmount() - 1);
               }
               break;
            }
         }

         p.getInventory().setContents(contents);
      }
   }

   private Player findTarget(Player shooter, double radius) {
      Vector origin = shooter.getEyeLocation().toVector();
      Vector dir = shooter.getEyeLocation().getDirection().normalize();
      Player best = null;
      double bestDot = -1.0;
      double bestDist = radius;

      for (Player other : shooter.getWorld().getPlayers()) {
         if (!other.getUniqueId().equals(shooter.getUniqueId()) && !other.isDead() && other.isOnline()) {
            Vector to = other.getLocation().add(0.0, 1.0, 0.0).toVector().subtract(origin);
            double dist = to.length();
            if (!(dist > radius) && !(dist < 0.01)) {
               double dot = to.clone().normalize().dot(dir);
               if (!(dot < 0.9) && (dot > bestDot || dot > 0.97 && dist < bestDist)) {
                  bestDot = dot;
                  bestDist = dist;
                  best = other;
               }
            }
         }
      }

      return best;
   }

   private static final class Grab {
      final UUID shooterId;
      final UUID targetId;
      final boolean tempArrow;
      final float prevTargetWalkSpeed;
      final boolean prevShooterAllowFlight;
      int taskId;

      Grab(UUID shooterId, UUID targetId, boolean tempArrow, float prevTargetWalkSpeed, boolean prevShooterAllowFlight) {
         this.shooterId = shooterId;
         this.targetId = targetId;
         this.tempArrow = tempArrow;
         this.prevTargetWalkSpeed = prevTargetWalkSpeed;
         this.prevShooterAllowFlight = prevShooterAllowFlight;
      }
   }

   private static final class Ride {
      final Arrow arrow;
      final UUID targetId;
      final Location start;
      final Vector velocity;
      Vector pos;
      int ticks;
      int taskId;
      double maxY;

      Ride(Arrow arrow, UUID targetId, Location start, Vector velocity) {
         this.arrow = arrow;
         this.targetId = targetId;
         this.start = start.clone();
         this.velocity = velocity.clone();
         this.pos = start.toVector();
         this.maxY = start.getY();
      }
   }
}
