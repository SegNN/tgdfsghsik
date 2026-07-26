package org.dimasik.litebinds.menu.listeners;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.litebinds.LiteBinds;
import org.dimasik.litebinds.database.ActionType;
import org.dimasik.litebinds.database.DatabaseManager;
import org.dimasik.litebinds.database.PlayerActions;
import org.dimasik.litebinds.menu.abst.AbstractListener;
import org.dimasik.litebinds.menu.menus.Menu;

public class MenuListener extends AbstractListener {
   public MenuListener() {
   }

   @EventHandler
   @Override
   public void onClick(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof Menu menu) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();
         if (slot == 22) {
            DatabaseManager dbm = LiteBinds.getInstance().getDatabaseManager();
            CompletableFuture.allOf(
                  dbm.updateActionDrop(player.getName(), ActionType.NONE),
                  dbm.updateActionSwap(player.getName(), ActionType.NONE),
                  dbm.updateActionInteract(player.getName(), ActionType.NONE)
               )
               .thenAccept(v -> Bukkit.getScheduler().runTask(LiteBinds.getInstance(), () -> menu.compile().open()));
            return;
         }
         ActionType actionType = switch (slot) {
            case 10 -> ActionType.SNOWBALL;
            case 11 -> ActionType.JAKE;
            case 12 -> ActionType.ALTERNATIVE_TRAP;
            case 13 -> ActionType.STAN;
            case 14 -> ActionType.TRAP;
            case 15 -> ActionType.EXPLOSIVE;
            case 16 -> ActionType.BACKPACK;
            default -> null;
         };
         if (event.isLeftClick()) {
            this.relative(player, true, actionType).thenAccept(v -> Bukkit.getScheduler().runTask(LiteBinds.getInstance(), () -> menu.compile().open()));
         } else if (event.isRightClick()) {
            this.relative(player, false, actionType).thenAccept(v -> Bukkit.getScheduler().runTask(LiteBinds.getInstance(), () -> menu.compile().open()));
         }
      }
   }

   private CompletableFuture<Void> relative(Player player, boolean next, ActionType actionType) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               Optional<PlayerActions> playerActionsOptional = LiteBinds.getInstance().getDatabaseManager().getPlayerActions(player.getName()).get();
               PlayerActions playerActions = playerActionsOptional.orElseGet(
                  () -> new PlayerActions(player.getName(), ActionType.NONE, ActionType.NONE, ActionType.NONE)
               );
               ActionType currentDrop = playerActions.getActionDrop();
               ActionType currentSwap = playerActions.getActionSwap();
               ActionType currentInteract = playerActions.getActionInteract();
               if (next) {
                  if (actionType == currentDrop) {
                     if (currentSwap == ActionType.NONE) {
                        playerActions.setActionDrop(ActionType.NONE);
                        playerActions.setActionSwap(actionType);
                     } else if (currentInteract == ActionType.NONE) {
                        playerActions.setActionDrop(ActionType.NONE);
                        playerActions.setActionInteract(actionType);
                     } else {
                        playerActions.setActionDrop(ActionType.NONE);
                     }
                  } else if (actionType == currentSwap) {
                     if (currentInteract == ActionType.NONE) {
                        playerActions.setActionSwap(ActionType.NONE);
                        playerActions.setActionInteract(actionType);
                     } else {
                        playerActions.setActionSwap(ActionType.NONE);
                     }
                  } else if (actionType == currentInteract) {
                     playerActions.setActionInteract(ActionType.NONE);
                  } else if (currentDrop == ActionType.NONE) {
                     playerActions.setActionDrop(actionType);
                  } else if (currentSwap == ActionType.NONE) {
                     playerActions.setActionSwap(actionType);
                  } else if (currentInteract == ActionType.NONE) {
                     playerActions.setActionInteract(actionType);
                  }
               } else if (actionType == currentDrop) {
                  playerActions.setActionDrop(ActionType.NONE);
               } else if (actionType == currentSwap) {
                  if (currentDrop == ActionType.NONE) {
                     playerActions.setActionSwap(ActionType.NONE);
                     playerActions.setActionDrop(actionType);
                  } else {
                     playerActions.setActionSwap(ActionType.NONE);
                  }
               } else if (actionType == currentInteract) {
                  if (currentSwap == ActionType.NONE) {
                     playerActions.setActionInteract(ActionType.NONE);
                     playerActions.setActionSwap(actionType);
                  } else if (currentDrop == ActionType.NONE) {
                     playerActions.setActionInteract(ActionType.NONE);
                     playerActions.setActionDrop(actionType);
                  } else {
                     playerActions.setActionInteract(ActionType.NONE);
                  }
               } else if (currentInteract == ActionType.NONE) {
                  playerActions.setActionInteract(actionType);
               } else if (currentSwap == ActionType.NONE) {
                  playerActions.setActionSwap(actionType);
               } else if (currentDrop == ActionType.NONE) {
                  playerActions.setActionDrop(actionType);
               }

               LiteBinds.getInstance().getDatabaseManager().savePlayerActions(playerActions).get();
            } catch (Exception var8) {
               throw new RuntimeException(var8);
            }
         }
      );
   }
}
