package ru.kotikstasika.eventship.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.items.KeyItem;
import ru.kotikstasika.eventship.npc.CaptainNPC;

public class EventShipListener implements Listener {
   private final EventShip plugin;

   public EventShipListener(EventShip plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onBarrelClick(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block block = event.getClickedBlock();
         if (block != null) {
            if (this.plugin.getEventManager().isActive()) {
               if (block.getType() == Material.BARREL) {
                  if (this.plugin.getEventManager().getShipLocation() != null
                     && block.getLocation().distance(this.plugin.getEventManager().getShipLocation()) <= 50.0) {
                     event.setCancelled(true);
                     if (this.plugin.getEventManager().getAnchorManager().handleBarrelClick(event.getPlayer(), block)) {
                        return;
                     }

                     this.plugin.getEventManager().getBarrelManager().openBarrel(event.getPlayer(), block);
                  }
               } else {
                  if (block.getType() == Material.RESPAWN_ANCHOR) {
                     event.setCancelled(true);
                     this.plugin.getEventManager().getAnchorManager().useAnchor(event.getPlayer(), block);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onCaptainClick(PlayerInteractEntityEvent event) {
      if (event.getRightClicked() instanceof WanderingTrader) {
         WanderingTrader trader = (WanderingTrader)event.getRightClicked();
         if (trader.getCustomName() == null || trader.getCustomName().isEmpty()) {
            if (this.plugin.getEventManager().isActive()) {
               event.setCancelled(true);
               CaptainNPC captain = this.plugin.getEventManager().getCaptain();
               if (captain != null) {
                  Player player = event.getPlayer();
                  if (!captain.hasKey()) {
                     captain.pushPlayerAway(player);
                  } else {
                     ItemStack key = captain.getKey();
                     if (player.getInventory().addItem(new ItemStack[]{key}).isEmpty()) {
                        captain.removeKey();
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      ItemStack item = event.getItemInHand();
      if (KeyItem.isKey(item, this.plugin)) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onItemInteract(InventoryClickEvent event) {
      if (this.plugin.getEventManager().isActive()) {
         Inventory inventory = event.getInventory();
         if (inventory.getHolder() != null) {
            if (inventory.getHolder() == this.plugin.getBarrelInventory() || inventory.getHolder() == this.plugin.getAnchorInventory()) {
               if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                  return;
               }

               Player player = (Player)event.getWhoClicked();
               if (player.getCooldown(Material.BEDROCK) == 0) {
                  player.setCooldown(Material.BEDROCK, 5);
               } else {
                  event.setCancelled(true);
                  player.sendMessage("Вы забираете предметы слишком быстро!");
               }
            }
         }
      }
   }
}
