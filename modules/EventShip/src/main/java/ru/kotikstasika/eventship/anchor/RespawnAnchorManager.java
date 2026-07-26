package ru.kotikstasika.eventship.anchor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.enums.KeyType;
import ru.kotikstasika.eventship.hologram.HologramManager;
import ru.kotikstasika.eventship.items.KeyItem;
import ru.kotikstasika.eventship.loot.LootManager;

public class RespawnAnchorManager {
   private final EventShip plugin;
   private final HologramManager hologramManager;
   private final LootManager lootManager;
   private final Map<Location, AnchorData> activeAnchors = new HashMap<>();
   private final Map<UUID, Long> playerCooldowns = new HashMap<>();

   public RespawnAnchorManager(EventShip plugin, HologramManager hologramManager, LootManager lootManager) {
      this.plugin = plugin;
      this.hologramManager = hologramManager;
      this.lootManager = lootManager;
   }

   public void initializeAnchor(Block block) {
      if (block.getType() == Material.RESPAWN_ANCHOR) {
         Location location = block.getLocation();
         if (!this.activeAnchors.containsKey(location)) {
            AnchorData anchorData = new AnchorData(location);
            this.activeAnchors.put(location, anchorData);
            this.createAnchorHologram(anchorData);
            this.startAutoFillTask(anchorData);
         }
      }
   }

   public boolean handleBarrelClick(Player player, Block block) {
      AnchorData data = this.activeAnchors.get(block.getLocation());
      if (data == null || !data.isActivated()) {
         return false;
      } else if (this.isPlayerOnCooldown(player)) {
         return true;
      } else if (!data.isOpenPhase()) {
         this.setPlayerCooldown(player);
         return true;
      } else if (data.getInventory() != null) {
         player.openInventory(data.getInventory());
         this.setPlayerCooldown(player);
         return true;
      } else {
         return false;
      }
   }

   private void createAnchorHologram(AnchorData anchorData) {
      List<String> lines = this.plugin.getConfig().getStringList("messages.anchor.text");

      for (int i = 0; i < lines.size(); i++) {
         lines.set(i, lines.get(i).replace("%usedKeys%", String.valueOf(anchorData.getUsedKeys())).replace("&", "§"));
      }

      String hologramId = this.hologramManager.createHologram(anchorData.getLocation().clone().add(0.5, 1.5, 0.5), lines);
      anchorData.setHologramId(hologramId);
   }

   public boolean useAnchor(Player player, Block block) {
      if (block.getType() != Material.RESPAWN_ANCHOR) {
         return false;
      } else {
         Location location = block.getLocation();
         if (!this.activeAnchors.containsKey(location)) {
            return false;
         } else if (!this.plugin.getEventManager().isActive()) {
            return false;
         } else {
            Location shipLocation = this.plugin.getEventManager().getShipLocation();
            if (shipLocation == null) {
               return false;
            } else if (location.distance(shipLocation) > 50.0) {
               return false;
            } else if (this.isPlayerOnCooldown(player)) {
               return false;
            } else {
               AnchorData anchorData = this.activeAnchors.get(location);
               if (anchorData.isActivated()) {
                  if (!anchorData.isOpenPhase()) {
                     this.setPlayerCooldown(player);
                     return true;
                  } else {
                     this.openAnchorInventory(player, anchorData);
                     this.setPlayerCooldown(player);
                     return true;
                  }
               } else {
                  ItemStack key = this.findKeyInHands(player);
                  if (key == null) {
                     player.sendMessage("§c▶ §fЭту отмычку §cнельзя использовать для открытия §fсокровища.");
                     return false;
                  } else {
                     String keyType = KeyItem.getKeyType(key, this.plugin);
                     if (keyType == null) {
                        player.sendMessage("§c▶ §fЭту отмычку §cнельзя использовать для открытия §fсокровища.");
                        return false;
                     } else {
                        KeyType type = this.getKeyTypeFromNBT(keyType);
                        if (type != KeyType.UNIQUE && type != KeyType.SECRET) {
                           player.sendMessage("§c▶ §fЭту отмычку §cнельзя использовать для открытия §fсокровища.");
                           return false;
                        } else {
                           this.removeKeyFromPlayer(player, key);
                           anchorData.addKey();
                           this.updateAnchorHologramProgress(anchorData);
                           location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0F, 1.0F);
                           Block anchorBlock = location.getBlock();
                           if (anchorBlock.getType() == Material.RESPAWN_ANCHOR) {
                              int currentLevel = anchorData.getUsedKeys() - 1;
                              int targetLevel = anchorData.getUsedKeys();
                              this.chargeRespawnAnchor(anchorBlock, currentLevel, targetLevel, () -> {
                                 if (anchorData.getUsedKeys() >= 4) {
                                    this.activateAnchor(anchorData);
                                 }
                              });
                           } else if (anchorData.getUsedKeys() >= 4) {
                              this.activateAnchor(anchorData);
                           }

                           this.setPlayerCooldown(player);
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isPlayerOnCooldown(Player player) {
      long currentTime = System.currentTimeMillis();
      Long lastUse = this.playerCooldowns.get(player.getUniqueId());
      return lastUse != null && currentTime - lastUse < 500L;
   }

   private void setPlayerCooldown(Player player) {
      this.playerCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   private ItemStack findKeyInHands(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      if (KeyItem.isKey(mainHand, this.plugin)) {
         return mainHand;
      } else {
         return KeyItem.isKey(offHand, this.plugin) ? offHand : null;
      }
   }

   private void removeKeyFromPlayer(Player player, ItemStack key) {
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

   private KeyType getKeyTypeFromNBT(String keyTag) {
      for (KeyType type : KeyType.values()) {
         String cfg = this.plugin.getConfig().getString("keys." + type.getConfigKey() + ".nbtTag");
         if (cfg != null && cfg.equalsIgnoreCase(keyTag)) {
            return type;
         }
      }

      return null;
   }

   private void updateAnchorHologramProgress(AnchorData anchorData) {
      if (anchorData.getHologramId() != null) {
         List<String> lines = Arrays.asList("§x§E§F§0§2§F§BСокровища пиратов", "§fИспользовано " + anchorData.getUsedKeys() + "/4 отмычек");
         this.hologramManager.updateHologram(anchorData.getHologramId(), lines);
      }
   }

   private void activateAnchor(AnchorData anchorData) {
      anchorData.setActivated(true);
      anchorData.setOpenPhase(false);
      Location location = anchorData.getLocation();
      location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
      location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location.clone().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.2);
      List<String> timerLines = Arrays.asList("§x§E§F§0§2§F§BСокровища пиратов", "§fОткроется через 60 секунд");
      if (anchorData.getHologramId() != null) {
         this.hologramManager.removeHologram(anchorData.getHologramId());
      }

      String hologramId = this.hologramManager.createHologram(anchorData.getLocation().clone().add(0.5, 1.5, 0.5), timerLines);
      anchorData.setHologramId(hologramId);
      this.startOpenTimer(anchorData);
   }

   private void startOpenTimer(final AnchorData anchorData) {
      final int[] timeLeft = new int[]{60};
      (new BukkitRunnable() {
         public void run() {
            if (timeLeft[0] <= 0) {
               RespawnAnchorManager.this.openAnchorPhase(anchorData);
               this.cancel();
            } else {
               List<String> timerLines = Arrays.asList("§x§E§F§0§2§F§BСокровища пиратов", "§fОткроется через " + timeLeft[0] + " сек");
               if (anchorData.getHologramId() != null) {
                  RespawnAnchorManager.this.hologramManager.updateHologram(anchorData.getHologramId(), timerLines);
               }

               timeLeft[0]--;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   private void openAnchorPhase(AnchorData anchorData) {
      anchorData.setOpenPhase(true);
      Inventory inventory = this.plugin.getServer().createInventory(this.plugin.getAnchorInventory(), 54, "Огромное сокровище пиратов");
      anchorData.setInventory(inventory);
      List<ItemStack> loot = this.lootManager.generateLootFromExternalOrConfig(54, 4, KeyType.SECRET);
      Random rand = new Random();
      int itemCount = 10 + rand.nextInt(31);
      List<ItemStack> selectedItems = new ArrayList<>();

      for (int i = 0; i < itemCount && i < loot.size(); i++) {
         int randomIndex = rand.nextInt(loot.size());
         selectedItems.add(loot.get(randomIndex));
      }

      List<Integer> emptySlots = new ArrayList<>();

      for (int i = 0; i < inventory.getSize(); i++) {
         if (inventory.getItem(i) == null) {
            emptySlots.add(i);
         }
      }

      for (ItemStack item : selectedItems) {
         if (emptySlots.isEmpty()) {
            break;
         }

         int randomSlotIndex = rand.nextInt(emptySlots.size());
         int slot = emptySlots.remove(randomSlotIndex);
         inventory.setItem(slot, item);
      }

      this.startCloseTimer(anchorData);
   }

   private void startCloseTimer(final AnchorData anchorData) {
      final int[] timeLeft = new int[]{100};
      (new BukkitRunnable() {
         public void run() {
            if (timeLeft[0] <= 0) {
               RespawnAnchorManager.this.closeAnchor(anchorData);
               this.cancel();
            } else {
               List<String> timerLines = Arrays.asList("§x§E§F§0§2§F§BСокровища пиратов", "§fЗакроется через " + timeLeft[0] + " сек");
               if (anchorData.getHologramId() != null) {
                  RespawnAnchorManager.this.hologramManager.updateHologram(anchorData.getHologramId(), timerLines);
               }

               timeLeft[0]--;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   private void openAnchorInventory(Player player, AnchorData anchorData) {
      if (anchorData.getInventory() != null) {
         player.openInventory(anchorData.getInventory());
      }
   }

   private void closeAnchor(AnchorData anchorData) {
      for (Player player : this.plugin.getServer().getOnlinePlayers()) {
         if (player.getOpenInventory().getTopInventory().equals(anchorData.getInventory())) {
            player.closeInventory();
         }
      }

      Location loc = anchorData.getLocation().clone().add(0.5, 0.8, 0.5);
      loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.1F, 0.9F);
      Block anchorBlock = anchorData.getLocation().getBlock();
      if (anchorBlock.getType() == Material.RESPAWN_ANCHOR) {
         this.drainRespawnAnchor(anchorBlock, 4, 0, () -> {
            anchorData.setActivated(false);
            anchorData.setOpenPhase(false);
            anchorData.setInventory(null);
            anchorData.setUsedKeys(0);
            if (anchorData.getHologramId() != null) {
               this.hologramManager.removeHologram(anchorData.getHologramId());
               anchorData.setHologramId(null);
            }

            this.createAnchorHologram(anchorData);
         });
      } else {
         anchorData.setActivated(false);
         anchorData.setOpenPhase(false);
         anchorData.setInventory(null);
         anchorData.setUsedKeys(0);
         if (anchorData.getHologramId() != null) {
            this.hologramManager.removeHologram(anchorData.getHologramId());
            anchorData.setHologramId(null);
         }

         this.createAnchorHologram(anchorData);
      }
   }

   public void removeAllAnchors() {
      for (AnchorData data : this.activeAnchors.values()) {
         if (data.getHologramId() != null) {
            this.hologramManager.removeHologram(data.getHologramId());
         }
      }

      this.activeAnchors.clear();
   }

   private void startAutoFillTask(final AnchorData anchorData) {
      (new BukkitRunnable() {
         public void run() {
            if (!RespawnAnchorManager.this.plugin.getEventManager().isActive()) {
               this.cancel();
            } else if (!RespawnAnchorManager.this.activeAnchors.containsKey(anchorData.getLocation())) {
               this.cancel();
            } else if (anchorData.isActivated() && anchorData.isOpenPhase()) {
               Inventory inv = anchorData.getInventory();
               if (inv != null) {
                  for (ItemStack item : RespawnAnchorManager.this.lootManager.getRandomItemsExact(5, 3)) {
                     inv.addItem(new ItemStack[]{item});
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 3600L, 3600L);
   }

   private void chargeRespawnAnchor(final Block block, final int fromLevel, final int toLevel, final Runnable callback) {
      (new BukkitRunnable() {
         int level = fromLevel;

         public void run() {
            if (this.level > toLevel) {
               this.cancel();
               callback.run();
            } else {
               RespawnAnchorManager.this.setRespawnAnchorLevel(block, this.level);
               this.level++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 5L);
   }

   private void drainRespawnAnchor(final Block block, final int fromLevel, final int toLevel, final Runnable callback) {
      (new BukkitRunnable() {
         int level = fromLevel;

         public void run() {
            if (this.level < toLevel) {
               this.cancel();
               callback.run();
            } else {
               RespawnAnchorManager.this.setRespawnAnchorLevel(block, this.level);
               block.getWorld().playSound(block.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
               this.level--;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 5L);
   }

   private void setRespawnAnchorLevel(Block block, int level) {
      if (block.getType() == Material.RESPAWN_ANCHOR) {
         RespawnAnchor anchor = (RespawnAnchor)block.getBlockData();
         anchor.setCharges(level);
         block.setBlockData(anchor);
      }
   }

   public static class AnchorInventory implements InventoryHolder {
      public AnchorInventory() {
      }

      public Inventory getInventory() {
         return null;
      }
   }
}
