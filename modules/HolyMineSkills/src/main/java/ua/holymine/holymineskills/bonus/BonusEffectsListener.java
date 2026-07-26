package ua.holymine.holymineskills.bonus;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.holymine.holymineskills.database.DatabaseManager;

public class BonusEffectsListener implements Listener {
   private final DatabaseManager databaseManager;
   private final Random random = new Random();
   private final ConcurrentHashMap<UUID, Long> lastSpeedMsgAtMs = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<UUID, Long> lastSpeedCheck = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<UUID, Boolean> inMineRegionCache = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<UUID, Long> regionCheckTimestamp = new ConcurrentHashMap<>();
   private static final long SPEED_CHECK_INTERVAL = 500L;
   private static final long REGION_CACHE_DURATION = 10000L;

   public BonusEffectsListener(DatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      if (this.isInMineRegion(block.getLocation())) {
         CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
         pointsFuture.thenAccept(playerPoints -> {
            if (playerPoints == null) {
               playerPoints = 0.0;
            }

            if (block.getType() == Material.DIAMOND_ORE) {
               this.handleEmeraldization(event, player, block, playerPoints);
            }

            this.handleRepair(player, playerPoints);
            this.handleUniversalKey(player, playerPoints);
         }).exceptionally(ex -> null);
      }
   }

   private void handleEmeraldization(BlockBreakEvent event, Player player, Block block, double playerPoints) {
      double chance = 0.0;
      if (BonusSystem.hasBonus(playerPoints, "emeraldization", 7)) {
         chance = 8.0;
      } else if (BonusSystem.hasBonus(playerPoints, "emeraldization", 6)) {
         chance = 6.0;
      } else if (BonusSystem.hasBonus(playerPoints, "emeraldization", 5)) {
         chance = 5.7;
      } else if (BonusSystem.hasBonus(playerPoints, "emeraldization", 4)) {
         chance = 4.5;
      } else if (BonusSystem.hasBonus(playerPoints, "emeraldization", 3)) {
         chance = 3.3;
      } else if (BonusSystem.hasBonus(playerPoints, "emeraldization", 2)) {
         chance = 2.1;
      }

      if (chance > 0.0 && this.random.nextInt(500) < chance) {
         event.setCancelled(true);
         block.setType(Material.AIR);
         player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.EMERALD, 1)});
         player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.DIAMOND, 1)});
         String msg = ChatColor.translateAlternateColorCodes('&', "&aИзумрудизация! &bАлмаз &fпревратился в &aизумруд &f(&x&0&0&F&1&F&d/mineskills&f)");
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
      }
   }

   private void handleRepair(Player player, double playerPoints) {
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (tool != null && tool.getType().isItem() && tool.hasItemMeta()) {
         if (tool.getItemMeta() instanceof Damageable) {
            if (BonusSystem.hasBonus(playerPoints, "repair", 1)) {
               if (this.random.nextInt(1000) == 0) {
                  int restorePercent = 1;
                  if (BonusSystem.hasBonus(playerPoints, "repair", 7)) {
                     restorePercent = 3;
                  } else if (BonusSystem.hasBonus(playerPoints, "repair", 6)) {
                     restorePercent = 3;
                  } else if (BonusSystem.hasBonus(playerPoints, "repair", 5)) {
                     restorePercent = 2;
                  } else if (BonusSystem.hasBonus(playerPoints, "repair", 4)) {
                     restorePercent = 2;
                  } else if (BonusSystem.hasBonus(playerPoints, "repair", 3)) {
                     restorePercent = 1;
                  } else if (BonusSystem.hasBonus(playerPoints, "repair", 2)) {
                     restorePercent = 1;
                  }

                  Damageable meta = (Damageable)tool.getItemMeta();
                  int currentDamage = meta.getDamage();
                  int maxDurability = tool.getType().getMaxDurability();
                  int restoreAmount = (int)(maxDurability * restorePercent / 100.0);
                  meta.setDamage(Math.max(0, currentDamage - restoreAmount));
                  tool.setItemMeta((ItemMeta)meta);
                  String msg = ChatColor.translateAlternateColorCodes('&', "&x&C&E&7&1&E&AВосстановление! &fИнструмент починился (&x&0&0&F&1&F&d/mineskills&f)");
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
               }
            }
         }
      }
   }

   private void handleUniversalKey(Player player, double playerPoints) {
      int keyLevel = BonusSystem.getMaxUnlockedLevel(playerPoints, "universal_key");
      if (keyLevel > 0) {
         double chance = 0.0;
         switch (keyLevel) {
            case 1:
               chance = 1.0E-6;
               break;
            case 2:
               chance = 2.0E-6;
               break;
            case 3:
               chance = 3.0E-6;
               break;
            case 4:
               chance = 4.0E-6;
         }

         if (this.random.nextDouble() < chance) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kotikdang give unic " + player.getName());
            Bukkit.dispatchCommand(
               Bukkit.getConsoleSender(),
               "litebans broadcast &x&F&F&7&0&0&0▶ &fИгрок " + player.getName() + " получил &x&F&F&7&0&0&0Универсальный ключ&f, копаясь в автошахте!"
            );
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockDrop(BlockDropItemEvent event) {
      Player player = event.getPlayer();
      if (player != null) {
         Location loc = event.getBlock().getLocation();
         if (this.isInMineRegion(loc)) {
            Material type = event.getBlockState().getType();
            if (type != Material.PURPUR_BLOCK
               && type != Material.PURPUR_PILLAR
               && type != Material.PURPUR_SLAB
               && type != Material.PURPUR_STAIRS
               && type != Material.PURPLE_GLAZED_TERRACOTTA) {
               CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
               pointsFuture.thenAccept(
                     playerPoints -> {
                        if (playerPoints == null) {
                           playerPoints = 0.0;
                        }

                        boolean hasAutoSmelt = BonusSystem.hasBonus(playerPoints, "auto_smelting", 1);
                        boolean hasLuck = BonusSystem.hasBonus(playerPoints, "luck", 1);
                        double rageChance = 0.0;
                        if (BonusSystem.hasBonus(playerPoints, "rage", 2)) {
                           rageChance = 0.06;
                        } else if (BonusSystem.hasBonus(playerPoints, "rage", 1)) {
                           rageChance = 0.03;
                        }

                        boolean rageTriggered = rageChance > 0.0 && Math.random() < rageChance;
                        if (rageTriggered) {
                           String msg = ChatColor.translateAlternateColorCodes(
                              '&', "&b▶ &fСработал эффект " + BonusSystem.getColoredEffectName("Ярость") + " &f(квестовик: Автошахта)"
                           );
                           player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                        }

                        boolean hasMagnet = this.checkMagnetEnchantment(player.getInventory().getItemInMainHand());
                        boolean[] luckApplied = new boolean[]{false};
                        Double finalPlayerPoints = playerPoints;
                        event.getItems().forEach(itemEntity -> {
                           ItemStack stack = itemEntity.getItemStack();
                           if (hasAutoSmelt) {
                              ItemStack smelted = this.getSmeltedResult(stack.getType());
                              if (smelted != null) {
                                 smelted.setAmount(stack.getAmount());
                                 stack.setType(smelted.getType());
                              }
                           }

                           if (hasLuck) {
                              int chance = this.getLuckChance(finalPlayerPoints);
                              if (this.random.nextInt(100) < chance) {
                                 stack.setAmount(stack.getAmount() * 2);
                                 luckApplied[0] = true;
                              }
                           }

                           if (rageTriggered) {
                              stack.setAmount(stack.getAmount() * 3);
                           }
                        });
                        if (luckApplied[0]) {
                           String msg = ChatColor.translateAlternateColorCodes('&', "&b▶ &fСработал эффект &eУдача&f &f(квестовик: Автошахта)");
                           player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                        }

                        if (hasMagnet) {
                           event.getItems().forEach(itemEntity -> this.addToInventorySafely(player, itemEntity.getItemStack(), event.getBlock().getLocation()));
                           event.getItems().clear();
                        }
                     }
                  )
                  .exceptionally(ex -> null);
            } else {
               event.getItems().clear();
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onSpeed(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      Location from = event.getFrom();
      Location to = event.getTo();
      if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
         UUID playerId = player.getUniqueId();
         long now = System.currentTimeMillis();
         Long lastCheck = this.lastSpeedCheck.get(playerId);
         if (lastCheck == null || now - lastCheck >= 500L) {
            this.lastSpeedCheck.put(playerId, now);
            if (!this.isInMineRegionCached(player, to)) {
               if (player.getWalkSpeed() != 0.2F) {
                  player.setWalkSpeed(0.2F);
               }
            } else {
               CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
               pointsFuture.thenAccept(
                     points -> {
                        double playerPoints = points != null ? points : 0.0;
                        float speedMultiplier = 1.0F;
                        if (BonusSystem.hasBonus(playerPoints, "speed", 6)) {
                           speedMultiplier = 1.5F;
                        } else if (BonusSystem.hasBonus(playerPoints, "speed", 5)) {
                           speedMultiplier = 1.4F;
                        } else if (BonusSystem.hasBonus(playerPoints, "speed", 4)) {
                           speedMultiplier = 1.35F;
                        } else if (BonusSystem.hasBonus(playerPoints, "speed", 3)) {
                           speedMultiplier = 1.3F;
                        } else if (BonusSystem.hasBonus(playerPoints, "speed", 2)) {
                           speedMultiplier = 1.2F;
                        } else if (BonusSystem.hasBonus(playerPoints, "speed", 1)) {
                           speedMultiplier = 1.1F;
                        }

                        if (speedMultiplier > 1.0F && player.isOnGround() && !player.isFlying() && !player.isSwimming()) {
                           float newSpeed = 0.2F * speedMultiplier;
                           if (Math.abs(player.getWalkSpeed() - newSpeed) > 0.01F) {
                              player.setWalkSpeed(newSpeed);
                           }

                           long last = this.lastSpeedMsgAtMs.getOrDefault(player.getUniqueId(), 0L);
                           if (now - last >= 20000L) {
                              this.lastSpeedMsgAtMs.put(player.getUniqueId(), now);
                              String msg = ChatColor.translateAlternateColorCodes(
                                 '&', "&b▶ &fСкорость увеличена до &b" + (int)((speedMultiplier - 1.0F) * 100.0F) + "% &f(квестовик: Автошахта)"
                              );
                              player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                           }
                        } else if (player.getWalkSpeed() != 0.2F) {
                           player.setWalkSpeed(0.2F);
                        }
                     }
                  )
                  .exceptionally(ex -> null);
            }
         }
      }
   }

   private boolean isInMineRegionCached(Player player, Location location) {
      UUID playerId = player.getUniqueId();
      long now = System.currentTimeMillis();
      Long lastCheckTime = this.regionCheckTimestamp.get(playerId);
      if (lastCheckTime != null && now - lastCheckTime < 10000L) {
         Boolean cached = this.inMineRegionCache.get(playerId);
         if (cached != null) {
            return cached;
         }
      }

      boolean result = this.isInMineRegion(location);
      this.inMineRegionCache.put(playerId, result);
      this.regionCheckTimestamp.put(playerId, now);
      return result;
   }

   private boolean isInMineRegion(Location bukkitLocation) {
      if (bukkitLocation.getWorld() == null) {
         return false;
      } else if (!bukkitLocation.getWorld().getName().equalsIgnoreCase("spawn")) {
         return false;
      } else {
         try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitLocation.getWorld()));
            if (regionManager == null) {
               return false;
            } else {
               ProtectedRegion mineRegion = regionManager.getRegion("mine");
               ProtectedRegion mine2Region = regionManager.getRegion("mine2");
               if (mineRegion == null && mine2Region == null) {
                  return false;
               } else {
                  com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(bukkitLocation);
                  boolean isInMine = mineRegion != null && mineRegion.contains(wgLocation.toVector().toBlockPoint());
                  boolean isInMine2 = mine2Region != null && mine2Region.contains(wgLocation.toVector().toBlockPoint());
                  return isInMine || isInMine2;
               }
            }
         } catch (Exception var81) {
            return false;
         }
      }
   }

   private boolean checkMagnetEnchantment(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
         NamespacedKey magnetKey = NamespacedKey.fromString("holyenchantments:holyenchantments-magnet");
         return magnetKey != null && container.has(magnetKey, PersistentDataType.INTEGER);
      } else {
         return false;
      }
   }

   private int getLuckChance(double playerPoints) {
      if (BonusSystem.hasBonus(playerPoints, "luck", 4)) {
         return 15;
      } else if (BonusSystem.hasBonus(playerPoints, "luck", 3)) {
         return 13;
      } else if (BonusSystem.hasBonus(playerPoints, "luck", 2)) {
         return 10;
      } else {
         return BonusSystem.hasBonus(playerPoints, "luck", 1) ? 5 : 0;
      }
   }

   private void addToInventorySafely(Player player, ItemStack item, Location dropLocation) {
      HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(new ItemStack[]{item});
      if (!remaining.isEmpty()) {
         for (ItemStack remainingItem : remaining.values()) {
            player.getWorld().dropItemNaturally(dropLocation, remainingItem);
         }
      }
   }

   private ItemStack getSmeltedResult(Material original) {
      switch (original) {
         case IRON_ORE:
            return new ItemStack(Material.IRON_INGOT);
         case GOLD_ORE:
            return new ItemStack(Material.GOLD_INGOT);
         case ANCIENT_DEBRIS:
            return new ItemStack(Material.NETHERITE_SCRAP);
         case NETHER_GOLD_ORE:
            return new ItemStack(Material.GOLD_INGOT);
         case COBBLESTONE:
            return new ItemStack(Material.STONE);
         case CLAY:
            return new ItemStack(Material.TERRACOTTA);
         case CACTUS:
            return new ItemStack(Material.GREEN_DYE);
         case WET_SPONGE:
            return new ItemStack(Material.SPONGE);
         default:
            return null;
      }
   }
}
