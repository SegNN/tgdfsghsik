package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AnvilListener implements Listener {
   private final EnchantControl plugin;
   boolean bypassMinecraftMaxLevel = false;
   boolean ignoreConflict = false;

   public AnvilListener(EnchantControl plugin) {
      this.plugin = plugin;
      if (plugin.getConfigManager().doPathExist("bypass_levels")) {
         this.bypassMinecraftMaxLevel = plugin.getConfigManager().getBooleanFromPath("bypass_levels");
      }

      if (plugin.getConfigManager().doPathExist("ignore_conflict")) {
         this.ignoreConflict = plugin.getConfigManager().getBooleanFromPath("ignore_conflict");
      }

      plugin.getLogManager().logDebugConsole("bypassMinecraftMaxLevel : " + this.bypassMinecraftMaxLevel);
      plugin.getLogManager().logDebugConsole("ignoreConflict : " + this.ignoreConflict);
   }

   @EventHandler
   public void onFusionPreparation(PrepareAnvilEvent e) {
      if (this.canContinue(e)) {
         if (e.getResult() == null
            && this.ignoreConflict
            && e.getInventory().getItem(0) != null
            && e.getInventory().getItem(1) != null
            && Enchantment.VANISHING_CURSE.getItemTarget().includes(e.getInventory().getItem(0).getType())
            && (
               e.getInventory().getItem(1).getType() == e.getInventory().getItem(0).getType()
                  || e.getInventory().getItem(1).getType() == Material.ENCHANTED_BOOK
            )) {
            e.setResult(e.getInventory().getItem(0).clone());
         }

         Map<Enchantment, Integer> baseEnchantments = null;
         if (!e.getInventory().getItem(0).getItemMeta().getEnchants().isEmpty()
            || e.getResult().getItemMeta() instanceof EnchantmentStorageMeta
               && ((EnchantmentStorageMeta)e.getInventory().getItem(0).getItemMeta()).getStoredEnchants().isEmpty()) {
            if (!e.getInventory().getItem(0).getItemMeta().getEnchants().isEmpty()) {
               baseEnchantments = new HashMap<>(e.getInventory().getItem(0).getItemMeta().getEnchants());
            } else {
               baseEnchantments = new HashMap<>(((EnchantmentStorageMeta)e.getInventory().getItem(0).getItemMeta()).getStoredEnchants());
            }
         }

         Map<Enchantment, Integer> enchantments;
         if (this.bypassMinecraftMaxLevel || this.ignoreConflict) {
            enchantments = this.computeFusion(e.getInventory().getContents());
         } else if (!e.getResult().getItemMeta().getEnchants().isEmpty()) {
            enchantments = new HashMap<>(e.getResult().getItemMeta().getEnchants());
         } else {
            enchantments = new HashMap<>(((EnchantmentStorageMeta)e.getResult().getItemMeta()).getStoredEnchants());
         }

         this.plugin.getLogManager().logDebugConsole("Enchantments (Start) : " + enchantments.toString());
         ItemMeta meta = e.getResult().getItemMeta();
         if (!e.getResult().getItemMeta().getEnchants().isEmpty()) {
            for (Entry<Enchantment, Integer> enchantment : meta.getEnchants().entrySet()) {
               meta.removeEnchant(enchantment.getKey());
            }
         } else if (meta instanceof EnchantmentStorageMeta && !((EnchantmentStorageMeta)meta).getStoredEnchants().isEmpty()) {
            for (Entry<Enchantment, Integer> enchantment : ((EnchantmentStorageMeta)meta).getStoredEnchants().entrySet()) {
               ((EnchantmentStorageMeta)meta).removeStoredEnchant(enchantment.getKey());
            }
         }

         Map<Enchantment, Integer> finalEnchantments = new HashMap<>();

         for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantment.getKey()) <= 0) {
               this.plugin.getLogManager().logDebugConsole("Removed : " + ChatColor.GREEN + enchantment.getKey().getKey());
            } else if (enchantment.getValue() > this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantment.getKey())) {
               PersistentDataContainer container = e.getResult().getItemMeta().getPersistentDataContainer();
               if (container.has(this.plugin.getNamespacedKey(), PersistentDataType.STRING)
                  && ((String)container.get(this.plugin.getNamespacedKey(), PersistentDataType.STRING)).equalsIgnoreCase("event")
                  && baseEnchantments != null
                  && baseEnchantments.containsKey(enchantment.getKey())
                  && baseEnchantments.get(enchantment.getKey()) == enchantment.getValue()) {
                  this.plugin.getLogManager().logDebugConsole("Skipped : " + ChatColor.GREEN + enchantment.getKey().getKey() + ChatColor.WHITE + ".");
                  finalEnchantments.put(enchantment.getKey(), enchantment.getValue());
               } else {
                  this.plugin
                     .getLogManager()
                     .logDebugConsole(
                        "Replaced : "
                           + ChatColor.GREEN
                           + enchantment.getKey().getKey()
                           + ChatColor.WHITE
                           + ". Level was : "
                           + ChatColor.GREEN
                           + enchantment.getValue()
                           + ChatColor.WHITE
                           + " and is now : "
                           + ChatColor.GREEN
                           + this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantment.getKey())
                     );
                  finalEnchantments.put(enchantment.getKey(), this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantment.getKey()));
               }
            } else {
               finalEnchantments.put(enchantment.getKey(), enchantment.getValue());
            }
         }

         this.plugin.getLogManager().logDebugConsole("Enchantments (End) : " + finalEnchantments.toString());
         if (finalEnchantments.isEmpty()) {
            e.setResult(null);
         } else {
            for (Entry<Enchantment, Integer> enchantmentx : finalEnchantments.entrySet()) {
               this.plugin.getLogManager().logDebugConsole("Enchantment (Loop) : " + finalEnchantments.toString());
               if (!e.getResult().getItemMeta().getEnchants().isEmpty()) {
                  meta.addEnchant(enchantmentx.getKey(), enchantmentx.getValue(), true);
               } else {
                  ((EnchantmentStorageMeta)meta).addStoredEnchant(enchantmentx.getKey(), enchantmentx.getValue(), true);
               }
            }

            e.getResult().setItemMeta(meta);
            if (!e.getResult().getItemMeta().getEnchants().isEmpty()) {
               this.plugin.getLogManager().logDebugConsole("Enchantments (Meta) : " + e.getResult().getItemMeta().getEnchants().toString());
            } else {
               this.plugin
                  .getLogManager()
                  .logDebugConsole("Stored Enchantments (Meta) : " + ((EnchantmentStorageMeta)e.getResult().getItemMeta()).getStoredEnchants().toString());
            }
         }
      }
   }

   private Map<Enchantment, Integer> computeFusion(ItemStack[] items) {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      Map<Enchantment, Integer> lhsEnchantments = null;
      Map<Enchantment, Integer> rhsEnchantments = null;
      boolean isFusionPossible = false;

      for (int i = 0; i < 2; i++) {
         if (items[i] == null) {
            return null;
         }

         if (items[i].getItemMeta() != null
            && (
               !items[i].getItemMeta().getEnchants().isEmpty()
                  || items[i].getItemMeta() instanceof EnchantmentStorageMeta
                     && !((EnchantmentStorageMeta)items[i].getItemMeta()).getStoredEnchants().isEmpty()
            )) {
            isFusionPossible = true;
         }
      }

      if (!isFusionPossible) {
         return null;
      } else {
         HashMap<Enchantment, Integer> var11;
         if (!items[0].getItemMeta().getEnchants().isEmpty()) {
            var11 = new HashMap<>(items[0].getItemMeta().getEnchants());
         } else if (items[0].getItemMeta() instanceof EnchantmentStorageMeta && !((EnchantmentStorageMeta)items[0].getItemMeta()).getStoredEnchants().isEmpty()
            )
          {
            var11 = new HashMap<>(((EnchantmentStorageMeta)items[0].getItemMeta()).getStoredEnchants());
         } else {
            var11 = new HashMap<>();
         }

         HashMap<Enchantment, Integer> var12;
         if (!items[1].getItemMeta().getEnchants().isEmpty()) {
            var12 = new HashMap<>(items[1].getItemMeta().getEnchants());
         } else if (items[1].getItemMeta() instanceof EnchantmentStorageMeta && !((EnchantmentStorageMeta)items[1].getItemMeta()).getStoredEnchants().isEmpty()
            )
          {
            var12 = new HashMap<>(((EnchantmentStorageMeta)items[1].getItemMeta()).getStoredEnchants());
         } else {
            var12 = new HashMap<>();
         }

         for (Entry<Enchantment, Integer> enchantment : var11.entrySet()) {
            if (var12.containsKey(enchantment.getKey())) {
               if (enchantment.getValue() == var12.get(enchantment.getKey())
                  && enchantment.getValue() < this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantment.getKey())) {
                  enchantments.put(enchantment.getKey(), enchantment.getValue() + 1);
               } else if (enchantment.getValue() < (Integer)var12.get(enchantment.getKey())) {
                  enchantments.put(enchantment.getKey(), (Integer)var12.get(enchantment.getKey()));
               } else {
                  enchantments.put(enchantment.getKey(), enchantment.getValue());
               }
            } else {
               enchantments.put(enchantment.getKey(), enchantment.getValue());
            }
         }

         boolean conflicts = false;

         for (Entry<Enchantment, Integer> enchantmentx : var12.entrySet()) {
            if (!enchantments.containsKey(enchantmentx.getKey()) && enchantmentx.getKey().canEnchantItem(items[0])) {
               if (!this.ignoreConflict) {
                  conflicts = false;

                  for (Entry<Enchantment, Integer> current : enchantments.entrySet()) {
                     if (current.getKey().conflictsWith(enchantmentx.getKey())) {
                        conflicts = true;
                        break;
                     }
                  }

                  if (!conflicts) {
                     enchantments.put(enchantmentx.getKey(), enchantmentx.getValue());
                  }
               } else {
                  enchantments.put(enchantmentx.getKey(), enchantmentx.getValue());
               }
            }
         }

         this.plugin.getLogManager().logDebugConsole("Enchants : " + enchantments);
         return enchantments;
      }
   }

   private boolean canContinue(PrepareAnvilEvent e) {
      if (e.getResult() != null) {
         if (e.getResult().getItemMeta() == null) {
            return false;
         } else {
            if (e.getResult().getEnchantments().isEmpty()) {
               if (!(e.getResult().getItemMeta() instanceof EnchantmentStorageMeta)) {
                  return false;
               }

               if (((EnchantmentStorageMeta)e.getResult().getItemMeta()).getStoredEnchants().isEmpty()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return this.ignoreConflict
            && e.getInventory().getItem(0) != null
            && e.getInventory().getItem(1) != null
            && Enchantment.VANISHING_CURSE.getItemTarget().includes(e.getInventory().getItem(0).getType())
            && (
               e.getInventory().getItem(1).getType() == e.getInventory().getItem(0).getType()
                  || e.getInventory().getItem(1).getType() == Material.ENCHANTED_BOOK
            );
      }
   }
}
