package ua.fichadev.fichabambuk.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ua.fichadev.fichabambuk.item.BambukItem;
import ua.fichadev.fichabambuk.manager.GrabManager;

public final class BambukListener implements Listener {
   private final Plugin plugin;
   private final BambukItem item;
   private final GrabManager grabManager;

   public BambukListener(Plugin plugin, BambukItem item, GrabManager grabManager) {
      this.plugin = plugin;
      this.item = item;
      this.grabManager = grabManager;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent e) {
      if (e.getHand() == EquipmentSlot.HAND) {
         if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = e.getPlayer();
            ItemStack held = p.getInventory().getItemInMainHand();
            if (this.item.matches(held)) {
               if (p.hasPermission("fichabambuk.use")) {
                  this.grabManager.start(p);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onShoot(EntityShootBowEvent e) {
      if (e.getEntity() instanceof Player) {
         if (this.item.matches(e.getBow())) {
            Player shooter = (Player)e.getEntity();
            if (shooter.hasPermission("fichabambuk.use")) {
               e.setCancelled(true);
               if (e.getProjectile() != null) {
                  e.getProjectile().remove();
               }

               Bukkit.getScheduler().runTask(this.plugin, () -> this.grabManager.release(shooter));
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.grabManager.onQuit(e.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onItemHeld(PlayerItemHeldEvent e) {
      if (this.grabManager.isGrabbing(e.getPlayer().getUniqueId())) {
         ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
         if (!this.item.matches(newItem)) {
            this.grabManager.cancelGrab(e.getPlayer());
         }
      }
   }

   @EventHandler
   public void onDrop(PlayerDropItemEvent e) {
      if (this.grabManager.isGrabbing(e.getPlayer().getUniqueId())) {
         if (this.item.matches(e.getItemDrop().getItemStack())) {
            this.grabManager.cancelGrab(e.getPlayer());
         }
      }
   }

   @EventHandler
   public void onSwap(PlayerSwapHandItemsEvent e) {
      if (this.grabManager.isGrabbing(e.getPlayer().getUniqueId())) {
         if (this.item.matches(e.getMainHandItem())) {
            this.grabManager.cancelGrab(e.getPlayer());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onDamage(EntityDamageEvent e) {
      if (e.getEntity() instanceof Player) {
         Player target = (Player)e.getEntity();
         if (this.grabManager.hasFallGrace(target.getUniqueId())) {
            DamageCause c = e.getCause();
            if (c == DamageCause.FALL || c == DamageCause.FLY_INTO_WALL || c == DamageCause.SUFFOCATION) {
               e.setCancelled(true);
               if (target.isOnGround()) {
                  this.grabManager.clearFallGrace(target.getUniqueId());
               }
            }
         }
      }
   }
}
