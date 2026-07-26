package org.impldev.holycore.extensions;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.impldev.holycore.HolyCore;
import org.impldev.holycore.checker.ShulkerTotemChecker;

public class InventoryExtension implements Listener {
   private final HolyCore plugin;

   public InventoryExtension(HolyCore plugin) {
      this.plugin = plugin;
   }

   private boolean isShulkerBox(ItemStack item) {
      return item != null && item.getType().toString().endsWith("SHULKER_BOX");
   }

   private void sendShulkerDenyMessage(Player player) {
      player.spigot()
         .sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fВы не можете класть шалкера в &6эндер-сундук"))
         );
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getClickedInventory() != null) {
         Player player = (Player)event.getWhoClicked();
         Inventory topInventory = event.getView().getTopInventory();
         if (topInventory.getType() != InventoryType.ENDER_CHEST || !this.handleEnderChestShulkerBlock(event, player, topInventory)) {
            this.handleTotemCheck(event, player);
            this.handleShieldCheck(event);
         }
      }
   }

   private boolean handleEnderChestShulkerBlock(InventoryClickEvent event, Player player, Inventory topInventory) {
      if (event.getClick() == ClickType.NUMBER_KEY) {
         if (event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if (this.isShulkerBox(hotbarItem)) {
               this.sendShulkerDenyMessage(player);
               event.setCancelled(true);
               return true;
            }
         }
      } else if (event.isShiftClick()) {
         if (event.getClickedInventory().getType() != InventoryType.ENDER_CHEST && this.isShulkerBox(event.getCurrentItem())) {
            this.sendShulkerDenyMessage(player);
            event.setCancelled(true);
            return true;
         }
      } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
         if (event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
            ItemStack offhandItem = event.getWhoClicked().getInventory().getItemInOffHand();
            if (this.isShulkerBox(offhandItem)) {
               this.sendShulkerDenyMessage(player);
               event.setCancelled(true);
               return true;
            }
         }
      } else if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
         if (this.isShulkerBox(event.getCursor())) {
            for (int i = 0; i < topInventory.getSize(); i++) {
               if (this.isShulkerBox(topInventory.getItem(i))) {
                  this.sendShulkerDenyMessage(player);
                  event.setCancelled(true);
                  return true;
               }
            }
         }
      } else if (event.getClickedInventory().getType() == InventoryType.ENDER_CHEST && this.isShulkerBox(event.getCursor())) {
         this.sendShulkerDenyMessage(player);
         event.setCancelled(true);
         return true;
      }

      return false;
   }

   @EventHandler
   public void onDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Inventory topInventory = event.getView().getTopInventory();
         if (topInventory.getType() == InventoryType.ENDER_CHEST) {
            if (this.isShulkerBox(event.getOldCursor())) {
               int topSize = topInventory.getSize();

               for (int slot : event.getRawSlots()) {
                  if (slot < topSize) {
                     Player player = (Player)event.getWhoClicked();
                     this.sendShulkerDenyMessage(player);
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         }
      }
   }

   private void handleTotemCheck(InventoryClickEvent event, Player player) {
      new ShulkerTotemChecker(this.plugin).checkShulker(player);
      new ShulkerTotemChecker(this.plugin).checkTotems(player);
      ItemStack current = event.getCurrentItem();
      ItemStack cursor = event.getCursor();
      Inventory clickedInventory = event.getClickedInventory();
      InventoryAction action = event.getAction();
      boolean isTotemInvolved = current != null && current.getType() == Material.TOTEM_OF_UNDYING
         || cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING;
      if (isTotemInvolved) {
         int currentTotems = 0;

         for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
               currentTotems += item.getAmount();
            }
         }

         int futureTotems = currentTotems;
         ClickType click = event.getClick();
         if (action != InventoryAction.PICKUP_ALL
            && action != InventoryAction.PICKUP_SOME
            && action != InventoryAction.PICKUP_HALF
            && action != InventoryAction.PICKUP_ONE) {
            if (action != InventoryAction.PLACE_ALL && action != InventoryAction.PLACE_SOME && action != InventoryAction.PLACE_ONE) {
               if (click == ClickType.SWAP_OFFHAND) {
                  if (current != null
                     && current.getType() == Material.TOTEM_OF_UNDYING
                     && clickedInventory != null
                     && clickedInventory.getType() != InventoryType.PLAYER
                     && clickedInventory.getType() != InventoryType.CRAFTING) {
                     futureTotems = currentTotems + current.getAmount();
                  }
               } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                  if (clickedInventory != null
                     && clickedInventory.getType() != InventoryType.PLAYER
                     && current != null
                     && current.getType() == Material.TOTEM_OF_UNDYING) {
                     futureTotems = currentTotems - current.getAmount();
                  }
               } else if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
                  ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                  if (hotbarItem != null && hotbarItem.getType() == Material.TOTEM_OF_UNDYING) {
                     futureTotems = currentTotems + hotbarItem.getAmount();
                  }

                  if (current != null && current.getType() == Material.TOTEM_OF_UNDYING) {
                     futureTotems -= current.getAmount();
                  }
               } else if (action != InventoryAction.DROP_ALL_SLOT && action != InventoryAction.DROP_ONE_SLOT) {
                  if (action == InventoryAction.CLONE_STACK) {
                     return;
                  }
               } else if (current != null && current.getType() == Material.TOTEM_OF_UNDYING) {
                  futureTotems = currentTotems - (click == ClickType.LEFT ? current.getAmount() : 1);
               }
            } else if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
               futureTotems = currentTotems - (click == ClickType.LEFT ? cursor.getAmount() : 1);
            }
         } else {
            if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER) {
               return;
            }

            if (current != null && current.getType() == Material.TOTEM_OF_UNDYING) {
               futureTotems = currentTotems + (click == ClickType.LEFT ? current.getAmount() : 1);
            }
         }

         boolean isTakingFromContainer = clickedInventory != null
            && clickedInventory.getType() != InventoryType.PLAYER
            && (current != null && current.getType() == Material.TOTEM_OF_UNDYING || cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING);
         if (futureTotems > 4 && !action.toString().contains("PLACE") || isTakingFromContainer && currentTotems >= 4) {
            player.spigot()
               .sendMessage(
                  ChatMessageType.ACTION_BAR,
                  TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fВы не можете носить с собой больше &6x4 Тотемов бессмертия"))
               );
            event.setCancelled(true);
         }
      }
   }

   private void handleShieldCheck(InventoryClickEvent event) {
      if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SHIELD) {
         event.getCurrentItem().setAmount(0);
         event.setCancelled(true);
      }
   }
}
