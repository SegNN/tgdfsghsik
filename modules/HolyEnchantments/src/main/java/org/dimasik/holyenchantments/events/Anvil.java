package org.dimasik.holyenchantments.events;

import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.enchantscore.EnchantManager;
import org.dimasik.holyenchantments.enchantscore.MEnchant;
import org.dimasik.holyenchantments.grindstone.GrindstoneMenu;

public class Anvil implements Listener {
   private final EnchantManager enchantManager;

   public Anvil(EnchantManager enchantManager) {
      this.enchantManager = enchantManager;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onAnvil(PrepareAnvilEvent e) {
      ItemStack one = e.getInventory().getFirstItem();
      ItemStack two = e.getInventory().getSecondItem();
      if (one != null && two != null) {
         ItemMeta metaTwo = two.getItemMeta();
         ItemMeta metaOne = one.getItemMeta();
         if (metaOne != null && metaTwo != null) {
            PersistentDataContainer dataTwo = metaTwo.getPersistentDataContainer();
            if (two.getType() == Material.ENCHANTED_BOOK) {
               for (Entry<String, NamespacedKey> entry : this.enchantManager.getEnchantsNames().entrySet()) {
                  if (!metaOne.getPersistentDataContainer().has(entry.getValue(), PersistentDataType.INTEGER)
                     && metaTwo.getPersistentDataContainer().has(entry.getValue(), PersistentDataType.INTEGER)) {
                     ItemStack result = e.getResult() == null ? one.clone() : e.getResult();
                     ItemMeta resultMeta = result.getItemMeta();
                     MEnchant menchant = this.enchantManager.getEnchantsNameSpaced().get(this.enchantManager.getEnchantsNames().get(entry.getKey()));
                     resultMeta = this.enchantManager
                        .applyEnchant(
                           resultMeta, menchant, (Integer)metaTwo.getPersistentDataContainer().getOrDefault(entry.getValue(), PersistentDataType.INTEGER, 0)
                        );
                     result.setItemMeta(resultMeta);
                     e.getInventory().setRepairCost(30);
                     e.setResult(result);
                  } else if (metaOne.getPersistentDataContainer().has(entry.getValue(), PersistentDataType.INTEGER)
                     && metaTwo.getPersistentDataContainer().has(entry.getValue(), PersistentDataType.INTEGER)) {
                     ItemStack result = e.getResult() == null ? one.clone() : e.getResult();
                     ItemMeta resultMeta = result.getItemMeta();
                     MEnchant menchant = this.enchantManager.getEnchantsNameSpaced().get(this.enchantManager.getEnchantsNames().get(entry.getKey()));
                     int max = 0;

                     for (int i : menchant.getLevels().keySet()) {
                        max = i;
                     }

                     max = Math.min(max, menchant.getMaxAnvilLevel());
                     int level = Math.max(
                        (Integer)metaOne.getPersistentDataContainer().getOrDefault(entry.getValue(), PersistentDataType.INTEGER, 0),
                        (Integer)metaTwo.getPersistentDataContainer().getOrDefault(entry.getValue(), PersistentDataType.INTEGER, 0)
                     );
                     if (((Integer)metaOne.getPersistentDataContainer().getOrDefault(entry.getValue(), PersistentDataType.INTEGER, 0))
                        .equals(metaTwo.getPersistentDataContainer().getOrDefault(entry.getValue(), PersistentDataType.INTEGER, 0))) {
                        level++;
                     }

                     if (level > max) {
                        level = max;
                     }

                     resultMeta = this.enchantManager.applyEnchant(resultMeta, menchant, level);
                     result.setItemMeta(resultMeta);
                     e.getInventory().setRepairCost(30);
                     e.setResult(result);
                  }
               }
            } else if (one.getType() == two.getType() && one.getType() != Material.PLAYER_HEAD && one.getType() != Material.TOTEM_OF_UNDYING) {
               for (Entry<String, NamespacedKey> entryx : this.enchantManager.getEnchantsNames().entrySet()) {
                  if (!metaOne.getPersistentDataContainer().has(entryx.getValue(), PersistentDataType.INTEGER)
                     && metaTwo.getPersistentDataContainer().has(entryx.getValue(), PersistentDataType.INTEGER)) {
                     ItemStack result = e.getResult() == null ? one.clone() : e.getResult();
                     ItemMeta resultMeta = result.getItemMeta();
                     MEnchant menchant = this.enchantManager.getEnchantsNameSpaced().get(this.enchantManager.getEnchantsNames().get(entryx.getKey()));
                     resultMeta = this.enchantManager
                        .applyEnchant(
                           resultMeta, menchant, (Integer)metaTwo.getPersistentDataContainer().getOrDefault(entryx.getValue(), PersistentDataType.INTEGER, 0)
                        );
                     result.setItemMeta(resultMeta);
                     e.getInventory().setRepairCost(30);
                     e.setResult(result);
                  } else if (metaOne.getPersistentDataContainer().has(entryx.getValue(), PersistentDataType.INTEGER)
                     && metaTwo.getPersistentDataContainer().has(entryx.getValue(), PersistentDataType.INTEGER)) {
                     ItemStack result = e.getResult() == null ? one.clone() : e.getResult();
                     ItemMeta resultMeta = result.getItemMeta();
                     MEnchant menchant = this.enchantManager.getEnchantsNameSpaced().get(this.enchantManager.getEnchantsNames().get(entryx.getKey()));
                     int max = 0;

                     for (int i : menchant.getLevels().keySet()) {
                        max = i;
                     }

                     max = Math.min(max, menchant.getMaxAnvilLevel());
                     int levelx = Math.max(
                        (Integer)metaOne.getPersistentDataContainer().getOrDefault(entryx.getValue(), PersistentDataType.INTEGER, 0),
                        (Integer)metaTwo.getPersistentDataContainer().getOrDefault(entryx.getValue(), PersistentDataType.INTEGER, 0)
                     );
                     if (((Integer)metaOne.getPersistentDataContainer().getOrDefault(entryx.getValue(), PersistentDataType.INTEGER, 0))
                        .equals(metaTwo.getPersistentDataContainer().getOrDefault(entryx.getValue(), PersistentDataType.INTEGER, 0))) {
                        levelx++;
                     }

                     if (levelx > max) {
                        levelx = max;
                     }

                     resultMeta = this.enchantManager.applyEnchant(resultMeta, menchant, levelx);
                     result.setItemMeta(resultMeta);
                     e.getInventory().setRepairCost(30);
                     e.setResult(result);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onAnvil1(PrepareAnvilEvent event) {
      ItemStack one = event.getInventory().getFirstItem();
      ItemStack two = event.getInventory().getSecondItem();
      if (one != null && two != null) {
         ItemMeta metaTwo = two.getItemMeta();
         ItemMeta metaOne = one.getItemMeta();
         if (metaOne != null
            && metaTwo != null
            && one.getType() == two.getType()
            && one.getType() != Material.PLAYER_HEAD
            && one.getType() != Material.TOTEM_OF_UNDYING) {
            event.getInventory().setRepairCost(Math.min(event.getInventory().getRepairCost(), 30));
         }
      }
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (event.getView().getTopInventory() instanceof AnvilInventory anvilInventory
         && event.getClickedInventory() == event.getView().getTopInventory()
         && event.getSlot() == 2
         && anvilInventory.getResult() != null) {
         if (anvilInventory.getResult().getType() == Material.LIME_STAINED_GLASS_PANE) {
            return;
         }

         if (player.getLevel() >= anvilInventory.getRepairCost()) {
            event.setCursor(anvilInventory.getResult().clone());
            player.setLevel(player.getLevel() - anvilInventory.getRepairCost());
            anvilInventory.setFirstItem(null);
            anvilInventory.setSecondItem(null);
            anvilInventory.setResult(null);
         }

         event.setCancelled(true);
      }
   }

   public static String convertInt(int i) {
      if (i >= 1 && i <= 3999) {
         int[] arabicValues = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
         String[] romanSymbols = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
         StringBuilder result = new StringBuilder();

         for (int j = 0; j < arabicValues.length; j++) {
            while (i >= arabicValues[j]) {
               result.append(romanSymbols[j]);
               i -= arabicValues[j];
            }
         }

         return result.toString();
      } else {
         throw new IllegalArgumentException("Число должно быть в диапазоне от 1 до 3999");
      }
   }

   @EventHandler
   public void on(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.GRINDSTONE) {
         event.setCancelled(true);
         new GrindstoneMenu(event.getPlayer(), null);
      }
   }
}
