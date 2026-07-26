package org.dimasik.shame.modules.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.shame.Shame;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.modules.Module;
import org.dimasik.shame.utils.Pair;
import org.dimasik.shame.utils.ParticleDataStorage;

public class BanModule extends Module {
   public static final HashSet<UUID> frozenPlayers = new HashSet<>();
   public static final HashMap<Player, BukkitTask> banTasks = new HashMap<>();
   public static final HashMap<Player, String> reasons = new HashMap<>();
   public static final HashMap<Player, Boolean> booleans = new HashMap<>();
   public static final HashMap<Player, Boolean> instant = new HashMap<>();

   public BanModule() {
      super.registerListener();
   }

   public static void startBanAnimation(final Player player, final String reason, final Boolean ban, Boolean newAnim) {
      UUID playerId = player.getUniqueId();
      if (!frozenPlayers.contains(playerId)) {
         frozenPlayers.add(playerId);
         reasons.put(player, reason);
         booleans.put(player, ban);
         final Location startLocation = player.getLocation().clone();
         if (ban) {
            player.setAllowFlight(false);
            player.setGliding(false);
            player.setFlying(false);
            player.setSwimming(false);
         }

         if (newAnim) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 130, 6, false, false, false));
            BukkitTask bukkitTask = (new BukkitRunnable() {
               public int ticks = 0;
               final Inventory inventory = player.getInventory();
               final World world = player.getWorld();
               int currentSlot = 0;

               public void run() {
                  if (this.ticks >= 60 || player.getLocation().getBlock().isLiquid() || BanModule.instant.getOrDefault(player, false)) {
                     BanModule.finishNewBanAnimation(player, reason, ban, player.getLocation());
                  }

                  if (this.ticks < 70 && !player.getLocation().getBlock().isLiquid() && !BanModule.instant.getOrDefault(player, false)) {
                     if (ban) {
                        player.setAllowFlight(false);
                        player.setGliding(false);
                        player.setFlying(false);
                        player.setSwimming(false);
                     }

                     if (ban) {
                        for (int dropped = 0; dropped < new Random().nextInt(1) + 1 && this.currentSlot < this.inventory.getSize(); this.currentSlot++) {
                           ItemStack item = this.inventory.getItem(this.currentSlot);
                           if (item != null) {
                              Location location = player.getLocation();
                              location.setY(location.getY() + 1.0);
                              this.world.dropItemNaturally(location, item);
                              this.world.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 2.0F, 0.8F);
                              player.getInventory().setItem(this.currentSlot, null);
                              dropped++;
                           }
                        }

                        if (this.currentSlot >= this.inventory.getSize()) {
                           this.currentSlot = 0;
                        }
                     }

                     Location playerLoc = player.getLocation();
                     Location particleLoc = playerLoc.clone();
                     particleLoc.setY(playerLoc.getY() - 0.5);
                     Random random = new Random();

                     for (int i = 0; i < 3; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * 0.5;
                        double offsetZ = (random.nextDouble() - 0.5) * 0.5;
                        Location spawnLoc = particleLoc.clone().add(offsetX, 0.0, offsetZ);
                        spawnLoc.setY(spawnLoc.getY() + 0.6);
                        this.world.spawnParticle(Particle.SPELL_WITCH, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0, null, true);
                        this.world.spawnParticle(Particle.SPELL_WITCH, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0, null, true);
                        this.world.spawnParticle(Particle.SPELL_WITCH, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0, null, true);
                        this.world.spawnParticle(Particle.SPELL_WITCH, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0, null, true);
                     }

                     this.ticks++;
                  } else {
                     this.cancel();
                  }
               }
            }).runTaskTimer(Shame.getInstance(), 0L, 1L);
            banTasks.put(player, bukkitTask);
         }

         if (!newAnim) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 140, 1, false, false, false));
            BukkitTask bukkitTask = (new BukkitRunnable() {
                  public int ticks = 0;
                  final Location origin = startLocation.clone();

                  public void run() {
                     if (this.ticks >= 105 || player.getLocation().getBlock().isLiquid() || BanModule.instant.getOrDefault(player, false)) {
                        BanModule.finishBanAnimation(player, reason, ban, player.getLocation());
                     }

                     if (this.ticks < 110 && !player.getLocation().getBlock().isLiquid() && !BanModule.instant.getOrDefault(player, false)) {
                        if (ban) {
                           player.setAllowFlight(false);
                           player.setGliding(false);
                           player.setFlying(false);
                           player.setSwimming(false);
                        }

                        for (ParticleDataStorage.Frame frame : ParticleDataStorage.startAnimation) {
                           if (frame.tick == this.ticks) {
                              for (ParticleDataStorage.P p : frame.particles) {
                                 Location spawnLoc = this.origin.clone().add(p.x, p.y, p.z);
                                 if (p.color == -1) {
                                    player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0, null, true);
                                 } else {
                                    player.getWorld()
                                       .spawnParticle(Particle.REDSTONE, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0, new DustOptions(Color.fromRGB(p.color), 1.0F), true);
                                 }
                              }

                              for (ParticleDataStorage.S s : frame.sounds) {
                                 try {
                                    Sound sound = Sound.valueOf(s.sound);
                                    player.getWorld().playSound(this.origin, sound, s.vol, s.pitch);
                                 } catch (Exception var10) {
                                 }
                              }
                           }
                        }

                        this.ticks++;
                     } else {
                        this.cancel();
                     }
                  }
               })
               .runTaskTimer(Shame.getInstance(), 0L, 1L);
            banTasks.put(player, bukkitTask);
         }
      }
   }

   public static void finishNewBanAnimation(Player player, String reason, Boolean ban, Location location) {
      if (banTasks.get(player) != null) {
         banTasks.remove(player).cancel();
      }

      instant.remove(player);
      frozenPlayers.remove(player.getUniqueId());
      player.removePotionEffect(PotionEffectType.LEVITATION);
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 3.0F, 3.0F);
      List<String> banacmsg = Config.getStringList("banacmsg");
      if (!banacmsg.isEmpty()) {
         String randomMsg = banacmsg.get(new Random().nextInt(banacmsg.size()));
         randomMsg = Config.replace(randomMsg, new Pair<>("%player%", player.getName()));
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litebans broadcast " + randomMsg);
      }

      if (ban) {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " " + reason);
         player.setHealth(0.0);
      }
   }

   public static void finishBanAnimation(Player player, String reason, Boolean ban, Location location) {
      if (banTasks.get(player) != null) {
         banTasks.remove(player).cancel();
      }

      instant.remove(player);
      frozenPlayers.remove(player.getUniqueId());
      player.removePotionEffect(PotionEffectType.LEVITATION);
      player.teleport(location.clone().add(0.0, 4.0, 0.0));
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(0.0, 3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(1.0, 3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(2.0, 3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(-1.0, 3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(-2.0, 3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(0.0, -3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(1.0, -3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(2.0, -3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(-1.0, -3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(-2.0, -3.0, 0.0), 5, 0.0, 0.0, 0.0, 0.0, null, true);
      if (ban) {
         String broadcastCmd = Config.replace(
            Config.getString("ban.broadcast", "litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом."),
            new Pair<>("%player%", player.getName())
         );
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " " + reason);
         if (!broadcastCmd.isEmpty()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCmd);
         }
      }
   }

   @EventHandler
   public void onLeave(PlayerQuitEvent event) {
      if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
         String reason = reasons.getOrDefault(event.getPlayer(), "Не указана");
         Boolean ban = booleans.getOrDefault(event.getPlayer(), true);
         finishBanAnimation(event.getPlayer(), reason, ban, event.getPlayer().getLocation());
      }
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      if (event.getPlayer() != null) {
         if (frozenPlayers.contains(event.getPlayer().getUniqueId())
            && (event.getTo().getX() != event.getFrom().getX() || event.getTo().getX() != event.getFrom().getX())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onCmd(PlayerCommandPreprocessEvent event) {
      if (event.getPlayer() != null) {
         if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onCmd(PlayerTeleportEvent event) {
      if (event.getPlayer() != null) {
         if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPickup(EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (frozenPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }
}
