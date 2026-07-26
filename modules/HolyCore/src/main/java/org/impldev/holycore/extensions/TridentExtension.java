package org.impldev.holycore.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class TridentExtension implements Listener {
   private static final int TRIDENT_THROW_COOLDOWN = 40;
   private static final int TRIDENT_HIT_COOLDOWN = 100;
   private static final double DAMAGE_PER_BLOCK = 0.5;
   private static final double MIN_HEALTH = 2.0;
   private final HashMap<Trident, Location> tridents = new HashMap<>();

   public TridentExtension() {
   }

   @EventHandler
   public void onLaunch(ProjectileLaunchEvent event) {
      if (event.getEntity() instanceof Trident) {
         if (event.getEntity().getShooter() instanceof Player) {
            Trident trident = (Trident)event.getEntity();
            Player shooter = (Player)trident.getShooter();
            if (shooter.hasCooldown(Material.TRIDENT)) {
               event.setCancelled(true);
            } else {
               shooter.setCooldown(Material.TRIDENT, 40);
               this.tridents.put(trident, shooter.getLocation());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onTridentDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Trident) {
         if (event.getEntity() instanceof Player) {
            Trident trident = (Trident)event.getDamager();
            Player victim = (Player)event.getEntity();
            if (!(trident.getShooter() instanceof Player)) {
               event.setCancelled(true);
               this.tridents.remove(trident);
            } else {
               Player shooter = (Player)trident.getShooter();
               Location launchLocation = this.tridents.get(trident);
               if (launchLocation == null) {
                  launchLocation = shooter.getLocation();
               }

               if (this.canPvp(launchLocation) && this.canPvp(victim.getLocation()) && this.canPvp(shooter.getLocation())) {
                  double distance = victim.getLocation().distance(launchLocation);
                  double damage = distance * 0.5;
                  double maxDamage = victim.getHealth() - 2.0;
                  if (maxDamage < 0.0) {
                     maxDamage = 0.0;
                  }

                  if (damage > maxDamage) {
                     damage = maxDamage;
                  }

                  event.setDamage(damage);
                  String cmd = "updatepvp " + victim.getName() + " " + shooter.getName();
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                  short durabilityDamage = (short)(damage * 0.85);
                  ItemStack item = trident.getItem().clone();
                  item.setDurability((short)(item.getDurability() + durabilityDamage));
                  if (item.getDurability() > item.getType().getMaxDurability()) {
                     trident.remove();
                  } else {
                     trident.setItem(item);
                  }

                  shooter.setCooldown(Material.TRIDENT, 100);
                  this.tridents.remove(trident);
               } else {
                  event.setCancelled(true);
                  this.tridents.remove(trident);
               }
            }
         }
      }
   }

   @EventHandler
   public void onHit(ProjectileHitEvent event) {
      if (event.getEntity() instanceof Trident) {
         if (!(event.getHitEntity() instanceof Player)) {
            this.tridents.remove((Trident)event.getEntity());
         }
      }
   }

   private boolean canPvp(Location location) {
      try {
         RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
         RegionQuery query = container.createQuery();
         State state = query.queryState(BukkitAdapter.adapt(location), Associables.constant(Association.NON_MEMBER), new StateFlag[]{Flags.PVP});
         return state != State.DENY;
      } catch (Throwable var5) {
         return true;
      }
   }
}
