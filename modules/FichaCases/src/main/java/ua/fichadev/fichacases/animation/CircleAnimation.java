package ua.fichadev.fichacases.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockAction;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ua.fichadev.fichacases.animation.entity.AnimationStand;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.reward.Reward;

public class CircleAnimation implements Animation {
   private Plugin plugin;
   private boolean randomRewards;
   private double rewardRadius;
   private int animationStartDelay = 20;
   private int animationStartInterval = 20;
   private int showOpenUpTicks;
   private double showOpenAnglePerTick;
   private double showSpinStartAnglePerTick = 10.0;
   private double showSpinMinimalAnglePerTick = 2.0;
   private int showSpinMinSlowdownTime = 100;
   private int showSpinMaxSlowdownTime = 200;
   private int showSpinMinIdle = 40;
   private int showSpinMaxIdle = 80;
   private int animationEndDelay = 20;
   private int showEndDownTicks = 40;
   private double showEndAnglePerTick = 6.0;
   private Sound chestOpenSound;
   private Sound chestCloseSound;
   private List<AnimationStand> animationStands;

   public CircleAnimation(
      Plugin plugin,
      boolean randomRewards,
      double rewardRadius,
      int animationStartDelay,
      int animationStartInterval,
      int showOpenUpTicks,
      double showOpenAnglePerTick,
      double showSpinStartAnglePerTick,
      double showSpinMinimalAnglePerTick,
      int showSpinMinSlowdownTime,
      int showSpinMaxSlowdownTime,
      int showSpinMinIdle,
      int showSpinMaxIdle,
      int animationEndDelay,
      int showEndDownTicks,
      double showEndAnglePerTick,
      Sound chestOpenSound,
      Sound chestCloseSound
   ) {
      this.plugin = plugin;
      this.randomRewards = randomRewards;
      this.rewardRadius = rewardRadius + 0.5;
      this.animationStartDelay = animationStartDelay;
      this.animationStartInterval = animationStartInterval;
      this.showOpenUpTicks = showOpenUpTicks;
      this.showOpenAnglePerTick = showOpenAnglePerTick;
      this.showSpinStartAnglePerTick = showSpinStartAnglePerTick;
      this.showSpinMinimalAnglePerTick = showSpinMinimalAnglePerTick;
      this.showSpinMinSlowdownTime = showSpinMinSlowdownTime;
      this.showSpinMaxSlowdownTime = showSpinMaxSlowdownTime;
      this.showSpinMinIdle = showSpinMinIdle;
      this.showSpinMaxIdle = showSpinMaxIdle;
      this.animationEndDelay = animationEndDelay;
      this.showEndDownTicks = showEndDownTicks;
      this.showEndAnglePerTick = showEndAnglePerTick;
      this.chestCloseSound = chestCloseSound;
      this.chestOpenSound = chestOpenSound;
   }

   @Override
   public void animate(final GameCase gameCase) {
      final Location caseLocation = gameCase.getLocation();
      List<Reward> rewards = this.getRewards(gameCase);
      final Reward winnerReward = gameCase.getRandomReward();
      float caseYaw = this.directionToYaw(gameCase.getDirection());
      final Location pivot = caseLocation.clone().add(0.5, 0.6, 0.5);
      pivot.setYaw(caseYaw);
      this.animationStands = this.spawnStands(rewards, pivot);
      caseLocation.getWorld().playSound(caseLocation, this.chestOpenSound, 1.0F, 1.0F);
      final BlockFace direction = gameCase.getDirection();
      final double rewardsAngle = 360.0 / this.animationStands.size();
      this.sendChestAnimation(pivot.getBlock(), true);
      (new BukkitRunnable() {
         int counter = 0;

         public void run() {
            if (this.counter >= CircleAnimation.this.animationStands.size()) {
               this.cancel();
            } else {
               AnimationStand animationStand = CircleAnimation.this.animationStands.get(this.counter);
               double offset = (this.counter + 1) * rewardsAngle;
               CircleAnimation.this.showOpen(animationStand, offset, CircleAnimation.this.showOpenUpTicks, pivot, direction);
               this.counter++;
            }
         }
      }).runTaskTimer(this.plugin, this.animationStartDelay, this.animationStartInterval);
      final int showOpenDurationTicks = this.animationStartDelay + this.showOpenUpTicks + this.animationStartInterval * this.animationStands.size();
      (new BukkitRunnable() {
         int counter = showOpenDurationTicks;

         public void run() {
            caseLocation.getWorld().spawnParticle(Particle.WITCH, pivot.clone().add(0.0, 0.2, 0.0), 5);
            this.counter--;
            if (this.counter < 0) {
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
      final AnimationStand sword = this.createSword(pivot, direction);
      (new BukkitRunnable() {
            public void run() {
               int showSpinTicksRotate = CircleAnimation.this.showSpinMinSlowdownTime
                  + new Random().nextInt(CircleAnimation.this.showSpinMaxSlowdownTime - CircleAnimation.this.showSpinMinSlowdownTime + 1);
               int idleTicks = CircleAnimation.this.showSpinMinIdle
                  + new Random().nextInt(CircleAnimation.this.showSpinMaxIdle - CircleAnimation.this.showSpinMinIdle + 1);
               CircleAnimation.this.sendChestAnimation(pivot.getBlock(), false);
               pivot.getWorld().playSound(pivot, CircleAnimation.this.chestCloseSound, 1.0F, 1.0F);
               CircleAnimation.this.showSpin(
                  CircleAnimation.this.animationStands, showSpinTicksRotate, idleTicks, pivot, direction, sword, winnerReward, gameCase
               );
            }
         })
         .runTaskLater(this.plugin, showOpenDurationTicks);
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
   }

   private void showOpen(final AnimationStand animationStand, final double offset, final int upTicks, final Location pivot, final BlockFace direction) {
      Reward reward = (Reward)animationStand.getData("reward");
      animationStand.setItem(reward.getIcon());
      animationStand.setName(reward.getName());
      (new BukkitRunnable() {
            int counter = 0;
            Location startUpLocation = pivot.clone();
            double upPerTick = CircleAnimation.this.rewardRadius / upTicks;
            Vector circleDirection = direction.getDirection().multiply(1.0E-6).setY(0);
            double currentAngle = 0.0;

            public void run() {
               if (this.counter < upTicks) {
                  animationStand.teleport(this.startUpLocation.add(0.0, this.upPerTick, 0.0));
               } else {
                  if (this.currentAngle <= offset - 360.0) {
                     animationStand.setData("last_angle", this.currentAngle);
                     this.cancel();
                     return;
                  }

                  this.currentAngle = -CircleAnimation.this.showOpenAnglePerTick * (this.counter - upTicks);
                  if (this.currentAngle < offset - 360.0) {
                     this.currentAngle = offset - 360.0;
                  }

                  Location loc = this.circleDirection
                     .clone()
                     .add(new Vector(0.0, CircleAnimation.this.rewardRadius, 0.0))
                     .rotateAroundAxis(this.circleDirection.clone(), -Math.toRadians(this.currentAngle))
                     .toLocation(this.startUpLocation.getWorld())
                     .add(pivot);
                  loc.setYaw(animationStand.getLocation().getYaw());
                  animationStand.teleport(loc);
               }

               this.counter++;
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private void showSpin(
      final List<AnimationStand> animationStands,
      final int slowdownTicks,
      final int idleTicks,
      final Location pivot,
      final BlockFace direction,
      final AnimationStand sword,
      final Reward winnerReward,
      final GameCase gameCase
   ) {
      (new BukkitRunnable() {
            int counter = 0;
            double anglePerTick = CircleAnimation.this.showSpinStartAnglePerTick;
            double slowdownPerTick = (this.anglePerTick - CircleAnimation.this.showSpinMinimalAnglePerTick) / slowdownTicks;
            Vector circleDirection = direction.getDirection().multiply(1.0E-6).setY(0);
            double ticksRotate = (double)(slowdownTicks + idleTicks);

            public void run() {
               if (sword.getItem().getType() == Material.AIR) {
                  sword.setItem(new ItemStack(Material.DIAMOND_SWORD));
               }

               if (this.counter > this.ticksRotate) {
                  this.cancel();
                  CircleAnimation.this.showCloser(animationStands, pivot, direction, sword, winnerReward, gameCase);
               } else {
                  animationStands.forEach(
                     animationStand -> {
                        double angle = (Double)animationStand.getData("last_angle") - this.anglePerTick;
                        if (angle <= -360.0) {
                           angle += 360.0;
                           pivot.getWorld().playSound(pivot.clone().add(0.0, CircleAnimation.this.rewardRadius, 0.0), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                        }

                        animationStand.setData("last_angle", angle);
                        Location loc = this.circleDirection
                           .clone()
                           .add(new Vector(0.0, CircleAnimation.this.rewardRadius, 0.0))
                           .rotateAroundAxis(this.circleDirection.clone(), -Math.toRadians(angle))
                           .toLocation(animationStand.getWorld());
                        loc = pivot.clone().add(loc);
                        loc.setYaw(animationStand.getLocation().getYaw());
                        animationStand.teleport(loc);
                     }
                  );
                  if (this.counter <= slowdownTicks) {
                     this.anglePerTick = this.anglePerTick - this.slowdownPerTick;
                  }

                  this.counter++;
               }
            }
         })
         .runTaskTimer(this.plugin, 5L, 1L);
   }

   private void showCloser(
      final List<AnimationStand> animationStands,
      final Location pivot,
      final BlockFace direction,
      final AnimationStand sword,
      final Reward winnerReward,
      final GameCase gameCase
   ) {
      final AnimationStand winner = animationStands.stream().filter(animationStand -> animationStand.getData("reward").equals(winnerReward)).findFirst().get();
      (new BukkitRunnable() {
            double anglePerTick = CircleAnimation.this.showSpinMinimalAnglePerTick;
            double winnerAngle = (Double)winner.getData("last_angle");
            Vector circleDirection = direction.getDirection().multiply(0.001).setY(0);

            public void run() {
               this.winnerAngle = this.winnerAngle - this.anglePerTick;
               animationStands.forEach(
                  entityReward -> {
                     double angle = (Double)entityReward.getData("last_angle") - this.anglePerTick;
                     if (angle <= -360.0) {
                        angle += 360.0;
                        pivot.getWorld().playSound(pivot.clone().add(0.0, CircleAnimation.this.rewardRadius, 0.0), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                     }

                     entityReward.setData("last_angle", angle);
                     Location loc = this.circleDirection
                        .clone()
                        .add(new Vector(0.0, CircleAnimation.this.rewardRadius, 0.0))
                        .rotateAroundAxis(this.circleDirection.clone(), -Math.toRadians(angle))
                        .toLocation(entityReward.getWorld())
                        .add(pivot);
                     loc.setYaw(entityReward.getLocation().getYaw());
                     entityReward.teleport(loc);
                  }
               );
               if (this.winnerAngle <= -360.0) {
                  this.cancel();
                  gameCase.givePrize(gameCase.getOpening(), winnerReward);
                  CircleAnimation.this.startEnd(animationStands, pivot, direction, sword, gameCase);
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private void startEnd(
      List<AnimationStand> animationStands, final Location pivot, final BlockFace direction, final AnimationStand sword, final GameCase gameCase
   ) {
      final List<AnimationStand> sorted = animationStands.stream()
         .sorted(Comparator.comparing(animationStand -> (Double)animationStand.getData("last_angle"), Comparator.reverseOrder()))
         .collect(Collectors.toList());
      (new BukkitRunnable() {
            public void run() {
               pivot.getWorld().playSound(pivot, CircleAnimation.this.chestOpenSound, 1.0F, 1.0F);

               for (int i = 0; i < sorted.size(); i++) {
                  sword.remove();
                  CircleAnimation.this.sendChestAnimation(pivot.getBlock(), true);
                  AnimationStand animationStand = sorted.get(i);
                  CircleAnimation.this.showEnd(
                     animationStand,
                     pivot,
                     CircleAnimation.this.showEndDownTicks,
                     CircleAnimation.this.showEndAnglePerTick,
                     direction,
                     i + 1 == sorted.size(),
                     gameCase
                  );
               }
            }
         })
         .runTaskLater(this.plugin, this.animationEndDelay);
   }

   private void showEnd(
      final AnimationStand animationStand,
      final Location pivot,
      final int downTicks,
      final double showCloseAnglePerTick,
      final BlockFace direction,
      final boolean isLast,
      final GameCase gameCase
   ) {
      (new BukkitRunnable() {
            int counter = 0;
            Location startDownLocation = pivot.clone().add(0.0, CircleAnimation.this.rewardRadius, 0.0);
            double downPerTick = (CircleAnimation.this.rewardRadius - 0.5) / downTicks;
            double waitTicks = 2.0;
            Vector circleDirection = direction.getDirection().multiply(0.001).setY(0);
            double currentAngle = (Double)animationStand.getData("last_angle");

            public void run() {
               if (this.currentAngle != 0.0) {
                  this.currentAngle = this.currentAngle + showCloseAnglePerTick;
                  if (this.currentAngle > 0.0) {
                     this.currentAngle = 0.0;
                  }

                  Location loc = this.circleDirection
                     .clone()
                     .add(new Vector(0.0, CircleAnimation.this.rewardRadius, 0.0))
                     .rotateAroundAxis(this.circleDirection.clone(), -Math.toRadians(this.currentAngle))
                     .toLocation(animationStand.getWorld());
                  loc = pivot.clone().add(loc);
                  animationStand.teleport(loc);
               } else {
                  if (this.counter >= downTicks - this.waitTicks) {
                     this.cancel();
                     animationStand.getWorld().playSound(pivot, Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
                     animationStand.remove();
                     if (isLast) {
                        CircleAnimation.this.sendChestAnimation(pivot.getBlock(), false);
                        (new BukkitRunnable() {
                           public void run() {
                              pivot.getWorld().playSound(pivot, CircleAnimation.this.chestCloseSound, 1.0F, 1.0F);
                           }
                        }).runTaskLater(CircleAnimation.this.plugin, 5L);

                        for (AnimationStand stand : CircleAnimation.this.animationStands) {
                           stand.remove();
                        }

                        CircleAnimation.this.animationStands.clear();
                        gameCase.onOpenEnd();
                     }

                     return;
                  }

                  if (this.waitTicks != 0.0) {
                     this.waitTicks--;
                     return;
                  }

                  animationStand.teleport(this.startDownLocation.subtract(0.0, this.downPerTick, 0.0));
                  this.counter++;
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private void sendChestAnimation(Block block, boolean open) {
      if (block.getType().name().contains("CHEST")) {
         World nmsWorld = ((CraftWorld)block.getWorld()).getHandle();
         BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
         IBlockData iBlockData = nmsWorld.getType(blockPosition);
         net.minecraft.server.v1_16_R3.Block nmsBlock = iBlockData.getBlock();
         PacketPlayOutBlockAction animationPacket = new PacketPlayOutBlockAction(blockPosition, nmsBlock, 1, open ? 1 : 0);
         block.getWorld().getPlayers().forEach(player -> {
            PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
            if (connection != null) {
               connection.sendPacket(animationPacket);
            }
         });
      }
   }

   private float directionToYaw(BlockFace direction) {
      return switch (direction) {
         case NORTH -> 180.0F;
         case EAST -> -90.0F;
         case WEST -> 90.0F;
         default -> 0.0F;
      };
   }

   private AnimationStand createSword(Location spawnLocation, BlockFace direction) {
      double xOffset = 0.0;
      double yOffset = 2.1;
      double zOffset = 0.0;
      switch (direction) {
         case NORTH:
            xOffset = -0.372;
            zOffset = -0.2;
            break;
         case EAST:
            xOffset = 0.2;
            zOffset = -0.372;
            break;
         case WEST:
            xOffset = -0.2;
            zOffset = 0.372;
            break;
         case SOUTH:
            xOffset = 0.372;
            zOffset = 0.2;
      }

      AnimationStand sword = new AnimationStand();
      sword.spawn(spawnLocation.clone().add(xOffset, yOffset, zOffset));
      sword.teleport(spawnLocation.clone().add(xOffset, yOffset, zOffset));
      sword.setHeadRotation(0.0, 0.0, -45.0);
      sword.setBaby(true);
      return sword;
   }

   private List<AnimationStand> spawnStands(List<Reward> rewards, Location pivot) {
      List<AnimationStand> animationStands = new ArrayList<>();
      rewards.forEach(reward -> {
         AnimationStand animationStand = new AnimationStand();
         animationStand.spawn(pivot);
         animationStand.teleport(pivot);
         animationStand.setData("reward", reward);
         animationStands.add(animationStand);
      });
      return animationStands;
   }

   private List<Reward> getRewards(GameCase gameCase) {
      if (!this.randomRewards) {
         return gameCase.getRewards();
      } else {
         List<Reward> result = gameCase.getRewards();
         Collections.shuffle(result);
         return result;
      }
   }
}
