package ru.kotikstasika.eventship.barrel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.enums.KeyType;
import ru.kotikstasika.eventship.hologram.HologramManager;
import ru.kotikstasika.eventship.items.KeyItem;
import ru.kotikstasika.eventship.loot.LootManager;

public class BarrelManager {
   private final EventShip plugin;
   private final HologramManager hologramManager;
   private final LootManager lootManager;
   private final Map<Location, BarrelData> activeBarrels = new HashMap<>();
   private final Map<Location, BukkitTask> barrelTimers = new HashMap<>();

   public BarrelManager(EventShip plugin, HologramManager hologramManager, LootManager lootManager) {
      this.plugin = plugin;
      this.hologramManager = hologramManager;
      this.lootManager = lootManager;
   }

   public boolean openBarrel(Player player, Block block) {
      if (block.getType() != Material.BARREL) {
         return false;
      } else {
         Location location = block.getLocation();
         if (!this.plugin.getEventManager().isActive()) {
            return false;
         } else {
            Location shipLocation = this.plugin.getEventManager().getShipLocation();
            if (shipLocation == null) {
               return false;
            } else if (location.distance(shipLocation) > 50.0) {
               return false;
            } else {
               if (this.activeBarrels.containsKey(location)) {
                  BarrelData data = this.activeBarrels.get(location);
                  if (data.isOpen() && data.getInventory() != null) {
                     if (player != null) {
                        player.openInventory(data.getInventory());
                     }

                     return true;
                  }
               }

               if (player == null) {
                  return false;
               } else {
                  location.getWorld().playSound(location, Sound.BLOCK_BARREL_OPEN, 1.0F, 1.0F);
                  CompletableFuture.runAsync(() -> {
                     ItemStack key = this.findKeyInInventory(player);
                     if (key == null) {
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                           player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c▶ &fЭту бочку необходимо &cоткрыть пиратской отмычкой!"));
                           location.getWorld().playSound(location, Sound.BLOCK_BARREL_CLOSE, 0.8F, 1.0F);
                        });
                     } else {
                        String keyType = KeyItem.getKeyType(key, this.plugin);
                        if (keyType != null) {
                           KeyType type = this.getKeyTypeFromNBT(keyType);
                           if (type != null) {
                              this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                                 this.removeKeyFromPlayer(player, key);
                                 this.openBarrelInventoryAsync(player, location, type);
                              });
                           }
                        }
                     }
                  });
                  return true;
               }
            }
         }
      }
   }

   public void initializeBarrel(Block block) {
      if (block.getType() == Material.BARREL) {
         Location location = block.getLocation();
         if (!this.activeBarrels.containsKey(location)) {
            BarrelData barrelData = new BarrelData(null, location, null);
            this.activeBarrels.put(location, barrelData);
            List<String> lines = Arrays.asList("§6Пиратская бочка", "§fИспользуйте отмычку");
            String hologramId = this.hologramManager.createHologram(location.clone().add(0.5, 1.2, 0.5), lines);
            barrelData.setHologramId(hologramId);
         }
      }
   }

   private ItemStack findKeyInInventory(Player player) {
      if (player == null) {
         return null;
      } else {
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (KeyItem.isKey(mainHand, this.plugin)) {
            return mainHand;
         } else {
            return KeyItem.isKey(offHand, this.plugin) ? offHand : null;
         }
      }
   }

   private void removeKeyFromPlayer(Player player, ItemStack key) {
      if (player != null) {
         if (player.getInventory().getItemInMainHand().equals(key)) {
            if (key.getAmount() > 1) {
               key.setAmount(key.getAmount() - 1);
               player.getInventory().setItemInMainHand(key);
            } else {
               player.getInventory().setItemInMainHand(null);
            }
         } else if (player.getInventory().getItemInOffHand().equals(key)) {
            if (key.getAmount() > 1) {
               key.setAmount(key.getAmount() - 1);
               player.getInventory().setItemInOffHand(key);
            } else {
               player.getInventory().setItemInOffHand(null);
            }
         }
      }
   }

   private KeyType getKeyTypeFromNBT(String nbtTag) {
      for (KeyType type : KeyType.values()) {
         String configNBT = this.plugin.getConfig().getString("keys." + type.getConfigKey() + ".nbtTag");
         if (nbtTag.equals(configNBT)) {
            return type;
         }
      }

      return null;
   }

   private void openBarrelInventoryAsync(Player player, Location location, KeyType keyType) {
      int size = 27;
      BarrelData existingData = this.activeBarrels.get(location);
      Inventory inventory = this.plugin.getServer().createInventory(this.plugin.getBarrelInventory(), size, "Пиратская бочка");
      BarrelData barrelData;
      if (existingData == null) {
         barrelData = new BarrelData(inventory, location, keyType);
         this.activeBarrels.put(location, barrelData);
      } else {
         existingData.setInventory(inventory);
         existingData.setKeyType(keyType);
         barrelData = existingData;
      }

      if (player != null) {
         List<String> lines = Arrays.asList("§6Клад", "§fЗакроется через 60 секунд");
         if (barrelData.getHologramId() != null) {
            this.hologramManager.removeHologram(barrelData.getHologramId());
         }

         String hologramId = this.hologramManager.createHologram(barrelData.getLocation().clone().add(0.5, 1.7, 0.5), lines);
         barrelData.setHologramId(hologramId);
         player.openInventory(inventory);
         this.startAsyncBarrelTimer(barrelData);
      }

      CompletableFuture.runAsync(() -> {
         int level = this.lootManager.mapKeyTypeToExternalLevel(keyType);
         List<ItemStack> itemsToDistribute = this.lootManager.generateLootFromExternalOrConfig(size, level, keyType);
         Random rand = new Random();
         int itemCount = rand.nextInt(11) + 5;
         List<ItemStack> selectedItems = new ArrayList<>();

         for (int i = 0; i < itemCount && i < itemsToDistribute.size(); i++) {
            int randomIndex = rand.nextInt(itemsToDistribute.size());
            selectedItems.add(itemsToDistribute.get(randomIndex));
         }

         this.fillInventoryImmediately(inventory, selectedItems);
      });
   }

   private void fillInventoryImmediately(Inventory inventory, List<ItemStack> items) {
      List<Integer> emptySlots = new ArrayList<>();

      for (int i = 0; i < inventory.getSize(); i++) {
         if (inventory.getItem(i) == null) {
            emptySlots.add(i);
         }
      }

      Random rand = new Random();

      for (ItemStack item : items) {
         if (emptySlots.isEmpty()) {
            break;
         }

         int randomSlotIndex = rand.nextInt(emptySlots.size());
         int slot = emptySlots.remove(randomSlotIndex);
         inventory.setItem(slot, item);
      }
   }

   private void startAsyncBarrelTimer(final BarrelData barrelData) {
      int configured = 60;
      final int[] timeLeft = new int[]{Math.max(60, configured)};
      BukkitTask timerTask = (new BukkitRunnable() {
         public void run() {
            if (timeLeft[0] <= 0) {
               BarrelManager.this.closeBarrelAsync(barrelData);
               this.cancel();
            } else {
               CompletableFuture.runAsync(() -> BarrelManager.this.updateBarrelHologramAsync(barrelData, timeLeft[0]));
               timeLeft[0]--;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
      this.barrelTimers.put(barrelData.getLocation(), timerTask);
   }

   private void updateBarrelHologramAsync(BarrelData barrelData, int timeLeft) {
      if (barrelData.getHologramId() != null) {
         List<String> lines = Arrays.asList("§6Клад", "§fЗакроется через " + timeLeft + " секунд");
         this.hologramManager.updateHologram(barrelData.getHologramId(), lines);
      }
   }

   private void closeBarrelAsync(BarrelData barrelData) {
      BukkitTask timer = this.barrelTimers.remove(barrelData.getLocation());
      if (timer != null) {
         timer.cancel();
      }

      barrelData.getLocation().getWorld().playSound(barrelData.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1.0F, 1.0F);
      if (barrelData.getHologramId() != null) {
         CompletableFuture.runAsync(() -> this.hologramManager.removeHologram(barrelData.getHologramId()));
      }

      this.activeBarrels.remove(barrelData.getLocation());
      this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
         for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().equals(barrelData.getInventory())) {
               player.closeInventory();
            }
         }
      });
   }

   public void removeAllBarrels() {
      this.barrelTimers.values().forEach(BukkitTask::cancel);
      this.barrelTimers.clear();
      CompletableFuture.runAsync(() -> {
         for (BarrelData data : this.activeBarrels.values()) {
            if (data.getHologramId() != null) {
               this.hologramManager.removeHologram(data.getHologramId());
            }
         }
      });
      this.activeBarrels.clear();
   }

   public static class BarrelInventory implements InventoryHolder {
      public BarrelInventory() {
      }

      public Inventory getInventory() {
         return null;
      }
   }
}
