package ua.fichadev.fichaspawners.listener;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichaspawners.utils.ColorUtils;
import ua.fichadev.fichaspawners.utils.ConfigManager;

public class SpawnerListener implements Listener {
   private final ConfigManager configManager;

   public SpawnerListener(ConfigManager configManager) {
      this.configManager = configManager;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onItemSpawn(ItemSpawnEvent event) {
      Item item = event.getEntity();
      ItemStack itemStack = item.getItemStack();
      if (itemStack.getType() == Material.SPAWNER) {
         if (!this.isCustomSpawner(itemStack)) {
            if (this.updateSpawnerItem(itemStack)) {
               item.setItemStack(itemStack);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Block block = event.getBlock();
      if (block.getType() == Material.SPAWNER) {
         ItemStack itemInHand = event.getItemInHand();
         if (itemInHand.getType() == Material.SPAWNER) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta instanceof BlockStateMeta) {
               BlockStateMeta blockMeta = (BlockStateMeta)meta;
               if (blockMeta.getBlockState() instanceof CreatureSpawner) {
                  CreatureSpawner itemSpawner = (CreatureSpawner)blockMeta.getBlockState();
                  EntityType entityType = itemSpawner.getSpawnedType();
                  if (entityType != null) {
                     if (block.getState() instanceof CreatureSpawner) {
                        CreatureSpawner placedSpawner = (CreatureSpawner)block.getState();
                        placedSpawner.setSpawnedType(entityType);
                        placedSpawner.update(true);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      for (Block block : event.blockList()) {
         if (block.getType() == Material.SPAWNER && block.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner)block.getState();
            EntityType entityType = spawner.getSpawnedType();
            if (entityType != null) {
               ItemStack spawnerItem = this.createSpawnerItem(entityType);
               block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockExplode(BlockExplodeEvent event) {
      for (Block block : event.blockList()) {
         if (block.getType() == Material.SPAWNER && block.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner)block.getState();
            EntityType entityType = spawner.getSpawnedType();
            if (entityType != null) {
               ItemStack spawnerItem = this.createSpawnerItem(entityType);
               block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
            }
         }
      }
   }

   private boolean isCustomSpawner(ItemStack item) {
      if (!item.hasItemMeta()) {
         return false;
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta.hasDisplayName()) {
            return true;
         } else if (meta.hasLore()) {
            return true;
         } else if (meta.hasEnchants()) {
            return true;
         } else if (meta.isUnbreakable()) {
            return true;
         } else {
            try {
               if (item.getItemMeta().getPersistentDataContainer().getKeys().size() > 0) {
                  return true;
               }
            } catch (Exception var4) {
            }

            return false;
         }
      }
   }

   private ItemStack createSpawnerItem(EntityType entityType) {
      ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
      ItemMeta meta = spawnerItem.getItemMeta();
      if (meta instanceof BlockStateMeta) {
         BlockStateMeta blockMeta = (BlockStateMeta)meta;
         CreatureSpawner spawner = (CreatureSpawner)blockMeta.getBlockState();
         spawner.setSpawnedType(entityType);
         blockMeta.setBlockState(spawner);
         String entityName = entityType.name();
         String name = this.configManager.getSpawnerName(entityName);
         List<String> lore = this.configManager.getSpawnerLore(entityName);
         if (name != null) {
            blockMeta.setDisplayName(ColorUtils.translate(name));
         }

         if (lore != null && !lore.isEmpty()) {
            blockMeta.setLore(lore.stream().map(ColorUtils::translate).collect(Collectors.toList()));
         }

         spawnerItem.setItemMeta(blockMeta);
      }

      return spawnerItem;
   }

   private boolean updateSpawnerItem(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (!(meta instanceof BlockStateMeta)) {
         return false;
      } else {
         BlockStateMeta blockMeta = (BlockStateMeta)meta;
         if (!(blockMeta.getBlockState() instanceof CreatureSpawner)) {
            return false;
         } else {
            CreatureSpawner spawner = (CreatureSpawner)blockMeta.getBlockState();
            EntityType entityType = spawner.getSpawnedType();
            if (entityType == null) {
               return false;
            } else {
               String entityName = entityType.name();
               String name = this.configManager.getSpawnerName(entityName);
               List<String> lore = this.configManager.getSpawnerLore(entityName);
               if (name != null) {
                  meta.setDisplayName(ColorUtils.translate(name));
               }

               if (lore != null && !lore.isEmpty()) {
                  meta.setLore(lore.stream().map(ColorUtils::translate).collect(Collectors.toList()));
               }

               item.setItemMeta(meta);
               return true;
            }
         }
      }
   }
}
