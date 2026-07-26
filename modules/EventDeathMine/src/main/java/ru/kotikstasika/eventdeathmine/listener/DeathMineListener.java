package ru.kotikstasika.eventdeathmine.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;
import ru.kotikstasika.eventdeathmine.model.DeathMineState;

public class DeathMineListener implements Listener {
   private final DeathMineManager manager;

   public DeathMineListener(DeathMineManager manager) {
      this.manager = manager;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
         && event.getItem() != null
         && event.getItem().getType() == Material.ENDER_PEARL
         && this.manager.isInsideAnyCube(event.getPlayer().getLocation())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.manager.getState() == DeathMineState.SPAWNED) {
         Player player = event.getPlayer();
         Location blockLoc = event.getBlock().getLocation();
         if (this.manager.isBlockInMineArea(blockLoc)) {
            if (this.manager.isTrackedResourceBlock(event.getBlock().getLocation())) {
               if (this.manager.isAllowedMineBlock(event.getBlock().getType())) {
                  ItemStack tool = player.getInventory().getItemInMainHand();
                  if (player.getGameMode() == GameMode.CREATIVE) {
                     event.setCancelled(true);
                     Material type = event.getBlock().getType();
                     this.manager.onResourceBlockBroken(event.getBlock().getLocation());
                     event.getBlock().setType(Material.AIR);
                     this.manager.getLootService().giveLootForBlock(player, type);
                     this.manager.incrementMined(player.getName());
                     return;
                  }

                  Material brokenType = event.getBlock().getType();
                  if (tool != null && tool.getType().toString().endsWith("_PICKAXE")) {
                     event.setCancelled(true);
                     this.manager.onResourceBlockBroken(event.getBlock().getLocation());
                     event.getBlock().setType(Material.AIR);
                     if (brokenType == Material.OBSIDIAN) {
                        this.relocatePickaxeAnywhere(player, tool);
                     }

                     if (brokenType == Material.ANCIENT_DEBRIS) {
                        this.relocatePickaxeAnywhere(player, tool);
                     }

                     if (brokenType == Material.CRYING_OBSIDIAN) {
                        this.relocatePickaxeAnywhere(player, tool);
                     }
                  } else {
                     int randomSlot = (int)(Math.random() * 36.0);
                     ItemStack clonedTool = tool.clone();
                     player.getInventory().setItem(randomSlot, clonedTool);
                  }

                  this.manager.getLootService().giveLootForBlock(player, brokenType);
                  this.manager.incrementMined(player.getName());
               }
            }
         }
      }
   }

   private void relocatePickaxeAnywhere(Player player, ItemStack pickaxe) {
      int choice = (int)(Math.random() * 13.0);
      int slot = (int)(Math.random() * 35.0);
      ItemStack dest = player.getInventory().getItem(slot);
      switch (choice) {
         case 1:
            player.getInventory().setItem(slot, pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         case 2:
            player.getInventory().setItem(slot, pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         case 3:
            player.getInventory().setItem(slot, pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         case 4:
            player.getInventory().setItem(slot, pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         case 5:
            player.getInventory().setItem(slot, pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         case 6:
            this.swapWithArmor(player, pickaxe, DeathMineListener.ArmorSlot.HELMET);
            break;
         case 7:
            this.swapWithArmor(player, pickaxe, DeathMineListener.ArmorSlot.CHEST);
            break;
         case 8:
            this.swapWithArmor(player, pickaxe, DeathMineListener.ArmorSlot.LEGS);
            break;
         case 9:
            this.swapWithArmor(player, pickaxe, DeathMineListener.ArmorSlot.BOOTS);
            break;
         default:
            player.getInventory().setItem(slot, pickaxe);
            player.getInventory().setItemInMainHand(dest);
      }
   }

   private void swapWithArmor(Player player, ItemStack pickaxe, DeathMineListener.ArmorSlot slot) {
      switch (slot) {
         case HELMET: {
            ItemStack dest = player.getInventory().getHelmet();
            player.getInventory().setHelmet(pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         }
         case CHEST: {
            ItemStack dest = player.getInventory().getChestplate();
            player.getInventory().setChestplate(pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         }
         case LEGS: {
            ItemStack dest = player.getInventory().getLeggings();
            player.getInventory().setLeggings(pickaxe);
            player.getInventory().setItemInMainHand(dest);
            break;
         }
         case BOOTS: {
            ItemStack dest = player.getInventory().getBoots();
            player.getInventory().setBoots(pickaxe);
            player.getInventory().setItemInMainHand(dest);
         }
      }
   }

   private static enum ArmorSlot {
      HELMET,
      CHEST,
      LEGS,
      BOOTS;

      private ArmorSlot() {
      }
   }
}
