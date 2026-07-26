package ru.kotikstasika.kotiktotemrune.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TotemUseListener implements Listener {
   private final JavaPlugin plugin;
   private final NamespacedKey immortalityKey;
   private final NamespacedKey recoveryKey;

   public TotemUseListener(JavaPlugin plugin) {
      this.plugin = plugin;
      this.immortalityKey = new NamespacedKey(plugin, "bessmertie");
      this.recoveryKey = new NamespacedKey(plugin, "vosstanovlenie");
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onTotemActivate(EntityResurrectEvent event) {
      if (event.getEntity() instanceof Player) {
         final Player player = (Player)event.getEntity();
         ItemStack totem = this.getUsedTotem(player);
         if (totem != null) {
            ItemMeta meta = totem.getItemMeta();
            if (meta != null) {
               if (!player.hasMetadata("totemCooldown")) {
                  player.setMetadata("totemCooldown", new FixedMetadataValue(this.plugin, true));
                  boolean hasRecovery = meta.getPersistentDataContainer().has(this.recoveryKey, PersistentDataType.INTEGER);
                  boolean hasImmortality = meta.getPersistentDataContainer().has(this.immortalityKey, PersistentDataType.INTEGER);
                  if (hasRecovery) {
                     (new BukkitRunnable() {
                           public void run() {
                              if (player.isOnline() && !player.isDead()) {
                                 player.setHealth(player.getMaxHealth());
                                 player.setFoodLevel(20);
                                 player.setSaturation(20.0F);

                                 for (PotionEffect effect : player.getActivePotionEffects()) {
                                    if (effect.getType().equals(PotionEffectType.POISON)
                                       || effect.getType().equals(PotionEffectType.WITHER)
                                       || effect.getType().equals(PotionEffectType.HUNGER)
                                       || effect.getType().equals(PotionEffectType.WEAKNESS)) {
                                       player.removePotionEffect(effect.getType());
                                    }
                                 }
                              }
                           }
                        })
                        .runTaskLater(this.plugin, 1L);
                  }

                  if (hasImmortality) {
                     player.setInvulnerable(true);
                     (new BukkitRunnable() {
                        public void run() {
                           if (player.isOnline()) {
                              player.setInvulnerable(false);
                           }
                        }
                     }).runTaskLater(this.plugin, 60L);
                  }

                  (new BukkitRunnable() {
                     public void run() {
                        player.removeMetadata("totemCooldown", TotemUseListener.this.plugin);
                     }
                  }).runTaskLater(this.plugin, 5L);
               }
            }
         }
      }
   }

   private ItemStack getUsedTotem(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      if (mainHand != null && mainHand.getType() == Material.TOTEM_OF_UNDYING) {
         return mainHand;
      } else {
         return offHand != null && offHand.getType() == Material.TOTEM_OF_UNDYING ? offHand : null;
      }
   }
}
