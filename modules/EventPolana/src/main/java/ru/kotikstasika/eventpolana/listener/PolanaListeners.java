package ru.kotikstasika.eventpolana.listener;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.kotikstasika.eventpolana.EventPolana;
import ru.kotikstasika.eventpolana.config.PluginConfig;
import ru.kotikstasika.eventpolana.manager.FlowerManager;
import ru.kotikstasika.eventpolana.manager.LootManagerBridge;
import ru.kotikstasika.eventpolana.manager.PolanaManager;
import ru.kotikstasika.eventpolana.model.PolanaState;
import ru.kotikstasika.eventpolana.service.ShearsService;

public class PolanaListeners implements Listener {
   private final EventPolana plugin;
   private final PluginConfig cfg;
   private final ShearsService shearsService;
   private final LootManagerBridge lootBridge;
   private final PolanaManager polanaManager;
   private final FlowerManager flowerManager;
   private final Random random = new Random();

   public PolanaListeners(
      EventPolana plugin, PluginConfig cfg, ShearsService shearsService, LootManagerBridge lootBridge, PolanaManager polanaManager, FlowerManager flowerManager
   ) {
      this.plugin = plugin;
      this.cfg = cfg;
      this.shearsService = shearsService;
      this.lootBridge = lootBridge;
      this.polanaManager = polanaManager;
      this.flowerManager = flowerManager;
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      Villager gardener = this.polanaManager.getGardener();
      if (gardener != null && event.getEntity().equals(gardener)) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      if (this.polanaManager.getState() == PolanaState.SPAWNED) {
         Villager gardener = this.polanaManager.getGardener();
         if (gardener != null && event.getRightClicked().equals(gardener)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (this.polanaManager.isShearsTaken()) {
               this.pushPlayerAway(player);
            } else {
               player.getInventory().addItem(new ItemStack[]{this.shearsService.createCustomShears()});

               try {
                  Sound sound = Sound.valueOf(this.cfg.getSoundShearsGiven());
                  player.getWorld().playSound(player.getLocation(), sound, 1.0F, 1.0F);
               } catch (IllegalArgumentException var5) {
                  player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
               }

               this.polanaManager.setShearsTaken(true);
               int regenTime;
               switch (this.polanaManager.getRarity()) {
                  case EPIC:
                     regenTime = 20 * this.cfg.getRespawnShearsSecondsEpic();
                     break;
                  case LEGENDARY:
                     regenTime = 20 * this.cfg.getRespawnShearsSecondsLegendary();
                     break;
                  default:
                     regenTime = 20 * this.cfg.getRespawnShearsSecondsDefault();
               }

               (new BukkitRunnable() {
                  public void run() {
                     PolanaListeners.this.polanaManager.setShearsTaken(false);
                  }
               }).runTaskLater(this.plugin, regenTime);
            }
         }
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.polanaManager.getState() == PolanaState.SPAWNED && this.polanaManager.getPolanaLocation() != null) {
         Location blockLoc = event.getBlock().getLocation();
         if (blockLoc.getWorld().equals(this.polanaManager.getPolanaLocation().getWorld())) {
            try {
               if (blockLoc.distance(this.polanaManager.getPolanaLocation()) <= 35.0 && FlowerManager.isFlower(event.getBlock().getType())) {
                  Player player = event.getPlayer();
                  ItemStack tool = player.getInventory().getItemInMainHand();
                  if (this.shearsService.isCustomShears(tool)) {
                     int durability = this.shearsService.getDurability(tool) - 1;
                     this.shearsService.setDurability(tool, durability);
                     if (durability <= 0) {
                        player.getInventory().remove(tool);
                     }

                     this.flowerManager.decreaseFlowerCount();
                     int lootLevel;
                     switch (this.polanaManager.getRarity()) {
                        case EPIC:
                           lootLevel = this.cfg.getLootLevelEpic();
                           break;
                        case LEGENDARY:
                           lootLevel = this.cfg.getLootLevelLegendary();
                           break;
                        default:
                           lootLevel = this.cfg.getLootLevelDefault();
                     }

                     this.lootBridge.dropRandomLoot(blockLoc, lootLevel);

                     try {
                        Particle particle = Particle.valueOf(this.cfg.getParticleFlowerBreakType());
                        blockLoc.getWorld()
                           .spawnParticle(
                              particle,
                              blockLoc.clone().add(0.5, 0.5, 0.5),
                              this.cfg.getParticleFlowerBreakCount(),
                              this.cfg.getParticleFlowerBreakOffsetX(),
                              this.cfg.getParticleFlowerBreakOffsetY(),
                              this.cfg.getParticleFlowerBreakOffsetZ()
                           );
                     } catch (IllegalArgumentException var9) {
                        blockLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, blockLoc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3);
                     }

                     try {
                        Sound sound = Sound.valueOf(this.cfg.getSoundFlowerBreak());
                        blockLoc.getWorld().playSound(blockLoc, sound, 1.0F, 1.5F);
                     } catch (IllegalArgumentException var8) {
                        blockLoc.getWorld().playSound(blockLoc, Sound.BLOCK_GRASS_BREAK, 1.0F, 1.5F);
                     }

                     event.getBlock().setType(Material.AIR);
                  } else {
                     event.setCancelled(true);
                  }
               }
            } catch (IllegalArgumentException var10) {
            }
         }
      }
   }

   private void pushPlayerAway(Player player) {
      Villager gardener = this.polanaManager.getGardener();
      if (gardener != null) {
         Vector velocity = player.getLocation().toVector().subtract(gardener.getLocation().toVector()).normalize().multiply(5 + this.random.nextInt(6));
         velocity.setY(0.1);
         player.setVelocity(velocity);
         player.damage(8.0);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
      }
   }
}
