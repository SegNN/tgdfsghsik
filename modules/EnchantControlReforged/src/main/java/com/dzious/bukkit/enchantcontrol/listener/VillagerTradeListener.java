package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class VillagerTradeListener implements Listener {
   private EnchantControl plugin;

   public VillagerTradeListener(EnchantControl plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = false
   )
   public void onVillagerAcquireTrade(VillagerAcquireTradeEvent e) {
      ItemStack item = e.getRecipe().getResult();
      if (item.getItemMeta() != null
         && (
            !item.getItemMeta().getEnchants().isEmpty()
               || item.getItemMeta() instanceof EnchantmentStorageMeta && !((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().isEmpty()
         )) {
         this.plugin.getLogManager().logDebugConsole("Trade item : " + e.getRecipe().getResult());
         Map<Enchantment, Integer> enchantments;
         if (!item.getItemMeta().getEnchants().isEmpty()) {
            enchantments = new HashMap<>(item.getItemMeta().getEnchants());
         } else {
            enchantments = new HashMap<>(((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants());
         }

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
            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.BOOK), 12, 12, true, 1, 0.05F);
            List<ItemStack> ingredients = new ArrayList<>();
            ingredients.add(new ItemStack(Material.EMERALD, 4));
            ingredients.add(new ItemStack(Material.AIR));
            recipe.setIngredients(ingredients);
            e.setRecipe(recipe);
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
            MerchantRecipe recipe = new MerchantRecipe(
               item,
               e.getRecipe().getUses(),
               e.getRecipe().getMaxUses(),
               e.getRecipe().hasExperienceReward(),
               e.getRecipe().getVillagerExperience(),
               e.getRecipe().getPriceMultiplier()
            );
            recipe.setIngredients(e.getRecipe().getIngredients());
            e.setRecipe(recipe);
            if (!item.getItemMeta().getEnchants().isEmpty()) {
               this.plugin.getLogManager().logDebugConsole("Enchantments (Meta) : " + e.getRecipe().getResult().getItemMeta().getEnchants().toString());
            } else {
               this.plugin
                  .getLogManager()
                  .logDebugConsole(
                     "Stored Enchantments (Meta) : " + ((EnchantmentStorageMeta)e.getRecipe().getResult().getItemMeta()).getStoredEnchants().toString()
                  );
            }
         }
      } else {
         this.plugin.getLogManager().logDebugConsole("Trade item is not enchanted.");
      }
   }
}
