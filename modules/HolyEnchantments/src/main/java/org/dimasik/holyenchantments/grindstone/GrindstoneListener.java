package org.dimasik.holyenchantments.grindstone;

import java.util.Arrays;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.MEnchant;
import org.dimasik.holyenchantments.utils.Parser;
import org.dimasik.holyenchantments.utils.Schedulers;

public class GrindstoneListener implements Listener {
   public GrindstoneListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      Player player = (Player)event.getWhoClicked();
      if (inventory.getHolder() instanceof GrindstoneMenu) {
         GrindstoneMenu grindstoneMenu = (GrindstoneMenu)inventory.getHolder();
         if (event.getClickedInventory() != inventory) {
            if (event.getClick() == ClickType.DOUBLE_CLICK
               && event.getCurrentItem() != null
               && (
                  event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE
                     || event.getCurrentItem().getType() == Material.ORANGE_STAINED_GLASS_PANE
               )) {
               event.setCancelled(true);
            }
         } else {
            int slot = event.getSlot();
            if (event.getSlot() != 19) {
               event.setCancelled(true);
               Integer[] slots = new Integer[]{12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34};
               if (slot == 4) {
                  if (grindstoneMenu.vanillaEnchantments.isEmpty() && grindstoneMenu.customEnchantments.isEmpty()) {
                     grindstoneMenu.vanillaEnchantments.putAll(grindstoneMenu.vanillaEnchantmentsList);
                     grindstoneMenu.vanillaEnchantmentLevels.putAll(grindstoneMenu.vanillaEnchantmentsListLevels);
                     grindstoneMenu.customEnchantments.putAll(grindstoneMenu.customEnchantmentsList);
                     grindstoneMenu.customEnchantmentLevels.putAll(grindstoneMenu.customEnchantmentsListLevels);
                  } else {
                     grindstoneMenu.vanillaEnchantments.clear();
                     grindstoneMenu.vanillaEnchantmentLevels.clear();
                     grindstoneMenu.customEnchantments.clear();
                     grindstoneMenu.customEnchantmentLevels.clear();
                  }

                  grindstoneMenu.buildEnchantments(false);
               } else if (slot != 26) {
                  if (Arrays.asList(slots).contains(slot)) {
                     if (grindstoneMenu.vanillaEnchantmentsList.containsKey(slot)) {
                        if (grindstoneMenu.vanillaEnchantments.containsKey(slot)) {
                           grindstoneMenu.vanillaEnchantments.remove(slot);
                           grindstoneMenu.vanillaEnchantmentLevels.remove(slot);
                        } else {
                           grindstoneMenu.vanillaEnchantments.put(slot, grindstoneMenu.vanillaEnchantmentsList.get(slot));
                           grindstoneMenu.vanillaEnchantmentLevels.put(slot, grindstoneMenu.vanillaEnchantmentsListLevels.get(slot));
                        }

                        grindstoneMenu.buildEnchantments(false);
                     } else if (grindstoneMenu.customEnchantmentsList.containsKey(slot)) {
                        if (grindstoneMenu.customEnchantments.containsKey(slot)) {
                           grindstoneMenu.customEnchantments.remove(slot);
                           grindstoneMenu.customEnchantmentLevels.remove(slot);
                        } else {
                           grindstoneMenu.customEnchantments.put(slot, grindstoneMenu.customEnchantmentsList.get(slot));
                           grindstoneMenu.customEnchantmentLevels.put(slot, grindstoneMenu.customEnchantmentsListLevels.get(slot));
                        }

                        grindstoneMenu.buildEnchantments(false);
                     }
                  }
               } else if (grindstoneMenu.vanillaEnchantments.isEmpty() && grindstoneMenu.customEnchantments.isEmpty()) {
                  player.sendMessage(Parser.color(" &cУ вас нет выбранных зачарований для продажи"));
               } else {
                  ItemStack itemStack = inventory.getItem(19);
                  if (itemStack == null) {
                     player.sendMessage(Parser.color(" &cУ вас нет выбранных зачарований для продажи"));
                     return;
                  }

                  int cnt = 0;

                  for (Entry<Integer, Enchantment> entry : grindstoneMenu.vanillaEnchantments.entrySet()) {
                     cnt++;
                     itemStack.removeEnchantment(entry.getValue());
                  }

                  for (Entry<Integer, MEnchant> entry : grindstoneMenu.customEnchantments.entrySet()) {
                     cnt++;
                     ItemMeta itemMeta = itemStack.getItemMeta();
                     HolyEnchantments.getInstance().getEnchantManager().applyEnchant(itemMeta, entry.getValue(), 0);
                     itemStack.setItemMeta(itemMeta);
                  }

                  player.sendMessage(Parser.color(" &6Вы продали зачарований на &a" + cnt * 15 + " &6очков опыта"));
                  player.giveExp(cnt * 15);
                  grindstoneMenu.buildEnchantments(true);
               }
            }
         }

         ItemStack old19;
         if (inventory.getItem(19) != null) {
            old19 = inventory.getItem(19).clone();
         } else {
            old19 = null;
         }

         Bukkit.getScheduler().runTaskLater(HolyEnchantments.getInstance(), () -> {
            if (player != null) {
               ItemStack new19;
               if (inventory.getItem(19) != null) {
                  new19 = inventory.getItem(19).clone();
               } else {
                  new19 = null;
               }

               if (new19 != null && old19 != null && !new19.isSimilar(old19) || new19 != null && old19 == null || new19 == null && old19 != null) {
                  grindstoneMenu.buildEnchantments(true);
               }
            }
         }, 1L);
      }
   }

   @EventHandler
   public void on(InventoryCloseEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      Player player = (Player)event.getPlayer();
      if (inventory.getHolder() instanceof GrindstoneMenu && inventory.getItem(19) != null) {
         this.addItemInventory(player.getInventory(), inventory.getItem(19), player.getLocation());
      }
   }

   private void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      for (int id = 0; id < inventory.getStorageContents().length; id++) {
         ItemStack item = inventory.getItem(id);
         if (item == null || item.getType().isAir()) {
            inventory.addItem(new ItemStack[]{itemStack});
            return;
         }

         if (item.isSimilar(itemStack)) {
            int count = item.getMaxStackSize() - item.getAmount();
            if (count > 0) {
               if (itemStack.getAmount() <= count) {
                  inventory.addItem(new ItemStack[]{itemStack});
                  return;
               }

               ItemStack i = itemStack.clone();
               i.setAmount(count);
               inventory.addItem(new ItemStack[]{i});
               itemStack.setAmount(itemStack.getAmount() - count);
            }
         }
      }

      Schedulers.sync(() -> location.getWorld().dropItemNaturally(location, itemStack));
   }
}
