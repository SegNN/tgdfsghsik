package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FishingListener implements Listener {
   private final EnchantControl plugin;

   public FishingListener(EnchantControl plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerFishTreasure(PlayerFishEvent e) {
      if (e.getCaught() != null && e.getCaught() instanceof Item) {
         var item = (Item)e.getCaught();
         if (item.getItemStack().getItemMeta() != null
            && (
               !item.getItemStack().getItemMeta().getEnchants().isEmpty()
                  || item.getItemStack().getItemMeta() instanceof EnchantmentStorageMeta
                     && !((EnchantmentStorageMeta)item.getItemStack().getItemMeta()).getStoredEnchants().isEmpty()
            )) {
            this.plugin.getLogManager().logDebugConsole("Caught : " + ((Item)e.getCaught()).getItemStack());
            Map<Enchantment, Integer> enchantments;
            if (!item.getItemStack().getItemMeta().getEnchants().isEmpty()) {
               enchantments = new HashMap<>(item.getItemStack().getItemMeta().getEnchants());
            } else {
               enchantments = new HashMap<>(((EnchantmentStorageMeta)item.getItemStack().getItemMeta()).getStoredEnchants());
            }

            ItemMeta meta = item.getItemStack().getItemMeta();

            for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
               if (!item.getItemStack().getItemMeta().getEnchants().isEmpty() && meta.getEnchants().containsKey(enchantment.getKey())) {
                  meta.removeEnchant(enchantment.getKey());
               } else if (((EnchantmentStorageMeta)meta).getStoredEnchants().containsKey(enchantment.getKey())) {
                  ((EnchantmentStorageMeta)meta).removeStoredEnchant(enchantment.getKey());
               }
            }

            Map<Enchantment, Integer> finalEnchantments = new HashMap<>();

            for (Entry<Enchantment, Integer> enchantmentx : enchantments.entrySet()) {
               if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey()) <= 0) {
                  this.plugin.getLogManager().logDebugConsole("Removed : " + ChatColor.GREEN + enchantmentx.getKey().getKey());
                  Enchantment newEnchantment = this.plugin
                     .getEnchantmentManager()
                     .rerollEnchantment(item.getItemStack(), new ArrayList<>(enchantments.keySet()));
                  Integer enchantmentLevel = enchantmentx.getValue();
                  if (newEnchantment != null) {
                     if (enchantmentLevel > this.plugin.getEnchantmentManager().getAffectedEnchantments().get(newEnchantment)) {
                        enchantmentLevel = this.plugin.getEnchantmentManager().getAffectedEnchantments().get(newEnchantment);
                     }

                     finalEnchantments.put(newEnchantment, enchantmentLevel);
                  }
               } else if (enchantmentx.getValue() > this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey())) {
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
               } else {
                  finalEnchantments.put(enchantmentx.getKey(), enchantmentx.getValue());
               }
            }

            this.plugin.getLogManager().logDebugConsole("Enchantments (End) : " + finalEnchantments.toString());
            if (finalEnchantments.isEmpty() && item.getItemStack().getType() == Material.ENCHANTED_BOOK) {
               ((Item)e.getCaught()).setItemStack(new ItemStack(Material.BOOK));
            } else {
               for (Entry<Enchantment, Integer> enchantmentxx : finalEnchantments.entrySet()) {
                  this.plugin.getLogManager().logDebugConsole("Enchantment (Loop) : " + finalEnchantments.toString());
                  if (!item.getItemStack().getItemMeta().getEnchants().isEmpty()) {
                     meta.addEnchant(enchantmentxx.getKey(), enchantmentxx.getValue(), true);
                  } else {
                     ((EnchantmentStorageMeta)meta).addStoredEnchant(enchantmentxx.getKey(), enchantmentxx.getValue(), true);
                  }
               }

               ((Item)e.getCaught()).getItemStack().setItemMeta(meta);
               if (!item.getItemStack().getItemMeta().getEnchants().isEmpty()) {
                  this.plugin
                     .getLogManager()
                     .logDebugConsole("Enchantments (Meta) : " + ((Item)e.getCaught()).getItemStack().getItemMeta().getEnchants().toString());
               } else {
                  this.plugin
                     .getLogManager()
                     .logDebugConsole(
                        "Stored Enchantments (Meta) : "
                           + ((EnchantmentStorageMeta)((Item)e.getCaught()).getItemStack().getItemMeta()).getStoredEnchants().toString()
                     );
               }
            }
         } else {
            this.plugin.getLogManager().logDebugConsole("Item Caught Is not Enchanted");
         }
      } else {
         if (e.getCaught() == null) {
            this.plugin.getLogManager().logDebugConsole("Nothing caught");
         } else {
            this.plugin.getLogManager().logDebugConsole("caught thing was not an enchantable item");
         }
      }
   }
}
