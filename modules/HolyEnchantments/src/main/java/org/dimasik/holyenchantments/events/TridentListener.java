package org.dimasik.holyenchantments.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.EnchantsMap;
import org.dimasik.holyenchantments.enchantscore.levels.DoubleLevel;

public class TridentListener implements Listener {
   private final EnchantsMap enchantsMap;
   private final HashMap<UUID, BukkitTask> tridents = new HashMap<>();
   private final HashMap<Trident, Location> tridentsLocations = new HashMap<>();

   public TridentListener(EnchantsMap enchantsMap) {
      this.enchantsMap = enchantsMap;
   }

   @EventHandler
   public void onLaunch(ProjectileLaunchEvent event) {
      if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof Trident) {
         final Trident trident = (Trident)event.getEntity();
         this.tridentsLocations.put(trident, event.getLocation());
         ItemStack itemStack = trident.getItem();
         PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
         if (persistentDataContainer.has(this.enchantsMap.aim.getNamespacedKey(), PersistentDataType.INTEGER)) {
            final DoubleLevel doubleLevel = (DoubleLevel)this.enchantsMap.aim.getLevel(persistentDataContainer);
            this.tridents.put(trident.getUniqueId(), (new BukkitRunnable() {
               final boolean[] isFirstAdjustment = new boolean[]{true};

               public void run() {
                  if (!trident.isDead() && !trident.isOnGround()) {
                     Player nearestPlayer = TridentListener.this.findNearestPlayer(trident, doubleLevel.getValue());
                     if (nearestPlayer != null) {
                        TridentListener.this.adjustTridentDirection(trident, nearestPlayer, this.isFirstAdjustment);
                     }
                  } else {
                     this.cancel();
                  }
               }
            }).runTaskTimer(HolyEnchantments.getInstance(), 0L, 3L));
         }
      }
   }

   @EventHandler
   public void on(ProjectileHitEvent event) {
      if (!event.isCancelled()) {
         if (event.getEntity() instanceof Trident) {
            Trident trident = (Trident)event.getEntity();
            if (this.tridents.containsKey(trident.getUniqueId())) {
               BukkitTask task = this.tridents.remove(trident.getUniqueId());
               if (task != null && !task.isCancelled()) {
                  task.cancel();
               }
            }

            Location location = this.tridentsLocations.remove(trident);
            if (location == null) {
               return;
            }

            if (!this.canPvp(location)) {
               return;
            }

            if (event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
               Player player = (Player)event.getHitEntity();
               if (trident.getShooter() instanceof Player) {
                  if (!this.canPvp(player.getLocation()) || !this.canPvp(((Player)trident.getShooter()).getLocation())) {
                     return;
                  }

                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + player.getName() + " " + ((Player)trident.getShooter()).getName());
                  double damagePlayer = player.getLocation().distance(location) * 4.0;
                  player.damage(damagePlayer);
                  short damageItem = (short)(damagePlayer * 0.5);
                  ItemStack itemStack = trident.getItem().clone();
                  itemStack.setDurability((short)(itemStack.getDurability() + damageItem));
                  if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                     trident.remove();
                  } else {
                     trident.setItem(itemStack);
                  }
               }
            }
         }
      }
   }

   private Player findNearestPlayer(Trident trident, double radius) {
      Player nearestPlayer = null;
      double nearestDistance = radius * radius;
      Player shooter = (Player)trident.getShooter();

      for (Player player : trident.getWorld().getPlayers()) {
         if (!player.equals(shooter) && !player.isDead() && player.isValid() && player.getGameMode() == GameMode.SURVIVAL) {
            Location playerLoc = player.getEyeLocation();
            double distanceSquared = playerLoc.distanceSquared(trident.getLocation());
            if (!(distanceSquared > radius * radius)
               && this.hasLineOfSight(trident.getLocation(), playerLoc)
               && (nearestPlayer == null || distanceSquared < nearestDistance)) {
               nearestDistance = distanceSquared;
               nearestPlayer = player;
            }
         }
      }

      return nearestPlayer;
   }

   private boolean hasLineOfSight(Location from, Location to) {
      World world = from.getWorld();
      Vector direction = to.toVector().subtract(from.toVector());
      double distance = direction.length();
      if (distance < 2.0) {
         return true;
      } else {
         direction.normalize();

         for (double d = 0.0; d <= distance; d += 0.5) {
            Location checkLoc = from.clone().add(direction.clone().multiply(d));
            Material type = world.getBlockAt(checkLoc).getType();
            if (type.isSolid() && type != Material.WATER && type != Material.LAVA) {
               return false;
            }
         }

         return true;
      }
   }

   private void adjustTridentDirection(Trident trident, Player target, boolean[] isFirstAdjustment) {
      Location tridentLoc = trident.getLocation();
      Location targetLoc = target.getEyeLocation();
      double distance = targetLoc.distance(tridentLoc);
      Vector currentVelocity = trident.getVelocity();
      double speed = currentVelocity.length();
      Vector targetDirection = targetLoc.toVector().subtract(tridentLoc.toVector()).normalize();
      Vector currentDirection = currentVelocity.normalize();
      Vector finalDirection;
      if (distance > 1.5) {
         finalDirection = currentDirection.multiply(0.66).add(targetDirection.multiply(0.66)).normalize();
      } else {
         finalDirection = targetDirection;
      }

      if (isFirstAdjustment[0]) {
         speed *= 0.2;
         isFirstAdjustment[0] = false;
      }

      Vector newVelocity = finalDirection.multiply(speed);
      tridentLoc.setDirection(finalDirection);
      trident.setVelocity(newVelocity);
   }

   private boolean canPvp(Location location) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      Collection<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
      ProtectedRegion highestPriorityRegion = null;

      for (ProtectedRegion region : regions) {
         if (highestPriorityRegion == null || region.getPriority() > highestPriorityRegion.getPriority()) {
            highestPriorityRegion = region;
         }
      }

      if (highestPriorityRegion == null) {
         return true;
      } else {
         StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("pvp");
         if (flag == null) {
            return true;
         } else {
            State flagState = (State)highestPriorityRegion.getFlag(flag);
            return flagState == null || flagState == State.ALLOW;
         }
      }
   }
}
