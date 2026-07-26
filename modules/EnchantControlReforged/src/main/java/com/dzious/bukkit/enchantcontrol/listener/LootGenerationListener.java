package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class LootGenerationListener implements Listener {
   private EnchantControl plugin;

   public LootGenerationListener(EnchantControl plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onLootGeneration(LootGenerateEvent e) {
      if (!e.getLoot().isEmpty()) {
         for (ItemStack item : e.getLoot()) {
            if (item != null
               && item.getItemMeta() != null
               && (
                  !item.getEnchantments().isEmpty()
                     || item.getItemMeta() instanceof EnchantmentStorageMeta && !((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().isEmpty()
               )) {
               this.plugin.getLogManager().logDebugConsole("Generated item type : " + item.getType());
               Map<Enchantment, Integer> enchantments;
               if (!item.getItemMeta().getEnchants().isEmpty()) {
                  enchantments = new HashMap<>(item.getItemMeta().getEnchants());
               } else {
                  enchantments = new HashMap<>(((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants());
               }

               this.plugin.getLogManager().logDebugConsole("Enchantments (Start) : " + enchantments.toString());
               ItemMeta meta = item.getItemMeta();

               for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                  if (!item.getItemMeta().getEnchants().isEmpty() && meta.getEnchants().containsKey(enchantment.getKey())) {
                     meta.removeEnchant(enchantment.getKey());
                  } else if (((EnchantmentStorageMeta)meta).getStoredEnchants().containsKey(enchantment.getKey())) {
                     ((EnchantmentStorageMeta)meta).removeStoredEnchant(enchantment.getKey());
                  }
               }

               Map<Enchantment, Integer> finalEnchantments = new HashMap<>();

               for (Entry<Enchantment, Integer> enchantmentx : enchantments.entrySet()) {
                  if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey()) <= 0) {
                     this.plugin.getLogManager().logDebugConsole("Removed : " + ChatColor.GREEN + enchantmentx.getKey().getKey());
                     Enchantment newEnchantment = this.plugin.getEnchantmentManager().rerollEnchantment(item, new ArrayList<>(enchantments.keySet()));
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
               if (finalEnchantments.isEmpty() && item.getType() == Material.ENCHANTED_BOOK) {
                  new ItemStack(Material.BOOK);
               } else {
                  for (Entry<Enchantment, Integer> enchantmentxx : finalEnchantments.entrySet()) {
                     this.plugin.getLogManager().logDebugConsole("Enchantment (Loop) : " + finalEnchantments.toString());
                     if (!item.getItemMeta().getEnchants().isEmpty()) {
                        meta.addEnchant(enchantmentxx.getKey(), enchantmentxx.getValue(), true);
                     } else {
                        ((EnchantmentStorageMeta)meta).addStoredEnchant(enchantmentxx.getKey(), enchantmentxx.getValue(), true);
                     }
                  }

                  item.setItemMeta(meta);
                  if (!item.getItemMeta().getEnchants().isEmpty()) {
                     this.plugin.getLogManager().logDebugConsole("Enchantments (Meta) : " + item.getItemMeta().getEnchants().toString());
                  } else {
                     this.plugin
                        .getLogManager()
                        .logDebugConsole("Stored Enchantments (Meta) : " + ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().toString());
                  }
               }
            }
         }
      }
   }
}
