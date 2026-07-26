package ua.holydev.holyautocraft.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ua.holydev.holyautocraft.HolyAutoCraft;
import ua.holydev.holyautocraft.service.BlockService;
import ua.holydev.holyautocraft.service.CraftingService;
import ua.holydev.holyautocraft.service.ItemService;

public class BlockHandler implements Listener {
   private final HolyAutoCraft plugin;

   public BlockHandler(HolyAutoCraft plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlace(BlockPlaceEvent e) {
      if (!e.isCancelled()) {
         ItemStack item = e.getItemInHand();
         if (item.getType() == Material.DISPENSER) {
            if (item.hasItemMeta()) {
               boolean hasCraftData = item.getItemMeta()
                  .getPersistentDataContainer()
                  .has(this.plugin.getConfigManager().getCraftKey(), PersistentDataType.BYTE);
               if (hasCraftData) {
                  Block placed = e.getBlockPlaced();
                  if (placed.getState() instanceof TileState) {
                     TileState state = (TileState)placed.getState();
                     BlockService.markAsAutoCraft(state, this.plugin.getConfigManager().getCraftKey());
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onBreak(BlockBreakEvent e) {
      if (!e.isCancelled()) {
         Block block = e.getBlock();
         if (block.getType() == Material.DISPENSER) {
            if (BlockService.isAutoCraftBlock(block)) {
               e.setDropItems(false);
               if (block.getState() instanceof InventoryHolder) {
                  InventoryHolder holder = (InventoryHolder)block.getState();
                  Inventory inv = holder.getInventory();

                  for (ItemStack stack : inv.getContents()) {
                     if (stack != null && stack.getType() != Material.AIR) {
                        block.getWorld().dropItemNaturally(block.getLocation(), stack);
                     }
                  }
               }

               ItemStack drop = new ItemStack(Material.DISPENSER);
               ItemService.applyAutoCraftProperties(drop, this.plugin);
               block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
         }
      }
   }

   @EventHandler
   public void onDispense(BlockDispenseEvent e) {
      Block block = e.getBlock();
      if (BlockService.isAutoCraftBlock(block)) {
         e.setCancelled(true);
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            try {
               CraftingService.executeCrafting(block);
            } catch (Exception var2x) {
               var2x.printStackTrace();
            }
         });
      }
   }
}
