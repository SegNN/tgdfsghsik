package ua.fichadev.fichacases.animation;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ua.fichadev.fichacases.animation.entity.AnimationStand;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.reward.Reward;

public class SpinningAnimation implements Animation {
   private Plugin plugin;
   private double liftHeight;
   private int liftDuration;
   private int spinDuration;
   private int changeInterval;
   private int dropDuration;
   private int dropDurationAfterWin;
   private Sound liftSound;
   private Sound dropSound;

   public SpinningAnimation(
      Plugin plugin, double liftHeight, int liftDuration, int spinDuration, int changeInterval, int dropDuration, Sound liftSound, Sound dropSound
   ) {
      this.plugin = plugin;
      this.liftHeight = liftHeight;
      this.liftDuration = liftDuration;
      this.spinDuration = spinDuration;
      this.changeInterval = changeInterval;
      this.dropDuration = dropDuration;
      this.dropDurationAfterWin = 30;
      this.liftSound = liftSound;
      this.dropSound = dropSound;
   }

   @Override
   public void animate(final GameCase gameCase) {
      Block caseBlock = gameCase.getLocation().getBlock();
      (new BukkitRunnable() {
         public void run() {
            if (!gameCase.isOpening()) {
               this.cancel();
            } else {
               for (Entity nearbyEntity : gameCase.getLocation().getWorld().getNearbyEntities(gameCase.getLocation(), 3.3, 3.3, 3.3)) {
                  if (nearbyEntity.getType() == EntityType.PLAYER) {
                     Vector vector = nearbyEntity.getLocation().toVector().subtract(gameCase.getLocation().toVector()).normalize().multiply(0.7);
                     nearbyEntity.setVelocity(vector);
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 5L);
      if (caseBlock.getType() == Material.RESPAWN_ANCHOR) {
         RespawnAnchor anchor = (RespawnAnchor)caseBlock.getBlockData();
         if (anchor.getCharges() < 1) {
            anchor.setCharges(1);
            caseBlock.setBlockData(anchor);
         }

         this.chargeRespawnAnchor(caseBlock, 1, 4, () -> this.startAnimation(gameCase));
      } else {
         this.startAnimation(gameCase);
      }
   }

   private void startAnimation(final GameCase gameCase) {
      Location caseLocation = gameCase.getLocation();
      final List<Reward> rewards = gameCase.getRewards();
      final Reward winnerReward = gameCase.getRandomReward();
      final Location pivot = caseLocation.clone().add(0.5, 0.6, 0.5);
      final AnimationStand animationStand = new AnimationStand();
      animationStand.spawn(pivot);
      animationStand.setItem(rewards.get(0).getIcon());
      (new BukkitRunnable() {
         int counter = 0;
         int rewardCounter = 0;

         public void run() {
            if (this.counter >= SpinningAnimation.this.liftDuration) {
               this.cancel();
               animationStand.setItem(winnerReward.getIcon());
               animationStand.setName(winnerReward.getName());
               animationStand.getWorld().playSound(animationStand.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
               SpinningAnimation.this.dropStand(animationStand, winnerReward, pivot, gameCase, SpinningAnimation.this.dropDurationAfterWin);
               Firework firework = (Firework)animationStand.getWorld().spawn(animationStand.getLocation(), Firework.class);
               FireworkMeta fireworkMeta = firework.getFireworkMeta();
               fireworkMeta.setPower(0);
               firework.setFireworkMeta(fireworkMeta);
               firework.detonate();
               animationStand.getWorld().playSound(animationStand.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
            } else {
               Reward newReward = rewards.get(this.rewardCounter % rewards.size());
               animationStand.setItem(newReward.getIcon());
               animationStand.setName(newReward.getName());
               animationStand.getWorld().playSound(animationStand.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
               this.rewardCounter++;
               Location newLocation = pivot.clone().add(0.0, SpinningAnimation.this.liftHeight * this.counter / SpinningAnimation.this.liftDuration, 0.0);
               newLocation.setYaw(animationStand.getLocation().getYaw() + 360.0F / SpinningAnimation.this.liftDuration * 2.0F);
               animationStand.teleport(newLocation);
               this.counter++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 2L);
   }

   private void dropStand(final AnimationStand animationStand, final Reward winnerReward, final Location pivot, final GameCase gameCase, final int dropDuration) {
      (new BukkitRunnable() {
            int counter = 0;

            public void run() {
               if (this.counter >= dropDuration) {
                  this.cancel();
                  animationStand.remove();
                  gameCase.givePrize(gameCase.getOpening(), winnerReward);
                  Block caseBlock = gameCase.getLocation().getBlock();
                  if (caseBlock.getType() == Material.RESPAWN_ANCHOR) {
                     SpinningAnimation.this.drainRespawnAnchor(caseBlock, 4, 1, gameCase::onOpenEnd);
                  } else {
                     gameCase.onOpenEnd();
                  }
               } else {
                  animationStand.teleport(
                     pivot.clone().add(0.0, SpinningAnimation.this.liftHeight - SpinningAnimation.this.liftHeight * this.counter / dropDuration, 0.0)
                  );
                  this.counter++;
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private void chargeRespawnAnchor(final Block block, final int fromLevel, final int toLevel, final Runnable callback) {
      (new BukkitRunnable() {
         int level = fromLevel;

         public void run() {
            if (this.level > toLevel) {
               this.cancel();
               callback.run();
            } else {
               SpinningAnimation.this.setRespawnAnchorLevel(block, this.level);
               block.getWorld().playSound(block.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0F, 1.0F);
               this.level++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 10L);
   }

   private void drainRespawnAnchor(final Block block, final int fromLevel, final int toLevel, final Runnable callback) {
      (new BukkitRunnable() {
         int level = fromLevel;

         public void run() {
            if (this.level < toLevel) {
               this.cancel();
               callback.run();
            } else {
               SpinningAnimation.this.setRespawnAnchorLevel(block, this.level);
               block.getWorld().playSound(block.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
               this.level--;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 10L);
   }

   private void setRespawnAnchorLevel(Block block, int level) {
      if (block.getType() == Material.RESPAWN_ANCHOR) {
         RespawnAnchor anchor = (RespawnAnchor)block.getBlockData();
         anchor.setCharges(level);
         block.setBlockData(anchor);
      }
   }
}
