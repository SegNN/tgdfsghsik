package org.dimasik.lootmanager.frontend.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.LootItem;
import org.dimasik.lootmanager.backend.utils.ItemHoverUtil;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.dimasik.lootmanager.frontend.menus.Main;
import org.dimasik.lootmanager.frontend.menus.RemoveItem;

public class RemoveItemListener implements Listener {
   public RemoveItemListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof RemoveItem) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         RemoveItem removeItem = (RemoveItem)inventory.getHolder();
         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();
         switch (slot) {
            case 0:
            case 1:
            case 2:
            case 9:
            case 10:
            case 11:
            case 18:
            case 19:
            case 20:
               LootItem lootItem = LootManager.getInstance().getItemManager().getItem(removeItem.getLootItem().getId());
               if (lootItem == null) {
                  player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно удалить предмет, так как его уже нет."));
                  return;
               }

               ItemStack itemStack = removeItem.getLootItem().getItem();
               ItemHoverUtil.sendHoverItemMessage(
                  player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount() + " &fбыл удален."), itemStack
               );
               LootManager.getInstance().getItemManager().removeItem(removeItem.getLootItem().getId());
               Main main = removeItem.getBack();
               main.compile().open();
            case 3:
            case 4:
            case 5:
            case 12:
            case 13:
            case 14:
            case 21:
            case 22:
            case 23:
            default:
               break;
            case 6:
            case 7:
            case 8:
            case 15:
            case 16:
            case 17:
            case 24:
            case 25:
            case 26:
               Main back = removeItem.getBack();
               back.compile().open();
         }
      }
   }
}
