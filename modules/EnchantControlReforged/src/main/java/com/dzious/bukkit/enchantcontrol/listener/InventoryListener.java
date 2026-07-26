package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {
   final EnchantControl plugin;

   public InventoryListener(EnchantControl plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   void onPlayerOpenInventory(InventoryOpenEvent e) {
      ItemStack[] items = this.removeEnchants(e.getView().getTopInventory().getContents());
      e.getView().getTopInventory().setContents(items);
      items = this.removeEnchants(e.getView().getBottomInventory().getContents());
      e.getView().getBottomInventory().setContents(items);
   }

   ItemStack[] removeEnchants(ItemStack[] items) {
      for (int i = 0; i < items.length; i++) {
         if (items[i] != null
            && items[i].getType() != Material.SHULKER_BOX
            && items[i].getItemMeta() != null
            && (
               !items[i].getEnchantments().isEmpty()
                  || items[i].getItemMeta() instanceof EnchantmentStorageMeta
                     && !((EnchantmentStorageMeta)items[i].getItemMeta()).getStoredEnchants().isEmpty()
            )) {
            this.plugin.getLogManager().logDebugConsole("Item[" + i + "] type : " + items[i].getType());
            Map<Enchantment, Integer> enchantments;
            if (!items[i].getItemMeta().getEnchants().isEmpty()) {
               enchantments = new HashMap<>(items[i].getItemMeta().getEnchants());
            } else {
               enchantments = new HashMap<>(((EnchantmentStorageMeta)items[i].getItemMeta()).getStoredEnchants());
            }

            this.plugin.getLogManager().logDebugConsole("Enchantments (Start) : " + enchantments.toString());
            ItemMeta meta = items[i].getItemMeta();

            for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
               if (!items[i].getItemMeta().getEnchants().isEmpty() && meta.getEnchants().containsKey(enchantment.getKey())) {
                  meta.removeEnchant(enchantment.getKey());
               } else if (((EnchantmentStorageMeta)meta).getStoredEnchants().containsKey(enchantment.getKey())) {
                  ((EnchantmentStorageMeta)meta).removeStoredEnchant(enchantment.getKey());
               }
            }

            Map<Enchantment, Integer> finalEnchantments = new HashMap<>();

            for (Entry<Enchantment, Integer> enchantmentx : enchantments.entrySet()) {
               if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey()) <= 0) {
                  this.plugin.getLogManager().logDebugConsole("Removed : " + ChatColor.GREEN + enchantmentx.getKey().getKey());
               } else if (enchantmentx.getValue() > this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey())) {
                  PersistentDataContainer container = items[i].getItemMeta().getPersistentDataContainer();
                  if (container.has(this.plugin.getNamespacedKey(), PersistentDataType.STRING)
                     && ((String)container.get(this.plugin.getNamespacedKey(), PersistentDataType.STRING)).equalsIgnoreCase("event")) {
                     this.plugin.getLogManager().logDebugConsole("Skipped : " + ChatColor.GREEN + enchantmentx.getKey().getKey() + ChatColor.WHITE + ".");
                  } else {
                     this.plugin
                        .getLogManager()
                        .logDebugConsole(
                           "Replaced : "
                              + ChatColor.GREEN
                              + enchantmentx.getKey().getKey()
                              + ChatColor.WHITE
                              + ". Level was : "
                              + ChatColor.GREEN
                              + enchantmentx.getValue()
                              + ChatColor.WHITE
                              + " and is now : "
                              + ChatColor.GREEN
                        );
                     finalEnchantments.put(enchantmentx.getKey(), this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey()));
                  }
               } else {
                  finalEnchantments.put(enchantmentx.getKey(), enchantmentx.getValue());
               }
            }

            this.plugin.getLogManager().logDebugConsole("Enchantments (End) : " + finalEnchantments.toString());
            if (finalEnchantments.isEmpty() && items[i].getType() == Material.ENCHANTED_BOOK) {
               items[i] = new ItemStack(Material.BOOK);
            } else {
               for (Entry<Enchantment, Integer> enchantmentxx : finalEnchantments.entrySet()) {
                  this.plugin.getLogManager().logDebugConsole("Enchantment (Loop) : " + finalEnchantments.toString());
                  if (!items[i].getItemMeta().getEnchants().isEmpty()) {
                     meta.addEnchant(enchantmentxx.getKey(), enchantmentxx.getValue(), true);
                  } else {
                     ((EnchantmentStorageMeta)meta).addStoredEnchant(enchantmentxx.getKey(), enchantmentxx.getValue(), true);
                  }
               }

               this.plugin.getLogManager().logDebugConsole("Meta (End) : " + meta);
               items[i].setItemMeta(meta);
               if (!items[i].getItemMeta().getEnchants().isEmpty()) {
                  this.plugin.getLogManager().logDebugConsole("Enchantments (Meta) : " + items[i].getItemMeta().getEnchants().toString());
               } else if (meta instanceof EnchantmentStorageMeta) {
                  this.plugin
                     .getLogManager()
                     .logDebugConsole("Stored Enchantments (Meta) : " + ((EnchantmentStorageMeta)items[i].getItemMeta()).getStoredEnchants().toString());
               } else {
                  this.plugin.getLogManager().logDebugConsole("Item has no more enchantments!");
               }
            }
         }
      }

      return items;
   }
}
