package org.dimasik.holyenchantments.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.enchantscore.EnchantsMap;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;
import org.dimasik.holyenchantments.enchantscore.levels.DoubleLevel;
import org.dimasik.holyenchantments.utils.EncoderSound;

public class DamageHandlers implements Listener {
   private final EnchantsMap enchantsMap;

   public DamageHandlers(EnchantsMap enchantsMap) {
      this.enchantsMap = enchantsMap;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onDamage(EntityDamageByEntityEvent e) {
      if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player target) {
         ItemStack itemStack = damager.getInventory().getItemInMainHand();
         if (itemStack.hasItemMeta()) {
            PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
            if (!persistentDataContainer.isEmpty()) {
               if (persistentDataContainer.has(this.enchantsMap.critical.getNamespacedKey(), PersistentDataType.INTEGER)) {
                  AbstractLevel level = this.enchantsMap.critical.getLevel(persistentDataContainer);
                  if (this.isCriticalHit(damager) && level != null && new Random().nextInt(100) < level.getChance()) {
                     EncoderSound.playSound(damager, this.enchantsMap.critical.getSoundAlone());
                     EncoderSound.playSound(damager.getLocation(), this.enchantsMap.critical.getSoundAround());
                     e.setDamage(e.getDamage() * ((DoubleLevel)level).getValue());
                  }
               }

               if (persistentDataContainer.has(this.enchantsMap.destroyer.getNamespacedKey(), PersistentDataType.INTEGER)) {
                  AbstractLevel level = this.enchantsMap.destroyer.getLevel(persistentDataContainer);
                  if (this.isCriticalHit(damager) && level != null && new Random().nextInt(100) <= level.getChance()) {
                     EncoderSound.playSound(damager, this.enchantsMap.destroyer.getSoundAlone());
                     EncoderSound.playSound(damager.getLocation(), this.enchantsMap.destroyer.getSoundAround());
                     EntityEquipment ee = target.getEquipment();
                     if (ee != null) {
                        double damageBoost = (((DoubleLevel)level).getValue() - 1.0) * e.getDamage();

                        for (EquipmentSlot equipmentSlot : Arrays.asList(EquipmentSlot.FEET, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.LEGS)) {
                           ItemStack item = target.getInventory().getItem(equipmentSlot);
                           if (item != null && item.getItemMeta() != null && item.getItemMeta() instanceof Damageable) {
                              item.setDurability((short)(item.getDurability() + damageBoost));
                              if (itemStack.getDurability() > itemStack.getType().getMaxDurability()) {
                                 target.getInventory().setItem(equipmentSlot, null);
                              } else {
                                 target.getInventory().setItem(equipmentSlot, itemStack);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean isCriticalHit(Player player) {
      return player != null && !player.isOnGround() && !player.isInWater() && player.getFallDistance() > 0.0F && player.getVelocity().getY() < 0.0;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onDamage1(EntityDamageByEntityEvent e) {
      if (e.getDamager() instanceof Player damager) {
         Entity var10 = e.getEntity();
         if (var10 instanceof Player) {
            Player target = (Player)e.getEntity();
            int chance = 0;

            for (int i = 36; i < 40; i++) {
               ItemStack itemStack = target.getInventory().getItem(i);
               if (itemStack != null && itemStack.hasItemMeta()) {
                  PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
                  if (!persistentDataContainer.isEmpty()
                     && persistentDataContainer.has(this.enchantsMap.impenetrable.getNamespacedKey(), PersistentDataType.INTEGER)) {
                     DoubleLevel intLevel = (DoubleLevel)this.enchantsMap.impenetrable.getLevel(persistentDataContainer);
                     if (intLevel != null && new Random().nextInt(100) <= intLevel.getChance()) {
                        EncoderSound.playSound(damager, this.enchantsMap.impenetrable.getSoundAlone());
                        EncoderSound.playSound(damager.getLocation(), this.enchantsMap.impenetrable.getSoundAround());
                        e.setDamage(e.getDamage() * intLevel.getValue());
                     }
                  }
               }
            }

            return;
         }
      }
   }

   @EventHandler
   public void onDeath(EntityDeathEvent event) {
      if (event.getEntity().getKiller() != null) {
         if (!(event.getEntity() instanceof Player)) {
            Player player = event.getEntity().getKiller();
            if (player.getItemInHand() != null) {
               if (player.getItemInHand().getItemMeta() != null) {
                  int boost = (Integer)player.getItemInHand()
                     .getItemMeta()
                     .getPersistentDataContainer()
                     .getOrDefault(this.enchantsMap.farmer.getNamespacedKey(), PersistentDataType.INTEGER, 1);
                  event.setDroppedExp(event.getDroppedExp() * boost);
                  if (player.getItemInHand()
                     .getItemMeta()
                     .getPersistentDataContainer()
                     .has(this.enchantsMap.filter.getNamespacedKey(), PersistentDataType.INTEGER)) {
                     List<ItemStack> drops = new ArrayList<>();

                     for (ItemStack itemStack : event.getDrops()) {
                        if (itemStack.getType() == Material.TOTEM_OF_UNDYING) {
                           drops.add(itemStack);
                        } else if (itemStack.getType().getMaxStackSize() != 1) {
                           drops.add(itemStack);
                        }
                     }

                     event.getDrops().clear();

                     for (ItemStack itemStackx : drops) {
                        event.getDrops().add(itemStackx);
                     }
                  }
               }
            }
         }
      }
   }
}
