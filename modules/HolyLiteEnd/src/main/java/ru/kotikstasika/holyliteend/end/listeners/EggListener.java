package ru.kotikstasika.holyliteend.end.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.utils.DatabaseManager;

public class EggListener implements Listener {
   private final HolyLiteEnd plugin;
   private final Map<Inventory, Long> chestOpenTime = new HashMap<>();
   private final Set<Chunk> chunksWithEggs = ConcurrentHashMap.newKeySet();
   private final Map<Chunk, Long> lastChunkCheck = new ConcurrentHashMap<>();
   private static final long CHUNK_CHECK_INTERVAL = 30000L;

   public EggListener(HolyLiteEnd plugin) {
      this.plugin = plugin;
      this.startValueIncreaseTask();
   }

   @EventHandler
   public void onInventoryPickupItem(InventoryPickupItemEvent event) {
      ItemStack currentItem = event.getItem().getItemStack();
      if (EggItem.isDragonEgg(currentItem)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         Inventory chestInventory = event.getInventory();
         InventoryHolder chestHolder = chestInventory.getHolder();
         ItemStack clickedItem = event.getCurrentItem();
         ItemStack cursorItem = event.getCursor();
         boolean wasCancelled = event.isCancelled();
         boolean hasEggInteraction = false;
         if (clickedItem != null && EggItem.isDragonEgg(clickedItem)) {
            hasEggInteraction = true;
         }

         if (cursorItem != null && EggItem.isDragonEgg(cursorItem)) {
            hasEggInteraction = true;
         }

         if (event.getHotbarButton() >= 0) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (hotbarItem != null && EggItem.isDragonEgg(hotbarItem)) {
               hasEggInteraction = true;
            }
         }

         if (hasEggInteraction) {
            boolean isAllowed = chestHolder instanceof Player || chestHolder instanceof Chest;
            if (!isAllowed) {
               event.setCancelled(true);
               if (!wasCancelled) {
                  if (cursorItem != null && EggItem.isDragonEgg(cursorItem)) {
                     HashMap leftover = player.getInventory().addItem(new ItemStack[]{cursorItem.clone()});
                     if (leftover.isEmpty()) {
                        player.setItemOnCursor((ItemStack)null);
                     } else {
                        player.setItemOnCursor((ItemStack)leftover.values().iterator().next());
                     }
                  } else if (clickedItem != null && EggItem.isDragonEgg(clickedItem) && event.getClickedInventory() != null) {
                     event.getClickedInventory().setItem(event.getSlot(), (ItemStack)null);
                     HashMap leftover = player.getInventory().addItem(new ItemStack[]{clickedItem.clone()});
                     if (!leftover.isEmpty()) {
                        event.getClickedInventory().setItem(event.getSlot(), (ItemStack)leftover.values().iterator().next());
                     }
                  }

                  player.updateInventory();
               }
            } else {
               if (!(chestHolder instanceof Player)) {
                  Location storeLoc = this.getCanonicalLocationFromInventory(chestInventory, chestHolder);
                  if (storeLoc == null) {
                     return;
                  }

                  if (!player.getWorld().getName().equals("world")) {
                     player.closeInventory();
                     event.setCancelled(true);
                     return;
                  }

                  if (!this.chestOpenTime.containsKey(chestInventory)) {
                     this.chestOpenTime.put(chestInventory, System.currentTimeMillis());
                  }

                  if (cursorItem != null && EggItem.isDragonEgg(cursorItem)) {
                     EggItem.setPlaceTime(cursorItem);
                     this.plugin.getDatabaseManager().storeDragonEgg(player, storeLoc, EggItem.getBaseValue(cursorItem));
                  }

                  if (clickedItem != null && EggItem.isDragonEgg(clickedItem)) {
                     EggItem.setPlaceTime(clickedItem);
                     this.plugin.getDatabaseManager().storeDragonEgg(player, storeLoc, EggItem.getBaseValue(clickedItem));
                  }
               }

               if (event.isShiftClick()) {
                  Inventory topInv = event.getView().getTopInventory();
                  Inventory clickedInv = event.getClickedInventory();
                  if (clickedInv != null && clickedInv.getHolder() instanceof Player) {
                     if (topInv != null && topInv.getHolder() instanceof Chest) {
                        if (!player.getWorld().getName().equals("world")) {
                           event.setCancelled(true);
                           player.closeInventory();
                           return;
                        }

                        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                           Location storeLocx = this.getCanonicalLocationFromInventory(topInv, topInv.getHolder());
                           if (storeLocx != null) {
                              boolean hasEgg = false;

                              for (ItemStack currentItem : topInv.getContents()) {
                                 if (currentItem != null && EggItem.isDragonEgg(currentItem)) {
                                    hasEgg = true;
                                    if (!this.plugin.getDatabaseManager().isEggStored(storeLocx)) {
                                       this.plugin.getDatabaseManager().storeDragonEgg(player, storeLocx, EggItem.getBaseValue(currentItem));
                                    }

                                    EggItem.setPlaceTime(currentItem);
                                 }
                              }

                              if (!hasEgg && this.plugin.getDatabaseManager().isEggStored(storeLocx)) {
                                 this.plugin.getDatabaseManager().removeDragonEgg(storeLocx);
                              }
                           }
                        }, 1L);
                     } else if (EggItem.isDragonEgg(event.getCurrentItem())) {
                        event.setCancelled(true);
                        return;
                     }
                  } else if (clickedInv != null && clickedInv.getHolder() instanceof Chest) {
                     this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        boolean hasEgg = false;

                        for (ItemStack item : clickedInv.getContents()) {
                           if (item != null && EggItem.isDragonEgg(item)) {
                              hasEgg = true;
                              break;
                           }
                        }

                        if (!hasEgg) {
                           Location storeLocx = this.getCanonicalLocationFromInventory(clickedInv, clickedInv.getHolder());
                           if (storeLocx == null) {
                              return;
                           }

                           if (this.plugin.getDatabaseManager().isEggStored(storeLocx)) {
                              this.plugin.getDatabaseManager().removeDragonEgg(storeLocx);
                           }
                        }
                     }, 1L);
                  }
               }

               this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.getEggBossBarManager().checkPlayerForEgg(player), 1L);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         Inventory chestInventory = event.getInventory();
         InventoryHolder chestHolder = chestInventory.getHolder();

         for (ItemStack item : event.getNewItems().values()) {
            if (item != null && EggItem.isDragonEgg(item)) {
               boolean isAllowed = false;
               if (chestHolder instanceof Player) {
                  isAllowed = true;
               } else if (chestHolder instanceof Chest) {
                  if (!player.getWorld().getName().equals("world")) {
                     event.setCancelled(true);
                     player.closeInventory();
                     return;
                  }

                  isAllowed = true;
                  Location storeLoc = this.getCanonicalLocationFromInventory(chestInventory, chestHolder);
                  EggItem.setPlaceTime(item);
                  this.plugin.getDatabaseManager().storeDragonEgg(player, storeLoc, EggItem.getBaseValue(item));
               }

               if (!isAllowed) {
                  event.setCancelled(true);
                  return;
               }
               break;
            }
         }

         this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.getEggBossBarManager().checkPlayerForEgg(player), 1L);
      }
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         Inventory inventory = event.getInventory();
         if (!(inventory.getHolder() instanceof Player) && inventory.getHolder() instanceof Chest) {
            Location storeLoc = this.getCanonicalLocationFromInventory(inventory, inventory.getHolder());
            if (storeLoc == null) {
               return;
            }

            Location below = storeLoc.clone().subtract(0.0, 1.0, 0.0);
            if (below.getBlock().getType() == Material.HOPPER) {
               Hopper hopper = (Hopper)below.getBlock().getState();
               Inventory hopperInv = hopper.getInventory();

               for (ItemStack hopperItem : hopperInv.getContents()) {
                  if (hopperItem != null && EggItem.isDragonEgg(hopperItem)) {
                     hopperInv.removeItem(new ItemStack[]{hopperItem});
                     ItemStack remainingItem = (ItemStack)inventory.addItem(new ItemStack[]{hopperItem.clone()}).get(0);
                     if (remainingItem != null) {
                        hopperInv.addItem(new ItemStack[]{hopperItem});
                     }
                     break;
                  }
               }
            }

            this.plugin
               .getServer()
               .getScheduler()
               .runTaskLater(
                  this.plugin,
                  () -> {
                     List<DatabaseManager.EggData> eggsData = this.plugin.getDatabaseManager().getEggsData(storeLoc);

                     for (ItemStack item : inventory.getContents()) {
                        if (item != null && EggItem.isDragonEgg(item)) {
                           int itemBaseValue = EggItem.getBaseValue(item);
                           int itemCurrentValue = EggItem.getEggValue(item);
                           DatabaseManager.EggData matchingEgg = null;

                           for (DatabaseManager.EggData eggData : eggsData) {
                              if (eggData.baseValue == itemBaseValue) {
                                 matchingEgg = eggData;
                                 break;
                              }
                           }

                           if (matchingEgg != null) {
                              int dbValue = matchingEgg.currentValue;
                              if (dbValue != itemCurrentValue) {
                                 if (itemCurrentValue > dbValue) {
                                    this.plugin
                                       .getDatabaseManager()
                                       .saveEggValueToDatabase(
                                          storeLoc.getBlockX(),
                                          storeLoc.getBlockY(),
                                          storeLoc.getBlockZ(),
                                          storeLoc.getWorld().getName(),
                                          itemBaseValue,
                                          itemCurrentValue
                                       );
                                 } else {
                                    EggItem.updateEggValue(item, dbValue);
                                 }
                              }
                           } else {
                              this.plugin.getDatabaseManager().storeDragonEgg(player, storeLoc, itemBaseValue);
                              this.plugin
                                 .getDatabaseManager()
                                 .saveEggValueToDatabase(
                                    storeLoc.getBlockX(),
                                    storeLoc.getBlockY(),
                                    storeLoc.getBlockZ(),
                                    storeLoc.getWorld().getName(),
                                    itemBaseValue,
                                    itemCurrentValue
                                 );
                           }
                        }
                     }
                  },
                  1L
               );
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         Inventory inventory = event.getInventory();
         if (!(inventory.getHolder() instanceof Player)) {
            Location storeLoc = this.getCanonicalLocationFromInventory(inventory, inventory.getHolder());
            if (storeLoc == null) {
               return;
            }

            boolean hasEgg = false;

            for (ItemStack item : inventory.getContents()) {
               if (item != null && EggItem.isDragonEgg(item)) {
                  hasEgg = true;
                  int itemBaseValue = EggItem.getBaseValue(item);
                  int itemCurrentValue = EggItem.getEggValue(item);
                  if (!this.plugin.getDatabaseManager().isEggStored(storeLoc)) {
                     EggItem.setPlaceTime(item);
                     this.plugin.getDatabaseManager().storeDragonEgg(player, storeLoc, itemBaseValue);
                  }

                  this.plugin
                     .getDatabaseManager()
                     .saveEggValueToDatabase(
                        storeLoc.getBlockX(), storeLoc.getBlockY(), storeLoc.getBlockZ(), storeLoc.getWorld().getName(), itemBaseValue, itemCurrentValue
                     );
               }
            }

            if (!hasEgg && this.plugin.getDatabaseManager().isEggStored(storeLoc)) {
               this.plugin.getDatabaseManager().removeDragonEgg(storeLoc);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.removeEggsFromInventory(player);
      this.plugin.getEggBossBarManager().onPlayerQuit(player);
   }

   @EventHandler
   public void onEntityPickupItem(EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack item = event.getItem().getItemStack();
         if (EggItem.isDragonEgg(item)) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.getEggBossBarManager().checkPlayerForEgg(player), 1L);
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      ItemStack item = event.getItemInHand();
      if (EggItem.isDragonEgg(item)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryMoveItem(InventoryMoveItemEvent event) {
      ItemStack item = event.getItem();
      if (EggItem.isDragonEgg(item)) {
         Inventory source = event.getSource();
         Inventory destination = event.getDestination();
         InventoryHolder sourceHolder = source.getHolder();
         InventoryHolder destHolder = destination.getHolder();
         if ((destHolder instanceof Hopper || destHolder instanceof HopperMinecart) && (sourceHolder instanceof Chest || sourceHolder instanceof DoubleChest)) {
            event.setCancelled(true);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
               for (ItemStack hopperItem : destination.getContents()) {
                  if (hopperItem != null && EggItem.isDragonEgg(hopperItem)) {
                     destination.removeItem(new ItemStack[]{hopperItem});
                     break;
                  }
               }
            }, 1L);
            return;
         }

         if (sourceHolder instanceof Hopper
            || destHolder instanceof Hopper
            || sourceHolder instanceof HopperMinecart
            || destHolder instanceof HopperMinecart
            || sourceHolder instanceof Dropper
            || destHolder instanceof Dropper
            || sourceHolder instanceof Dispenser
            || destHolder instanceof Dispenser
            || sourceHolder instanceof StorageMinecart
            || destHolder instanceof StorageMinecart) {
            event.setCancelled(true);
            return;
         }

         String sourceName = event.getSource().getType().name();
         String destName = event.getDestination().getType().name();
         if (sourceName.contains("HOPPER")
            || destName.contains("HOPPER")
            || sourceName.contains("MINECART")
            || destName.contains("MINECART")
            || sourceName.contains("DROPPER")
            || destName.contains("DROPPER")
            || sourceName.contains("DISPENSER")
            || destName.contains("DISPENSER")) {
            event.setCancelled(true);
         }
      }

      if (event.getItem().getType() == Material.DRAGON_EGG) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onItemSpawn(final ItemSpawnEvent event) {
      ItemStack item = event.getEntity().getItemStack();
      if (EggItem.isDragonEgg(item)) {
         (new BukkitRunnable() {
               public void run() {
                  if (!event.getEntity().isDead() && event.getEntity().isValid()) {
                     for (Entity nearby : event.getEntity().getNearbyEntities(3.0, 3.0, 3.0)) {
                        if (nearby instanceof HopperMinecart || nearby instanceof StorageMinecart) {
                           event.setCancelled(true);
                           break;
                        }
                     }

                     Location loc = event.getEntity().getLocation();
                     Block block = loc.getBlock();
                     Block below = loc.clone().subtract(0.0, 1.0, 0.0).getBlock();
                     if (block.getType() == Material.HOPPER
                        || below.getType() == Material.HOPPER
                        || block.getState() instanceof Hopper
                        || below.getState() instanceof Hopper) {
                        event.setCancelled(true);
                     }
                  } else {
                     this.cancel();
                  }
               }
            })
            .runTaskTimer(this.plugin, 1L, 5L);
      }
   }

   private void removeEggsFromInventory(Player player) {
      for (int i = 0; i < player.getInventory().getSize(); i++) {
         ItemStack armorItem = player.getInventory().getItem(i);
         if (armorItem != null && EggItem.isDragonEgg(armorItem)) {
            player.getInventory().setItem(i, (ItemStack)null);
         }
      }

      for (int var5 = 0; var5 < player.getInventory().getArmorContents().length; var5++) {
         ItemStack armorItem = player.getInventory().getArmorContents()[var5];
         if (armorItem != null && EggItem.isDragonEgg(armorItem)) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            armor[var5] = null;
            player.getInventory().setArmorContents(armor);
         }
      }

      ItemStack offHand = player.getInventory().getItemInOffHand();
      if (offHand != null && EggItem.isDragonEgg(offHand)) {
         player.getInventory().setItemInOffHand((ItemStack)null);
      }

      if (player.getItemOnCursor() != null && EggItem.isDragonEgg(player.getItemOnCursor())) {
         player.setItemOnCursor((ItemStack)null);
      }
   }

   private void startValueIncreaseTask() {
      (new BukkitRunnable() {
            public void run() {
               long currentTime = System.currentTimeMillis();
               long threeMinutes = 180000L;

               for (DatabaseManager.EggData eggData : EggListener.this.plugin.getDatabaseManager().getAllEggs()) {
                  if (currentTime - eggData.lastIncreaseTime >= threeMinutes && eggData.currentValue < 1500) {
                     int oldValue = eggData.currentValue;
                     int newValue = oldValue + 1;
                     EggListener.this.plugin
                        .getDatabaseManager()
                        .updateEggValueInDatabase(eggData.x, eggData.y, eggData.z, eggData.world, eggData.baseValue, oldValue, newValue);
                     Bukkit.getScheduler().runTask(EggListener.this.plugin, () -> EggListener.this.applyIncrementToChestItem(eggData, oldValue, newValue));
                  }
               }

               EggListener.this.reconcileLoadedChunkEggs();
            }
         })
         .runTaskTimerAsynchronously(this.plugin, 100L, 100L);
      (new BukkitRunnable() {
         public void run() {
            EggListener.this.updateEggLoresWithTimers();
         }
      }).runTaskTimer(this.plugin, 200L, 200L);
   }

   private void applyIncrementToChestItem(DatabaseManager.EggData eggData, int oldValue, int newValue) {
      World world = this.plugin.getServer().getWorld(eggData.world);
      if (world != null && world.isChunkLoaded(eggData.x >> 4, eggData.z >> 4)) {
         Block block = new Location(world, eggData.x, eggData.y, eggData.z).getBlock();
         if (block.getType() == Material.CHEST) {
            Chest chest = (Chest)block.getState();
            ItemStack[] itemsInChest = chest.getInventory().getContents();

            for (ItemStack item : itemsInChest) {
               if (item != null && EggItem.isDragonEgg(item) && EggItem.getBaseValue(item) == eggData.baseValue && EggItem.getEggValue(item) == oldValue) {
                  EggItem.updateEggValue(item, newValue);
                  EggItem.setPlaceTime(item);
                  break;
               }
            }
         }
      }
   }

   private void reconcileLoadedChunkEggs() {
      List<EggListener.EggSnapshot> snapshots;
      try {
         snapshots = (List<EggListener.EggSnapshot>)Bukkit.getScheduler().callSyncMethod(this.plugin, this::collectEggSnapshots).get();
      } catch (Exception var9) {
         return;
      }

      for (EggListener.EggSnapshot snap : snapshots) {
         List<DatabaseManager.EggData> eggsData = this.plugin.getDatabaseManager().getEggsData(snap.location);
         DatabaseManager.EggData matchingEgg = null;

         for (DatabaseManager.EggData candidate : eggsData) {
            if (candidate.baseValue == snap.baseValue) {
               matchingEgg = candidate;
               break;
            }
         }

         if (matchingEgg != null) {
            int dbValue = matchingEgg.currentValue;
            if (dbValue != snap.currentValue) {
               if (snap.currentValue > dbValue) {
                  this.plugin
                     .getDatabaseManager()
                     .saveEggValueToDatabase(
                        snap.location.getBlockX(),
                        snap.location.getBlockY(),
                        snap.location.getBlockZ(),
                        snap.location.getWorld().getName(),
                        snap.baseValue,
                        snap.currentValue
                     );
               } else {
                  ItemStack item = snap.item;
                  Bukkit.getScheduler().runTask(this.plugin, () -> EggItem.updateEggValue(item, dbValue));
               }
            }
         } else {
            this.plugin.getDatabaseManager().storeDragonEgg((Player)null, snap.location, snap.baseValue);
            this.plugin
               .getDatabaseManager()
               .saveEggValueToDatabase(
                  snap.location.getBlockX(),
                  snap.location.getBlockY(),
                  snap.location.getBlockZ(),
                  snap.location.getWorld().getName(),
                  snap.baseValue,
                  snap.currentValue
               );
         }
      }
   }

   private List<EggListener.EggSnapshot> collectEggSnapshots() {
      List<EggListener.EggSnapshot> result = new ArrayList<>();

      for (World world : this.plugin.getServer().getWorlds()) {
         if (world.getName().equals("world")) {
            for (Chunk chunk : world.getLoadedChunks()) {
               long lastCheck = this.lastChunkCheck.getOrDefault(chunk, 0L);
               if (System.currentTimeMillis() - lastCheck >= 30000L) {
                  boolean hasEggs = false;

                  for (BlockState blockState : chunk.getTileEntities()) {
                     if (blockState instanceof Chest) {
                        Chest chestx = (Chest)blockState;

                        for (ItemStack item : chestx.getInventory().getContents()) {
                           if (item != null && EggItem.isDragonEgg(item)) {
                              hasEggs = true;
                              result.add(new EggListener.EggSnapshot(chestx.getLocation(), EggItem.getBaseValue(item), EggItem.getEggValue(item), item));
                           }
                        }
                     }
                  }

                  if (hasEggs) {
                     this.chunksWithEggs.add(chunk);
                  } else {
                     this.chunksWithEggs.remove(chunk);
                  }

                  this.lastChunkCheck.put(chunk, System.currentTimeMillis());
               }
            }
         }
      }

      return result;
   }

   private void updateEggLoresWithTimers() {
      for (Chunk chunk : this.chunksWithEggs) {
         if (!chunk.isLoaded()) {
            this.chunksWithEggs.remove(chunk);
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      Action action = event.getAction();
      if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
         ItemStack item = player.getInventory().getItemInMainHand();
         if (item != null && EggItem.isDragonEgg(item)) {
            if (event.getClickedBlock() != null && !this.isAllowedBlock(event.getClickedBlock().getType())) {
               player.closeInventory();
               event.setCancelled(true);
               return;
            }

            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST && !player.getWorld().getName().equals("world")) {
               player.closeInventory();
               event.setCancelled(true);
               return;
            }
         }
      }

      if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)
         && event.getClickedBlock() != null
         && event.getClickedBlock().getType() == Material.DRAGON_EGG) {
         event.setCancelled(true);
         event.getClickedBlock().setType(Material.AIR);
      }
   }

   private boolean isAllowedBlock(Material material) {
      return material == Material.CHEST;
   }

   private Location getCanonicalLocationFromInventory(Inventory inventory, InventoryHolder holder) {
      Location loc = inventory.getLocation();
      if (loc != null) {
         return loc;
      } else {
         return holder instanceof Chest ? ((Chest)holder).getLocation() : null;
      }
   }

   private static class EggSnapshot {
      final Location location;
      final int baseValue;
      final int currentValue;
      final ItemStack item;

      EggSnapshot(Location location, int baseValue, int currentValue, ItemStack item) {
         this.location = location;
         this.baseValue = baseValue;
         this.currentValue = currentValue;
         this.item = item;
      }
   }
}
