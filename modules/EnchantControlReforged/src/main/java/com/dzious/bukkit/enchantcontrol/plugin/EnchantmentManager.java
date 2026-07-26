package com.dzious.bukkit.enchantcontrol.plugin;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EnchantmentManager {
   private EnchantControl plugin;
   private final Map<Enchantment, Integer> enchantments = new HashMap<>();

   public EnchantmentManager(EnchantControl plugin) {
      this.plugin = plugin;
      this.registerEnchantments("minecraft", Enchantment.values());
   }

   public void registerEnchantments(@NotNull String path, @NotNull Enchantment[] enchantmentList) {
      Map<String, Integer> configEnchantments = new HashMap<>();
      if (this.plugin.getConfigManager().doPathExist("enchantments." + path)) {
         configEnchantments = this.plugin.getConfigManager().loadEnchantments(path);
      }

      for (Enchantment enchantment : enchantmentList) {
         this.plugin
            .getLogManager()
            .logDebugConsole(
               "Currently processing : "
                  + ChatColor.GREEN
                  + enchantment.getKey()
                  + ChatColor.WHITE
                  + ". It's level is "
                  + ChatColor.RED
                  + enchantment.getMaxLevel()
            );
         this.enchantments.put(enchantment, enchantment.getMaxLevel());

         for (Entry<String, Integer> config : configEnchantments.entrySet()) {
            if (enchantment.getKey().toString().equals(path + ":" + config.getKey())) {
               this.plugin
                  .getLogManager()
                  .logDebugConsole(
                     "Replaced " + enchantment.getKey() + ". Old value was " + enchantment.getMaxLevel() + ", new value is " + config.getValue() + "."
                  );
               this.enchantments.replace(enchantment, config.getValue());
               break;
            }
         }
      }

      this.plugin.getLogManager().logDebugConsole(this.enchantments.toString());
   }

   public void reload() {
      this.plugin.getLogManager().logDebugConsole("Reloading enchantments");

      for (Entry<Enchantment, Integer> enchantment : this.enchantments.entrySet()) {
         this.plugin
            .getLogManager()
            .logDebugConsole(
               "Currently processing : "
                  + ChatColor.GREEN
                  + enchantment.getKey()
                  + ChatColor.WHITE
                  + ". It's level is "
                  + ChatColor.RED
                  + enchantment.getValue()
            );
         String[] enchantmentId = enchantment.getKey().getKey().toString().split(":");
         if (this.plugin.getConfigManager().doPathExist("enchantments." + enchantmentId[0] + "." + enchantmentId[1])) {
            int newLevel = this.plugin.getConfigManager().getIntFromPath("enchantments." + enchantmentId[0] + "." + enchantmentId[1]);
            if (enchantment.getValue() != newLevel) {
               this.plugin
                  .getLogManager()
                  .logDebugConsole("Replaced " + enchantment.getKey() + ". Old value was " + enchantment.getValue() + ", new value is " + newLevel + ".");
               enchantment.setValue(newLevel);
            }
         }
      }

      this.plugin.getLogManager().logDebugConsole("Enchantments Reloaded");
   }

   public Map<Enchantment, Integer> getAffectedEnchantments() {
      return this.enchantments;
   }

   public boolean hasEnchantActive(ItemStack item) {
      for (Entry<Enchantment, Integer> enchantment : this.enchantments.entrySet()) {
         if (enchantment.getKey().canEnchantItem(item)
            && (!this.enchantments.containsKey(enchantment.getKey()) || this.enchantments.get(enchantment.getKey()) > 0)) {
            return true;
         }
      }

      return false;
   }

   public boolean isValidEnchant(Enchantment enchantment) {
      return !this.enchantments.containsKey(enchantment) || this.enchantments.get(enchantment) > 0;
   }

   public List<Enchantment> getValidEnchantments(ItemStack item) {
      List<Enchantment> validEnchantments = new ArrayList<>();

      for (Entry<Enchantment, Integer> enchantment : this.enchantments.entrySet()) {
         if (enchantment.getValue() > 0) {
            if (item.getType() == Material.BOOK) {
               validEnchantments.add(enchantment.getKey());
            } else if (enchantment.getKey().canEnchantItem(item)) {
               validEnchantments.add(enchantment.getKey());
            }
         }
      }

      return validEnchantments;
   }

   public List<Enchantment> getValidEnchantments(ItemStack item, List<Enchantment> currents) {
      List<Enchantment> validEnchantmentsList = this.getValidEnchantments(item);
      List<Enchantment> validEnchantments = new ArrayList<>();

      for (Enchantment enchantment : currents) {
         if (validEnchantmentsList.contains(enchantment)) {
            validEnchantments.add(enchantment);
         }
      }

      return validEnchantments;
   }

   public List<Enchantment> getValidEnchantments(List<Enchantment> validEnchantments, List<Enchantment> currents) {
      List<Enchantment> newValidEnchantments = new ArrayList<>();
      boolean conflict = false;

      for (Enchantment enchantment : validEnchantments) {
         conflict = false;

         for (Enchantment current : currents) {
            if (current.conflictsWith(enchantment)) {
               conflict = true;
               break;
            }
         }

         if (!conflict) {
            newValidEnchantments.add(enchantment);
         }
      }

      return validEnchantments;
   }

   public Enchantment rerollEnchantment(ItemStack item, List<Enchantment> currentEnchantments) {
      List<Enchantment> validEnchantmentsList = this.getValidEnchantments(item);
      List<Enchantment> currentValidEnchantmentsList = new ArrayList<>();
      if (currentEnchantments != null) {
         currentValidEnchantmentsList = this.getValidEnchantments(item, currentEnchantments);
         validEnchantmentsList = this.getValidEnchantments(validEnchantmentsList, currentEnchantments);
      }

      if (validEnchantmentsList.size() == currentValidEnchantmentsList.size()) {
         return null;
      } else {
         int idx = (int)(Math.random() % validEnchantmentsList.size());
         Enchantment newEnchantment = validEnchantmentsList.get(idx);

         while (currentValidEnchantmentsList.contains(newEnchantment)) {
            newEnchantment = validEnchantmentsList.get(idx);
            if (++idx == validEnchantmentsList.size()) {
               idx = 0;
            }
         }

         return newEnchantment;
      }
   }
}
